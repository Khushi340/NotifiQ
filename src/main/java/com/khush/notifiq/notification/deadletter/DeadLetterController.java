package com.khush.notifiq.notification.deadletter;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dead-letters")
@RequiredArgsConstructor
@Tag(name = "Dead Letter Queue", description = "Failed notification management APIs")
public class DeadLetterController {
    private final DeadLetterService deadLetterService;

    @Operation(
            summary = "Get dead letter notifications",
            description = "Returns notifications that exhausted all retry attempts."
    )
    @GetMapping
    public ResponseEntity<List<DeadLetterResponse>> getAllDeadLetters(){
        return ResponseEntity.ok(deadLetterService.getAllDeadLetters());
    }

    @Operation(
            summary = "Replay dead letter notification",
            description = "Requeues a failed notification for delivery."
    )
    @PostMapping("/{id}/replay")
    public ResponseEntity<DeadLetterResponse> replayDeadLetter(@PathVariable Long id) {
        return ResponseEntity.ok(deadLetterService.replayDeadLetter(id));
    }
}
