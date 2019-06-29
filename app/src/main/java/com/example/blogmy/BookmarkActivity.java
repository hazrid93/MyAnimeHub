package com.example.blogmy;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView bookmarkList;
    private View parent_view;
    private AdapterListExpand mAdapter;

    //Firebase stuff
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserID;
    private final List<Anime> animeLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //Firebase stuff
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        fetchBookmarkAnimes();


        mToolbar = findViewById(R.id.bookmark_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bookmarks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookmarkList = (RecyclerView) findViewById(R.id.bookmark_recyclerview);
        bookmarkList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BookmarkActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        bookmarkList.setLayoutManager(linearLayoutManager);
        bookmarkList.addItemDecoration(new DividerItemDecoration(bookmarkList.getContext(), DividerItemDecoration.VERTICAL));

        initComponent();
       // parent_view = findViewById(android.R.id.content);

    }


    private void fetchBookmarkAnimes() {
        usersRef.child(currentUserID).child("bookmarks").orderByChild("counter").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Anime animeData = dataSnapshot.getValue(Anime.class);
                    animeLists.add(animeData);
                    // update the RecyclerView list
                    mAdapter.notifyDataSetChanged();
                } else {

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Anime animeData = dataSnapshot.getValue(Anime.class);
                int indexId = -1;
                for(Anime id: animeLists){
                    if(animeData.getId().equals(id.getId())){
                        indexId = animeLists.indexOf(id);
                        break;

                    }
                }
                if(indexId != -1) {
                    animeLists.remove(indexId);
                    // update the RecyclerView list
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

        private void initComponent() {
        //set data and list adapter
        mAdapter = new AdapterListExpand(BookmarkActivity.this, animeLists);
        bookmarkList.setAdapter(mAdapter);
        // on item list clicked

        mAdapter.setOnItemClickListener(new AdapterListExpand.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Anime obj, int position) {
                //Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
                //System.out.println("CLICKER: " + messages.getMal_id());
                Intent searchAnimeIntent = new Intent(BookmarkActivity.this, ClickSearchAnime.class);
                searchAnimeIntent.putExtra("anime_id", obj.getId());
                startActivity(searchAnimeIntent);
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }

}
