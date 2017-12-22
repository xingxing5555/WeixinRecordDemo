package com.example.admin.weixinrecorddemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.weixinrecorddemo.R;
import com.example.admin.weixinrecorddemo.audio.AudioManager;

import java.util.List;

/**
 * @author Xinxin Shi
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private List<String> fileList;
    private LayoutInflater inflater;

    public RecordAdapter(List<String> fileList, Context context) {
        this.fileList = fileList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_left_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AudioManager audioManager = AudioManager.getInstance();
        final String url = fileList.get(position);
        holder.tvTime.setText(audioManager.getTime(url) / 1000 + "秒");
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.startPlaying(url);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList == null ? 0 : fileList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private Button btnPlay;
        private TextView tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            btnPlay = itemView.findViewById(R.id.btn_play);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
