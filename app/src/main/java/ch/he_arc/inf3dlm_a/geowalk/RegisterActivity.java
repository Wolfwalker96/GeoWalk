package ch.he_arc.inf3dlm_a.geowalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String email;
    private String password;
    private String username;

    private TextView error_short;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);

        error_short = findViewById(R.id.error_password_short);

        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error_short.setVisibility(View.GONE);
                if(getPasswordEmailUsername()){
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                user = mAuth.getCurrentUser();
                                if(user != null){
                                    String userId = user.getUid();
                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                                    database.child("users").child(userId).setValue(new User(username));
                                }
                                openMap();
                            }
                        }
                    });
                }
                else{
                    error_short.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void openMap(){
        if (user != null) {
            Log.d("USER",user.getUid());
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("user_id",user.getUid());
            startActivity(intent);
            this.finish();
        }
    }

    protected boolean getPasswordEmailUsername(){

        username = ((EditText)findViewById(R.id.username)).getText().toString();
        email = ((EditText)findViewById(R.id.email)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(password.length() < 8 ){
            return false;
        }

        return true;
    }
}
