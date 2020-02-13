package com.e.firebaselogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class Welcome extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private SharedPrefrences sharedPrefrences;
    String email="",name="",phoneNo="",address;
    EditText userName,emailId,phoneNumber;
    TextView addrs;
    TextView logOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        sharedPrefrences = new SharedPrefrences(this);
        sharedPrefrences.loginStatus(true);

        phoneNumber = findViewById(R.id.phoneNo);
        emailId = findViewById(R.id.emailId);
        userName = findViewById(R.id.userName);
        addrs = findViewById(R.id.address);
        logOut = findViewById(R.id.logOut);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try{
            name = bundle.getString("name");
            email = bundle.getString("email");
            phoneNo = bundle.getString("phoneNo");
        }catch (Exception e){

        }


        phoneNumber.setText(phoneNo);
        emailId.setText(email);
        userName.setText(name);

        permission();
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefrences.loginStatus(false);
                Intent intent = new Intent(view.getContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @AfterPermissionGranted(123)
    public void permission(){
        String perm[] = {Manifest.permission.ACCESS_FINE_LOCATION};

        if(EasyPermissions.hasPermissions(this,perm)){
            SmartLocation.with(Welcome.this).location()
                    .continuous()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            Geocoder geocoder = new Geocoder(getApplicationContext());
                            try {
                                ArrayList<Address> addr = (ArrayList<Address>) geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                String addres = addr.get(0).getAddressLine(0);
                                Log.d("addr", addres);
                                addrs.setText(addres);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }



        else {
            EasyPermissions.requestPermissions(this,"We need to Access GPS permission to get your Address..",123,perm);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }


    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){

            new AppSettingsDialog.Builder(this).build().show();
        }else if(EasyPermissions.somePermissionDenied(this,Manifest.permission.ACCESS_FINE_LOCATION))
        {
            sharedPrefrences.loginStatus(false);
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }
}
