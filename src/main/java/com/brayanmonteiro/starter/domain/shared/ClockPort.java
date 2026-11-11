package com.brayanmonteiro.starter.domain.shared;

import java.time.Instant;

public interface ClockPort {
    Instant now();
}
