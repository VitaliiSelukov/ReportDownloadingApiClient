package com.vs.report.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vs.report.model.Report;
import com.vs.report.system.ErrorMessages;
import com.vs.report.system.TechnicalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
public class ReportSavingService {

    private static final Logger logger = LogManager.getLogger(ReportSavingService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${report.save.dir}")
    private String baseDir;

    private File baseDirFile;

    @PostConstruct
    private void setUp() {
        baseDirFile = new File(baseDir);
        if (baseDirFile.exists() && baseDirFile.isDirectory()) return;
        if (baseDirFile.exists() && !baseDirFile.isDirectory())
            throw new TechnicalException(ErrorMessages.COULD_NOT_CREATE_OR_OVERWRITE_FILE_OR_DIRECTORY + ": " + baseDirFile);
        if (!baseDirFile.exists() && !baseDirFile.mkdir())
            throw new TechnicalException(ErrorMessages.COULD_NOT_CREATE_OR_OVERWRITE_FILE_OR_DIRECTORY + ": " + baseDirFile);
    }

    /**
     * Performs overwriting existing file using report name as file name in the given directory (report.save.dir)
     * with the given report object in JSON format.
     * The directory used for saving is specified in report.save.dir property.
     *
     * @param report report to be saved on the disk
     * @throws IOException
     */
    public void saveReport(Report report) throws IOException {
        File resultFile = new File(baseDirFile, report.getName());
        logger.trace("Saving report to file: {}", resultFile.getAbsolutePath());
        if (resultFile.exists() && resultFile.delete() && resultFile.createNewFile()) {
            save(report, resultFile);
        } else if (!resultFile.exists() && resultFile.createNewFile()) {
            save(report, resultFile);
        } else {
            throw new TechnicalException(ErrorMessages.COULD_NOT_CREATE_OR_OVERWRITE_FILE_OR_DIRECTORY + ": " + resultFile.getName());
        }
    }

    private void save(Report report, File resultFile) throws IOException {
        String stringReport = objectMapper.writeValueAsString(report);
        objectMapper.writeValue(resultFile, stringReport);
        logger.trace("Saved report string value: \n{}", stringReport);
    }
}
