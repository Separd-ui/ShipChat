package com.example.shipchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.shipchat.Adapter.UserAdapter;
import com.example.shipchat.R;
import com.example.shipchat.Utills.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<User> users;
    private UserAdapter adapter;
    private EditText ed_search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user, container, false);
        recyclerView=view.findViewById(R.id.rec_view_users);
        ed_search=view.findViewById(R.id.user_ed_search);
        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        users=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new UserAdapter(users,getContext(),false);
        recyclerView.setAdapter(adapter);
        getUsers();
        return view;
    }
    private void searchUsers(String text)
    {
        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(text)
                .endAt(text+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    if(!user.getUserui().equals(FirebaseAuth.getInstance().getUid()))
                    {
                        users.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getUsers()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(ed_search.getText().toString().equals(""))
                {
                    users.clear();
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        User user=ds.getValue(User.class);
                        if(!user.getUserui().equals(FirebaseAuth.getInstance().getUid()))
                            users.add(user);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}