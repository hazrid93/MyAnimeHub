package com.example.blogmy;

import android.media.Image;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;

public class FullscreenCharActivity extends AppCompatActivity {
    private ImageView full_image_char;
    private String charName, charId, charURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_char);

        charName = getIntent().getExtras().get("charName").toString();
        charId = getIntent().getExtras().get("charId").toString();
        charURL = getIntent().getExtras().get("charURL").toString();

        Toolbar toolbar = findViewById(R.id.fullscreen_char_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image Viewer");

        full_image_char = (ImageView) findViewById(R.id.char_image_full);
        Picasso.with(FullscreenCharActivity.this).load(charURL).placeholder(R.drawable.profile).into(full_image_char);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }

}
