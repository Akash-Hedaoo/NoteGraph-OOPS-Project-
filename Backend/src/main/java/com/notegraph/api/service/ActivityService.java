package com.notegraph.api.service;

import com.notegraph.api.domain.ActivityLog;
import com.notegraph.api.domain.User;
import com.notegraph.api.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void logActivity(User user, String action, String targetType, UUID targetId, String targetName) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .build();
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getRecentActivityForUser(UUID userId) {
        return activityLogRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
}
