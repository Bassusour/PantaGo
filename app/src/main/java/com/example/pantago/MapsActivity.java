package com.example.pantago;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "pantaGo";
    double userLat;
    double userLong;
    double dist;
    int minP;
    double maxDist;
    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private View headerView;

    private HashMap<String, Marker> markerMap = new HashMap<String, Marker>();

    Spinner minPant;
    Spinner maxRad;

    FirebaseAuth firebaseAuth;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int PANT_ZOOM = 10;
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

    FragmentManager fragmentManager;


    Button postButton;
    Button listButton;
    private Geocoder geocoder;
    private MapsActivity mContext;
    List<PantListObject> pantlist;
    private Button goToList;

    PantFragment listFragment = new PantFragment();
    private SupportMapFragment mapFragment;

    private String[] minPantOptions = {getResources().getString(R.string.none), "1", "5", "10", "15", "20", "30"};
    private String[] maxRadOptions = {getResources().getString(R.string.none), "1.0", "1.5", "2.0", "2.5", "3.0", "5.0"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        setContentView(R.layout.activity_maps);
        mContext = this;

        firebaseAuth = FirebaseAuth.getInstance();


        FirebaseUser user = firebaseAuth.getCurrentUser();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view); //Maybe cast
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);

        //UI
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        if (mDrawerLayout == null) {
            Log.e(TAG, "mDraweLayout is null");
        }
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateUI(user);


        fragmentManager = getSupportFragmentManager();

        geocoder = new Geocoder(this, getResources().getConfiguration().locale);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        pants = new ArrayList<Pant>();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        pantlist = new ArrayList<PantListObject>();
        minPant = (Spinner) navigationView.getMenu().findItem(R.id.minPant).getActionView();
        minPant.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, minPantOptions));
        minPant.setSelected(false);  // must
        minPant.setSelection(0,true);


        minPant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "amount select called");
                for (HashMap.Entry<String, Marker> entry : markerMap.entrySet()) {
                    decideVisible(firebaseAuth.getCurrentUser(), getPantFromKey(entry.getKey()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        maxRad = (Spinner) navigationView.getMenu().findItem(R.id.maxRad).getActionView();
        maxRad.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, maxRadOptions));
        maxRad.setSelected(false);  // must
        maxRad.setSelection(0,true);

        maxRad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "radius selected called");
                for (HashMap.Entry<String, Marker> entry : markerMap.entrySet()) {
                    decideVisible(firebaseAuth.getCurrentUser(), getPantFromKey(entry.getKey()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        postButton = findViewById(R.id.postButton);
        listButton = findViewById(R.id.listButton);
        databaseReference.child("pants").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onAdded() called");
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
                    address = thoroughfare + " " + subThoroughfare + ", " + postalCode + " " + city;
                } catch (Exception e) {
                    e.printStackTrace();
                    address = getResources().getString(R.string.no_adress);
                }

                LatLng latlng = new LatLng(latitude, longitude);
                Marker mark = mMap.addMarker(new MarkerOptions().position(latlng)
                        .title(address)
                        .snippet("Antal pant: " + pant.getQuantity())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mark.setTag(pant);
                markerMap.put(pant.getPantKey(), mark);
                pants.add(pant);
                decideVisible(firebaseAuth.getCurrentUser(), pant);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChange() called");
                Pant pant = dataSnapshot.getValue(Pant.class);
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                decideVisible(currentUser, pant);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Pant pant = dataSnapshot.getValue(Pant.class);
                markerMap.get(pant.getPantKey()).remove();
                pants.remove(getPantFromKey(pant.getPantKey()));
                markerMap.remove(getPantFromKey(pant.getPantKey()));
                listFragment.removePant(pant);
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

                try {
                    if (locationPermissionGranted) {
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
                } catch (SecurityException e)  {
                    Log.e("Exception: %s", e.getMessage(), e);
                }


            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {


                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                Pant pant =(Pant) marker.getTag();
                if(currentUser.getUid().equals(pant.getOwnerUID())){
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


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

   private void getDeviceLocation() {
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

   private void getLocationPermission() {
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
        if(item.getItemId() == R.id.listButton){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slidein, R.anim.slideout);

            if(listFragment.isAdded()){
                transaction.remove(listFragment);
                transaction.commit();
                item.setTitle(getResources().getString(R.string.list));
            } else {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                transaction.setCustomAnimations(R.anim.slidein, R.anim.slidein);
                transaction.addToBackStack(null);
                transaction.add(R.id.frameLayout, listFragment);
                transaction.commit();
                item.setTitle(getResources().getString(R.string.back));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void decideVisible(FirebaseUser currentUser, Pant pant){
        if(markerMap.get(pant.getPantKey())==null){
            return;
        }

        double markerlat = pant.getLatitude();
        double markerLong = pant.getLongitude();

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLat = location.getLatitude();
                userLong = location.getLongitude();
                dist = getDistance(userLat, userLong, markerlat, markerLong);
                dist = dist / 1000.0;
                if (!minPant.getSelectedItem().equals(getResources().getString(R.string.none))) {
                    minP = Integer.parseInt((String) minPant.getSelectedItem());
                } else {
                    minP = 0;
                }

                if (!maxRad.getSelectedItem().equals(getResources().getString(R.string.none))) {
                    maxDist = Double.valueOf((String) maxRad.getSelectedItem());
                } else {
                    maxDist = 1000000.0;
                }
                if (pant.getClaimerUID().equals("") && pant.getQuantity() >= minP && dist < maxDist) {
                    markerMap.get(pant.getPantKey()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    markerMap.get(pant.getPantKey()).setVisible(true);
                    listFragment.addPant(pant, location.getLatitude(), location.getLongitude());
                } else if (currentUser.getUid().equals(pant.getClaimerUID()) || currentUser.getUid().equals(pant.getOwnerUID())) {
                    markerMap.get(pant.getPantKey()).setVisible(true);
                    markerMap.get(pant.getPantKey()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    listFragment.addPant(pant, location.getLatitude(), location.getLongitude());
                } else {
                    markerMap.get(pant.getPantKey()).setVisible(false);
                    listFragment.removePant(pant);
                }
            }
        });
    }

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
            TextView email = (TextView) headerView.findViewById(R.id.emailDrawer);
            TextView title = (TextView) headerView.findViewById(R.id.titleDrawer);
            ImageView userImage = (ImageView) headerView.findViewById(R.id.userImage);

            email.setText(user.getEmail().toString());
            title.setText("title: starter");
            if(user.getPhotoUrl() != null){
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl = photoUrl + "?type=large";
                Picasso.get().load(photoUrl).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.anonymous);
            }
        }
    }


    public Pant getPantFromKey(String key){
        Pant pant = new Pant();
        for(Pant pant1 : pants){
            if(pant1.getPantKey().equals(key)){
                pant = pant1;

            }
        }
        return pant;
    }

    // [END maps_current_place_update_location_ui]

   public static double getDistance(double lat1, double lon1, double lat2, double lon2){
        double r = 6371000;
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI/180;
        double deltaPhi = (lat2 -lat1) * Math.PI/180;
        double deltaLambda = (lon2-lon1) * Math.PI/180;

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda/2)* Math.sin(deltaLambda/2);
       double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        return r*c;
    }

    public void zoomToPant(Pant pant){
       FragmentTransaction transaction = fragmentManager.beginTransaction();
       transaction.setCustomAnimations(R.anim.slidein, R.anim.slideout);
       transaction.remove( listFragment);
       transaction.commit();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(pant.getLatitude(),
                        pant.getLongitude()), PANT_ZOOM));
    }
}
