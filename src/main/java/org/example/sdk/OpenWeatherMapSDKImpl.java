package org.example.sdk;

import org.example.cache.CacheEntry;
import org.example.cache.WeatherCache;
import org.example.constant.Mode;
import org.example.error.SDKException;
import org.example.model.WeatherData;
import org.example.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link OpenWeatherMapSDK}.
 * - On-demand and polling modes for data retrieval.
 * - LRU in-memory cache for up to 10 cities with 10-minute freshness TTL.
 * - Error handling via {@link SDKException}.
 */
public class OpenWeatherMapSDKImpl implements OpenWeatherMapSDK {

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapSDKImpl.class);
    private static final int POLLING_INTERVAL_MINUTES = 10;
    private final String apiKey;
    private final Mode mode;
    private final WeatherCache cache;
    private final WeatherService weatherService;
    private final ScheduledExecutorService scheduler;

    /**
     * Advanced constructor primarily for testing or custom dependency injection.
     * Prefer creating instances via {@link OpenWeatherMapSDKFactory} to ensure one-per-key semantics.
     */
    OpenWeatherMapSDKImpl(String apiKey, Mode mode, WeatherService service, WeatherCache cache) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.cache = cache;
        this.weatherService = service;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        if (mode == Mode.POLLING) startPolling();
    }

    OpenWeatherMapSDKImpl(String apiKey, Mode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.cache = new WeatherCache();
        this.weatherService = new WeatherService(apiKey);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        if (mode == Mode.POLLING) startPolling();
    }

    /**
     * Exposes the internal scheduler for testing/monitoring purposes.
     * Consumers should not rely on this in production code.
     */
    ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Disposes this SDK instance and releases resources (stops polling if running).
     */
    @Override
    public void delete() throws SDKException {
        shutdownPolling();
    }

    /**
     * Returns current weather for the requested city. Uses instance mode
     * (ON_DEMAND or POLLING) which is specified at SDK construction.
     */
    @Override
    public WeatherData getCurrentWeather(String city) throws SDKException {
        CacheEntry freshEntry = cache.getFresh(city);
        if (freshEntry != null) return freshEntry.getData();

        // If the cache misses or stales, decide based on instance mode
        if (this.mode == Mode.ON_DEMAND || this.mode == Mode.POLLING) return updateAndReturnWeather(city);

        throw new SDKException("Unknown SDK mode: " + this.mode);
    }

    /**
     * Internal method: fetches data from the service and caches it.
     */
    private WeatherData updateAndReturnWeather(String city) throws SDKException {
        WeatherData data = weatherService.fetchWeather(city);
        cache.put(city, data);
        return data;
    }

    /**
     * Start a background poller for cached cities.
     */
    private void startPolling() {
        Runnable poller = () -> {
            logger.info("Polling started. Updating {} cities", cache.size());
            List<String> cacheCopy = new ArrayList<>(cache.keySet());
            cacheCopy.forEach(city -> {
                try {
                    updateAndReturnWeather(city);
                    logger.info("Updated weather for {}", city);
                } catch (SDKException e) {
                    logger.error("Polling error for {} city : {}", city, e.getMessage());
                }
            });
        };

        scheduler.scheduleWithFixedDelay(
                poller,
                POLLING_INTERVAL_MINUTES,
                POLLING_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * Gracefully shutdown polling scheduler.
     */
    public void shutdownPolling() {
        if (mode == Mode.POLLING && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Polling scheduler for {} stopped.", apiKey);
        }
    }
}