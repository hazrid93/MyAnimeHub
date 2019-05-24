package com.example.blogmy;

import android.content.Context;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerOptions<Friends> options;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter;
    private String online_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        myFriendList = (RecyclerView) findViewById(R.id.friends_lists);
        myFriendList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        
        displayAllFriends();
    }

    private void displayAllFriends() {
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendsRef, Friends.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {
                friendsViewHolder.setData(friends);

                final String usersIDs = getRef(i).getKey();

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String username = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            friendsViewHolder.setFullName(username);
                            friendsViewHolder.setProfileImage(getApplicationContext(), profileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout,parent,false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView fullname, date;
        CircleImageView profileimage;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            fullname = (TextView) itemView.findViewById(R.id.all_users_profile_full_name);
            date = (TextView) itemView.findViewById(R.id.all_users_status);
            profileimage = (CircleImageView) itemView.findViewById(R.id.all_users_profile_image);
        }

        public void setData(Friends friendsViewHolderData){
            this.date.setText("Friend since: " + friendsViewHolderData.getDate());
        }

        public void setFullName(String fullName){
            this.fullname.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileimage){
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(this.profileimage);
        }
    }

}
