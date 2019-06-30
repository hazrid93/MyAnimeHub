package com.example.blogmy;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopCommentsAdapter extends ArrayAdapter<TopComments> {

    public TopCommentsAdapter(Context context, int resource, ArrayList<TopComments> comments) {
        super(context, resource, comments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        TopComments comments = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.top_comments_lists, parent, false);
        }
        // Lookup view for data population
        TextView comment_name = (TextView) convertView.findViewById(R.id.top_comment_name);
        TextView comment_date = (TextView) convertView.findViewById(R.id.top_comment_date);
        TextView comment_content = (TextView) convertView.findViewById(R.id.top_comment_content);
        TextView comment_like = (TextView) convertView.findViewById(R.id.top_comment_likes);

        // set scrollable
        // comment_content.setMovementMethod(new ScrollingMovementMethod());

        CircleImageView comment_image_url = (CircleImageView) convertView.findViewById(R.id.top_comments_img_url);

        // Populate the data into the template view using the data object
        comment_name.setText("@" + comments.getUsername());
        comment_date.setText("Posted on: " + comments.getDate());

        String contentText = comments.getContent();

        contentText = contentText.replace("\n", "\\n");

        comment_content.setText(contentText);
        comment_like.setText("Likes: " + comments.getHelpful_count());
        Picasso.with(getContext()).load(comments.image_url).placeholder(R.drawable.profile).into(comment_image_url);
        // Return the completed view to render on screen
        return convertView;
    }
}
