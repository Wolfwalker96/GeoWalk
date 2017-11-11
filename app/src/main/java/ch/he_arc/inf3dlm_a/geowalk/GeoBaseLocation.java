package ch.he_arc.inf3dlm_a.geowalk;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by paul.jeanbour on 10.11.2017.
 */
@IgnoreExtraProperties
public class GeoBaseLocation implements Serializable {
    public double latitude;
    public double longitude;

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    public GeoBaseLocation(){}
    public GeoBaseLocation(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }
}
