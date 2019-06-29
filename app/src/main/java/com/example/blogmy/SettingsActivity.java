package com.example.blogmy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputEditText userName, userProfileName, userStatus, userCountry, userGender, userRelationship, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;
    private DatabaseReference settingsUserRef,userDetailRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    private StorageReference userProfileImageRef;
    private String downloadUrl;
    final int REQUEST_CODE = 123;

    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Profile");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        // ref to database at user node.
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userDetailRef = FirebaseDatabase.getInstance().getReference().child("Users");
        loadingBar = new ProgressDialog(this);
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        // link the activity view with toolbar view
        /*
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        */

        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userName = (TextInputEditText) findViewById(R.id.settings_username);
        userProfileName = (TextInputEditText) findViewById(R.id.settings_profile_fullname);
        userStatus = (TextInputEditText) findViewById(R.id.settings_status);
        userCountry = (TextInputEditText) findViewById(R.id.settings_country);
        userGender = (TextInputEditText) findViewById(R.id.settings_gender);
        userRelationship = (TextInputEditText) findViewById(R.id.settings_relationship_status);
        userDOB = (TextInputEditText) findViewById(R.id.settings_dob);

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


                // Check for gallery permission
                if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(SettingsActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                } else {
                    openGallery();
                }
            }
        });
    }

    public void openGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    // when request permission complete
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Blogmy", "onRequestPermissionsResult() called / PASSED");
                openGallery();
            } else {
                Log.d("Blogmy", "onRequestPermissionsResult() called / DENIED");
            }
        }
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

    private void updateAccountInfo(final String username, String profilename, String status, String country, String gender, String dob, String relation) {

        final HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("profilename", profilename);
        userMap.put("status", status);
        userMap.put("country", country);
        userMap.put("gender", gender);
        userMap.put("dob", dob);
        userMap.put("relationship", relation);

        userDetailRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    boolean userexisted = false;
                    for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){
                        //Loop 1 to go through all the child nodes of users
                        //for(DataSnapshot booksSnapshot : uniqueKeySnapshot.child("username").g){
                            //loop 2 to go through all the child nodes of books node
                            if(uniqueKeySnapshot.child("username").getValue().toString().equals(username)){
                                userexisted = true;
                            }
                        //}
                    }

                    if(userexisted){
                        Toast.makeText(SettingsActivity.this, "This username is taken", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    sendUserToMainActivity();
                                    Toast.makeText(SettingsActivity.this, "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Error occured, while updating account settings", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                } else {
                    settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Error occured, while updating account settings", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingBar.dismiss();
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
            // this will enter onActivityResult again.
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
                Bitmap bitmap;
                try {
                    bitmap = new Compressor(this)
                            .setMaxHeight(300) //Set height and width
                            .setMaxWidth(300)
                            .setQuality(70)
                            .compressToBitmap(new File(resultUri.getPath()));
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(SettingsActivity.this, "Image cannot be cropped, try again.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    return;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                final byte[] fileBytes = baos.toByteArray();


                // format to save a file as
                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                filePath.putBytes(fileBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }

}
