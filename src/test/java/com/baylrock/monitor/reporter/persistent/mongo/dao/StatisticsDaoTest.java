package com.baylrock.monitor.reporter.persistent.mongo.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.baylrock.monitor.reporter.monitoring.StatusResponse;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StatisticsDaoTest {

    @Autowired StatisticsDao dao;

    @Before
    public void setUp() throws Exception {
        dao.removeMonitoringData();
    }

    @Test
    public void testSave() {
        ServiceStatusEntity testEntity = new ServiceStatusEntity("xxx", Instant.now(), StatusResponse.Status.OFFLINE);
        dao.saveStatus(testEntity);
        List<ServiceStatusEntity> serviceStatusEntities = dao.listAll();
        assertEquals(1, serviceStatusEntities.size());
        assertEquals(testEntity, serviceStatusEntities.get(0));
    }

    @Test
    public void testSave$lastStatusSame() {
        testSave();
        ServiceStatusEntity testEntity = new ServiceStatusEntity("xxx", Instant.now(), StatusResponse.Status.OFFLINE);
        dao.saveStatus(testEntity);
        List<ServiceStatusEntity> serviceStatusEntities = dao.listAll();
        assertEquals(1, serviceStatusEntities.size());
        assertNotEquals(testEntity, serviceStatusEntities.get(0));

        testEntity = new ServiceStatusEntity("xxx", Instant.now(), StatusResponse.Status.READY);
        dao.saveStatus(testEntity);
        serviceStatusEntities = dao.listAll();
        assertEquals(2, serviceStatusEntities.size());
        assertEquals(testEntity, serviceStatusEntities.get(1));
    }

    @Test
    public void testListFromDate() throws MalformedURLException, URISyntaxException {
        Instant now = Instant.now();
        dao.saveStatus(new ServiceStatusEntity("xxx", now.minusSeconds(100), StatusResponse.Status.OFFLINE));
        dao.saveStatus(new ServiceStatusEntity("xxx", now, StatusResponse.Status.READY));
        dao.saveStatus(new ServiceStatusEntity("xxx", now.plusSeconds(100), StatusResponse.Status.OFFLINE));

        assertEquals(3, dao.listStatusesFrom("xxx", Instant.ofEpochMilli(0)).size());
        assertEquals(2, dao.listStatusesFrom("xxx", now).size());
        assertEquals(1, dao.listStatusesFrom("xxx", now.plusSeconds(100)).size());
        assertEquals(0, dao.listStatusesFrom("xxx", now.plusSeconds(200)).size());
    }

    @Test
    public void tetListFromDate$wrongUri() throws MalformedURLException, URISyntaxException {
        Instant now = Instant.now();
        dao.saveStatus(new ServiceStatusEntity("xxx", now, StatusResponse.Status.OFFLINE));

        assertEquals(0, dao.listStatusesFrom("sadasdas", now).size());
        assertEquals(1, dao.listStatusesFrom("xxx", now).size());
    }

    @Test
    public void testRemoveData() {
        dao.saveStatus(new ServiceStatusEntity("xxx", Instant.now(), StatusResponse.Status.OFFLINE));
        assertEquals(1, dao.listAll().size());
        dao.removeMonitoringData("asdads");
        assertEquals(1, dao.listAll().size());
        dao.removeMonitoringData("xxx");
        assertEquals(0, dao.listAll().size());
    }
}