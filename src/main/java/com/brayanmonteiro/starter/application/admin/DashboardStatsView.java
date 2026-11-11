package com.brayanmonteiro.starter.application.admin;

import java.util.List;

public record DashboardStatsView(
        long users,
        long roles,
        long audits,
        int days,
        List<DashboardDayCountView> usersCreatedByDay
) {}
