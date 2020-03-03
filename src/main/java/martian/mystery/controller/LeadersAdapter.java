package martian.mystery.controller;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
    private ArrayList<Integer> countPlayers = new ArrayList<>();
    private Context context;

    private static final String TAG = "RecyclerViewAdapter";

    public LeadersAdapter(Context context, ArrayList<String> playersNames, ArrayList<String> levels, ArrayList<Integer> countPlayers) {
        this.playersNames = playersNames;
        this.levels = levels;
        this.countPlayers = countPlayers;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item,parent,false);
        ViewHolder holder = new ViewHolder(view);*/
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called with position = " + position);

        if(position == 0) {
            Log.d(TAG, "onBindViewHolder: change underline");
            holder.underLine.setBackgroundColor(context.getResources().getColor(R.color.leader_line));
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.leader_line));
            holder.tvLevel.setAlpha(1);
        } else if(position == 1) {
            holder.underLine.setBackgroundColor(context.getResources().getColor(R.color.second_line));
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.second_line));
        } else if(position == 2) {
            holder.underLine.setBackgroundColor(context.getResources().getColor(R.color.third_line));
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.third_line));
        } else {
            holder.underLine.setBackgroundColor(context.getResources().getColor(R.color.player_line));
            holder.tvNumber.setTextColor(context.getResources().getColor(R.color.player_line));
        }
        if(countPlayers.get(position) == 1) {
            holder.tvNamePlayer.setText(playersNames.get(position));
        } else if(countPlayers.get(position) > 1){
            holder.tvNamePlayer.setText(holder.spanText(playersNames.get(position) + " и еще " + (countPlayers.get(position) - 1) + " чел."));
        }
        holder.tvNumber.setText((position+1) + ".");
        holder.tvLevel.setText(levels.get(position) + " ур.");
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

        TextView tvNumber;
        TextView tvNamePlayer;
        TextView tvLevel;
        View underLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            /*tvNumber = itemView.findViewById(R.id.tvNumber);
            tvNamePlayer = itemView.findViewById(R.id.tvNameLeader);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            underLine = itemView.findViewById(R.id.underLine);*/
        }

        public Spannable spanText(String str) {
            Spannable spans = new SpannableString(str);
            spans.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.count_players)), str.lastIndexOf('и'), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spans;
        }
    }
}
