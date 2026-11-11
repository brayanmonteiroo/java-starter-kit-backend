package com.brayanmonteiro.starter.application.admin;

import com.brayanmonteiro.starter.domain.access.RoleRepositoryPort;
import com.brayanmonteiro.starter.domain.audit.AuditRepositoryPort;
import com.brayanmonteiro.starter.domain.identity.User;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardUseCase {
    private static final Set<Integer> ALLOWED_DAYS = Set.of(7, 14, 30);

    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final AuditRepositoryPort audits;
    private final ClockPort clock;

    public DashboardUseCase(UserRepositoryPort users, RoleRepositoryPort roles, AuditRepositoryPort audits, ClockPort clock) {
        this.users = users;
        this.roles = roles;
        this.audits = audits;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardStatsView execute(int days) {
        int safeDays = ALLOWED_DAYS.contains(days) ? days : 7;
        LocalDate today = LocalDate.ofInstant(clock.now(), ZoneOffset.UTC);
        LocalDate fromDay = today.minusDays(safeDays - 1L);
        var fromInstant = fromDay.atStartOfDay().toInstant(ZoneOffset.UTC);

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < safeDays; i++) {
            counts.put(fromDay.plusDays(i), 0L);
        }
        for (User user : users.findCreatedSince(fromInstant)) {
            LocalDate day = LocalDate.ofInstant(user.getCreatedAt(), ZoneOffset.UTC);
            if (counts.containsKey(day)) {
                counts.put(day, counts.get(day) + 1);
            }
        }

        List<DashboardDayCountView> series = new ArrayList<>();
        counts.forEach((day, count) -> series.add(new DashboardDayCountView(day.toString(), count)));

        return new DashboardStatsView(users.count(), roles.count(), audits.count(), safeDays, series);
    }
}
