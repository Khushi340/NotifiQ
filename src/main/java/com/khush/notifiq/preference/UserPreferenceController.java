package com.khush.notifiq.preference;

import com.khush.notifiq.preference.dto.PreferenceRequest;
import com.khush.notifiq.preference.dto.PreferenceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {
    private final UserPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<PreferenceResponse> getPreferenceByUser(@PathVariable Long userId){
        PreferenceResponse preference = preferenceService.getPreferenceByUserId(userId);
        return ResponseEntity.ok(preference);
    }

    @PutMapping
    public ResponseEntity<PreferenceResponse> updatePreference(@PathVariable Long userId, @RequestBody @Valid PreferenceRequest request){
        PreferenceResponse response = preferenceService.updatePreference(userId, request);
        return ResponseEntity.ok(response);
    }
}
