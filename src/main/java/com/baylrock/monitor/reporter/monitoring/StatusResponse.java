package com.baylrock.monitor.reporter.monitoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {

    private Status status = Status.OFFLINE;

    public enum Status {
        NA, OFFLINE, READY
    }
}
