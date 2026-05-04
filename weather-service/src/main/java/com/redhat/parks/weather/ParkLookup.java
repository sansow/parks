package com.redhat.parks.weather;

import java.util.Map;
import java.util.Locale;

/** Hard-coded park-to-coordinates map for the demo. */
public final class ParkLookup {

    public record Coords(double lat, double lon) {}

    private static final Map<String, Coords> PARKS = Map.ofEntries(
        Map.entry("yellowstone",        new Coords(44.428, -110.589)),
        Map.entry("yosemite",           new Coords(37.865, -119.538)),
        Map.entry("grand-canyon",       new Coords(36.106, -112.113)),
        Map.entry("zion",               new Coords(37.298, -113.026)),
        Map.entry("acadia",             new Coords(44.350,  -68.214)),
        Map.entry("glacier",            new Coords(48.696, -113.718)),
        Map.entry("olympic",            new Coords(47.802, -123.604)),
        Map.entry("great-smoky",        new Coords(35.611,  -83.489)),
        Map.entry("rocky-mountain",     new Coords(40.343, -105.688)),
        Map.entry("joshua-tree",        new Coords(33.881, -115.900)),
        Map.entry("denali",             new Coords(63.069, -151.005)),
        Map.entry("everglades",         new Coords(25.286,  -80.899))
    );

    private ParkLookup() {}

    public static Coords find(String park) {
        if (park == null) return null;
        return PARKS.get(park.trim().toLowerCase(Locale.ROOT));
    }
}
