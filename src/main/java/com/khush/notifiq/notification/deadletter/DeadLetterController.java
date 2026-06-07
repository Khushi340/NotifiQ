package com.khush.notifiq.notification.deadletter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dead-letters")
@RequiredArgsConstructor
public class DeadLetterController {
    private final DeadLetterService deadLetterService;

    @GetMapping
    public ResponseEntity<List<DeadLetterResponse>> getAllDeadLetters(){
        return ResponseEntity.ok(deadLetterService.getAllDeadLetters());
    }

    @PostMapping("/{id}/replay")
    public ResponseEntity<DeadLetterResponse> replayDeadLetter(@PathVariable Long id) {
        return ResponseEntity.ok(deadLetterService.replayDeadLetter(id));
    }
}
