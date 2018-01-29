package ch.he_arc.inf3dlm_a.geowalk;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by paul.jeanbour on 10.11.2017.
 */

/**
 * GeoBase class
 */
@IgnoreExtraProperties
public class GeoBase implements Serializable{
    public String id;
    public int score;
    public GeoBaseLocation location;

    /**
     * Default constructor
     */
    public GeoBase(){

    }

    /**
     * Full constructor
     * @param latitude the latitude
     * @param longitude the longitude
     * @param score the score
     */
    public GeoBase(double latitude, double longitude, int score){
        this.score=score;
        location = new GeoBaseLocation(latitude,longitude);
    }

    /**
     * Compare if obj is equal to this
     * @param obj a GeoBase to compare
     * @return True if equals
     */
    @Exclude
    @Override
    public boolean equals(Object obj) {;
        return this.id.equals(((GeoBase)obj).id);
    }
}
