package com.example.shipchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.shipchat.Adapter.MessageAdapter;
import com.example.shipchat.Utills.Message;
import com.example.shipchat.Utills.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView img_profile;
    private TextView username;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ImageButton b_send;
    private ImageView img_smile;
    private EmojiconEditText ed_mes;
    private String userUI;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private ValueEventListener seenListener;
    private DatabaseReference databaseReference;
    private EmojIconActions actions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        init();
    }
    private void init()
    {
        username=findViewById(R.id.mes_username);
        img_profile=findViewById(R.id.mes_profile_img);
        ed_mes=findViewById(R.id.mes_ed_send);
        b_send=findViewById(R.id.mes_b_send);
        img_smile=findViewById(R.id.mes_smile);
        actions=new EmojIconActions(this,getWindow().getDecorView().getRootView(),ed_mes,img_smile);
        actions.ShowEmojIcon();


        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(ed_mes.getText().toString()))
                {
                    sendMessage(ed_mes.getText().toString(),userUI);
                    ed_mes.setText("");
                }
                else {
                    Toast.makeText(MessageActivity.this, "Вы не можете отпроавить пустое сообщение.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        databaseReference=firebaseDatabase.getReference("Chats");

        recyclerView=findViewById(R.id.rec_view_mes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList=new ArrayList<>();
        adapter=new MessageAdapter(messageList,this);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar=findViewById(R.id.mes_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GetIntent();
    }
    private void GetIntent()
    {
        Intent i=getIntent();
        userUI=i.getStringExtra(Constans.USERUI);
        getMessages();
        getUserInfo();
        seenMessage();
    }
    private void getUserInfo()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
    private void seenMessage()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        seenListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Message message=ds.getValue(Message.class);
                    if(message.getReceiver().equals(auth.getUid()) && message.getSender().equals(userUI))
                    {
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message_text,String receiver)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");

        Message message=new Message();
        message.setMessage(message_text);
        message.setReceiver(receiver);
        message.setSender(auth.getUid());
        message.setIsseen(false);
        message.setTime(System.currentTimeMillis());
        databaseReference.push().setValue(message);

        DatabaseReference chatRef=firebaseDatabase.getReference("ChatList").child(auth.getUid());
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child(userUI).exists())
                {
                    chatRef.child(userUI).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatRef1=firebaseDatabase.getReference("ChatList").child(userUI);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child(auth.getUid()).exists())
                {
                    chatRef1.child(auth.getUid()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getMessages()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Message message=ds.getValue(Message.class);
                    if((message.getSender().equals(auth.getUid()) && message.getReceiver().equals(userUI)) ||
                            (message.getSender().equals(userUI) && message.getReceiver().equals(auth.getUid())))
                    {
                        messageList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {
            finish();
        }
        return true;
    }
    private void setStatus(String status)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
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
        databaseReference.removeEventListener(seenListener);
        setStatus("offline");
    }
}