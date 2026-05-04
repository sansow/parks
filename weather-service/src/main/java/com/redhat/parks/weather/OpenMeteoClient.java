package com.redhat.parks.weather;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the public Open-Meteo API.
 * Free, no auth, ~50ms response time. Configured via
 * application.properties: quarkus.rest-client.OpenMeteoClient.uri.
 */
@RegisterRestClient(configKey = "OpenMeteoClient")
@Path("/v1/forecast")
@Produces(MediaType.APPLICATION_JSON)
public interface OpenMeteoClient {

    @GET
    OpenMeteoResponse forecast(@QueryParam("latitude")        double latitude,
                               @QueryParam("longitude")       double longitude,
                               @QueryParam("current_weather") boolean current);
}
