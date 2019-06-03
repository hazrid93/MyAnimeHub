package com.example.blogmy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StatsFragment extends Fragment {
    private TextView textView;
    private String watching;
    private String completed;
    private String on_hold;
    private String dropped;
    private String plan_to_watch;
    private String total;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            watching = bundle.getString("watching");
            completed = bundle.getString("completed");
            on_hold = bundle.getString("on_hold");
            dropped = bundle.getString("dropped");
            plan_to_watch = bundle.getString("plan_to_watch");
            total = bundle.getString("total");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stats_fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = (TextView) view.findViewById(R.id.stats_textView);

        textView.setText(watching + ", " + completed + ", " + total);
    }




}