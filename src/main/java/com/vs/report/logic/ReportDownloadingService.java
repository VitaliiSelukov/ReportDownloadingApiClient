package com.vs.report.logic;

import com.vs.report.model.Report;
import com.vs.report.system.ErrorMessages;
import com.vs.report.system.TechnicalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;

@Service
public class ReportDownloadingService {

    private static final Logger logger = LogManager.getLogger(ReportDownloadingService.class);

    @Autowired
    private ReportingApiClient reportingApiClient;

    @Autowired
    private ReportSavingService reportSavingService;

    @Value("${report.download.count}")
    private Integer reportCount;
    @Value("${report.download.max.parallelism}")
    private Integer maxParallelism;

    public ReportDownloadingService() {
    }

    public ReportDownloadingService(ReportingApiClient reportingApiClient, ReportSavingService reportSavingService) {
        this.reportingApiClient = reportingApiClient;
        this.reportSavingService = reportSavingService;
    }

    /**
     * Downloads reports using reporting api client in parallel and saves each one on the disk.
     * The directory used for saving is specified in report.save.dir property.
     * The number of reports to download is specified in report.download.count.
     * The number of threads used has a limit specified in report.download.max.parallelism.
     */
    public void downloadAndSave() {
        logger.debug("Reports downloading has been started");
        ExecutorCompletionService<Report> completionService = submitTasks();
        joinResults(completionService);
        logger.debug("Reports downloading has been finished");
    }

    private void joinResults(ExecutorCompletionService<Report> completionService) {
        logger.debug("Joining results");
        for (int i = 0; i < reportCount; i++) {
            try {
                completionService.take().get();
            } catch (Exception e) {
                throw new TechnicalException(ErrorMessages.EXCEPTION_DURING_REPORT_DOWNLOADING + ": " + e.getMessage(), e);
            }
        }
    }

    //TODO: profile parallelism level
    private ExecutorCompletionService<Report> submitTasks() {
        logger.debug("Submitting tasks");
        int parallelism = reportCount > 0 ? reportCount : 1;
        if (parallelism > maxParallelism) parallelism = maxParallelism;
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
        ExecutorCompletionService<Report> completionService = new ExecutorCompletionService<>(forkJoinPool);
        for (int i = 0; i < reportCount; i++) {
            final String reportName = "report_" + i;
            completionService.submit(() -> downloadAndSaveReport(reportName));
        }
        return completionService;
    }

    private Report downloadAndSaveReport(String reportName) throws IOException {
        logger.trace("Downloading and saving report with name: {}", reportName);
        Report report = reportingApiClient.getReport(reportName);
        reportSavingService.saveReport(report);
        return report;
    }
}
