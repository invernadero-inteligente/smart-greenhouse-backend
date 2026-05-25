package com.greenhouse.smart_backend.shared.enums;

import java.math.BigDecimal;

public enum ReadingStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    UNKNOWN;

    public static ReadingStatus from(BigDecimal value, BigDecimal minValue, BigDecimal maxValue) {
        if (value == null || minValue == null || maxValue == null) {
            return UNKNOWN;
        }

        if (value.compareTo(minValue) < 0 || value.compareTo(maxValue) > 0) {
            return CRITICAL;
        }

        if (value.compareTo(minValue) == 0 || value.compareTo(maxValue) == 0) {
            return WARNING;
        }

        return NORMAL;
    }
}

