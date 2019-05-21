package com.example.blogmy;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton searchButton;
    private EditText searchInputText;
    private RecyclerView searchResultList;
    private DatabaseReference allUsersDatabaseRef;
    private FirebaseRecyclerOptions<FindFriends> options;
    FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        toolbar = (Toolbar) findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        searchInputText = (EditText) findViewById(R.id.search_box_input);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();
                searchPeopleAndFriends(searchBoxInput);
            }
        });

    }
    // to use recyclerview must has firebase-ui-database
    private void searchPeopleAndFriends(String searchBoxInput) {

        // set the query criteria
        // https://firebase.google.com/docs/database/rest/retrieve-data#range-queries = for \uf8ff
        Query searchPeopleAndFriendsQuery = allUsersDatabaseRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        // set the custom query inside setQuery  method
        options = new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(searchPeopleAndFriendsQuery, FindFriends.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, int i, @NonNull FindFriends findFriends) {
                findFriendsViewHolder.setData(findFriends);
            }

            //this method will create view that will be populated by onBindViewHolder
            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout,parent,false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        searchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    public class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        CircleImageView profileimage;
        TextView fullname, status;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setData(FindFriends friendsViewHolderData){
            fullname.setText(friendsViewHolderData.getFullname());
            status.setText(friendsViewHolderData.getStatus());

            // https://github.com/square/picasso/issues/1291 seems like require runtime permission.
            Picasso.with(FindFriendsActivity.this).load(friendsViewHolderData.getProfileimage()).into(profileimage);
        }
    }

}
