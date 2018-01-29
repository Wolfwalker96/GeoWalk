package ch.he_arc.inf3dlm_a.geowalk;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul.jeanbour on 24.11.2017.
 */

@IgnoreExtraProperties
public class User implements Serializable {
    public long score;
    public String username;
    public List<GeoBase> geoBasesFound;

    public User(){
        score = 0;
        geoBasesFound = new ArrayList<GeoBase>();
    }

    public User(long score, String username){
        this.username = username;
        this.score = score;
    }
    public User(String username)
    {
        this();
        this.username = username;
    }
}
