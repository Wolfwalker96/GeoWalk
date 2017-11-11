package ch.he_arc.inf3dlm_a.geowalk;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by paul.jeanbour on 10.11.2017.
 */

@IgnoreExtraProperties
public class GeoBase {
    public String id;
    public int score;
    public GeoBaseLocation location;

    public GeoBase(){

    }

    public GeoBase(double latitude, double longitude, int score){
        this.score=score;
        location = new GeoBaseLocation(latitude,longitude);
    }
}
