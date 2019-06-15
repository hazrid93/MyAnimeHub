package com.example.blogmy;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView bookmarkList;
    private View parent_view;
    private AdapterListExpand mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("AZAD: IM HERE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        mToolbar = findViewById(R.id.bookmark_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bookmarks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bookmarkList = (RecyclerView) findViewById(R.id.bookmark_recyclerview);
        bookmarkList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BookmarkActivity.this);
       // linearLayoutManager.setReverseLayout(true);
       // linearLayoutManager.setStackFromEnd(true);
        bookmarkList.setLayoutManager(linearLayoutManager);

      //  parent_view = findViewById(android.R.id.content);
        initComponent();
    }


    private void initComponent() {
     //   bookmarkList.addItemDecoration(new DividerItemDecoration(bookmarkList.getContext(), DividerItemDecoration.VERTICAL));

        List<Anime> itemsData = new ArrayList<Anime>();
        itemsData.add(new Anime("https://www.staples-3p.com/s7/is/image/Staples/m004542954_sc7","test"));
        //itemsData.add(new Anime("https://www.staples-3p.com/s7/is/image/Staples/m004542954_sc7","test2"));
        //itemsData.add(new Anime("https://www.staples-3p.com/s7/is/image/Staples/m004542954_sc7","test2"));
        //List<Anime> items = itemsData;

        //set data and list adapter
        mAdapter = new AdapterListExpand(BookmarkActivity.this, itemsData);
        bookmarkList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // on item list clicked
        /*
        mAdapter.setOnItemClickListener(new AdapterListExpand.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Anime obj, int position) {
             //   Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });
        */
    }

}
