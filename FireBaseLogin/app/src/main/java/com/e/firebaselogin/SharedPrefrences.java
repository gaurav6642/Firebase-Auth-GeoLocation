package com.e.firebaselogin;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefrences {
    private SharedPreferences sharedPreferences;
    private Context context;
    public SharedPrefrences(Context context){
        this.context=context;
        sharedPreferences = context.getSharedPreferences("Login",Context.MODE_PRIVATE);

    }
    public void loginStatus(boolean status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status",status);
        editor.commit();

    }
    public boolean read_login_status(){
        boolean status = false;
        status = sharedPreferences.getBoolean("status",false);
        return status;
    }
}
