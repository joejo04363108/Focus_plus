package com.example.focus_plus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<NoteItem> noteList;

    public NoteAdapter(List<NoteItem> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 載入單項目布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // 綁定資料到視圖
        NoteItem note = noteList.get(position);
        holder.titleText.setText(note.getTitle());
        holder.typeText.setText(note.getType());
        holder.dateTimeText.setText(note.getDateTime());
        holder.contentText.setText(note.getContent());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    // 自定義 ViewHolder 類別
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, typeText, dateTimeText, contentText;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.note_title);
            typeText = itemView.findViewById(R.id.note_type);
            dateTimeText = itemView.findViewById(R.id.note_date_time);
            contentText = itemView.findViewById(R.id.note_content);
        }
    }
}
