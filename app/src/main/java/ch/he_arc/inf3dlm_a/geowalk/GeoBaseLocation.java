package ch.he_arc.inf3dlm_a.geowalk;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by paul.jeanbour on 10.11.2017.
 */

/**
 * GeoBaseLocation class
 */
@IgnoreExtraProperties
public class GeoBaseLocation implements Serializable {
    public double latitude;
    public double longitude;

    /**
     * Return a LatLng object for Google MAP Fragment
     * @return
     */
    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    /**
     * Default constructor
     */
    public GeoBaseLocation(){}

    /**
     * Full constructor
     * @param latitude the latitude
     * @param longitude the longitude
     */
    public GeoBaseLocation(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }
}
