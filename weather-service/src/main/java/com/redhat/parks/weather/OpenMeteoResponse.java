package com.redhat.parks.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Subset of fields returned by Open-Meteo we actually use. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {
    public Double latitude;
    public Double longitude;
    public CurrentWeather current_weather;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        public Double temperature;
        public Double windspeed;
        public Integer winddirection;
        public Integer weathercode;
        public String time;
    }
}
