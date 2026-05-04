package com.redhat.parks.weather;

import java.util.Map;

/** WMO weather interpretation codes used by Open-Meteo. */
public final class WeatherCode {
    private static final Map<Integer, String> CODES = Map.ofEntries(
        Map.entry(0,  "Clear sky"),
        Map.entry(1,  "Mainly clear"),
        Map.entry(2,  "Partly cloudy"),
        Map.entry(3,  "Overcast"),
        Map.entry(45, "Fog"),
        Map.entry(48, "Rime fog"),
        Map.entry(51, "Light drizzle"),
        Map.entry(53, "Drizzle"),
        Map.entry(55, "Heavy drizzle"),
        Map.entry(61, "Light rain"),
        Map.entry(63, "Rain"),
        Map.entry(65, "Heavy rain"),
        Map.entry(71, "Light snow"),
        Map.entry(73, "Snow"),
        Map.entry(75, "Heavy snow"),
        Map.entry(80, "Light rain showers"),
        Map.entry(81, "Rain showers"),
        Map.entry(82, "Heavy rain showers"),
        Map.entry(85, "Light snow showers"),
        Map.entry(86, "Heavy snow showers"),
        Map.entry(95, "Thunderstorm"),
        Map.entry(96, "Thunderstorm + hail"),
        Map.entry(99, "Thunderstorm + heavy hail")
    );

    private WeatherCode() {}

    public static String describe(Integer code) {
        if (code == null) return "Unknown";
        return CODES.getOrDefault(code, "Unknown (code " + code + ")");
    }
}
