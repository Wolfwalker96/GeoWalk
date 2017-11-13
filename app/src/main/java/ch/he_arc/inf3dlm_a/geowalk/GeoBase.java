package ch.he_arc.inf3dlm_a.geowalk;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by paul.jeanbour on 10.11.2017.
 */

@IgnoreExtraProperties
public class GeoBase implements Serializable{
    public String id;
    public int score;
    public GeoBaseLocation location;

    public GeoBase(){

    }

    public GeoBase(double latitude, double longitude, int score){
        this.score=score;
        location = new GeoBaseLocation(latitude,longitude);
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {;
        return this.id.equals(((GeoBase)obj).id);
    }
}
