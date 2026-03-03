package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CandidateAdapter extends BaseAdapter {

    private final Context context;
    private final String[] candidateNames;
    private final String[] candidateDescs;
    private final int[] candidateImages;
    private final int[] voteCounts;

    public CandidateAdapter(Context context, String[] candidateNames,
                            String[] candidateDescs, int[] candidateImages,
                            int[] voteCounts) {
        this.context = context;
        this.candidateNames = candidateNames;
        this.candidateDescs = candidateDescs;
        this.candidateImages = candidateImages;
        this.voteCounts = voteCounts;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.grid_item_candidate, parent, false);
        }

        ImageView imgCandidate = convertView.findViewById(R.id.imgGridCandidate);
        TextView tvName = convertView.findViewById(R.id.tvGridCandidateName);
        TextView tvDesc = convertView.findViewById(R.id.tvGridCandidateDesc);
        TextView tvVotes = convertView.findViewById(R.id.tvGridVoteCount);

        imgCandidate.setImageResource(candidateImages[position]);
        tvName.setText(candidateNames[position]);
        tvDesc.setText(candidateDescs[position]);
        tvVotes.setText(voteCounts[position] + " votes");

        return convertView;
    }
}