package com.brayanmonteiro.starter.adapters.out.time;

import com.brayanmonteiro.starter.domain.shared.ClockPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemClockAdapter implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
