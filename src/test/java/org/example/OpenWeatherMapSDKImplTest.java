package org.example;

import org.example.cache.WeatherCache;
import org.example.constant.Mode;
import org.example.error.SDKException;
import org.example.model.WeatherData;
import org.example.sdk.OpenWeatherMapSDK;
import org.example.sdk.OpenWeatherMapSDKFactory;
import org.example.sdk.OpenWeatherMapSDKImpl;
import org.example.service.WeatherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenWeatherMapSDKImplTest {

    private static final String API_KEY = "testKey";
    private static final String CITY = "zocca";
    private static final double TEMPERATURE = 269.6;
    private static final double FEELS_LIKE = 267.57;
    private static final int VISIBILITY = 10000;
    private static final double WIND_SPEED = 1.38;
    private static final int DATE_TIME = 1675744800;
    private static final int SUNRISE = 1675751262;
    private static final int SUNSET = 1675787560;
    private static final int TIMEZONE = 3600;
    private static final String DESCRIPTION = "Scattered clouds";
    private static final String WEATHER_MAIN = "Clouds";

    @Mock
    private WeatherService mockWeatherService;

    @Spy
    private WeatherCache cache = new WeatherCache();

    private OpenWeatherMapSDKImpl onDemandSdk;
    private OpenWeatherMapSDKImpl pollingSdk;
    private WeatherData mockWeatherData;

    @BeforeEach
    void setUp() throws SDKException {
        mockWeatherData = new WeatherData();
        mockWeatherData.setName(CITY);
        lenient().when(mockWeatherService.fetchWeather(anyString())).thenReturn(mockWeatherData);
        onDemandSdk = new OpenWeatherMapSDKImpl(API_KEY, Mode.ON_DEMAND, mockWeatherService, cache);
        pollingSdk = new OpenWeatherMapSDKImpl(API_KEY, Mode.POLLING, mockWeatherService, cache);
    }

    @AfterEach
    void tearDown() {
        pollingSdk.shutdownPolling();
    }

    @Test
    void getCurrentWeather_onDemandMode_emptyCache_ShouldCallsApi() throws SDKException {
        onDemandSdk.getCurrentWeather(CITY);

        verify(mockWeatherService).fetchWeather(CITY);
    }

    @Test
    void getCurrentWeather_onDemandMode_cacheHit_ShouldNotCallApi() throws SDKException {
        onDemandSdk.getCurrentWeather(CITY);
        onDemandSdk.getCurrentWeather(CITY);

        verify(mockWeatherService).fetchWeather(CITY);
    }

    @Test
    void getCurrentWeather_onDemand_apiFails_ShouldThrowsException() throws SDKException {
        when(mockWeatherService.fetchWeather(CITY))
                .thenThrow(new SDKException("Network error"));

        assertThrows(SDKException.class, () -> onDemandSdk.getCurrentWeather(CITY));
        verify(mockWeatherService).fetchWeather(CITY);
    }

    // POLLING

    @Test
    void pollingSdk_ShouldInitializeWithSchedulerRunning() {
        assertFalse(pollingSdk.getScheduler().isShutdown());
    }

    @Test
    void getCurrentWeatherPollingMode_ShouldCallApiAndCache() throws SDKException {
        pollingSdk.getCurrentWeather(CITY);
        verify(mockWeatherService, times(1)).fetchWeather(CITY);

        pollingSdk.getCurrentWeather(CITY);
        verify(mockWeatherService, times(1)).fetchWeather(CITY);
    }

    // Cache

    @Test
    void cacheOverflow_ShouldEvictOldestEntry() throws SDKException {
        for (int i = 0; i < 11; i++) {
            String city = "City" + i;
            onDemandSdk.getCurrentWeather(city);
        }

        assertEquals(10, cache.size());
        assertNull(cache.getFresh("city0"));
        assertNotNull(cache.getFresh("city1"));
    }

    // Delete

    @Test
    void delete_ShouldReleaseInstanceAndShutdownPolling() throws SDKException {
        OpenWeatherMapSDK sdk = OpenWeatherMapSDKFactory.getInstance(CITY, Mode.POLLING);
        sdk.delete();
        OpenWeatherMapSDKFactory.releaseInstance(CITY);
        OpenWeatherMapSDK newSdk = OpenWeatherMapSDKFactory.getInstance(CITY, Mode.POLLING);
        assertNotSame(sdk, newSdk);
        assertTrue(((OpenWeatherMapSDKImpl) sdk).getScheduler().isShutdown());
    }

    @Test
    void delete_ShouldHandleMultipleCalls() throws SDKException {
        OpenWeatherMapSDK sdk = OpenWeatherMapSDKFactory.getInstance(CITY, Mode.POLLING);
        sdk.delete();
        sdk.delete();
        assertTrue(((OpenWeatherMapSDKImpl) sdk).getScheduler().isShutdown());
    }

    // Positive scenario
    @Test
    void getCurrentWeather_ShouldReturnCompleteWeatherData() throws SDKException {
        OpenWeatherMapSDKImpl sdk = new OpenWeatherMapSDKImpl(CITY, Mode.ON_DEMAND, createMockWeatherService(), new WeatherCache());
        WeatherData weatherData = sdk.getCurrentWeather(CITY);

        assertNotNull(weatherData, "Weather data should not be null");
        assertNotNull(weatherData.getWeather(), "Weather info should not be null");
        assertEquals(WEATHER_MAIN, weatherData.getWeather().getMain());
        assertEquals(DESCRIPTION, weatherData.getWeather().getDescription());
        assertNotNull(weatherData.getTemperature(), "Temperature info should not be null");
        assertEquals(TEMPERATURE, weatherData.getTemperature().getTemp(), 0.01);
        assertEquals(FEELS_LIKE, weatherData.getTemperature().getFeelsLike(), 0.01);
        assertEquals(VISIBILITY, weatherData.getVisibility());
        assertNotNull(weatherData.getWind(), "Wind info should not be null");
        assertEquals(WIND_SPEED, weatherData.getWind().getSpeed(), 0.01);
        assertEquals(DATE_TIME, weatherData.getDatetime());
        assertNotNull(weatherData.getSys(), "System info should not be null");
        assertEquals(SUNRISE, weatherData.getSys().getSunrise());
        assertEquals(SUNSET, weatherData.getSys().getSunset());
        assertEquals(TIMEZONE, weatherData.getTimezone());
        assertEquals(CITY, weatherData.getName());
    }

    private WeatherService createMockWeatherService() {
        return new WeatherService(null) {
            @Override
            public WeatherData fetchWeather(String city) {
                WeatherData data = new WeatherData();

                data.setWeatherList(new java.util.ArrayList<>());
                WeatherData.WeatherInfo weatherInfo = new WeatherData.WeatherInfo();
                weatherInfo.setDescription(DESCRIPTION);
                weatherInfo.setMain(WEATHER_MAIN);
                data.getWeatherList().add(weatherInfo);

                WeatherData.TemperatureInfo tempInfo = new WeatherData.TemperatureInfo();
                tempInfo.setTemp(TEMPERATURE);
                tempInfo.setFeelsLike(FEELS_LIKE);

                data.setTemperature(tempInfo);
                data.setVisibility(VISIBILITY);

                WeatherData.WindInfo windInfo = new WeatherData.WindInfo();
                windInfo.setSpeed(WIND_SPEED);
                data.setWind(windInfo);
                data.setDatetime(DATE_TIME);

                WeatherData.SysInfo sysInfo = new WeatherData.SysInfo();
                sysInfo.setSunrise(SUNRISE);
                sysInfo.setSunset(SUNSET);
                data.setSys(sysInfo);
                data.setTimezone(TIMEZONE);
                data.setName(CITY);

                return data;
            }
        };
    }
}
