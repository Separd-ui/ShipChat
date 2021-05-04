package com.example.shipchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

public class StartActivity extends AppCompatActivity {
    private Animation animation;
    private TextView logo;
    private Button b_scan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        logo=findViewById(R.id.text_logo);
        animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_logo);
        logo.startAnimation(animation);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Toast.makeText(this, "Вы вошли как "+FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            Intent i=new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void onClickSignIn(View view) {
        Intent i=new Intent(this,LoginActivity.class);
        i.putExtra(Constans.REQ,1);
        startActivity(i);
        finish();

    }
    public void onClickSignUp(View view) {
        Intent i=new Intent(this,LoginActivity.class);
        i.putExtra(Constans.REQ,2);
        startActivity(i);
        finish();
    }
}