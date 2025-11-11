package org.example.cache;

import org.example.model.WeatherData;

import java.time.Instant;

/**
 * Lightweight container for cached WeatherData that stores the moment the data was received.
 */
public class CacheEntry {

    private final WeatherData data;

    private final long receiptTime; // time received in milliseconds

    CacheEntry(WeatherData data) {
        this.data = data;
        this.receiptTime = Instant.now().toEpochMilli();
    }

    public WeatherData getData() {
        return data;
    }

    public long getReceiptTime() {
        return receiptTime;
    }
}
