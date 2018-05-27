package com.baylrock.monitor.reporter.persistent.mongo.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;

public interface ServiceStatusRepository extends MongoRepository<ServiceStatusEntity, String> {

    /**
     * @return last (youngest/current status) status entity of particular service
     */
    Optional<ServiceStatusEntity> findFirstByServiceURIOrderByScanTimeDesc(String serviceURI);

    /**
     * @param serviceURI filters entities for particular service
     * @param scanTime   filters entities returning only those whose {@link ServiceStatusEntity#getScanTime()} after given parameter
     * @return status entities ordered ascending by time.
     */
    List<ServiceStatusEntity> findAllByServiceURIAndScanTimeGreaterThanEqual(String serviceURI, Instant scanTime);

    void deleteAllByServiceURI(String serviceURI);

}
