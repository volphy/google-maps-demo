package com.krzysztofwilk.google.maps.demo;

import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Elevation {

    private static final String GOOGLE_MAPS_ELEVATION_API_KEY;

    private static final String LONGITUDE_PROPERTY = "long";
    private static final String LATITUDE_PROPERTY = "lat";
    private static final String ADDRESS_PROPERTY = "address";

    static {
        GOOGLE_MAPS_ELEVATION_API_KEY = System.getProperty("apiKey", "missing");
    }

    private Elevation() {}

    public static void main(String[] args) throws Exception {
        GeoApiContext context = geoApiContext();

        LatLng location;
        if (!System.getProperty(LONGITUDE_PROPERTY, "").isEmpty()
                && !System.getProperty(LATITUDE_PROPERTY, "").isEmpty()) {

            location = location(System.getProperty(LATITUDE_PROPERTY),System.getProperty(LONGITUDE_PROPERTY) );
        } else if (!System.getProperty(ADDRESS_PROPERTY, "").isEmpty()) {
            location = location(context, System.getProperty(ADDRESS_PROPERTY));
        } else {
            throw new IllegalStateException("Either longitude&latitude or address needs to be provided!");
        }

        double elevation = elevation(context, location);

        log.info("GPS coordinates={} {}", convertLongitudeNumberToString(location.lng),
                convertLatitudeNumberToString(location.lat));
        log.info("Elevation = {} [m]", elevation);
    }

    private static LatLng location(String latitude, String longitude) {
        double latitudeValue = convertLatitudeStringToNumber(latitude);
        double longitudeValue = convertLongitudeStringToNumber(longitude);

        LatLng location;
        location = new LatLng(latitudeValue, longitudeValue);
        return location;
    }

    private static double convertLatitudeStringToNumber(String latitude) {

        double value;
        if (latitude.startsWith("N")) {
            value = Double.parseDouble(latitude.replace("N", ""));
        } else if (latitude.startsWith("S")) {
            value = Double.parseDouble(latitude.replace("S", "")) * -1;
        } else {
            value = Double.parseDouble(latitude);
        }

        return value;
    }

    private static double convertLongitudeStringToNumber(String longitude) {

        double value;
        if (longitude.startsWith("E")) {
            value = Double.parseDouble(longitude.replace("E", ""));
        } else if (longitude.startsWith("W")) {
            value = Double.parseDouble(longitude.replace("W", "")) * -1;
        } else {
            value = Double.parseDouble(longitude);
        }

        return value;
    }

    private static String convertLongitudeNumberToString(double longitude) {
        return (longitude > 0) ? "E" + Double.toString(longitude)
                : "W" + Double.toString(longitude * -1);
    }

    private static String convertLatitudeNumberToString(double latitude) {
        return (latitude > 0) ? "N" + Double.toString(latitude)
                : "S" + Double.toString(latitude * -1);
    }

    private static double elevation(GeoApiContext context, LatLng location) throws Exception {
        PendingResult<ElevationResult> point = ElevationApi.getByPoint(context, location);

        return point.await().elevation;
    }

    private static LatLng location(GeoApiContext context, String address) throws Exception {
        GeocodingResult[] results =  GeocodingApi.geocode(context, address).await();

        GeocodingResult firstResult = results[0];

        return firstResult.geometry.location;
    }

    private static GeoApiContext geoApiContext() {
        return new GeoApiContext().setApiKey(GOOGLE_MAPS_ELEVATION_API_KEY);
    }

}
