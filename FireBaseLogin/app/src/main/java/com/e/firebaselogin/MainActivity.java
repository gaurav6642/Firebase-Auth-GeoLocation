package com.e.firebaselogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    EditText phoneNumber;
    String email = "", name = "", phoneNo = "";

    int RC_SIGN_IN = 0;
    LinearLayout ll1;
    Bundle bundle = new Bundle();
    FirebaseAuth mAuth;
    CallbackManager mCallbackManager;
    TextView google, phoneLoginBtn, facebook, underline, continue_btn;
    GoogleApiClient googleApiClient;
    GoogleSignInClient googleSignInClient;
    private SharedPrefrences sharedPrefrences;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();


        mCallbackManager = CallbackManager.Factory.create();

        phoneNumber = findViewById(R.id.phoneNumber);
        google = findViewById(R.id.googleButton);
        facebook = findViewById(R.id.facebook);
        phoneLoginBtn = findViewById(R.id.phoneLoginBtn);
        continue_btn = findViewById(R.id.continue_btn);
        underline = findViewById(R.id.underline);
        ll1 = findViewById(R.id.ll1);
        sharedPrefrences = new SharedPrefrences(this);
        FirebaseAuth.getInstance().signOut();

        //sharedPreference for one Time Login Screen
        SharedPreferences sharedPreferences = getSharedPreferences("SHARED_REF", MODE_PRIVATE);
        if (sharedPrefrences.read_login_status()) {
            bundle.putString("email", email);
            bundle.putString("name", name);
            bundle.putString("phoneNo", phoneNo);
            Intent intent = new Intent(this, Welcome.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }


        phoneLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll1.setVisibility(View.VISIBLE);
                underline.setVisibility(View.VISIBLE);
                continue_btn.setVisibility(View.VISIBLE);
                facebook.setVisibility(View.GONE);
                phoneLoginBtn.setVisibility(View.GONE);
                google.setVisibility(View.GONE);
            }
        });


        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Hello", "facebook:onSuccess:" + loginResult);
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                Log.d("Name", "" + user);
                handleFacebookAccessToken(loginResult.getAccessToken());
                Log.d("Namm", "" + loginResult.getAccessToken());
                bundle.putString("email", email);
                bundle.putString("name", name);
                bundle.putString("phoneNo", phoneNo);
                Intent intent = new Intent(MainActivity.this, Welcome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                Log.d("cancel", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("ss", "facebook:onError", error);
                // ...
            }
        });


        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

        //phone verification Firebase

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobile = "+91" + phoneNumber.getText().toString();

                if (mobile.isEmpty() || mobile.length() < 10) {
                    phoneNumber.setError("Enter a valid mobile");
                    phoneNumber.requestFocus();
                    return;
                }

                bundle.putString("email", email);
                bundle.putString("name", name);
                bundle.putString("phoneNo", phoneNo);
                Intent intent = new Intent(MainActivity.this, getOtp.class);
                intent.putExtra("mobile", mobile);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });


        AccessTokenTracker tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    handleFacebookAccessToken(currentAccessToken);

                }

            }
        };
    }


    private void handleFacebookAccessToken(AccessToken accessToken) {

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Facebook Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            email = user.getEmail();
                            phoneNo = user.getPhoneNumber();
                            name = user.getDisplayName();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("failed", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //google on Activity Result
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            Log.d("data", "" + data);
        }

        //facebook on Activity Result
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            email = task.getResult().getEmail();
            name = task.getResult().getDisplayName();
            Log.d("Name", "" + email + name);
            bundle.putString("email", email);
            bundle.putString("name", name);
            bundle.putString("phoneNo", phoneNo);
            Intent intent = new Intent(MainActivity.this, Welcome.class);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.d("tag", "Google sign in failed");
            // ...
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Connection Failed..",Toast.LENGTH_LONG).show();
    }
}
