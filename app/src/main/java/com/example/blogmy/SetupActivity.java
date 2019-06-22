package com.example.blogmy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, userFullName, userCountry;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,userDetailRef;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImageRef;
    String currentUserId;
    final static int Gallery_Pick = 1;
    final int REQUEST_CODE = 123;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userDetailRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(this);

        userName = (EditText) findViewById(R.id.setup_username);
        userFullName = (EditText) findViewById(R.id.setup_full_name);
        userCountry = (EditText) findViewById(R.id.setup_country_name );
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);


        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Blogmy", "profile image clicked");
                // Check for gallery permission
                if (ActivityCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(SetupActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                } else {
                    openGallery();
                }
            }
        });



        // listen to an update to /User/<currentUID> node.
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("profileimage")){
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                Bitmap bitmap;
                try {
                    bitmap = new Compressor(this)
                            .setMaxHeight(300) //Set height and width
                            .setMaxWidth(300)
                            .setQuality(70)
                            .compressToBitmap(new File(resultUri.getPath()));
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("BlogMy: " + e.getMessage());
                    Toast.makeText(SetupActivity.this, "Image cannot be cropped, try again.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SetupActivity.this, "User profile image successfully uploaded", Toast.LENGTH_SHORT).show();

                                    userRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        //  Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        //  startActivity(selfIntent);

                                                        // requires the URI to set to null first if not it will reset back to default
                                                        profileImage.setImageURI(null);
                                                        profileImage.setImageURI(resultUri);
                                                        Toast.makeText(SetupActivity.this, "Your image is stored into Firebase successfuly", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SetupActivity.this, "Image cannot be cropped, try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveAccountSetupInformation(){
        final String user_name = userName.getText().toString();
        String user_full_name = userFullName.getText().toString();
        String user_country = userCountry.getText().toString();

        // Check if current image is default image
        Drawable.ConstantState user_profile_image = profileImage.getDrawable().getConstantState();

        if(TextUtils.isEmpty(user_name)){
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(user_full_name)){
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(user_country)) {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        }
        else if(user_profile_image == getResources().getDrawable(R.drawable.profile).getConstantState()){
            Toast.makeText(this, "Please set your profile picture...", Toast.LENGTH_SHORT).show();

        } else {
            // dont allow same user name
            /*
            Firebase userRef= new Firebase(USERS_LOCATION);
            userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() !== null) {
                        //user exists, do something
                    } else {
                        //user does not exist, do something else
                    }
                }
                @Override
                public void onCancelled(FirebaseError arg0) {
                }
            });
            */
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            final HashMap userMap = new HashMap<>();
            userMap.put("username", user_name);
            userMap.put("fullname", user_full_name);
            userMap.put("country", user_country);
            userMap.put("status", "Feeling good!");
            userMap.put("gender", "none");
            // date of birth
            userMap.put("dob", "1 January 2019");
            userMap.put("relationship", "none");

            userDetailRef.orderByChild("username").equalTo(user_name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        boolean userexisted = false;
                        for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){
                            //Loop 1 to go through all the child nodes of users
                            //for(DataSnapshot booksSnapshot : uniqueKeySnapshot.child("username").g){
                            //loop 2 to go through all the child nodes of books node
                            if(uniqueKeySnapshot.child("username").getValue().toString().equals(user_name)){
                                userexisted = true;
                            }
                            //}
                        }

                        if(userexisted){
                            Toast.makeText(SetupActivity.this, "This username is taken", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        sendUserToMainActivity();
                                        Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                    } else {
                        userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    sendUserToMainActivity();
                                    Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
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
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
