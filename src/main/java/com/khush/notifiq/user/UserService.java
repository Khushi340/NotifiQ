package com.khush.notifiq.user;

import com.khush.notifiq.common.BadRequestException;
import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.user.dto.CreateUserRequest;
import com.khush.notifiq.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest userRequest){
        if(userRepository.existsByEmail(userRequest.getEmail())){
            throw new BadRequestException("Email already in use");
        }

        User user= User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .build();
        User savedUser= userRepository.save(user);
        return UserResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id "+id));
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserResponse> getAllUsers(){
        List<User> users=userRepository.findAll();
        return users.stream()
                .map(user->UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build()
                ).toList();
    }

    public UserResponse updateUserById(Long id, CreateUserRequest userRequest){
        User user=userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id "+id));
        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new BadRequestException("Email already in use");
        }
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        User updatedUser = userRepository.save(user);
        return UserResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .build();
    }

    public void deleteUser(Long id){
        User user=userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("User Not Found with id "+id));
        userRepository.delete(user);
    }
}
