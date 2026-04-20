package com.godare.app.repository;

import com.godare.app.entity.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
    boolean existsByReportedByAndReportedId(String reportedBy, String reportedId);
}