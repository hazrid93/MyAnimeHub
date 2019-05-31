package com.example.blogmy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private RecyclerView myPostsList;
    private FirebaseRecyclerOptions<Posts> options;
    private FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference userRef, postRef, likesRef;
    String currentUserId;
    boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        displayAllMyPosts();
    }

    private void displayAllMyPosts() {

        Query sortPostsInDescendingOrder = postRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(sortPostsInDescendingOrder, Posts.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder postsViewHolder, int i, @NonNull Posts posts) {
                // get the key of the particular snapshot data at this index
                final String postKey = getRef(i).getKey();
                postsViewHolder.setData(posts);
                postsViewHolder.setLikeButtonStatus(postKey);
                postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });

                postsViewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(likeChecker == true){
                                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                                        likesRef.child(postKey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    } else {

                                        // add data into 'Likes' database using the Post key and add the userUid in there,
                                        // any duplicated userUid will not add another one.
                                        likesRef.child(postKey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                postsViewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", postKey);
                        startActivity(commentsIntent);

                    }
                });
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };
        myPostsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder{
        TextView username,date,time,summary;
        CircleImageView profileimage;
        ImageView postimage;
        DatabaseReference LikesRef;

        int countLikes;
        String currentUserId;

        // likes,comments section
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikes;

        public PostsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            summary = itemView.findViewById(R.id.click_post_description);
            postimage = (ImageView) itemView.findViewById(R.id.click_post_image);
            profileimage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);

            likePostButton = (ImageButton) itemView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
            displayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setData(Posts postViewHolderData){
            username.setText(postViewHolderData.getFullname());
            time.setText(" " + postViewHolderData.getTime());
            date.setText(" "+ postViewHolderData.getDate());
            summary.setText(postViewHolderData.getSummary());

            // https://github.com/square/picasso/issues/1291 seems like require runtime permission.
            Picasso.with(getApplicationContext()).load(postViewHolderData.getProfileimage()).into(profileimage);
            Picasso.with(getApplicationContext()).load(postViewHolderData.getPostimage()).into(postimage);
        }

        public void setLikeButtonStatus(final String postKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
