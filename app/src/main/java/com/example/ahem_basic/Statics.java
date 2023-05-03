package com.example.ahem_basic;

import com.google.android.gms.maps.model.LatLng;

public class Statics {
    public static final int sensitivityThreshold_Red = 150;
    public static final int sensitivityThreshold_Yellow = 50;
    public static final int initial_zoom = 15;

    public static boolean doLocation = false;
    public static float zoom;
    public static final double northLat = 33.0000;
    public static final double westLon = -96.7600;
    public static final double southLat = 32.9770;
    public static final double eastLon = -96.7430;
    public static final double centerLat = southLat+(northLat-southLat)/2;
    public static final double centerLon = westLon+(eastLon-westLon)/2;
    public static final int refreshRate_location = 8;
    public static final int refreshRate_polygons = 5;
    public static final int refreshRate_route = 10;
    public static final int refreshRate_dont_warn_Reset = 1;
    public static boolean GMapRouteClicked = false;
    public static boolean AHEMRouteClicked = false;
    public static boolean routing = true;
    public static boolean initial_routing = true;
    public static boolean updated_polygons = false;
    public static boolean afterInitialRoute = false;
    public static String selectedRoute = "";
    public static boolean warning_displayed = false;
    public static boolean polygons_refreshed = false;
    public static boolean in_danger_zone = false;
    public static LatLng userLocationLL = new LatLng(centerLat, centerLon);
    public static int current_zoom;

}
