package org.example.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherData {

    @SerializedName("weather")
    private List<WeatherInfo> weatherList;

    public List<WeatherInfo> getWeatherList() {
        return weatherList;
    }

    @SerializedName("main")
    private TemperatureInfo temperature;

    private int visibility;
    private WindInfo wind;

    @SerializedName("dt")
    private long datetime;

    private SysInfo sys;
    private int timezone;
    private String name;

    public static class WeatherInfo {
        private String main;
        private String description;

        public void setMain(String main) {
            this.main = main;
        }

        public String getMain() {
            return main;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class TemperatureInfo {
        private double temp;

        @SerializedName("feels_like")
        private double feelsLike;

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getTemp() {
            return temp;
        }

        public void setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
        }

        public double getFeelsLike() {
            return feelsLike;
        }
    }

    public static class WindInfo {
        private double speed;

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return speed;
        }
    }

    public static class SysInfo {
        private long sunrise;
        private long sunset;

        public void setSunrise(long sunrise) {
            this.sunrise = sunrise;
        }

        public long getSunrise() {
            return sunrise;
        }

        public void setSunset(long sunset) {
            this.sunset = sunset;
        }

        public long getSunset() {
            return sunset;
        }
    }

    public WeatherInfo getWeather() {
        return weatherList != null && !weatherList.isEmpty()
                ? weatherList.getFirst()
                : null;
    }

    public TemperatureInfo getTemperature() {
        return temperature;
    }

    public int getVisibility() {
        return visibility;
    }

    public WindInfo getWind() {
        return wind;
    }

    public long getDatetime() {
        return datetime;
    }

    public SysInfo getSys() {
        return sys;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public void setSys(SysInfo sys) {
        this.sys = sys;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public void setWind(WindInfo wind) {
        this.wind = wind;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public void setTemperature(TemperatureInfo temperature) {
        this.temperature = temperature;
    }

    public void setWeatherList(List<WeatherInfo> weatherList) {
        this.weatherList = weatherList;
    }
}
