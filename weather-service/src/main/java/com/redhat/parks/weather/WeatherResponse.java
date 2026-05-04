package com.redhat.parks.weather;

/** What we return to the caller. Public fields → simple JSON shape. */
public class WeatherResponse {
    public String park;
    public Double lat;
    public Double lon;
    public Double temperatureC;
    public Double windKph;
    public Integer weatherCode;
    public String  conditions;
    public String  fetchedAt;
    public Integer ttl;
    public String  instance;
    public String  revisionLabel;
}
