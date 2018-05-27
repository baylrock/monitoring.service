package com.baylrock.monitor.reporter.persistent.mongo.entities;

import java.time.Instant;

import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.baylrock.monitor.reporter.monitoring.StatusResponse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode

@Document
public class ServiceStatusEntity {

    @Indexed
    private String serviceURI;

    @Indexed(direction = IndexDirection.ASCENDING)
    private Instant scanTime;

    private StatusResponse.Status status;

}
