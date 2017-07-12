package com.vs.report.logic;

import com.vs.report.Application;
import com.vs.report.model.Report;
import com.vs.report.system.TechnicalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

//TODO: write more tests
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class ReportDownloadingServiceTest {

    private static final Logger logger = LogManager.getLogger(ReportDownloadingService.class);

    @Autowired
    private ReportDownloadingService reportDownloadingService;
    @Mock
    private ReportSavingService reportSavingService;
    @Mock
    private ReportingApiClient reportingApiClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(reportDownloadingService, "reportCount", 10);
        ReflectionTestUtils.setField(reportDownloadingService, "reportSavingService", reportSavingService);
        ReflectionTestUtils.setField(reportDownloadingService, "reportingApiClient", reportingApiClient);
    }

    @Test
    public void testDownloadAndSaveWithSuccess() throws IOException {
        Report mockedReport = Mockito.mock(Report.class);
        Mockito.when(reportingApiClient.getReport(Mockito.anyString())).thenReturn(mockedReport);

        reportDownloadingService.downloadAndSave();
        Integer reportCount = (Integer) ReflectionTestUtils.getField(reportDownloadingService, "reportCount");
        Mockito.verify(reportSavingService, Mockito.times(reportCount)).saveReport(mockedReport);
    }

    @Test(expected = TechnicalException.class)
    public void testDownloadAndSaveWithException() throws IOException {
        ReflectionTestUtils.setField(reportDownloadingService, "reportCount", 1);
        Report mockedReport = Mockito.mock(Report.class);
        Mockito.when(reportingApiClient.getReport(Mockito.anyString())).thenReturn(mockedReport);
        Mockito.doThrow(IOException.class).when(reportSavingService).saveReport(mockedReport);
        reportDownloadingService.downloadAndSave();
    }
}
