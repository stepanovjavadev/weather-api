package org.example.cache;

import org.example.model.WeatherData;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache for weather data with TTL and case-insensitive city keys.
 * - Keeps at most MAX_SIZE entries (LRU eviction).
 * - Entries older than MAX_AGE_MS are considered stale and removed on access.
 * - City names are normalized to lower-case, so lookups are case-insensitive.
 */
public class WeatherCache extends LinkedHashMap<String, CacheEntry> {

    private static final int MAX_SIZE = 10;
    private static final long MAX_AGE_MS = 10L * 60L * 1000L; // 10 minutes
    private static final float LOAD_FACTOR = 0.75f;
    private static final boolean ACCESS_ORDER_LRU = true;

    public WeatherCache() {
        super(MAX_SIZE, LOAD_FACTOR, ACCESS_ORDER_LRU);
    }

    private String norm(String city) {
        return city == null ? null : city.toLowerCase();
    }

    /**
     * Returns a fresh CacheEntry for the normalized city name or null if missing/stale.
     * Stale entries are removed.
     */
    public CacheEntry getFresh(String city) {
        String key = norm(city);
        CacheEntry entry = super.get(key);
        if (entry != null && (Instant.now().toEpochMilli() - entry.getReceiptTime()) < MAX_AGE_MS) {
            return entry;
        }
        // Remove stale or missing entry
        super.remove(key);
        return null;
    }

    /**
     * Put a WeatherData into a cache under a normalized city key.
     */
    public void put(String city, WeatherData data) {
        super.put(norm(city), new CacheEntry(data));
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
        return size() > MAX_SIZE;
    }
}
