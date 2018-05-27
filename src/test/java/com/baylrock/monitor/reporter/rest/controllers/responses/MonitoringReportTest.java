package com.baylrock.monitor.reporter.rest.controllers.responses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.baylrock.monitor.reporter.monitoring.StatusResponse;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

public class MonitoringReportTest {

    private final Instant now = Instant.now();
    private List<ServiceStatusEntity> entities;

    @Before
    public void setUp() {
        entities = Arrays.asList(
                new ServiceStatusEntity("", now, StatusResponse.Status.OFFLINE),
                new ServiceStatusEntity("", now.plusSeconds(10), StatusResponse.Status.READY),
                new ServiceStatusEntity("", now.plusSeconds(20), StatusResponse.Status.OFFLINE),
                new ServiceStatusEntity("", now.plusSeconds(30), StatusResponse.Status.READY),
                new ServiceStatusEntity("", now.plusSeconds(40), StatusResponse.Status.OFFLINE),
                new ServiceStatusEntity("", now.plusSeconds(50), StatusResponse.Status.READY)
        );
    }

    @Test
    public void testReportGeneration$correctReport() {
        MonitoringReport monitoringReport = new MonitoringReport(entities);
        assertEquals(StatusResponse.Status.READY, monitoringReport.getCurrentStatus());
        assertEquals(3, monitoringReport.getOnlinePeriods().size());
        assertEquals(3, monitoringReport.getOfflinePeriods().size());
        assertEquals(now, monitoringReport.getOfflinePeriods().get(0).getFrom());
        assertEquals(now.plusSeconds(10), monitoringReport.getOfflinePeriods().get(0).getTo());
        assertEquals(now.plusSeconds(10), monitoringReport.getOnlinePeriods().get(0).getFrom());
        assertEquals(now.plusSeconds(20), monitoringReport.getOnlinePeriods().get(0).getTo());
    }

    @Test
    public void avoidStatusDuplicates() {
        // last status is READY. add one more READY status (should be ignored)
        entities = new ArrayList<>(entities);
        entities.add(new ServiceStatusEntity("", now.plusSeconds(60), StatusResponse.Status.READY));
        MonitoringReport monitoringReport = new MonitoringReport(entities);
        assertEquals(StatusResponse.Status.READY, monitoringReport.getCurrentStatus());
        assertEquals(3, monitoringReport.getOnlinePeriods().size());
        assertEquals(3, monitoringReport.getOfflinePeriods().size());
    }

    @Test
    public void emptyData() {
        MonitoringReport monitoringReport = new MonitoringReport(Collections.emptyList());
        assertEquals(StatusResponse.Status.NA, monitoringReport.getCurrentStatus());
        assertTrue(monitoringReport.getOnlinePeriods().isEmpty());
        assertTrue(monitoringReport.getOfflinePeriods().isEmpty());
    }

    @Test
    public void singleStatus() {
        MonitoringReport monitoringReport = new MonitoringReport(Collections.singletonList(new ServiceStatusEntity("", now, StatusResponse.Status.READY)));
        assertEquals(StatusResponse.Status.READY, monitoringReport.getCurrentStatus());
        assertEquals(1, monitoringReport.getOnlinePeriods().size());
    }
}