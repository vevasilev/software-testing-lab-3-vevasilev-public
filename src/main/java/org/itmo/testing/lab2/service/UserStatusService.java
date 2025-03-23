package org.itmo.testing.lab2.service;


import java.util.List;
import java.util.Optional;

import static org.itmo.testing.lab2.service.UserAnalyticsService.*;

public class UserStatusService {

    private final UserAnalyticsService userAnalyticsService;

    public UserStatusService(UserAnalyticsService userAnalyticsService) {
        this.userAnalyticsService = userAnalyticsService;
    }

    public String getUserStatus(String userId) {

        long totalActivityTime = userAnalyticsService.getTotalActivityTime(userId);

        if (totalActivityTime < 60) {
            return "Inactive";
        } else if (totalActivityTime < 120) {
            return "Active";
        } else {
            return "Highly active";
        }
    }

    public Optional<String> getUserLastSessionDate(String userId) {
        List<Session> sessionList = userAnalyticsService.getUserSessions(userId);
        if (sessionList.isEmpty()) {
            return Optional.empty();
        }
        Session lastSession = sessionList.getLast();
        return Optional.of(lastSession.getLogoutTime().toLocalDate().toString());
    }
}
