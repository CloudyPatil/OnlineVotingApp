package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CandidateAdapter extends BaseAdapter {

    // Data
    private final Context context;
    private final String[] candidateNames;
    private final String[] candidateDescs;
    private final int[] candidateImages;

    // Constructor
    public CandidateAdapter(Context context, String[] candidateNames,
                            String[] candidateDescs, int[] candidateImages) {
        this.context = context;
        this.candidateNames = candidateNames;
        this.candidateDescs = candidateDescs;
        this.candidateImages = candidateImages;
    }

    @Override
    public int getCount() {
        return candidateNames.length;
    }

    @Override
    public Object getItem(int position) {
        return candidateNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create view if not recycled
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_item_candidate, parent, false);
        }

        // Find views inside grid_item_candidate.xml
        ImageView imgCandidate = convertView.findViewById(R.id.imgGridCandidate);
        TextView tvName = convertView.findViewById(R.id.tvGridCandidateName);
        TextView tvDesc = convertView.findViewById(R.id.tvGridCandidateDesc);

        // Set data for this position
        imgCandidate.setImageResource(candidateImages[position]);
        tvName.setText(candidateNames[position]);
        tvDesc.setText(candidateDescs[position]);

        return convertView;
    }
}