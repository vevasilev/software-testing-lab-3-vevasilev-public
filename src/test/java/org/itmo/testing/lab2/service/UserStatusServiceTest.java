package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeEach
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @ParameterizedTest
    @CsvSource({
            "30, Inactive", "60, Active", "90, Active", "120, Highly active", "1000, Highly active"
    })
    public void getUserStatus_validActivityTime_shouldReturnCorrectStatus(long activityTime, String expectedStatus) {
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(activityTime);
        String actualStatus = userStatusService.getUserStatus("user123");
        assertEquals(expectedStatus, actualStatus);
        verify(userAnalyticsService, times(1)).getTotalActivityTime("user123");
    }

    @Test
    public void getUserLastSessionDate_existingSession_shouldReturnCorrectDate() {
        UserAnalyticsService.Session mockSession = mock(UserAnalyticsService.Session.class);
        when(userAnalyticsService.getUserSessions("user123")).thenReturn(List.of(mockSession));
        when(mockSession.getLogoutTime()).thenReturn(java.time.LocalDateTime.of(2025, 1, 1, 1, 1));
        Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user123");
        assertTrue(lastSessionDate.isPresent());
        assertEquals("2025-01-01", lastSessionDate.get());
        verify(userAnalyticsService, times(1)).getUserSessions("user123");
        verify(mockSession, times(1)).getLogoutTime();
    }

    @Test
    public void getUserLastSessionDate_noSessions_shouldReturnEmpty() {
        when(userAnalyticsService.getUserSessions("user123")).thenReturn(Collections.emptyList());
        Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user123");
        assertFalse(lastSessionDate.isPresent());
        verify(userAnalyticsService, times(1)).getUserSessions("user123");
    }
}
