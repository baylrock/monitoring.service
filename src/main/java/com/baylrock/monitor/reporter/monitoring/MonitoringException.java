package com.baylrock.monitor.reporter.monitoring;

import lombok.Getter;

public class MonitoringException extends Exception {

    @Getter
    private String url;

    public MonitoringException(String url, Exception cause) {
        super(cause);
        this.url = url;
    }
}
