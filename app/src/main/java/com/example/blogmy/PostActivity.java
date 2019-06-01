package com.example.blogmy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import android.os.Environment;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.GroupedCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.richtexteditor.model.DownloadImageConfig;
import net.dankito.richtexteditor.model.DownloadImageUiSetting;
import net.dankito.utils.android.permissions.IPermissionsService;
import net.dankito.utils.android.permissions.PermissionsService;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity<imageCounter> extends AppCompatActivity {
    private Toolbar toolbar;
    private ProgressDialog loadingBar;
    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postSummary;
    final static int Gallery_Pick = 1;
    String summary;
    private Uri imageUri;
    private StorageReference postImageReference;
    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl;

    // RICH TEXT EDITOR PART
    private RichTextEditor editor;
    private GroupedCommandsEditorToolbar bottomGroupedCommandsToolbar;
    private IPermissionsService permissionsService = new PermissionsService(this);

    private static int imageCounter = 1;

    private long countPosts = 0;

    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // RICH TEXT EDITOR START
        editor = (RichTextEditor) findViewById(R.id.editor);
        editor.setHtml("<p>Hello There!</p>");
        // set editor not clickable, editable
        //editorTextView.setInputEnabled(false);

        // this is needed if you like to insert images so that the user gets asked for permission to access external storage if needed
        // see also onRequestPermissionsResult() below
        editor.setPermissionsService(permissionsService);

        bottomGroupedCommandsToolbar = (GroupedCommandsEditorToolbar) findViewById(R.id.editorToolbar);
        bottomGroupedCommandsToolbar.setEditor(editor);

        // you can adjust predefined toolbars by removing single commands
        // bottomGroupedCommandsToolbar.removeCommandFromGroupedCommandsView(CommandName.TOGGLE_GROUPED_TEXT_STYLES_COMMANDS_VIEW, CommandName.BOLD);
        // bottomGroupedCommandsToolbar.removeSearchView();

        editor.setEditorFontSize(20);
        editor.setPadding((4 * (int) getResources().getDisplayMetrics().density));

        // some properties you also can set on editor
        // editor.setEditorBackgroundColor(Color.YELLOW)
        // editor.setEditorFontColor(Color.MAGENTA)
        // editor.setEditorFontFamily("cursive")

        // show keyboard right at start up
        // editor.focusEditorAndShowKeyboardDelayed()

        // only needed if you allow to automatically download remote images
        editor.setDownloadImageConfig(new DownloadImageConfig(DownloadImageUiSetting.AllowSelectDownloadFolderInCode,
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "downloaded_images")));

        // RICH TEXT EDITOR END

        postImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //selectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postSummary = (EditText) findViewById(R.id.post_summary);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        toolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        /*
        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        */

        
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                summary = postSummary.getText().toString();
                save();
            }
        });
    }

    // RICH TEXT EDITOR START
    @Override
    public void onBackPressed() {
        if(bottomGroupedCommandsToolbar.handlesBackButtonPress() == false) {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void save() {
        editor.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {

            @Override
            public void htmlRetrieved(@NotNull String html) {
                validatePostInfo(html);
            }
        });
    }

    private void saveHtml(String html) {
        // upload images to firebase
        uploadImagesAndSaveHtmlToServer(html, new UploadImageCallback() {
            @Override
            public void onCallback(String value) {
                savePostInformation(value);
            }
        });

    }

    private void uploadImagesAndSaveHtmlToServer(final String html, final UploadImageCallback uploadImageCallback) {
        final Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        Elements images = doc.select("img");
        final int imageSize = images.size();
        // use this to let callback only happen when all image processed
        imageCounter = 1;

        for (final Element imageElement : images) {
            String imageUrl = imageElement.attr("src");
            // uploads this image to your server and returns remote image url (= url of image on your server)
            storeImageToFirebaseStorage(imageUrl, new StoreImageCallback() {
                @Override
                public void onCallback(String value) {
                    if (imageCounter == 1) {
                        downloadUrl = value;
                    }
                    imageElement.attr("src", value);
                    if (imageCounter == imageSize) {
                        uploadImageCallback.onCallback(doc.toString());
                    }
                    imageCounter++;
                }
            });
        }
       // savetoserver(htmlWithRemoteImageUrls); // calls your savetoserver(String) method
    }
    // RICH TEXT EDITOR END

    // need to modify this
    private void validatePostInfo(String html) {
        final Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        Elements images = doc.select("img");
        final int imageSize = images.size();

        // if 8 then description was never filled
        if(html.length()==8){
            Toast.makeText(this, "Please add description for your post", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(summary)){
            Toast.makeText(this, "Please add summary for your post", Toast.LENGTH_SHORT).show();
        } else if(imageSize == 0) {
            Toast.makeText(this, "Please add at least one image for your post", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            saveHtml(html);

        }
    }

    // use this to pass the value that we get from firebase async storage save method
    public interface StoreImageCallback {
        void onCallback(String value);
    }

    public interface UploadImageCallback {
        void onCallback(String value);
    }

    private void storeImageToFirebaseStorage(String imageUrl, final StoreImageCallback myCallback) {
        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + "-" + saveCurrentTime;
        // getLastPathSegment <-- the image name
        // final StorageReference filePath = postImageReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
        final StorageReference filePath = postImageReference.child("Post Images").child(imageUrl.substring(imageUrl.lastIndexOf('/'),imageUrl.lastIndexOf('.')) + "_" +postRandomName + ".jpg");

        InputStream stream;
        try {
            stream = new FileInputStream(imageUrl);
            // save post to firebase storage.
            filePath.putStream(stream).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                myCallback.onCallback(uri.toString());
                                Toast.makeText(PostActivity.this, "Image is uploaded successfully", Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else {
                        String messsage = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error occured: " + messsage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void savePostInformation(final String html) {

        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countPosts = dataSnapshot.getChildrenCount();
                } else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("summary", summary);
                    postMap.put("description", html);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("fullname", userFullName);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("country", countPosts);

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
