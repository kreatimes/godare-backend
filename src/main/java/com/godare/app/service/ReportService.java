package com.godare.app.service;

import com.godare.app.dto.CreateReportRequest;
import com.godare.app.entity.Report;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    private static final List<String> VALID_TYPES =
            List.of("POST", "USER", "COMMENT");

    private static final List<String> VALID_REASONS =
            List.of("SEXUAL_CONTENT", "VIOLENCE", "SPAM",
                    "HATE_SPEECH", "MISINFORMATION", "OTHER");

    public void createReport(String reportedBy, CreateReportRequest request) {

        if (!VALID_TYPES.contains(request.getType())) {
            throw new BadRequestException("INVALID_REPORT_TYPE");
        }

        if (!VALID_REASONS.contains(request.getReason())) {
            throw new BadRequestException("INVALID_REPORT_REASON");
        }

        // Evitar reportes duplicados del mismo usuario sobre el mismo elemento
        if (reportRepository.existsByReportedByAndReportedId(
                reportedBy, request.getReportedId())) {
            throw new BadRequestException("ALREADY_REPORTED");
        }

        Report report = new Report();
        report.setReportedId(request.getReportedId());
        report.setReportedBy(reportedBy);
        report.setType(request.getType());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus("PENDING");
        report.setCreatedAt(Instant.now());

        reportRepository.save(report);
    }
}