package ch.he_arc.inf3dlm_a.geowalk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.he_arc.inf3dlm_a.geowalk.databinding.ActivityMapBinding;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;

    private List<GeoBase> bases = new ArrayList<>();
    private HashMap<GeoBase, PendingIntent> basePendingIntentHashMap = new HashMap<>();
    public long score = 0;
    private List<GeoBase> geoBasesFound = new ArrayList<>();
    private LocationManager locationManager;
    private DatabaseReference myDb;
    private Location location;
    private String userId;
    private ActivityMapBinding binding;
    private NotificationManager notificationManager;
    private String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
    private boolean notifiable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        userId = getIntent().getStringExtra("user_id");
        initFirebase();
        initLocation();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        binding.setScore(score);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        findViewById(R.id.btnAddBase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDb.child("bases").push().setValue(new GeoBase(location.getLatitude(),location.getLongitude(),2));
            }
        });

        findViewById(R.id.btnScores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startActivity(new Intent(MapActivity.this,ScoreActivity.class));
            }
        });
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifiable = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null)
            setIntent(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(getIntent().getExtras().getBoolean("fromNotif",false)){
            getIntent().putExtra("fromNotif",false);
            GeoBase base = (GeoBase)getIntent().getExtras().get("base");
            Intent intent = new Intent(MapActivity.this, ScannerActivity.class);
            intent.putExtra("base", base);
            intent.setAction(Long.toString(System.currentTimeMillis()));// Some magic tricks
            startActivityForResult(intent,2);
        }
    }

    /**
     * Initialise the Firebase instance and events
     */
    protected void initFirebase() {
        myDb = FirebaseDatabase.getInstance().getReference();

        myDb.child("users").child(userId).child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TEST",dataSnapshot.getValue().toString());
                score = (long)dataSnapshot.getValue();
                refreshMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myDb.child("users").child(userId).child("geoBasesFound").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                geoBasesFound.add(base);
                refreshMap();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                geoBasesFound.remove(base);
                refreshMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query basesQuery = myDb.child("bases").orderByKey();
        basesQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                base.id = dataSnapshot.getKey();
                bases.add(base);
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                refreshMap();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GeoBase base = dataSnapshot.getValue(GeoBase.class);
                bases.remove(base);
                basePendingIntentHashMap.remove(base);
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

    /**
     * Initialize the location detection
     */
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

                // Proximity detection
                for (GeoBase base : MapActivity.this.bases) {
                    if (!geoBasesFound.contains(base)) {
                        float[] distance = new float[3];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), base.location.latitude, base.location.longitude, distance);
                        if (distance[0] <= 5) { // Distance of 5 meter
                            if (MapActivity.this.hasWindowFocus()) {
                                Intent intent = new Intent(MapActivity.this, ScannerActivity.class);
                                intent.putExtra("base", base);
                                intent.setAction(Long.toString(System.currentTimeMillis()));// Some magic tricks
                                startActivityForResult(intent,2);
                                notifiable = false;
                            }else if (notifiable){
                                sendNotification(base);
                                notifiable = false;
                            }
                        }
                    }
                }
            }
        }, android.os.Looper.myLooper());
    }


    /**
     * Refresh the map Fragement (Add marker)
     */
    public void refreshMap() {
        map.clear();
        for (GeoBase base : bases) {
            MarkerOptions marker = new MarkerOptions().position(base.location.getLatLng()).title("Score : "+Integer.toString(base.score)).flat(true);
            if(!geoBasesFound.contains(base))
                map.addMarker(marker);
            else
                map.addMarker(marker.alpha(0.5f));
        }
        binding.setScore(score);
    }

    private void sendNotification(GeoBase base){
        Intent intent = new Intent(MapActivity.this, MapActivity.class);
        intent.putExtra("base",base);
        intent.putExtra("fromNotif",true);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_geowalk)
                    .setContentTitle("Base in your surrounding")
                    .setContentText("Scan the base near you !")
                    .setContentIntent(resultPendingIntent);
            notificationManager.notify(1, notificationBuilder.build());
    }

    /**
     * Is exectued when the Google Map Fragement is ready
     * @param googleMap The Google Map Fragement
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    /**
     * Is executed when the ScannerActivtiy return the datas
     * @param requestCode Code of the request 10 for scanner return
     * @param resultCode Code of the results 1 if scanner true 0 else
     * @param data The results data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("resultCode",Integer.toString(resultCode));
        if(resultCode==1) {
            GeoBase baseFind = (GeoBase) data.getExtras().get("base");
            if (data.getBooleanExtra("isFound", false)) {
                // geoBasesFound.add(baseFind);
                myDb.child("users").child(userId).child("geoBasesFound").push().setValue(baseFind);
                score += baseFind.score;
                refreshMap();
            }
            myDb.child("users").child(userId).child("score").setValue(score);
            notifiable = true;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("No", null).show();
    }
}
