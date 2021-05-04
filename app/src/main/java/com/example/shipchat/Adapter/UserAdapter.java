package com.example.shipchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipchat.Constans;
import com.example.shipchat.MessageActivity;
import com.example.shipchat.R;
import com.example.shipchat.Utills.Message;
import com.example.shipchat.Utills.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.OffsetDateTime;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolderData> {
    private List<User> userList;
    private Context context;
    private boolean isChat;
    private String last_mes;

    public UserAdapter(List<User> userList, Context context,boolean isChat) {
        this.userList = userList;
        this.context = context;
        this.isChat=isChat;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolderData holder, int position) {
        holder.setData(userList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context, MessageActivity.class);
                i.putExtra(Constans.USERUI,userList.get(position).getUserui());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        private CircleImageView img_profile,img_offline,img_online,img_count;
        private TextView username,last_message,count_mes;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_username);
            img_profile=itemView.findViewById(R.id.user_image);
            img_online=itemView.findViewById(R.id.user_online);
            img_offline=itemView.findViewById(R.id.user_offline);
            last_message=itemView.findViewById(R.id.user_last_mes);
            img_count=itemView.findViewById(R.id.user_img_count);
            count_mes=itemView.findViewById(R.id.user_count);
        }
        private void setData(User user)
        {
            if(isChat)
            {
                //checkStatus(user.getUserui());
                if(user.getStatus().equals("online")){
                    img_online.setVisibility(View.VISIBLE);
                    img_offline.setVisibility(View.GONE);
                }
                else {
                    img_online.setVisibility(View.GONE);
                    img_offline.setVisibility(View.VISIBLE);
                }
                checkLastMes(user.getUserui());
                checkNumberUnread(user.getUserui());
            }
            else
            {
                img_offline.setVisibility(View.GONE);
                img_online.setVisibility(View.GONE);
                last_message.setVisibility(View.GONE);
                count_mes.setVisibility(View.GONE);
                img_count.setVisibility(View.GONE);
            }
            username.setText(user.getUsername());
            if(user.getImageid().equals("empty"))
                img_profile.setImageResource(R.mipmap.ic_launcher_round);
            else
                Picasso.get().load(user.getImageid()).into(img_profile);
        }
        private void checkLastMes(String userUI)
        {
            last_mes="empty";
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        Message message=ds.getValue(Message.class);
                        if((message.getSender().equals(FirebaseAuth.getInstance().getUid()) && message.getReceiver().equals(userUI)) ||
                                (message.getSender().equals(userUI) && message.getReceiver().equals(FirebaseAuth.getInstance().getUid())))
                        {
                            last_mes=message.getMessage();
                        }
                    }

                    if (last_mes.equals("empty")) {
                        last_message.setText("Нет сообщений");
                    } else {
                        last_message.setText(last_mes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void checkNumberUnread(String userUI)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count=0;
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        Message message=ds.getValue(Message.class);
                        if(message.getReceiver().equals(FirebaseAuth.getInstance().getUid()) && message.getSender().equals(userUI) && !message.getIsseen())
                            count++;
                    }
                    if(count==0){
                        count_mes.setVisibility(View.GONE);
                        img_count.setVisibility(View.GONE);
                    }
                    else {
                        count_mes.setText(String.valueOf(count));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        /*private void checkStatus(String userUI)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Connections").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue(Boolean.class))
                    {
                        img_online.setVisibility(View.VISIBLE);
                        img_offline.setVisibility(View.GONE);
                    }
                    else
                    {
                        img_online.setVisibility(View.GONE);
                        img_offline.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }*/

    }
}
