package com.example.ahem_basic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.ahem_basic.databinding.ActivityMapsBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final boolean do_location = false;
    private final boolean no_connection = false;
    private static final boolean do_bitmap = false;
    private Marker marker;
    private String Route_json_str;
    private String GMap_route_json_str;
    private String Polygon_json_str;
    private String user_polygon_json_str;
    User user;
    private Polyline AHEM_polyline;
    private Polyline GM_polyline;


    private GoogleMap mMap;
    private Handler mHandler = new Handler();
    private Handler mHandler2;
    private Runnable mRunnable;
    private Runnable mRunnable2;
    private Runnable mRunnable3;
    private Runnable mRunnable4;
    private Polyline routePolyline;
    private ActivityMapsBinding binding;

    private List<Polygon> polygonList = new ArrayList<Polygon>();
    private List<LatLng> latLngList = new ArrayList<LatLng>();
    private List<LatLng> currentRouteLatLngList = new ArrayList<LatLng>();
    private List<LatLng> googleRouteLatLngList = new ArrayList<LatLng>();
    private List<Polygon> currentPolygonList = new ArrayList<>();
    private Button endButton;
    private Button warningButton;

    private Logger Log = Logger.getAnonymousLogger();

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getIntent().getSerializableExtra("User");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_maps);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        doLocationTiming();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        endButton = findViewById(R.id.endRoutingButton);
        endButton.setEnabled(false);
        endButton.setVisibility(View.INVISIBLE);
        warningButton = findViewById(R.id.warningButton);
        warningButton.setEnabled(false);
        warningButton.setVisibility(View.INVISIBLE);


//        getPolygons();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        doTiming();
        if (Statics.routing) {
            endButton.setEnabled(true);
            endButton.setVisibility(View.VISIBLE);
            try {
                doRouting();
                doRefreshPolygons();
                doUserDanger();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else{ //roaming
            // set End Routing button invisible
            endButton.setEnabled(false);
            endButton.setVisibility(View.INVISIBLE);
            try {
                doRoaming();
                doRefreshPolygons();
                doUserDanger();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        warningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warningButton.setEnabled(false);
                warningButton.setVisibility(View.INVISIBLE);
                Statics.warning_displayed = false;
            }
        });

//        doTiming();
//        doRefreshPolygons();
//        doUserDanger();

    }
    private void doRoaming() throws JsonProcessingException {


        if (Statics.doLocation) {
            doLocation();
//            showLocation();
//            doTiming();
            mHandler.postDelayed(mRunnable, Statics.refreshRate_location * 1000);
        } else {

            // Add a marker in UTD and move the camera
//           previous 32.993017,
//           current  32.978890, -96.739511
            LatLng UTD = new LatLng(Statics.centerLat, Statics.centerLon);
//            mMap.addMarker(new MarkerOptions().position(UTD).title("UTD"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UTD, Statics.initial_zoom));
        }
        if (no_connection) {

        } else {
            doPolygons();
        }

        doPolyLineClicked();
        // Enable zoom controls
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        // Enable the location layer on the map
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void doRouting() throws JsonProcessingException {
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GM_polyline.remove();
                AHEM_polyline.remove();
                endButton.setEnabled(false);
                endButton.setVisibility(View.INVISIBLE);
                Statics.afterInitialRoute = false;
                Statics.selectedRoute = "";
                marker.remove();
            }
        });
        if (Statics.doLocation) {
            doLocation();
//            doTiming();
            mHandler.postDelayed(mRunnable, Statics.refreshRate_location * 1000);
        } else {

            // Add a marker in UTD and move the camera
//           previous 32.993017,
//           current  32.978890, -96.739511
            LatLng UTD = new LatLng(Statics.centerLat, Statics.centerLon);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UTD, Statics.initial_zoom));
        }

        if (no_connection) {

        } else {
            doPolygons();
        }

        if (no_connection) {

        } else {
            if (Statics.afterInitialRoute){
                doSubsequentRouting();
                mHandler.postDelayed(mRunnable3, Statics.refreshRate_route * 1000);
            } else {
                doInitialRouting();
                showLocation();
            }
        }
        // Enable zoom controls
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
//        uiSettings.setEndNavigationButtonVisible(true);
        // Enable the location layer on the map
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        Statics.afterInitialRoute = true;
    }

    private void doRunnables1_2_4(){
        mHandler.postDelayed(mRunnable, Statics.refreshRate_location * 1000);
        mHandler.postDelayed(mRunnable2, Statics.refreshRate_polygons * 1000);
        mHandler.postDelayed(mRunnable4, Statics.refreshRate_dont_warn_Reset * 1000);
    }
    private void doRunnable3(){
        // for subsequent routing
        mHandler.postDelayed(mRunnable3, Statics.refreshRate_route * 1000);
    }

    private void doTiming(){
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // code to run periodically
                try {
                    doLocation();
                    try {
                        doDanger();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("did Location");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                mHandler.postDelayed(this, Statics.refreshRate_location * 1000);
            }
        };
        mRunnable2 = new Runnable() {
            @Override
            public void run() {
                // code to run periodically
                refreshPolygons();
                Statics.updated_polygons = true;
                System.out.println("did run refresh Polygons");
                mHandler.postDelayed(this, Statics.refreshRate_polygons * 1000);
            }
        };
        mRunnable3 = new Runnable() {
            @Override
            public void run() {
                // code to run periodically
                if (Statics.routing){
                // get route/refresh route with new location;
                    System.out.println("Doing Subsequent routing");
                    doSubsequentRouting();
                }
                mHandler.postDelayed(this, Statics.refreshRate_route * 1000);
            }
        };
        mRunnable4 = new Runnable() {
            @Override
            public void run() {
                // code to run periodically
                Statics.updated_polygons = false;
                mHandler.postDelayed(this, Statics.refreshRate_dont_warn_Reset * 1000);
            }
        };
    }
    private void doSubsequentRouting(){
        LatLng origin;
        if (Statics.doLocation){;
            origin = Statics.userLocationLL;
//            origin = new LatLng(user.getLocationLatLng().latitude, user.getLocationLatLng().longitude);
        } else {
            origin = new LatLng(32.995889, -96.755083);
        }
        LatLng destination = new LatLng(user.getLatitude_double(), user.getLongitude_double());
        mMap.addMarker(new MarkerOptions().position(destination));
//            LatLng destination = new LatLng(lat,lon);
        getRoute(origin, destination);
        makeLLlist();
        currentRouteLatLngList = getLLlist();
        if (Statics.selectedRoute.equalsIgnoreCase("")){
            AHEM_polyline = drawRoute(currentRouteLatLngList, Color.BLACK);
            AHEM_polyline.setTag("AHEM");
            drawGMroute(origin, destination);
            doPolyLineClicked();
        } else if (Statics.selectedRoute.equalsIgnoreCase("AHEM")){
            AHEM_polyline = drawRoute(currentRouteLatLngList, Color.BLACK);
            AHEM_polyline.setTag("AHEM");
        } else if (Statics.selectedRoute.equalsIgnoreCase("GMAP")) {
            drawGMroute(origin, destination);
        }
    }
    private void doInitialRouting(){
        LatLng origin;
        if (Statics.doLocation){;
//            origin = new LatLng(user.getLocationLatLng().latitude, user.getLocationLatLng().longitude);
            origin = Statics.userLocationLL;// LatLng(user.getLocationLatLng().latitude, user.getLocationLatLng().longitude);
        } else {
            origin = new LatLng(32.995889, -96.755083);
        }
        LatLng destination = new LatLng(user.getLatitude_double(), user.getLongitude_double());
        marker = mMap.addMarker(new MarkerOptions().position(destination));
//            LatLng destination = new LatLng(lat,lon);
        getRoute(origin, destination);
        makeLLlist();
        currentRouteLatLngList = getLLlist();
        AHEM_polyline = drawRoute(currentRouteLatLngList, Color.BLACK);
        AHEM_polyline.setTag("AHEM");
        drawGMroute(origin, destination);
        doPolyLineClicked();
    }
    private void refreshPolygons(){
        doPolygons();
    }
    private void doRefreshPolygons(){
//        refreshPolygons();
//        doTiming();
        mHandler.postDelayed(mRunnable2, Statics.refreshRate_polygons * 1000);
    }
    private void doLocation() throws JsonProcessingException {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable the location layer if the permission has been granted
            mMap.setMyLocationEnabled(true);
            float zoom = mMap.getCameraPosition().zoom;

            // Get the last known location of the device and move the camera
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                user.setLocationLatLng(latLng);
                                Statics.current_zoom = (int) mMap.getCameraPosition().zoom;
                                Statics.userLocationLL = latLng;
//                                try {
//                                    doDanger();
//                                } catch (JsonProcessingException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                                System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSTHISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
                            }
                        }
                    });

        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void showLocation(){
        float zoom = Statics.initial_zoom;
//        LatLng latLng =user.getLocationLatLng();
        LatLng latLng =Statics.userLocationLL;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
    private void doUserDanger(){
        mHandler.postDelayed(mRunnable, Statics.refreshRate_polygons * 1000);
    }
    private void doDanger() throws JsonProcessingException {
//        LatLng NW = user.getLocationLatLng();
        LatLng NW = Statics.userLocationLL;
//        LatLng SE = new LatLng(user.getLocationLatLng().latitude - 0.001, user.getLocationLatLng().longitude +0.001);
        LatLng SE = new LatLng(Statics.userLocationLL.latitude - 0.001, Statics.userLocationLL.longitude +0.001);
        if (!no_connection) {
            System.out.println("NW = "+NW+"\nSE = "+SE+ "Statics.userLocationLL" + Statics.userLocationLL);
            getUserPolygon(NW.latitude, SE.latitude, NW.longitude, SE.longitude);


            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(user_polygon_json_str);
            // Get the "code" field
            int code = rootNode.get("code").asInt();
// Get the "message" field
            String message = rootNode.get("message").asText();
// Get the "polygons" field as an array
            JsonNode polygonsNode = rootNode.get("data").get("polygons");
            System.out.println(user_polygon_json_str);
            List<PolygonDetails> userPolygonDetails = makeUserDetails(polygonsNode);
            List<Measurements> userLocationMeasurements = userPolygonDetails.get(0).getMeasurements();
            boolean is_dangerous_to_user = isDangerousToUser(userLocationMeasurements);
            if (is_dangerous_to_user) {
                System.out.println("IN DANGER ZONE");
                Statics.in_danger_zone = true;
                if (!Statics.warning_displayed && Statics.updated_polygons){
                    warningButton.setVisibility(View.VISIBLE);
                    warningButton.setEnabled(false);
                    Statics.warning_displayed = true;
                }
            } else {
                Statics.in_danger_zone = false;
                System.out.println("NOT IN DANGER ZONE");
            }
        }
//        currentPolygonList = convertPolygonDetailsToPolygonList(polygonDetails);
    }
    private boolean isDangerousToUser(List<Measurements> userLocationMeasurements){
        for (Measurements measurement: userLocationMeasurements){
////            System.out.println("Pollutant selected is: "+measurement.getPollutantId());
////            System.out.println("Pollutant value is: "+ measurement.getValue());
            int pollutant_value = measurement.getValue();
            if (user.isSensitiveTo(measurement.getPollutantId())){
                pollutant_value += 100;
                if (pollutant_value > Statics.sensitivityThreshold_Red){
//                    System.out.println("Measurement = "+measurement.getPollutantId()+"\t and value = "+measurement.getValue());
                    return true; //red = alpha
                }
//
            }
            if (pollutant_value >Statics.sensitivityThreshold_Red){
                return true; // red = alpha
            }
//            else if (pollutant_value > Statics.sensitivityThreshold_Yellow){
//                // return value for yellow;
//            }
        }
        return false;
    }

    private void doPolygons(){
        clearPolygons();
        LatLng NW = new LatLng(33.0000, -96.7600);
        LatLng SE = new LatLng(32.9770, -96.7430);
        if (!no_connection) {
            getPolygons(NW.latitude, SE.latitude, NW.longitude, SE.longitude);
//            //
            try {
                makePlist();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            currentPolygonList = getPlist();
        }
    }
    private void clearPolygons(){
        if (!currentPolygonList.isEmpty()){
            for (int i = 0; i<currentPolygonList.size(); i++){
                currentPolygonList.remove(i);
            }
            for (Polygon polygon: currentPolygonList){
                polygon.remove();
            }
        }
    }
    private void doPolyLineClicked(){
        // Set a click listener on the polyline
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Handle the polyline click event here
                String polylineTag = (String) polyline.getTag();

                // Check which polyline was clicked
                if (polylineTag.equals("AHEM")) {
                    // Handle click on polyline 1
                    removePolyLine(GM_polyline);
                    Statics.selectedRoute = "AHEM";
                } else if (polylineTag.equals("GMAP")) {
                    // Handle click on polyline 2
                    removePolyLine(AHEM_polyline);
                    Statics.selectedRoute = "GMAP";
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);

                // Get the last known location of the device and move the camera
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(),
                                            location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                                }
                            }
                        });
            } else {
                // Permission has been denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private  Polygon convert2polygon(double lat1, double lat2, double lon1, double lon2, String tag){
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .clickable(false)
                .add(
                        new LatLng(lat1, lon1),
                        new LatLng(lat2, lon1),
                        new LatLng(lat2, lon2),
                        new LatLng(lat1, lon2)));
//        if(!tag.equalsIgnoreCase("alpha") && !tag.equalsIgnoreCase("beta")){
//            polygon.setTag("alpha");
//        } else {
//            polygon.setTag(tag);
//        }
        polygon.setTag(tag);
        stylePolygon(polygon);

        return polygon;
    }

    private void getDBinfo(){

    }
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_DARK_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_LIGHT_GREEN_ARGB = 0x3f81C784;
    private static final int COLOR_LIGHT_YELLOW_ARGB = 0x3fFFFF00;
    private static final int COLOR_DARK_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_LIGHT_ORANGE_ARGB = 0x4fF9A825;
    private static final int COLOR_RED_ARGB = 0x2fFF0000;
    private static final int COLOR_Green_ARGB = 0xff00E600;

    private static final int POLYGON_STROKE_WIDTH_PX = 0;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    private static final List<PatternItem> PATTERN_POLYGON_GAMMA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }
        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "beta": //green
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_DARK_GREEN_ARGB;
//                fillColor = COLOR_LIGHT_GREEN_ARGB;
                fillColor = COLOR_LIGHT_YELLOW_ARGB;
                break;
            case "alpha": // red
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_DARK_ORANGE_ARGB;
//                fillColor = COLOR_RED_ARGB;
                fillColor = COLOR_LIGHT_YELLOW_ARGB;
                break;
            case "gamma": //yellow
                pattern = PATTERN_POLYGON_GAMMA;
                strokeColor = COLOR_LIGHT_YELLOW_ARGB;
                fillColor = COLOR_LIGHT_YELLOW_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);

    }
    public void populateLatLonList(){
        int counter = 0;
        List<Double> latList = new ArrayList<Double>();
        List<Double> lonList = new ArrayList<Double>();
        double num_rows = 10;
        double lat1 = 33.020904, lat2 = 32.949863, lon1 = -96.786801, lon2 = -96.699440;//north,south,west,east
        double delta_lat = (lat1-lat2)/num_rows, delta_lon = (lon2-lon1)/num_rows;
        for (int i = 0; i<=num_rows; i++){
            latList.add(lat1 - delta_lat * i);
            lonList.add(lon1 + delta_lon * i);
//            System.out.println("latList.get("+i+") = "+latList.get(i));
//            System.out.println("lonList.get("+i+") = "+lonList.get(i));
        }
        for(int i =1; i<=num_rows; i++){
            for (int j = 1; j<=num_rows; j++){
                double lat11 = latList.get(i-1);
                double lat12 = latList.get(i);
                double lon11 = lonList.get(j-1);
                double lon12 = lonList.get(j);
                latLngList.add(new LatLng(lat11, lon11));
                latLngList.add(new LatLng(lat12, lon12));
                latLngList.add(new LatLng(lat12, lon11));
                latLngList.add(new LatLng(lat11, lon12));
                polygonList.add(convert2polygon(lat11, lat12, lon11, lon12, tag_generator()));
                counter++;
            }
        }
        System.out.println("**************************** "+counter+" ****************************");
    }
    private String tag_generator(){
        Random random = new Random();
        int randomNumber = random.nextInt(2); // Generates a random number between 0 and 1
        if(randomNumber == 0){
            return "alpha";
        } else {
            return "beta";
        }
    }
    private void populateGroundOverlay(){
        int counter = 0;
        List<Double> latList = new ArrayList<Double>();
        List<Double> lonList = new ArrayList<Double>();
        double num_rows = 2;
        double lat1 = 33.020904, lat2 = 32.949863, lon1 = -96.786801, lon2 = -96.699440;
        double delta_lat = (lat1-lat2)/num_rows, delta_lon = (lon2-lon1)/num_rows;
        for (int i = 0; i<=num_rows; i++){
            latList.add(lat1 - delta_lat * i);
            lonList.add(lon1 + delta_lon * i);
//            System.out.println("latList.get("+i+") = "+latList.get(i));
//            System.out.println("lonList.get("+i+") = "+lonList.get(i));
        }

        LatLngBounds bounds = new LatLngBounds(
                new LatLng(32.949863, -96.786801), // southwest corner
                new LatLng(33.020904, -96.699440)  // northeast corner
        );
        Bitmap bitmap = Bitmap.createBitmap(5, 5, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.RED);
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

        // Define the image to be used as the overlay
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        // Add the ground overlay to the map
        for(int i =1; i<=num_rows; i++){
            for (int j = 1; j<=num_rows; j++){
                double north = latList.get(i-1);
                double south = latList.get(i);
                double west = lonList.get(j-1);
                double east = lonList.get(j);
                LatLngBounds bound = new LatLngBounds(
                        new LatLng(south, west), // southwest corner
                        new LatLng(north, east)  // northeast corner
                );
                canvas.drawColor(colorGenerator());
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                        .image(bitmapDescriptor)
                        .positionFromBounds(bound)
                        .transparency(0.9f);
                GroundOverlay groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
                counter++;
            }
        }
        LatLng center = bounds.getCenter();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }
    private int colorGenerator(){
        Random random = new Random();
        int randomNumber = random.nextInt(2); // Generates a random number between 0 and 1
        if(randomNumber == 0){
            return Color.RED;
        } else {
            return Color.GREEN;
        }
    }

    private void getPolygons(double nLat, double sLat, double wLon, double eLon){
        String  northLat = String.valueOf(nLat), southLat = String.valueOf(sLat);
        String westlon = String.valueOf(wLon), eastlon = String.valueOf(eLon);
        String ip_address = getString(R.string.ip_address);
        String request_type = getString(R.string.request_type);

        String url = ip_address + request_type +
                northLat+","+southLat+","+westlon+","+eastlon+"&decimalPlaces=3";
        System.out.println(url);
        MyAsyncTask task = new MyAsyncTask(url);
        task.setMode("polygon");
        task.execute();
        String result = null;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Polygon_json_str = task.givePolygon();
    }
    private void getUserPolygon(double nLat, double sLat, double wLon, double eLon){
        String  northLat = String.valueOf(nLat), southLat = String.valueOf(sLat);
        String westlon = String.valueOf(wLon), eastlon = String.valueOf(eLon);
        String ip_address = getString(R.string.ip_address);
        String request_type = getString(R.string.request_type);

        String url = ip_address + request_type +
                northLat+","+southLat+","+westlon+","+eastlon+"&decimalPlaces=3";
        System.out.println(url);
        MyAsyncTask task = new MyAsyncTask(url);
        task.setMode("user_polygon");
        task.execute();
        String result = null;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        user_polygon_json_str = task.giveUserPolygon();
    }
    private void getRoute(LatLng origin, LatLng destination){
        String  start_lon = String.valueOf(origin.longitude), start_lat = String.valueOf(origin.latitude);
        String end_lon = String.valueOf(destination.longitude), end_lat = String.valueOf(destination.latitude);
        //method to get sensitivities
        String sensitivities= "1,2,3";
        sensitivities =  user.getSensitivities();
        String ip_address = getString(R.string.ip_address);
        String request_type = getString(R.string.request_type_route);
        String url = ip_address + request_type +start_lon+","+start_lat+","+end_lon+","+end_lat+"&"+
                "sensitivePollutants="+sensitivities;
//        url = "https://9506-66-253-176-157.ngrok-free.app/"+
//                "v1/route?coordinates=-96.7533417,32.9950066,-96.7504163,32.9862204&"+
//                "sensitivePollutants=1,2,3";
        MyAsyncTask task = new MyAsyncTask(url);
        task.setMode("routing");
        task.execute();
        String result = null;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Route_json_str = task.giveRoute();
    }
    private void drawGMroute(LatLng origin, LatLng destination){
        String originLatitude = String.valueOf(origin.latitude);
        String originLongitude = String.valueOf(origin.longitude);
        String destinationLatitude = String.valueOf(destination.latitude);
        String destinationLongitude = String.valueOf(destination.longitude);
        String apiKey = getString(R.string.api_key);;
        // Make an HTTP request to the Google Maps Directions API
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + originLatitude + "," + originLongitude +
                "&destination=" + destinationLatitude + "," + destinationLongitude +
                "&mode=walking" +
                "&key=" + apiKey;
        MyAsyncTask task = new MyAsyncTask(url);
        task.setMode("gMaproute");
        task.execute();
        String result = null;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        googleRouteLatLngList = task.giveGMapRoute();
        GM_polyline = drawRoute(googleRouteLatLngList, Color.CYAN);
        GM_polyline.setTag("GMAP");
    }

    private Polyline drawRoute(List<LatLng> llList, int color){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(color);
        polylineOptions.clickable(true);
        for (int i = 0; i<llList.size(); i++){
            polylineOptions.add(llList.get(i));
        }
        Polyline polyline = mMap.addPolyline(polylineOptions);
        return polyline;
    }
    private List<Polygon> getPlist(){
        return currentPolygonList;
    }
    private List<LatLng> getLLlist(){
        return currentRouteLatLngList;
    }
    private List<PolygonDetails> makeDetails(JsonNode polygonsNode){
        List<PolygonDetails> details = new ArrayList<>();
//        PolygonDetails detail = new PolygonDetails();
        int i = 0;
        for (JsonNode polygonNode : polygonsNode) {
            PolygonDetails detail = new PolygonDetails();
            // Get the latitude and longitude fields
            double northernLatitude = polygonNode.get("northernLatitude").asDouble();
            double southernLatitude = polygonNode.get("southernLatitude").asDouble();
            double westernLongitude = polygonNode.get("westernLongitude").asDouble();
            double easternLongitude = polygonNode.get("easternLongitude").asDouble();
            detail.setNorthernLatitude(northernLatitude);
            detail.setSouthernLatitude(southernLatitude);
            detail.setWesternLongitude(westernLongitude);
            detail.setEasternLongitude(easternLongitude);

            // Get the measurements field as an array
            JsonNode measurementsNode = polygonNode.get("measurements");
            List<Measurements> measures = new ArrayList<>();

            int j = 0;
            // Loop through the measurements array
            for (JsonNode measurementNode : measurementsNode) {
                Measurements measure = new Measurements();
                // Get the pollutantId, value, and timestamp fields
                String pollutantId = measurementNode.get("pollutantId").asText();
                int value = measurementNode.get("value").asInt();
                String timestamp = measurementNode.get("timestamp").asText();
                measure.setPollutantId(pollutantId);
                measure.setTimestamp(timestamp);
                measure.setValue(value);
                measures.add(measure);
                // Do something with the data
//                System.out.println("Pollutant: " + pollutantId + ", Value: " + value + ", Timestamp: " + timestamp);
            }
            detail.setMeasurements(measures);
            details.add(detail);
        }
        return details;
    }
    private List<PolygonDetails> makeUserDetails(JsonNode polygonsNode){
        List<PolygonDetails> details = new ArrayList<>();
//        PolygonDetails detail = new PolygonDetails();
        int i = 0;
        for (JsonNode polygonNode : polygonsNode) {
            PolygonDetails detail = new PolygonDetails();
            // Get the latitude and longitude fields
            double northernLatitude = polygonNode.get("northernLatitude").asDouble();
            double southernLatitude = polygonNode.get("southernLatitude").asDouble();
            double westernLongitude = polygonNode.get("westernLongitude").asDouble();
            double easternLongitude = polygonNode.get("easternLongitude").asDouble();
            detail.setNorthernLatitude(northernLatitude);
            detail.setSouthernLatitude(southernLatitude);
            detail.setWesternLongitude(westernLongitude);
            detail.setEasternLongitude(easternLongitude);

            // Get the measurements field as an array
            JsonNode measurementsNode = polygonNode.get("measurements");
            List<Measurements> measures = new ArrayList<>();

            int j = 0;
            // Loop through the measurements array
            for (JsonNode measurementNode : measurementsNode) {
                Measurements measure = new Measurements();
                // Get the pollutantId, value, and timestamp fields
                String pollutantId = measurementNode.get("pollutantId").asText();
                int value = measurementNode.get("value").asInt();
                String timestamp = measurementNode.get("timestamp").asText();
                measure.setPollutantId(pollutantId);
                measure.setTimestamp(timestamp);
                measure.setValue(value);
                measures.add(measure);
                // Do something with the data
//                System.out.println("Pollutant: " + pollutantId + ", Value: " + value + ", Timestamp: " + timestamp);
            }
            detail.setMeasurements(measures);
            details.add(detail);
        }
        return details;
    }
    private void makePlist() throws JsonProcessingException {
        currentRouteLatLngList.clear();
//        Gson gson = new Gson();
//        PolygonJson json = gson.fromJson(Polygon_json_str, PolygonJson.class);
//
//        PolygonData pData= json.getData();
//        pData.isNull();
//        currentPolygonList = convertPolygonDataToPolygonList(pData);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(Polygon_json_str);
        // Get the "code" field
        int code = rootNode.get("code").asInt();
// Get the "message" field
        String message = rootNode.get("message").asText();
// Get the "polygons" field as an array
        JsonNode polygonsNode = rootNode.get("data").get("polygons");
        List<PolygonDetails> polygonDetails = makeDetails(polygonsNode);
        currentPolygonList = convertPolygonDetailsToPolygonList(polygonDetails);
//        pData.isNull();
//        currentPolygonList = convertPolygonDataToPolygonList(pData);
//        return currentRouteLatLngList;
    }
    private void makeLLlist(){
        currentRouteLatLngList.clear();
        Gson gson = new Gson();
        RouteJson json = gson.fromJson(Route_json_str, RouteJson.class);
        List<RoutingData> dataList= json.getData();
        currentRouteLatLngList = convertRoutingDataToLLList(dataList);
//        return currentRouteLatLngList;
    }
    private List<Polygon> convertPolygonDetailsToPolygonList(List<PolygonDetails> polygonDetails){
//        List<PolygonDetails> polygonDetails= new ArrayList<>();
//        polygonDetails = polygonData.getDetails();
        clearPolygons();
        int counter = 0;
        List<Polygon> tempList = new ArrayList<>();
        for (int i = 0; i < polygonDetails.size(); i++){
            double northlat = polygonDetails.get(i).getNorthernLatitude();
            double southlat = polygonDetails.get(i).getSouthernLatitude();
            double westlon = polygonDetails.get(i).getWesternLongitude();
            double eastlon = polygonDetails.get(i).getEasternLongitude();
//            System.out.println("northlat = "+ northlat);
//            System.out.println("southlat = "+ southlat);
//            System.out.println("westlon = "+ westlon);
//            System.out.println("eastlon = "+ eastlon);
//            System.out.println("counter = "+(++counter));
            String tag = doPollutants(polygonDetails.get(i).getMeasurements());

            tempList.add(convert2polygon(northlat, southlat, westlon, eastlon, tag));//north,south,west,east
        }
        return tempList;
    }
    private List<LatLng> convertRoutingDataToLLList(List<RoutingData> dataList){
        List<LatLng> tempList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++){
            double longitude = dataList.get(i).getLongitude();
            double latitude = dataList.get(i).getLatitude();
            LatLng temp = new LatLng(latitude, longitude);
            tempList.add(temp);
        }
        return tempList;
    }


    private void printList(List<LatLng> list){
        for (int i = 0; i < list.size(); i++){
            System.out.println(list.get(i).latitude);
            System.out.println(list.get(i).longitude + "\n");
        }
    }
    private String doPollutants(List<Measurements> measurements){
        for (Measurements measurement: measurements){
////            System.out.println("Pollutant selected is: "+measurement.getPollutantId());
////            System.out.println("Pollutant value is: "+ measurement.getValue());
            int pollutant_value = measurement.getValue();
            if (user.isSensitiveTo(measurement.getPollutantId())){
//
                pollutant_value += 100;
                if (pollutant_value > Statics.sensitivityThreshold_Red){
//                    System.out.println("Measurement = "+measurement.getPollutantId()+"\t and value = "+measurement.getValue());
                    return "alpha"; //red = alpha
                }
//
            }
            if (pollutant_value >Statics.sensitivityThreshold_Red){
                return "alpha"; // red = alpha
            } else if (pollutant_value > Statics.sensitivityThreshold_Yellow){
                // return value for yellow;
//                System.out.println("Should be Yellow!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return "gamma";
            }
        }
        return "beta"; //green
    }

    private void removePolyLine(Polyline polyline){
        polyline.remove();
//        for(PolylineOptions polylineOption : polylineOptions) {
//            polylineOption
//        }
    }

}


