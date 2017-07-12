package com.vs.report.logic;

import com.vs.report.model.Report;

/**
 * This interface represents general API for report generation
 */
public interface ReportingApiClient {
    /**
     * Generates report by name provided.
     */
    Report getReport(String name);

}
