package com.baylrock.monitor.reporter.monitoring;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.baylrock.monitor.reporter.persistent.mongo.dao.StatisticsDao;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MonitoringService implements ErrorHandler {

    private final StatisticsDao statisticsDao;
    private final MonitoringRestAPI api;

    private final ThreadPoolTaskScheduler executorService;
    private final ConcurrentHashMap<String, ScheduledFuture> runningTasks;

    @Autowired
    public MonitoringService(StatisticsDao statisticsDao, MonitoringRestAPI api) {
        this.statisticsDao = statisticsDao;
        this.api = api;
        runningTasks = new ConcurrentHashMap<>();
        executorService = new ThreadPoolTaskScheduler();
        executorService.setErrorHandler(this);
        executorService.initialize();
    }

    /**
     * Monitoring task factory for autowiring purposes
     */
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    MonitoringTask createTask(String baseBath, URI monitoringUri, boolean continueOnError) {
        return new MonitoringTask(baseBath, monitoringUri, continueOnError);
    }

    /**
     * @return final uri to serviceStatus endpoint
     */
    private URI createMonitoringURI(String baseBath) throws MalformedURLException, URISyntaxException {
        URL url = new URL(baseBath);
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + "/accountmanagement/monitor", null).toURI();
    }

    /**
     * @param baseBath service http URL
     * @param freq     monitoring requests frequency (delay between requests) in ms
     * @param continueOnError
     */
    public void enableMonitoring(String baseBath, long freq, boolean continueOnError) throws MalformedURLException, URISyntaxException {
        ScheduledFuture scheduledFuture = runningTasks.remove(baseBath);
        if (scheduledFuture != null) {
            stopTask(scheduledFuture);
        } else {
            // do not delete data if task restarted
            statisticsDao.removeMonitoringData(baseBath);
        }
        URI monitoringURI = createMonitoringURI(baseBath);
        if (!continueOnError && !api.legalServiceUri(monitoringURI)) {
            throw new MalformedURLException(baseBath);
        }
        ScheduledFuture schedule = executorService.scheduleAtFixedRate(createTask(baseBath, monitoringURI, continueOnError), freq);
        runningTasks.put(baseBath, schedule);

    }

    private void stopTask(ScheduledFuture scheduledFuture) {
        scheduledFuture.cancel(false);
        try {
            scheduledFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
            log.error("Execution error while stopping scheduled monitoring task", e);
        }
    }

    /**
     * Stop and delete monitoring task by service url
     *
     * @param urlStr http url of service
     */
    public void stopTask(String urlStr) {
        ScheduledFuture scheduledFuture = runningTasks.remove(urlStr);
        if (scheduledFuture != null) {
            stopTask(scheduledFuture);
        }
    }

    /**
     * Scheduled task error handler. Stops and remove task on {@link MonitoringException} error
     */
    @Override
    public void handleError(Throwable throwable) {
        if (throwable instanceof MonitoringException) {
            stopTask(((MonitoringException) throwable).getUrl());
            log.error("Monitoring task for '" + ((MonitoringException) throwable).getUrl() + "' stopped due to monitoring exception", throwable);
        } else {
            log.error("Unexpected unhandled error occurred in monitoring some task", throwable);
        }
    }

    public Set<String> getMonitoringServicesList() {
        return runningTasks.keySet();
    }
}
