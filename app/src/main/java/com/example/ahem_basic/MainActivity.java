package com.example.ahem_basic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.ahem_basic.databinding.ActivityMainBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private User user;
    private Button button;
    private boolean bypass_button = false;
    private SearchView searchView;
    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private AutocompleteSessionToken token;
    private int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_main);
        // Get the context of the current activity

        doSearch();
        doCheckBoxes();



        user = new User();
        user.setTest();



//        MyAsyncTask task = new MyAsyncTask("https://f807-129-110-241-55.ngrok.io/v1/route?coordinates=-96.7533417,32.9950066,-96.7504163,32.9862204&sensitivePollutants=1,2,3");
//        task.execute();

//        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        if (!bypass_button){
            button = (Button) findViewById(R.id.simplebutton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Statics.routing = false;
                    openMapsActivity(user);
                }
            });
        } else {
            openMapsActivity(user);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void openMapsActivity(User user){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("User", user);
        startActivity(intent);
    }
    private void doSearch(){
        // Initialize Places API client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        placesClient = Places.createClient(this);
        token = AutocompleteSessionToken.newInstance();

        // Set up the autocomplete adapter
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Create the fetch place request
                FetchPlaceRequest request = FetchPlaceRequest.builder(place.getId(), Arrays.asList(Place.Field.LAT_LNG)).build();
                // Execute the request asynchronously
                Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);
                placeTask.addOnSuccessListener((response) -> {
                    // Get the Place object from the response
                    Place place2 = response.getPlace();

                    // Get the LatLng from the Place object
                    LatLng latLng = place2.getLatLng();
                    user.setLatitude(String.valueOf(latLng.latitude));
                    user.setLongitude(String.valueOf(latLng.longitude));
                    System.out.println("LatLong = "+latLng+"\n"+getString(R.string.end_polygon_request));

                    Statics.routing = true;
                    openMapsActivity(user);

                    // Use the LatLng as needed
                    // ...
                }).addOnFailureListener((exception) -> {
                    // Handle the exception
                    // ...
                });
                // Handle the selected place
                System.out.println("Place: " + place.getName() + ", " + place.getId());// + "\tLL = "+ place.getLatLng().latitude+","+place.getLatLng().latitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error
                System.out.println("An error occurred: " + status);
            }
        });

//        searchView = findViewById(R.id.search_view_in_fragment_first).findViewById(R.id.search_views);
//        searchView = findViewById(R.id.search_view);
//        searchView.setIconifiedByDefault(false);
//
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // This method will be called when the user submits the search query.
//                // You can perform the search operation here.
//                if (user.isLLString()){
//                    user.setSearchStringLL(query);
//                    System.out.println(query);
//                } else {
//                    user.setSearchStringAdd(query);
//                }
////                searchView.setQuery("", false);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // This method will be called when the user types or deletes any character in the search bar.
//                // You can update the search results here as the user types.
//                autocompleteFragment.setText(newText);
//                System.out.println("*********New_Text = "+ newText);
//
//                return false;
//            }
//        });
    }
    private void doCheckBoxes(){
//        CheckBox checkBox = findViewById(R.id.checkbox1);
//        CheckBox checkBox2 = findViewById(R.id.checkbox2);
        CheckBox checkBoxPM_25 = findViewById(R.id.checkBoxPM_25);
        CheckBox checkBoxPM_10 = findViewById(R.id.checkBoxPM_10);
        CheckBox checkBoxO3 = findViewById(R.id.checkBoxO3);
        CheckBox checkBoxSO2 = findViewById(R.id.checkBoxSO2);
        CheckBox checkBoxNO2 = findViewById(R.id.checkBoxNO2);
        CheckBox checkBoxCO = findViewById(R.id.checkBoxCO);
        CheckBox checkBoxLocation = findViewById(R.id.checkBox_do_location);

        // LL box
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // Do something based on checkbox state
//                if (isChecked) {
//                    // Checkbox is checked
//                    user.setLL(true);
//                    System.out.println("Box 1 Checked!!!!");
//                } else {
//                    // Checkbox is unchecked
//                    user.setLL(false);
//                    System.out.println("Box 1 NOT Checked!!!!");
//                }
//            }
//        });
//        // Address box
//        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // Do something based on checkbox state
//                if (isChecked) {
//                    // Checkbox is checked
//                    user.setAdd(true);
//                    System.out.println("Box 2 Checked!!!!");
//                } else {
//                    // Checkbox is unchecked
//                    user.setAdd(false);
//                    System.out.println("Box 2 NOT Checked!!!!");
//                }
//            }
//        });
        checkBoxPM_25.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("pm25", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("pm25", false);
                }
            }
        });
        checkBoxPM_10.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("pm10", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("pm10", false);
                }
            }
        });
        checkBoxO3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("O3", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("O3", false);
                }
            }
        });
        checkBoxSO2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("SO2", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("SO2", false);
                }
            }
        });
        checkBoxNO2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("NO2", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("NO2", false);
                }
            }
        });
        checkBoxCO.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    user.setSensitivities("CO", true);
                } else {
                    // Checkbox is unchecked
                    user.setSensitivities("CO", false);
                }
            }
        });
        checkBoxLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Do something based on checkbox state
                if (isChecked) {
                    // Checkbox is checked
                    doLocation();
                    Statics.doLocation = true;
                } else {
                    // Checkbox is unchecked
                    Statics.doLocation = false;
                }
            }
        });
    }
    private void doLocation() {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable the location layer if the permission has been granted

            // Get the last known location of the device and move the camera
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                if (Statics.doLocation){
//                                    user.setLocationLatLng(latLng);
                                    Statics.userLocationLL = latLng;
                                } else {
                                    user.setLocationLatLng(new LatLng(Statics.centerLat, Statics.centerLon));
                                    Statics.userLocationLL = new LatLng(Statics.centerLat, Statics.centerLon);
                                }
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

}