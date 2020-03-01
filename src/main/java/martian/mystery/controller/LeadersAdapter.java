package martian.mystery.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import martian.mystery.R;

public class LeadersAdapter extends RecyclerView.Adapter<LeadersAdapter.ViewHolder> {

    private ArrayList<String> playersNames = new ArrayList<>();
    private ArrayList<String> levels = new ArrayList<>();
    private Context context;

    private static final String TAG = "RecyclerViewAdapter";

    public LeadersAdapter(Context context, ArrayList<String> playersNames, ArrayList<String> levels) {
        this.playersNames = playersNames;
        this.levels = levels;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        holder.tvNamePlayer.setText(playersNames.get(position));
        holder.tvLevel.setText(levels.get(position));
        if(position == 0) {
            holder.underLine.setBackgroundColor(context.getResources().getColor(R.color.leader_line));
        }
        setFadeAnimation(holder.itemView);
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return playersNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNamePlayer;
        TextView tvLevel;
        View underLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamePlayer = itemView.findViewById(R.id.tvNameLeader);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            underLine = itemView.findViewById(R.id.underLine);
        }
    }
}
