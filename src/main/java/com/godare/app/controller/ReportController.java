package com.godare.app.controller;

import com.godare.app.dto.CreateReportRequest;
import com.godare.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createReport(@RequestBody CreateReportRequest request,
                               Authentication authentication) {
        reportService.createReport(authentication.getName(), request);
        return "Reporte enviado correctamente";
    }
}