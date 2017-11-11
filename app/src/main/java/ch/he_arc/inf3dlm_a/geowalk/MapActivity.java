package ch.he_arc.inf3dlm_a.geowalk;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private List<GeoBase> bases = new ArrayList<>();
    private LocationManager locationManager;
    private DatabaseReference myDb;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initFirebase();
        initLocation();

        findViewById(R.id.btnAddBase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDb.child("bases").push().setValue(new GeoBase(location.getLatitude(),location.getLongitude(),2));
            }
        });
    }

    protected void initFirebase() {
        myDb = FirebaseDatabase.getInstance().getReference();

        Query basesQuery = myDb.child("bases").orderByKey();
        basesQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                bases.add(base);
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Intent intent = new Intent(MapActivity.this, ScannerActivity.class);
                locationManager.addProximityAlert(
                        base.location.latitude,
                        base.location.longitude,
                        10,
                        -1,
                        PendingIntent.getActivity(MapActivity.this, 1,intent, PendingIntent.FLAG_IMMUTABLE));
                refreshMap();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                bases.remove(base);
                refreshMap();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(LocationRequest.create().setInterval(500), new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude()),18.5f));
                float bearing = location.getBearing();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(
                                new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()),
                                18.5f,
                                0,
                                bearing)), 1000, null);
                Log.d("BEARING", Float.toString(bearing));
            }
        }, android.os.Looper.myLooper());
    }

    public void refreshMap() {
        map.clear();
        for (GeoBase base : bases) {
            map.addMarker(new MarkerOptions().position(base.location.getLatLng()).title(base.id).flat(true));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }
}
