package org.example.service;

import org.example.error.SDKException;
import org.example.model.WeatherData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {

    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiKey;

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * Makes a synchronous request to the OpenWeatherMap API.
     * @throws SDKException if the request fails or the data cannot be retrieved
     */
    public WeatherData fetchWeather(String city) throws SDKException {
        String encodedCity;
        try {
            encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SDKException("Failed to encode the city name: " + city, e);
        }

        Request request = new Request.Builder()
                .url(String.format(API_BASE_URL, encodedCity, apiKey))
                .build();

        try (Response response = httpClient.newCall(request).execute()){
            if (!response.isSuccessful()) {
                // Handling errors from API (401 Unauthorized, 404 Not Found etc.)
                String responseBody = response.body().string();
                throw new SDKException(
                        String.format("Error API (HTTP %d) in request for %s. Response: %s",
                                response.code(), city, responseBody)
                );
            }
            return gson.fromJson(response.body().charStream(), WeatherData.class);
        } catch (IOException e) {
            throw new SDKException("Network error when accessing the OpenWeatherMap API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SDKException("Unexpected error while processing the API response: " + e.getMessage(), e);
        }
    }
}
