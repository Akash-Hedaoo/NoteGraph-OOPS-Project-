package com.notegraph.api.controller;

import com.notegraph.api.domain.ActivityLog;
import com.notegraph.api.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityLog>> getUserActivity(@PathVariable UUID userId) {
        return ResponseEntity.ok(activityService.getRecentActivityForUser(userId));
    }
}
