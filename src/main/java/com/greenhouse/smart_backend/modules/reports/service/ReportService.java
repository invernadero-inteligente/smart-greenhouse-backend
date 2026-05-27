package com.greenhouse.smart_backend.modules.reports.service;

import com.greenhouse.smart_backend.modules.reports.dto.ReportRequestDTO;

public interface ReportService {
    byte[] generateReport(ReportRequestDTO request);
    String getFilename(ReportRequestDTO.ReportType type);
}
