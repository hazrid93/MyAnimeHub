package com.example.blogmy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;
import net.dankito.richtexteditor.android.toolbar.GroupedCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.richtexteditor.model.DownloadImageConfig;
import net.dankito.richtexteditor.model.DownloadImageUiSetting;
import net.dankito.utils.android.permissions.IPermissionsService;
import net.dankito.utils.android.permissions.PermissionsService;
import net.dankito.utils.io.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import id.zelory.compressor.Compressor;

public class EditPostActivity extends AppCompatActivity {
    private String postKey, current_user_id, description, databaseUserID, summary, downloadUrl, saveCurrentDate, saveCurrentTime, postRandomName;
    private DatabaseReference clickPostRef, postRef, usersRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private Button updatePostButton;
    private EditText postSummary;
    private ProgressDialog loadingBar;
    private StorageReference postImageReference;

    // RICH TEXT EDITOR
    private RichTextEditor editorEdit;
    private AllCommandsEditorToolbar bottomGroupedCommandsToolbarEdit;
    private IPermissionsService permissionsService = new PermissionsService(this);

    private static int imageCounter = 1;
    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mToolbar = (Toolbar) findViewById(R.id.edit_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Post Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updatePostButton = (Button) findViewById(R.id.edit_activity_post_button);
        //postSummary = (EditText) findViewById(R.id.edit_summary);
        loadingBar = new ProgressDialog(this);

        editorEdit = (RichTextEditor) findViewById(R.id.editorPost);

        // set editor not clickable, editable
        //editorTextView.setInputEnabled(false);

        // this is needed if you like to insert images so that the user gets asked for permission to access external storage if needed
        // see also onRequestPermissionsResult() below
        editorEdit.setPermissionsService(permissionsService);

        bottomGroupedCommandsToolbarEdit = (AllCommandsEditorToolbar) findViewById(R.id.editorPostToolbar);
        bottomGroupedCommandsToolbarEdit.setEditor(editorEdit);

        // you can adjust predefined toolbars by removing single commands
        // bottomGroupedCommandsToolbar.removeCommandFromGroupedCommandsView(CommandName.TOGGLE_GROUPED_TEXT_STYLES_COMMANDS_VIEW, CommandName.BOLD);
        // bottomGroupedCommandsToolbar.removeSearchView();

        editorEdit.setEditorFontSize(20);
        editorEdit.setPadding((4 * (int) getResources().getDisplayMetrics().density));

        // some properties you also can set on editor
        // editorEdit.setEditorBackgroundColor(Color.YELLOW);
        // editor.setEditorFontColor(Color.MAGENTA)
        // editor.setEditorFontFamily("cursive")

        // show keyboard right at start up
        // editorEdit.focusEditorAndShowKeyboardDelayed();

        // only needed if you allow to automatically download remote images
        editorEdit.setDownloadImageConfig(new DownloadImageConfig(DownloadImageUiSetting.AllowSelectDownloadFolderInCode,
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "downloaded_images")));

        editorEdit.enterEditingMode();

        // RICH TEXT EDITOR END

        postKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // get current user id
        postImageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();


        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    description = dataSnapshot.child("description").getValue().toString();
                    editorEdit.setHtml(description);
                    //postSummary.requestFocus();

                    // check id of user for the post (creator)
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();
                    downloadUrl = dataSnapshot.child("postimage").getValue().toString();
                   // postSummary.setText(dataSnapshot.child("summary").getValue().toString());
                    summary = dataSnapshot.child("summary").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //summary = postSummary.getText().toString();
                // inflate the layout view on alert dialog
                View view = getLayoutInflater().inflate(R.layout.dialog_post,editorEdit,false);
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
                builder.setTitle("Post Summary");

                // Set up the input
                final EditText input = (EditText) view.findViewById(R.id.dialog_summary);
                input.setText(summary);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(view);

                // dialoginterface for alertdialog
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        summary = input.getText().toString();
                        save();
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
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }
        });

    }

    // RICH TEXT EDITOR START
    @Override
    public void onBackPressed() {
        if(bottomGroupedCommandsToolbarEdit.handlesBackButtonPress() == false) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void save() {
        editorEdit.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {

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

        if(imageSize==0){
            // set to none to not display anything
            downloadUrl = "none";
            uploadImageCallback.onCallback(doc.toString());
            return;
        }

        for (final Element imageElement : images) {
            String imageUrl = imageElement.attr("src");
            if(imageUrl.startsWith("https://firebasestorage.googleapis.com")){
                if(imageCounter==imageSize){
                    uploadImageCallback.onCallback(doc.toString());
                }
                imageCounter++;
                continue;
            }
            // uploads this image to your server and returns remote image url (= url of image on your server)
            storeImageToFirebaseStorage(imageUrl, new StoreImageCallback() {
                @Override
                public void onCallback(String value) {
                    if(imageCounter==1){
                        downloadUrl = value;
                    }
                    imageElement.attr("src", value);
                    if(imageCounter==imageSize){
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
        } else {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            saveHtml(html);
        }
        /*
        else if(imageSize == 0) {
            Toast.makeText(this, "Please add at least one image for your post", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            saveHtml(html);
        }*/
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

        int indexdot = imageUrl.lastIndexOf('.');
        String imageUrlCut = imageUrl.substring(0,indexdot);
        // note if image URL has anything else after .png for example the picasso load wont work
        final StorageReference filePath = postImageReference.child("Post Images").child(imageUrl.substring(imageUrlCut.lastIndexOf('/'),imageUrl.lastIndexOf('.')) + "_" +postRandomName + ".jpg");
        //InputStream stream;
        try {
            /*
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            stream = connection.getInputStream(); */
          //  final Bitmap bitmapLoad;
          //  bitmapLoad = Picasso.with(EditPostActivity.this).load(imageUrl).resize(200,200).centerInside().get();

            // reference to target need to be kept to avoid being garbage collected
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                    final byte[] fileBytes = baos.toByteArray();

                    // save post to firebase storage.
                    filePath.putBytes(fileBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        myCallback.onCallback(uri.toString());
                                        Toast.makeText(EditPostActivity.this, "Image is uploaded successfully", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            } else {
                                String messsage = task.getException().getMessage();
                                Toast.makeText(EditPostActivity.this, "Error occured: " + messsage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            Picasso.with(EditPostActivity.this).load(imageUrl).into(target);
            //final Uri resultUri = result.getUri();
                    /*
            Bitmap bitmap;
            bitmap = new Compressor(this)
                    .setMaxHeight(300) //Set height and width
                    .setMaxWidth(300)
                    .setQuality(70)
                    .compressToBitmap(BitmapFactory.decodeFileDescriptor(stream.g));*/


        } catch(Exception e){
            e.printStackTrace();
        }


    }

    /* custom transformation for picasso
    public class CropSquareTransformation implements Transformation {
        @Override public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override public String key() { return "square()"; }
    }
    */
    private void savePostInformation(final String html) {
        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        /*
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
        */

        usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String ts = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("summary", summary);
                    postMap.put("description", html);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("fullname", userFullName);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("counter", Integer.valueOf(ts));

                    clickPostRef.updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        sendUserToMainActivity();
                                        Toast.makeText(EditPostActivity.this, "Post is updated succesfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String messsage = task.getException().getMessage();
                                        Toast.makeText(EditPostActivity.this, "Error occured: " + messsage, Toast.LENGTH_SHORT).show();
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

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(EditPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
