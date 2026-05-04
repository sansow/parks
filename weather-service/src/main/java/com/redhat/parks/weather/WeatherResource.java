package com.redhat.parks.weather;

import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;

/**
 * /weather — National Parks weather lookup.
 *
 * Two ways to call:
 *   /weather?park=yellowstone           (resolves coords from ParkLookup)
 *   /weather?lat=44.428&lon=-110.589    (raw coordinates)
 *
 * Responses are cached for 5 minutes per park (CacheResult).
 * Includes the pod name and revision label so the Knative autoscaling
 * demo shows which replica handled each request.
 */
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {

    @Inject
    @RestClient
    OpenMeteoClient meteo;

    @ConfigProperty(name = "POD_NAME", defaultValue = "unknown")
    String podName;

    @ConfigProperty(name = "REVISION_LABEL", defaultValue = "v0")
    String revisionLabel;

    @GET
    public Response get(@QueryParam("park") String park,
                        @QueryParam("lat")  Double lat,
                        @QueryParam("lon")  Double lon) {
        if (park != null && !park.isBlank()) {
            ParkLookup.Coords c = ParkLookup.find(park);
            if (c == null) {
                return Response.status(404)
                        .entity("{\"error\":\"unknown park '" + park + "'\"," +
                                "\"hint\":\"try yellowstone, yosemite, grand-canyon, zion, glacier, acadia, olympic, rocky-mountain, great-smoky, joshua-tree, denali, everglades\"}")
                        .build();
            }
            return Response.ok(fetch(park, c.lat(), c.lon())).build();
        }
        if (lat != null && lon != null) {
            return Response.ok(fetch(String.format("%.3f,%.3f", lat, lon), lat, lon)).build();
        }
        return Response.status(400)
                .entity("{\"error\":\"specify either ?park=<id> or ?lat=<n>&lon=<n>\"}")
                .build();
    }

    @CacheResult(cacheName = "weather-5min")
    public WeatherResponse fetch(String parkKey, double lat, double lon) {
        OpenMeteoResponse r = meteo.forecast(lat, lon, true);
        WeatherResponse out = new WeatherResponse();
        out.park           = parkKey;
        out.lat            = lat;
        out.lon            = lon;
        out.temperatureC   = r.current_weather != null ? r.current_weather.temperature : null;
        out.windKph        = r.current_weather != null ? r.current_weather.windspeed   : null;
        out.weatherCode    = r.current_weather != null ? r.current_weather.weathercode : null;
        out.conditions     = WeatherCode.describe(out.weatherCode);
        out.fetchedAt      = Instant.now().toString();
        out.ttl            = 300;
        out.instance       = podName;
        out.revisionLabel  = revisionLabel;
        return out;
    }
}
