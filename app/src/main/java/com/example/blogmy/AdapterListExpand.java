package com.example.blogmy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterListExpand extends RecyclerView.Adapter<AdapterListExpand.OriginalViewHolder> {
    //Firebase stuff
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserID;

    private List<Anime> items;
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Anime obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListExpand(Context context, List<Anime> data) {
        items = data;
        ctx = context;
        //Firebase stuff
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }
    // onCreateViewHolder will use this class
    static class OriginalViewHolder extends RecyclerView.ViewHolder {
        CircleImageView bookmark_exp_image;
        TextView bookmark_exp_name;
        ImageButton bookmark_exp_bt_expand;
        View bookmark_exp_lyt_expand;
        View bookmark_exp_lyt_parent;
        Button deleteButton;

        public OriginalViewHolder(View itemView) {
            super(itemView);

            bookmark_exp_image = (CircleImageView) itemView.findViewById(R.id.bookmark_exp_image);
            bookmark_exp_name = (TextView) itemView.findViewById(R.id.bookmark_exp_name);
            bookmark_exp_bt_expand = (ImageButton) itemView.findViewById(R.id.bookmark_exp_bt_expand);
            bookmark_exp_lyt_expand = (View) itemView.findViewById(R.id.bookmark_exp_lyt_expand);
            bookmark_exp_lyt_parent = (View) itemView.findViewById(R.id.bookmark_exp_lyt_parent);
            deleteButton = (Button) itemView.findViewById(R.id.bookmark_del_button);
        }

        public void setImage(Context ctx, String image){
            Picasso.with(ctx).load(image).placeholder(R.drawable.profile).into(this.bookmark_exp_image);
        }

        public void setName(String name){
            this.bookmark_exp_name.setText(name);
        }
    }

    @Override
    public OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.bookmark_expand, parent, false);
        // vh = new AdapterListExpand.OriginalViewHolder(v);
        OriginalViewHolder vh = new OriginalViewHolder(view);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final OriginalViewHolder holder, final int position) {

            final Anime p = items.get(position);
            holder.setName(p.getName());
            holder.setImage(ctx, p.getImage());

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersRef.child(currentUserID).child("bookmarks").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(p.getId())){
                                usersRef.child(currentUserID).child("bookmarks").child(p.getId()).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });
             holder.bookmark_exp_lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            holder.bookmark_exp_bt_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!p.expanded, v, holder.bookmark_exp_lyt_expand);
                    items.get(position).expanded = show;
                }
            });



            // void recycling view

            if(p.expanded){
                holder.bookmark_exp_lyt_expand.setVisibility(View.VISIBLE);
            } else {
                holder.bookmark_exp_lyt_expand.setVisibility(View.GONE);
            }
            toggleArrow(p.expanded, holder.bookmark_exp_bt_expand, false);


    }



    public boolean toggleLayoutExpand(boolean show, View view, View lyt_expand) {
        toggleArrow(show, view, true);
        if (show) {
            expand(lyt_expand);
        } else {
            collapse(lyt_expand);
        }
        return show;
    }

    // rotate the down arrow
    public boolean toggleArrow(boolean show, View view, boolean delay) {
        if (show) {
            view.animate().setDuration(delay ? 200 : 0).rotation(180);
            return true;
        } else {
            view.animate().setDuration(delay ? 200 : 0).rotation(0);
            return false;
        }
    }

    public void expand(final View v) {
        Animation a = expandAction(v);
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private Animation expandAction(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
        return a;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

}

