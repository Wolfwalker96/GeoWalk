package ch.he_arc.inf3dlm_a.geowalk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;

public class ScoreActivity extends AppCompatActivity {

    List<User> users = new ArrayList<User>();
    ListView list;
    ArrayAdapter<User> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        //users.add(new User(56,"Marc"));
        //users.add(new User(12,"Jean"));
        //users.add(new User(43,"Valentin"));
        //users.add(new User(34,"Léo"));
        //users.add(new User(99,"PussyDestroyer"));

        list = (ListView) findViewById(R.id.scoreList);
        DatabaseReference myDb = FirebaseDatabase.getInstance().getReference();

        adapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_2, android.R.id.text1, users){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(users.get(position).userName);
                text2.setText("Score " + Long.toString(users.get(position).score));
                return view;
            }

            @Override
            public void notifyDataSetChanged() {
                this.setNotifyOnChange(false);
                this.sort(new Comparator<User>() {
                    @Override
                    public int compare(User user, User t1) {
                        return ((int)t1.score - (int)user.score);
                    }
                });
                this.setNotifyOnChange(true);
                super.notifyDataSetChanged();
            }
        };

        list.setAdapter(adapter);

        myDb.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                long score = 0;
                String username = "";
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Log.d("KEY",child.getKey());
                    if(child.getKey().equals("score")){
                        score = (long)child.getValue();
                    }
                    else if(child.getKey().equals("username")){
                        username = child.getValue(String.class);
                    }
                }
                Log.d("SCORE",username+" "+Long.toString(score));
                //adapter.add(new User(score, username));
                users.add(new User(score, username));
                Log.d("USERS",users.toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
