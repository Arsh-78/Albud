package com.example.albud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    public void startscan(View view) {
        Intent intent1 = new Intent(this,MainActivity.class);
        startActivity(intent1);
    }

    public void startAlergy(View view) {
        Intent intent2 = new Intent(this,UserAllergyForm.class);
        startActivity(intent2);
    }

    public void startfii(View view) {
        Intent intent3 = new Intent(this,FirstAidInfo.class);
        startActivity(intent3);
    }
}