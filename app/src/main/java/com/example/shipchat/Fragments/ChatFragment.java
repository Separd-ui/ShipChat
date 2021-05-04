package com.example.shipchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.example.shipchat.Adapter.UserAdapter;
import com.example.shipchat.R;
import com.example.shipchat.Utills.Message;
import com.example.shipchat.Utills.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<String> chatList;
    private List<User> userList;
    private UserAdapter adapter;
    private FirebaseAuth auth;
    private int tr=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView=view.findViewById(R.id.rec_view_chats);
        auth=FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList=new ArrayList<>();
        userList=new ArrayList<>();
        adapter=new UserAdapter(userList,getContext(),true);
        recyclerView.setAdapter(adapter);
        getUsersFromChats();
        return view;
    }
    private void getUsersFromChats()
    {
        /*DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Message message=ds.getValue(Message.class);
                    if(message.getSender().equals(auth.getUid()))
                        chatList.add(message.getReceiver());
                    else if(message.getReceiver().equals(auth.getUid()))
                        chatList.add(message.getSender());
                }
                readUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("ChatList").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    chatList.add(ds.getKey());
                }
                readUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readUsers()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    /*for(String ui:chatList)
                    {
                        if(user.getUserui().equals(ui))
                        {
                            if(userList.size()!=0)
                            {
                                for(User user1:userList)
                                {
                                    if(user1.getUserui().equals(user.getUserui()))
                                        tr=1;
                                }
                                if(tr==0){
                                    userList.add(user);
                                    break;
                                }
                                tr=0;
                            }
                            else
                            {
                                userList.add(user);
                                break;
                            }
                        }
                    }*/
                    for(String ui:chatList)
                    {
                        if(user.getUserui().equals(ui))
                            userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}