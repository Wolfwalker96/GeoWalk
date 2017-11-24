package ch.he_arc.inf3dlm_a.geowalk;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by paul.jeanbour on 24.11.2017.
 */

@IgnoreExtraProperties
public class User {
    public long score;

    public User(){
        score = 0;
    }
}
