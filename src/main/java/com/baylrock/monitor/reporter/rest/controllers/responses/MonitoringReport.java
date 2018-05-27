package com.baylrock.monitor.reporter.rest.controllers.responses;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.baylrock.monitor.reporter.monitoring.StatusResponse;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class MonitoringReport {

    private StatusResponse.Status currentStatus = StatusResponse.Status.NA;
    private List<Period> offlinePeriods = new ArrayList<>();
    private List<Period> onlinePeriods = new ArrayList<>();

    /**
     * @param statusEntities list of status entities ordered ascending by {@link ServiceStatusEntity#getScanTime()}
     */
    public MonitoringReport(List<ServiceStatusEntity> statusEntities) {
        ServiceStatusEntity last = null;
        for (ServiceStatusEntity entity : statusEntities) {
            if (last == null) {
                last = entity;
                continue;
            }
            if (last.getStatus() != entity.getStatus()) {
                if (entity.getStatus() == StatusResponse.Status.READY) {
                    offlinePeriods.add(new Period(last.getScanTime(), entity.getScanTime()));
                } else {
                    onlinePeriods.add(new Period(last.getScanTime(), entity.getScanTime()));
                }
                last = entity;
            }
        }
        if (last != null) {
            if (last.getStatus() == StatusResponse.Status.READY) {
                onlinePeriods.add(new Period(last.getScanTime(), null));
            } else {
                offlinePeriods.add(new Period(last.getScanTime(), null));
            }
            currentStatus = last.getStatus();
        }
    }

    @Getter
    @AllArgsConstructor
    static class Period {
        private Instant from;
        private Instant to;
    }

}
