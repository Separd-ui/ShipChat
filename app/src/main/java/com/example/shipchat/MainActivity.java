package com.example.shipchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.shipchat.Adapter.ViewPagerAdapter;
import com.example.shipchat.Fragments.ChatFragment;
import com.example.shipchat.Fragments.ProfileFragment;
import com.example.shipchat.Fragments.UserFragment;
import com.example.shipchat.Utills.Message;
import com.example.shipchat.Utills.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    private CircleImageView img_profile;
    private TextView username;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private String mAuth;
    private ViewPagerAdapter viewPagerAdapter;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        getUserInfo();
    }
    private void init()
    {
        Toolbar toolbar=findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        img_profile=findViewById(R.id.main_profile_img);
        username=findViewById(R.id.main_username);
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        mAuth=auth.getUid();

        TabLayout tabLayout=findViewById(R.id.tabLayout);
        ViewPager viewPager=findViewById(R.id.view_pager);

        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread=0;
                viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),getApplicationContext());
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Message message=ds.getValue(Message.class);
                    if(message.getReceiver().equals(auth.getUid()) && !message.getIsseen())
                    {
                        unread++;
                    }
                }
                if(unread==0)
                {
                    viewPagerAdapter.addFragment(new ChatFragment(),"Чаты");
                }
                else
                {
                    viewPagerAdapter.addFragment(new ChatFragment(),"("+unread+") Чаты");
                }
                viewPagerAdapter.addFragment(new UserFragment(),"Пользователи");
                viewPagerAdapter.addFragment(new ProfileFragment(),"Профиль");
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),this);
        viewPagerAdapter.addFragment(new ChatFragment(),"Чаты");
        viewPagerAdapter.addFragment(new UserFragment(),"Пользователи");
        viewPagerAdapter.addFragment(new ProfileFragment(),"Профиль");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);*/
        //managerConnections();


    }
    private void info()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.info_layout,null);
        builder.setView(view);
        Button button=view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog=builder.create();
        alertDialog.show();

    }

    private void getUserInfo()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageid().equals("empty"))
                    img_profile.setImageResource(R.mipmap.ic_launcher_round);
                else
                    Picasso.get().load(user.getImageid()).into(img_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout)
        {
            alertDialog();
        }
        if(item.getItemId()==R.id.info)
        {
            info();
        }
        return super.onOptionsItemSelected(item);
    }
    private void alertDialog()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out);
        builder.setMessage(R.string.sign_out_m);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this,StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        builder.show();
    }
    /*private  void managerConnections()
    {
        DatabaseReference conRef=firebaseDatabase.getReference("Connections").child(auth.getUid());
        conRef.setValue(true);

        conRef.onDisconnect().setValue(false);
    }*/

    private void setStatus(String status)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(mAuth);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        databaseReference.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }
}