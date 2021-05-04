package com.example.shipchat.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.shipchat.R;
import com.example.shipchat.Utills.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;


public class ProfileFragment extends Fragment {
    private EditText ed_username;
    private CircleImageView imageView;
    private ImageView b_save;
    private String imageId;
    private String oldImageId,username;
    private int IMAGE_REQ=10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        ed_username=view.findViewById(R.id.profile_username);
        imageView=view.findViewById(R.id.profile_img);
        b_save=view.findViewById(R.id.profile_img_save);
        imageId="empty";
        oldImageId="empty";
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(i,IMAGE_REQ);
            }
        });

        getUserInfo();

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK && requestCode== IMAGE_REQ && data!=null)
        {
            imageId=data.getData().toString();
            imageView.setImageURI(data.getData());
        }
    }
    private void getUserInfo()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                ed_username.setText(user.getUsername());
                if(user.getImageid().equals("empty"))
                    imageView.setImageResource(R.mipmap.ic_launcher_round);
                else{
                    imageId=user.getImageid();
                    oldImageId=user.getImageid();
                    Picasso.get().load(user.getImageid()).into(imageView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void uploadImage()
    {
        if(!imageId.equals(oldImageId))
        {
            ProgressDialog progressDialog=new ProgressDialog(getContext());
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();

            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse(imageId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
            byte[] bytes=out.toByteArray();
            final StorageReference storageReference;
            if(oldImageId.equals("empty"))
            {
                storageReference= FirebaseStorage.getInstance().getReference("ImageUser").child(FirebaseAuth.getInstance().getUid()+"_Image");
            }
            else {
                storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(oldImageId);
            }
            UploadTask uploadTask=storageReference.putBytes(bytes);
            Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    imageId=task.getResult().toString();
                    saveInf();
                    progressDialog.dismiss();
                }
            });
        }
        else {
            saveInf();
        }
    }
    private void saveInf()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("username",ed_username.getText().toString());
        hashMap.put("imageid",imageId);

        databaseReference.updateChildren(hashMap);
    }

}