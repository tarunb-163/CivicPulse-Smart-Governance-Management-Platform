package com.civicpulse.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void testDistanceMetersSameCoordinates() {
        double dist = GeoUtils.distanceMeters(14.4644, 75.9218, 14.4644, 75.9218);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void testDistanceMetersNearbyPoints() {
        // approx 100-300m in Davanagere
        double dist = GeoUtils.distanceMeters(14.4644, 75.9218, 14.4660, 75.9225);
        assertTrue(dist > 100 && dist < 300, "Distance should be ~200m, was: " + dist);
    }

    @Test
    void testDistanceMetersNullSafe() {
        double dist = GeoUtils.distanceMeters(null, 75.9218, 14.4644, 75.9218);
        assertEquals(Double.MAX_VALUE, dist);
    }
}
