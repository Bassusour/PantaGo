package com.example.pantago;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final int LAUNCH_POST = 1;
    private static final int LAUNCH_CLAIM = 2;
    private static final int LAUNCH_REMOVE = 3;
    private static final String TAG = "pantaGo";
    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private View headerView;

    private HashMap<String, Marker> markerMap = new HashMap<String, Marker>();

    FirebaseAuth firebaseAuth;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static DatabaseReference databaseReference;
    private static StorageReference storageReference;

    ArrayList<Pant> pants;

    Button postButton;
    private Geocoder geocoder;
    private MapsActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view); //Maybe cast
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);

        //UI
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        if(mDrawerLayout == null){
            Log.e(TAG,"mDraweLayout is null");
        }
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.i(TAG, "test4");
        updateUI(user);



        geocoder = new Geocoder(this, getResources().getConfiguration().locale);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        pants = new ArrayList<Pant>();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        postButton = findViewById(R.id.postButton);
        databaseReference.child("pants").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Pant pant = dataSnapshot.getValue(Pant.class);
                double latitude = pant.getLatitude();
                double longitude = pant.getLongitude();

                String address = "";

                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String thoroughfare = addresses.get(0).getThoroughfare();
                    String subThoroughfare = addresses.get(0).getSubThoroughfare();
                    String city = addresses.get(0).getLocality();
                    String postalCode = addresses.get(0).getPostalCode();
                    address = thoroughfare + " " +  subThoroughfare + ", " + postalCode + " " + city;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LatLng latlng = new LatLng(latitude, longitude);
                Marker mark = mMap.addMarker(new MarkerOptions().position(latlng)
                        .title(address)
                        .snippet("Antal pant: " + pant.getQuantity())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                pant.marker = mark;

                markerMap.put(pant.getPantKey(), mark);

                /*if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(pant.getClaimerUID()) && pant.getClaimed()){
                    pant.marker.setVisible(true);
                }else if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(pant.getClaimerUID()) && pant.getClaimed()){
                    pant.marker.setVisible(false);
                }else{
                    pant.marker.setVisible(true);
                }*/
                pants.add(pant);
                decideVisible(firebaseAuth.getCurrentUser(), pant);

                Log.i(TAG, "Resumed");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildChanged() called");
                Pant pant = dataSnapshot.getValue(Pant.class);
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                decideVisible(currentUser, pant);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Pant pant = dataSnapshot.getValue(Pant.class);
                markerMap.get(pant.getPantKey()).remove();
                //decideVisible(firebaseAuth.getCurrentUser(), pant);

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

   @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lastKnownLocation = location;
                        Intent intent = new Intent(MapsActivity.this, UploadActivity.class);
                        intent.putExtra("latitude", lastKnownLocation.getLatitude());
                        intent.putExtra("longitude", lastKnownLocation.getLongitude());
                        startActivity(intent);
                    }
                });
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                Pant pant = new Pant();
                for (int i = 0; i < pants.size(); i++) {
                    if (pants.get(i).marker.getId().equals(marker.getId())) {
                        pant = pants.get(i);
                    }
                }
                if(currentUser.getUid().equals(pant.getOwnerUID())){
                    Log.i(TAG, pant.getOwnerUID());
                    Intent intent = new Intent(MapsActivity.this, RemoveActivity.class);
                    intent.putExtra("id", marker.getId());
                    intent.putExtra("pantKey", pant.getPantKey());
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(MapsActivity.this, ClaimActivity.class);
                    intent.putExtra("longitudeMarker", marker.getPosition().longitude);
                    intent.putExtra("latitudeMarker", marker.getPosition().latitude);
                    intent.putExtra("id", marker.getId());
                    intent.putExtra("pantKey", pant.getPantKey());
                    startActivity(intent);
                }

            }
        });

        /*
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                return false;
            }
        });
        */


    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            }
                        } else {
                            //Log.d(TAG, "Current location is null. Using defaults.");
                            //Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void decideVisible(FirebaseUser currentUser, Pant pant){
            if (pant.getClaimerUID().equals("")) {
                markerMap.get(pant.getPantKey()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerMap.get(pant.getPantKey()).setVisible(true);
            } else if (currentUser.getUid().equals(pant.getClaimerUID()) || currentUser.getUid().equals(pant.getOwnerUID())) {
                markerMap.get(pant.getPantKey()).setVisible(true);
                markerMap.get(pant.getPantKey()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            } else {
                markerMap.get(pant.getPantKey()).setVisible(false);
            }
    }

    /**
     * Method for what item from the drawer was clicked
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            Log.i(TAG, "test1");
            TextView email = (TextView) headerView.findViewById(R.id.emailDrawer);
            TextView title = (TextView) headerView.findViewById(R.id.titleDrawer);
            ImageView userImage = (ImageView) headerView.findViewById(R.id.userImage);

            email.setText("email: " + user.getEmail().toString());
            title.setText("title: starter");
            if(user.getPhotoUrl() != null){
                Log.i(TAG, "test2");
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl = photoUrl + "?type=large";
                Picasso.get().load(photoUrl).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.anonymous);
            }
        }
    }

    // [END maps_current_place_update_location_ui]
}
