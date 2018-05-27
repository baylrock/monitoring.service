package com.baylrock.monitor.reporter.monitoring;

import java.net.URI;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;

import com.baylrock.monitor.reporter.persistent.mongo.dao.StatisticsDao;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitoringTask implements Runnable {

    private final URI uri;
    private final boolean continueOnError;
    private final String basePath;

    @Autowired
    private MonitoringRestAPI api;

    @Autowired
    private StatisticsDao dao;

    public MonitoringTask(String basePath, URI uri, boolean continueOnError) {
        this.basePath = basePath;
        this.uri = uri;
        this.continueOnError = continueOnError;
    }

    @Override
    @SneakyThrows
    public void run() {
        try {
            dao.saveStatus(new ServiceStatusEntity(basePath, Instant.now(), api.getServiceStatus(uri).getStatus()));
        } catch (Exception e) {
            if (!continueOnError) {
                throw new MonitoringException(basePath, e);
            } else {
                log.warn("Error occurred while processing monitoring task for '" + basePath + "' service, but ignored due to 'continueOnError' mode");
            }
        }
    }
}
