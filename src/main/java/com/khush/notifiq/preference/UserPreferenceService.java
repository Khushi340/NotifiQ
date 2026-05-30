package com.khush.notifiq.preference;

import com.khush.notifiq.common.ResourceNotFoundException;
import com.khush.notifiq.preference.dto.PreferenceRequest;
import com.khush.notifiq.preference.dto.PreferenceResponse;
import com.khush.notifiq.user.User;
import com.khush.notifiq.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public PreferenceResponse getPreferenceByUserId(Long userId){
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Preference not found for user id: "+userId));
        return mapToResponse(preference);

    }

    public PreferenceResponse updatePreference(Long userId, PreferenceRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id: "+userId));
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.builder()
                        .user(user)
                        .build());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setInAppEnabled(request.isInAppEnabled());
        preference.setWebhookEnabled(request.isWebhookEnabled());
        preference.setQuietHoursEnabled(request.isQuietHoursEnabled());
        preference.setQuietHoursStart(request.getQuietHoursStart());
        preference.setQuietHoursEnd(request.getQuietHoursEnd());
        preference.setPreferredChannel(request.getPreferredChannel());

        UserPreference savedPreference=preferenceRepository.save(preference);
        return mapToResponse(savedPreference);
    }

    private PreferenceResponse mapToResponse(UserPreference preference){
        return PreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .emailEnabled(preference.isEmailEnabled())
                .inAppEnabled(preference.isInAppEnabled())
                .webhookEnabled(preference.isWebhookEnabled())
                .quietHoursEnabled(preference.isQuietHoursEnabled())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .preferredChannel(preference.getPreferredChannel())
                .build();
    }
}
