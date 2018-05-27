package com.baylrock.monitor.reporter.persistent.mongo.dao;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;
import com.baylrock.monitor.reporter.persistent.mongo.repositories.ServiceStatusRepository;
import com.google.common.base.Strings;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DefaultStatisticsDaoImpl implements StatisticsDao {

    private final ServiceStatusRepository repository;

    public void saveStatus(ServiceStatusEntity entity) {
        Optional<ServiceStatusEntity> lastStatus = repository.findFirstByServiceURIOrderByScanTimeDesc(entity.getServiceURI());
        if (!lastStatus.isPresent() || lastStatus.get().getStatus() != entity.getStatus()) {
            repository.save(entity);
        }
    }

    @Override
    public void removeMonitoringData(String uri) {
        repository.deleteAllByServiceURI(uri);
    }

    @Override
    public void removeMonitoringData() {
        repository.deleteAll();
    }

    @Override
    public List<ServiceStatusEntity> listStatusesFrom(String uri, Instant time) {
        if (Strings.isNullOrEmpty(uri)) {
            return Collections.emptyList();
        }
        return repository.findAllByServiceURIAndScanTimeGreaterThanEqual(uri, time);
    }

    @Override
    public List<ServiceStatusEntity> listAll() {
        return repository.findAll();
    }

}
