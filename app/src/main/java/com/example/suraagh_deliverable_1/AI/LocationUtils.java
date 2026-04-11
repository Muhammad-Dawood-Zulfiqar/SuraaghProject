package com.example.suraagh_deliverable_1.AI;

/**
 * Utility class to calculate distances between geospatial coordinates.
 * Uses the Haversine formula to account for the curvature of the Earth.
 */
public class LocationUtils {

    // Radius of the earth in Kilometers
    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the distance between two points in Kilometers.
     *
     * @param lat1 Latitude of point A
     * @param lon1 Longitude of point A
     * @param lat2 Latitude of point B
     * @param lon2 Longitude of point B
     * @return Distance in Kilometers
     */
    public static double getDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        // If the points are exactly the same, return 0 immediately
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // The Haversine Formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                        Math.cos(lat1Rad) * Math.cos(lat2Rad);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Result in Kilometers
        return EARTH_RADIUS_KM * c;
    }
}