package com.example.shipchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shipchat.Utills.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class LoginActivity extends AppCompatActivity {
    private int REQ;
    private TextView text_header;
    private Button b_log;
    private EditText ed_username,ed_mail,ed_pas;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private CircleImageView img_profile;
    private String imageId;
    private CheckBox checkBox;
    private TextView restore_pas;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        GetIntent();
    }
    private void signIn(String mail,String password)
    {
        if(!TextUtils.isEmpty(mail) && !TextUtils.isEmpty(password))
        {
            auth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this, "Вы вошли как "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else
                    {
                        b_log.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Что-то пошло не так.Попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void signUp(String mail,String password)
    {

        auth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    if(!imageId.equals("empty"))
                        uploadImage();
                    else
                        saveInf();
                    Toast.makeText(LoginActivity.this,"Вы вошли как "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    b_log.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Что-то пошло не так.Попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void uploadImage()
    {
        if(!imageId.equals("empty"))
        {
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();

            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
            byte[] bytes=out.toByteArray();
            StorageReference storageReference=FirebaseStorage.getInstance().getReference("ImageUser").child(auth.getUid()+"_Image");
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
                }
            });
        }
        else
        {
            saveInf();
        }
    }
    private void saveInf()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        User user=new User();
        user.setImageid(imageId);
        user.setSearch(ed_username.getText().toString().toLowerCase());
        user.setUsername(ed_username.getText().toString());
        user.setUserui(auth.getUid());
        databaseReference.setValue(user);
        Intent i=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
        finish();
    }
    public void onClickSign(View view) {
        b_log.setEnabled(false);
        if(REQ==1)
        {
            signIn(ed_mail.getText().toString(),ed_pas.getText().toString());
        }
        else
        {
            signUp(ed_mail.getText().toString(),ed_pas.getText().toString());
        }
    }
    private void init()
    {
        imageId="empty";
        restore_pas=findViewById(R.id.log_reset);
        text_header=findViewById(R.id.log_text);
        ed_mail=findViewById(R.id.log_ed_mail);
        ed_pas=findViewById(R.id.log_ed_pas);
        ed_username=findViewById(R.id.log_ed_username);
        b_log=findViewById(R.id.b_log);
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        img_profile=findViewById(R.id.log_image);

        restore_pas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restorePassword();
            }
        });
        checkBox=findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                {
                    ed_pas.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else
                {
                    ed_pas.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });
    }

    private void restorePas(String mail)
    {

        auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!TextUtils.isEmpty(mail))
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this, "Мы отправили вам письмо для восстановления пароля.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Произошла ошибка.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Заполните поле.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private  void restorePassword()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View dialogView=getLayoutInflater().inflate(R.layout.restore_pas_layout,null);
        builder.setView(dialogView);


        EditText ed_mail_d;
        Button b_send;
        ed_mail_d=dialogView.findViewById(R.id.d_ed_mail);
        b_send=dialogView.findViewById(R.id.d_b_send);
        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restorePas(ed_mail_d.getText().toString());
            }
        });

        alertDialog=builder.create();
        if(alertDialog.getWindow()!=null)
        {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        alertDialog.show();

    }
    private void GetIntent()
    {
        Intent i=getIntent();
        REQ=i.getIntExtra(Constans.REQ,1);
        if(REQ==1)
        {
            ed_username.setVisibility(View.GONE);
            b_log.setText(R.string.sign_in);
            text_header.setText(R.string.sign_in_t);
            img_profile.setVisibility(View.GONE);
        }
        else
        {
            b_log.setText(R.string.sign_up);
            text_header.setText(R.string.sign_up_t);
            restore_pas.setVisibility(View.GONE);
        }
    }

    public void onClickImageSet(View view) {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            assert result != null;
            imageId=result.getUri().toString();
            img_profile.setImageURI(result.getUri());
        }
    }
}