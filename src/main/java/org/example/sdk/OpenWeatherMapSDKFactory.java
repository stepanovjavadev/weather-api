package org.example.sdk;

import org.example.constant.Mode;
import org.example.error.SDKException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory (Multiton) that manages SDK instances by API key.
 * <p>
 * Guarantees that only one SDK instance exists per API key at a time.
 * Also provides a way to release instances and stop their background resources.
 * <p>
 * Note: the Mode parameter is applied only when creating a new instance. If an instance for the
 * same API key already exists, getInstance(...) will return the existing instance regardless of
 * the Mode argument. To change the Mode for a key, release/delete the existing instance first
 * (OpenWeatherMapSDKFactory.releaseInstance(apiKey) or sdk.delete()), then call getInstance(...)
 * with the desired Mode.
 */
public final class OpenWeatherMapSDKFactory {

    private static final Map<String, OpenWeatherMapSDK> instances = new ConcurrentHashMap<>();

    private OpenWeatherMapSDKFactory() {}

    /**
     * Returns a singleton-like SDK instance for the given API key (Multiton).
     * If the instance does not exist yet, it will be created with the specified mode.
     * <p>
     * Note: If an instance for the same API key already exists, this method returns that instance
     * and ignores the provided mode parameter. To get an instance with a different mode, first
     * release the existing instance via {@link #releaseInstance(String)} or by calling {@code sdk.delete()}.
     *
     * @param apiKey non-empty OpenWeatherMap API key
     * @param mode SDK mode: ON_DEMAND or POLLING
     * @return existing or newly created SDK instance bound to the given key
     * @throws SDKException if the API key is invalid or initialization fails
     */
    public static OpenWeatherMapSDK getInstance(String apiKey, Mode mode) throws SDKException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new SDKException("API Key cannot be null or empty.");
        }

        // ConcurrentHashMap.computeIfAbsent guarantees atomicity
        return instances.computeIfAbsent(apiKey, k -> {
            try {
                return new OpenWeatherMapSDKImpl(k, mode);
            } catch (Exception e) {
                throw new SDKException("Failed to initialize SDK for key " + apiKey, e);
            }
        });
    }

    /**
     * Releases and unregisters the SDK instance for the given API key, if any.
     * Background polling is shut down gracefully.
     *
     * @param apiKey API key to remove
     */
    public static void releaseInstance(String apiKey) {
        if (apiKey == null) return;
        OpenWeatherMapSDK sdk = instances.remove(apiKey);
        if (sdk instanceof OpenWeatherMapSDKImpl impl) {
            impl.shutdownPolling();
        }
    }
}