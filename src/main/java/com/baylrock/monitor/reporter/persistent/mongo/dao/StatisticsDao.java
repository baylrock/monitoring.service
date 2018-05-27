package com.baylrock.monitor.reporter.persistent.mongo.dao;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

public interface StatisticsDao {

    List<ServiceStatusEntity> listStatusesFrom(String uri, Instant time) throws URISyntaxException, MalformedURLException;

    List<ServiceStatusEntity> listAll();

    /**
     * Save next service status. StatusEntity will be saved only if status value is different from last(current) or last status does not exists
     *
     * @param entity new status
     */
    void saveStatus(ServiceStatusEntity entity);

    /**
     * Cleanup monitoring data. Should be used before ne monitoring process started.
     */
    void removeMonitoringData(String uri);

    void removeMonitoringData();

}
