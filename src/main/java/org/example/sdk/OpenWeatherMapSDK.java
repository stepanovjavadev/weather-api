package org.example.sdk;

import org.example.error.SDKException;
import org.example.model.WeatherData;

/**
 * Public contract for the OpenWeatherMap SDK.
 * <p>
 * Implementations should respect cache and polling requirements described in the technical documentation.
 */
public interface OpenWeatherMapSDK {

    /**
     * Returns current weather for the given city.
     * <p>
     * Implementations should return cached data if it is still fresh (less than 10 minutes old)
     * and should update the cache otherwise. In polling mode, the SDK should keep the cache
     * warm in the background to provide near zero-latency responses.
     *
     * Mode is configured at SDK initialization, not per-call.
     *
     * @param city city name (case-insensitive)
     * @return weather data for the city
     * @throws SDKException if the request fails or the data cannot be retrieved
     */
    WeatherData getCurrentWeather(String city) throws SDKException;

    /**
     * Disposes this SDK instance and releases internal resources (e.g., stops background polling).
     * <p>
     * When using {@link OpenWeatherMapSDKFactory}, call {@link OpenWeatherMapSDKFactory#releaseInstance(String)}
     * afterward to unregister this instance and allow creating a new one with the same API key.
     * @throws SDKException if the request fails
     */
    void delete() throws SDKException;
}
