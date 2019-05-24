package com.example.blogmy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText userName, userProfileName, userStatus, userCountry, userGender, userRelationship, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;
    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    private StorageReference userProfileImageRef;
    private String downloadUrl;

    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        // ref to database at user node.
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        loadingBar = new ProgressDialog(this);
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        // link the activity view with toolbar view
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userName = (EditText) findViewById(R.id.settings_username);
        userProfileName = (EditText) findViewById(R.id.settings_profile_fullname);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userRelationship = (EditText) findViewById(R.id.settings_relationship_status);
        userDOB = (EditText) findViewById(R.id.settings_dob);

        updateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_buttons);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        //method is triggered once when the listener is attached and again every time the data changes
        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationship").getValue().toString();

                    Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelationship.setText(myRelationStatus);
                    userDOB.setText(myDOB);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }

    private void validateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfileName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userCountry.getText().toString();
        String dob = userDOB.getText().toString();
        String relation = userRelationship.getText().toString();

        /*
        if(TextUtils.isEmpty(username)){
            Toast.makeText(SettingsActivity.this, "Please write your username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(profilename)){
            Toast.makeText(SettingsActivity.this, "Please write your profilename", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(status)){
            Toast.makeText(SettingsActivity.this, "Please write your status", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(SettingsActivity.this, "Please write your country", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(gender)){
            Toast.makeText(SettingsActivity.this, "Please write your gender", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(dob)){
            Toast.makeText(SettingsActivity.this, "Please write your date of birth", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(relation)){
            Toast.makeText(SettingsActivity.this, "Please write your relationship status", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            updateAccountInfo(username, profilename, status, country, gender, dob , relation);
        }
        */

        loadingBar.setTitle("Profile Image");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        updateAccountInfo(username, profilename, status, country, gender, dob , relation);
    }

    private void updateAccountInfo(String username,String profilename,String status,String country,String gender,String dob,String relation) {

        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("profilename", profilename);
        userMap.put("status", status);
        userMap.put("country", country);
        userMap.put("gender", gender);
        userMap.put("dob", dob);
        userMap.put("relation", relation);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    Toast.makeText(SettingsActivity.this, "Error occured, whilte updating account settings", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }
        });
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    // when startactivity for result completes.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    //   .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                final Uri resultUri = result.getUri();

                // format to save a file as
                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri.toString();
                                    Log.i("user image:" , downloadUrl);
                                    Toast.makeText(SettingsActivity.this, "User profile image successfully uploaded", Toast.LENGTH_SHORT).show();

                                    settingsUserRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.i("create profileimage:" ,downloadUrl );
                                                        //  Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        //  startActivity(selfIntent);

                                                        // requires the URI to set to null first if not it will reset back to default
                                                        userProfImage.setImageURI(null);
                                                        userProfImage.setImageURI(resultUri);
                                                        Toast.makeText(SettingsActivity.this, "Your image is stored into Firebase successfuly", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();

                                                    }
                                                }
                                            });
                                }
                            });

                        }
                    }
                });
            }else{
                Toast.makeText(SettingsActivity.this, "Image cannot be cropped, try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

}
