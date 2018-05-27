package com.baylrock.monitor.reporter.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.baylrock.monitor.reporter.monitoring.MonitoringRestAPI;
import com.baylrock.monitor.reporter.monitoring.StatusResponse;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MonitoringControllerTest {

    @Autowired
    private MonitoringController controller;

    @MockBean
    private MonitoringRestAPI api;

    @Before
    public void setUp() throws Exception {
        controller.getStatisticsDao().removeMonitoringData();
        doReturn(true).when(api).legalServiceUri(any());
    }

    @Test
    public void testEnableMonitoring() throws MalformedURLException, URISyntaxException, InterruptedException {
        doReturn(new StatusResponse(StatusResponse.Status.READY)).when(api).getServiceStatus(any());
        controller.enableMonitoring("https://xxx.com", 10, false);
        Thread.sleep(50);
        verify(api, atLeastOnce()).getServiceStatus(any());
        controller.disableMonitoring("https://xxx.com");
        List<ServiceStatusEntity> statuses = controller.getStatisticsDao().listAll();
        assertEquals(1, statuses.size());
        assertEquals(StatusResponse.Status.READY, statuses.get(0).getStatus());
    }

    @Test
    public void testAdjustStatus() {
        List<ServiceStatusEntity> allStatuses = controller.getAllStatuses();
        assertTrue(allStatuses.isEmpty());
        controller.adjustStatus("http://xxx.com", new StatusResponse(StatusResponse.Status.READY));
        allStatuses = controller.getAllStatuses();
        assertEquals(1, allStatuses.size());
        assertEquals(StatusResponse.Status.READY, allStatuses.get(0).getStatus());
        assertEquals("http://xxx.com", allStatuses.get(0).getServiceURI());
    }

    @Test(expected = MalformedURLException.class)
    public void testEnableMonitoring$malformedUrl() throws MalformedURLException, URISyntaxException {
        controller.enableMonitoring("xxx.com", 10, false);
    }

    @Test
    public void testDisableMonitoring() throws MalformedURLException, URISyntaxException {
        doReturn(new StatusResponse(StatusResponse.Status.READY)).when(api).getServiceStatus(any());
        controller.enableMonitoring("https://xxx.com", 1000000, false);
        Set<String> stringSet = controller.getService().getMonitoringServicesList();
        assertEquals(1, stringSet.size());
        assertEquals("https://xxx.com", stringSet.iterator().next());
        controller.disableMonitoring("https://xxx.com");
        stringSet = controller.getService().getMonitoringServicesList();
        assertTrue(stringSet.isEmpty());
    }
}