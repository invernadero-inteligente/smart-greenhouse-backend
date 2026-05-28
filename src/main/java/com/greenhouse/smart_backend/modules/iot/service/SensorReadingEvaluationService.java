package com.greenhouse.smart_backend.modules.iot.service;

import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.shared.enums.ReadingStatus;

import java.math.BigDecimal;

public final class SensorReadingEvaluationService {

    private SensorReadingEvaluationService() {
    }

    public static BigDecimal parseValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static ReadingStatus resolveStatus(BigDecimal value, ThresholdConfig threshold) {
        if (threshold == null) {
            return ReadingStatus.UNKNOWN;
        }

        return ReadingStatus.from(value, threshold.getMinValue(), threshold.getMaxValue());
    }

    public static String resolveUnit(String readingUnit, ThresholdConfig threshold) {
        if (threshold != null && threshold.getUnit() != null && !threshold.getUnit().isBlank()) {
            return threshold.getUnit();
        }

        if (readingUnit != null && !readingUnit.isBlank()) {
            return readingUnit;
        }

        return "N/A";
    }
}