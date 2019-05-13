package com.example.blogmy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ProgressDialog loadingBar;
    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;
    final static int Gallery_Pick = 1;

    private Uri imageUri;
    private StorageReference postImageReference;
    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        selectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        toolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    private void validatePostInfo() {
        String description = postDescription.getText().toString();
        if(imageUri == null){
            Toast.makeText(this, "Please select image to post", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please add description for your post", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }
    }

    private void storeImageToFirebaseStorage() {
        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        // getLastPathSegment <-- the image name
        StorageReference filePath = postImageReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        // save post to firebase storage.
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image is uploaded successfully", Toast.LENGTH_SHORT).show();
                    savePostInformation();
                } else {
                    String messsage = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: " + messsage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savePostInformation() {
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", postDescription);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("fullname", userFullName);
                    postMap.put("profileimage", userProfileImage);

                    postRef.child(current_user_id + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "Post is created succesfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                    String messsage = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Error occured: " + messsage, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }
}
