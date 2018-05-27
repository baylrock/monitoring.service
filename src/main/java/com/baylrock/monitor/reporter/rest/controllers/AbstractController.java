package com.baylrock.monitor.reporter.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.baylrock.monitor.reporter.monitoring.MonitoringService;
import com.baylrock.monitor.reporter.persistent.mongo.dao.StatisticsDao;

import lombok.Getter;

public class AbstractController {

    @Autowired
    @Getter
    private StatisticsDao statisticsDao;

    @Autowired
    @Getter
    private MonitoringService service;

}
