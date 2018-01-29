package ch.he_arc.inf3dlm_a.geowalk;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String email;
    private String password;

    private static final int ACCESS_PERMISSION = 1;

    private TextView error_short;
    private TextView error_incorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        error_incorrect = findViewById(R.id.error_password_incorrect);
        error_short = findViewById(R.id.error_password_short);

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error_short.setVisibility(View.GONE);
                error_incorrect.setVisibility(View.GONE);
                if(getPasswordEmail()){
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                openMap();
                            } else {
                                error_incorrect.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                else{
                    error_short.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        if(getIntent().getExtras()!=null && getIntent().getExtras().getBoolean("logout",false))
        {
            mAuth.signOut();
        }
    }

    protected boolean getPasswordEmail(){

        email = ((EditText)findViewById(R.id.email)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(password.length() < 8 ){
            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        PermissionRequest();
        openMap();
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

    private void PermissionRequest(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == ACCESS_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {

                final Activity activity = this;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Exit");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage("You have to grant permissions to use this application.\nThe application will be closed now !");
                builder.setCancelable(false);

                builder.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                activity.finish();
                                System.exit(0);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
