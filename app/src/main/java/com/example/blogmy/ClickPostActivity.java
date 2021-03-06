package com.example.blogmy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.GroupedCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.utils.android.permissions.IPermissionsService;
import net.dankito.utils.android.permissions.PermissionsService;

import org.jetbrains.annotations.NotNull;

public class ClickPostActivity extends AppCompatActivity {

    private TextView postDescription;
    private Button deletePostButton, editPostButton;
    private String postKey, currentUserID, databaseUserID, description, image;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    // RICH TEXT EDITOR
    private RichTextEditor editorView;
    private GroupedCommandsEditorToolbar bottomGroupedCommandsToolbar;
    private IPermissionsService permissionsService = new PermissionsService(this);
   // private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        //MobileAds.initialize(this, "ca-app-pub-5012041161594376~1643148417");
        //mAdView = findViewById(R.id.clickpostactivity_adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        postKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        mToolbar = (Toolbar) findViewById(R.id.click_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Post Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Rich Text Editor
        editorView = (RichTextEditor) findViewById(R.id.click_editor_view);
        editorView.setInputEnabled(false);
        editorView.enterViewingMode();

        postDescription = (TextView) findViewById(R.id.click_post_description);
        editPostButton = (Button) findViewById(R.id.edit_post_button);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);

        editPostButton.setVisibility(View.GONE);
        deletePostButton.setVisibility(View.GONE);


        // get current user id
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    // check id of user for the post (creator)
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();
                    editorView.setHtml(description);

                    if (currentUserID.equals(databaseUserID)) {
                        editPostButton.setVisibility(View.VISIBLE);
                        deletePostButton.setVisibility(View.VISIBLE);
                    } else {
                        editPostButton.setVisibility(View.GONE);
                        deletePostButton.setVisibility(View.GONE);
                    }

                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(currentUserID.equals(databaseUserID)) {
                                editCurrentPost(description);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // delete post button implementation
        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserID.equals(databaseUserID)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);

                    builder.setTitle("Confirm");
                    builder.setMessage("Are you sure?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            deleteCurrentPost();
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });
    }

    private void editCurrentPost(String description) {

        // use new activity for this
        Intent editPostIntent = new Intent(ClickPostActivity.this,EditPostActivity.class);
        editPostIntent.putExtra("PostKey", postKey);
        startActivity(editPostIntent);
        // put EditText for editing post inside the alert dialog
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        // place where user edit his post
        final RichTextEditor inputField = new RichTextEditor(ClickPostActivity.this);
        inputField.setHtml(description);
        builder.setView(inputField);
        // dialoginterface for alertdialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.toString());
                Toast.makeText(ClickPostActivity.this, "Post has been updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // cancel the alert dialog popup
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        // R.XX.XX <-- for resources locally
        // android.R.XX.XX <-- for android default resources
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
        */
    }

    private void deleteCurrentPost() {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }


}
