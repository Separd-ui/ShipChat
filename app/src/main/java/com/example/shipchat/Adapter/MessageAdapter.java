package com.example.shipchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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

import android.text.format.DateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolderData> {
    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private List<Message> messageList;
    private Context context;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_LEFT) {
            View view= LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new ViewHolderData(view);
        } else {
            View view=LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new ViewHolderData(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolderData holder, int position) {
        holder.setData(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView mes_left,mes_right,seen,left_time,right_time;
        CircleImageView img_profile;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            mes_left=itemView.findViewById(R.id.left_text);
            mes_right=itemView.findViewById(R.id.right_text);
            right_time=itemView.findViewById(R.id.right_time);
            left_time=itemView.findViewById(R.id.left_time);
            img_profile=itemView.findViewById(R.id.left_img);
            seen=itemView.findViewById(R.id.right_seen);
        }
        private void setData(Message message)
        {
            if(message.getSender().equals(auth.getUid()))
            {
                right_time.setText(DateFormat.format("HH:mm", message.getTime()));
                mes_right.setText(message.getMessage());
                if(getAdapterPosition()==messageList.size()-1)
                {
                    if(message.getIsseen())
                        seen.setText("Просмотрено");
                    else
                        seen.setText("Доставлено");
                }
                else {
                    seen.setVisibility(View.GONE);
                }
            }
            else
            {
                left_time.setText(DateFormat.format("HH:mm", message.getTime()));
                mes_left.setText(message.getMessage());
                getUserInfo(message.getSender(),img_profile);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getSender().equals(auth.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }
    private void getUserInfo(String userUI,CircleImageView img)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(user.getImageid().equals("empty"))
                    img.setImageResource(R.mipmap.ic_launcher_round);
                else
                    Picasso.get().load(user.getImageid()).into(img);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

