package com.baylrock.monitor.reporter.rest.controllers;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baylrock.monitor.reporter.monitoring.StatusResponse;
import com.baylrock.monitor.reporter.persistent.mongo.entities.ServiceStatusEntity;
import com.baylrock.monitor.reporter.rest.controllers.responses.MonitoringReport;
import com.google.common.base.Strings;

@RestController
@RequestMapping("monitoring")
public class MonitoringController extends AbstractController {

    /**
     * List of all status entities
     */
    @GetMapping
    public List<ServiceStatusEntity> getAllStatuses() {
        return getStatisticsDao().listAll();
    }

    /**
     * Generates {@link MonitoringReport} for particular service by url.
     * Report will be generated for all statuses beginning from the earliest record or records limited by #fromDate parameter;
     *
     * @param fromDate report beginning date. Value format: ISO_ZONED_DATE_TIME
     *                 '2011-12-03T10:15:30+01:00[Europe/Paris]'
     */
    @GetMapping("report")
    public MonitoringReport generateReport(@RequestParam(value = "url") String url,
                                           @RequestParam(value = "fromDate", required = false) String fromDate)
            throws URISyntaxException, MalformedURLException {
        Instant fromDateParsed = Optional.ofNullable(fromDate)
                .map(Strings::emptyToNull)
                .map(ZonedDateTime::parse)
                .map(ZonedDateTime::toInstant)
                .orElse(Instant.ofEpochMilli(0));
        List<ServiceStatusEntity> serviceStatusEntities = getStatisticsDao().listStatusesFrom(url, fromDateParsed);
        return new MonitoringReport(serviceStatusEntities);
    }

    /**
     * Enables monitoring for service by url. Can be used to change monitoring frequency of already enabled monitoring task.
     *
     * @param url       service url (https://monitorable.service.com).
     * @param frequency delay between monitoring requests in ms
     */
    @GetMapping("enable")
    public void enableMonitoring(@RequestParam("url") String url,
                                 @RequestParam("frequency") long frequency,
                                 @RequestParam(value = "continueOnError", required = false) boolean continueOnError) throws URISyntaxException, MalformedURLException {
        getService().enableMonitoring(url, frequency, continueOnError);
    }

    /**
     * Stops monitoring task by url.
     *
     * @param url service url (https://monitorable.service.com).
     */
    @DeleteMapping
    public void disableMonitoring(@RequestParam("url") String url) {
        getService().stopTask(url);
    }

    /**
     * Adjusts status of service by url. Can be used for manual/integration testing purposes.
     *
     * @param url            service url (https://monitorable.service.com).
     * @param statusResponse {@link StatusResponse} which will be added to service statuses
     */
    @PostMapping
    public void adjustStatus(@RequestParam("url") String url, @RequestBody StatusResponse statusResponse) {
        getStatisticsDao().saveStatus(new ServiceStatusEntity(url, Instant.now(), statusResponse.getStatus()));
    }

    @GetMapping("activetasks/count")
    public int countActiveMonitoringTasks() {
        return getService().getMonitoringServicesList().size();
    }

    @GetMapping("activetasks/list")
    public Set<String> getActiveMonitoringTasks() {
        return getService().getMonitoringServicesList();
    }

}
