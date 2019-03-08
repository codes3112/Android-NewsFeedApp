package com.example.newsfeed;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URI;

public class Login extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private SignInButton sign_in;
    private GoogleApiClient googleApiClient;
    private final static int REQ_CODE = 9001;
    private TextView Name, Email;
    private NavigationView mNavigationView;
    View mHeaderView;
    FirebaseAuth mAuth;
    EditText editTextUserEmail, editTextUserPass;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextUserEmail = (EditText) findViewById(R.id.userEmail);
        editTextUserPass = (EditText) findViewById(R.id.userPass);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.registerBtn).setOnClickListener(this);
        findViewById(R.id.signBtn).setOnClickListener(this);

        sign_in = (SignInButton) findViewById(R.id.btn_login);
        sign_in.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                signIn();
                break;

            case R.id.registerBtn:
                finish();
                startActivity(new Intent(this, signUp.class));
                break;

            case R.id.signBtn:
                userLogin();
                break;

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);

    }

    private void handleResult(GoogleSignInResult result) {

        if (result.isSuccess()) {

            try {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

                Intent intent = new Intent(this, MainActivity.class);

                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                intent.putExtra("p_name", personName);
                intent.putExtra("p_email", personEmail);
                try {
                    String personPhoto = acct.getPhotoUrl().toString();
                    intent.putExtra("p_photo", personPhoto);
                    Log.w("TAG", "test url" + personPhoto);

                }catch (NullPointerException ex){
                    Log.d("Error", ex.getMessage());
                }


                startActivity(intent);


            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_CODE)
        {
            GoogleSignInResult result= Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }
    private void userLogin() {

        final String email = editTextUserEmail.getText().toString().trim();
        String password = editTextUserPass.getText().toString().trim();
        Log.d("email", email);

        if (email.isEmpty()) {
            editTextUserEmail.setError("Email is required");
            editTextUserEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextUserEmail.setError("Please enter a valid email");
            editTextUserEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextUserPass.setError("Password is required");
            editTextUserPass.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    Intent intent = new Intent(Login.this, MainActivity.class);
                    //to clear all the acticities on the top of the stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("p_email", email);
                    startActivity(intent);



                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
