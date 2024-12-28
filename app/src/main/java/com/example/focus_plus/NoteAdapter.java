package com.example.focus_plus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<NoteItem> noteList;

    public NoteAdapter(List<NoteItem> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    public void updateNotes(List<NoteItem> newNotes) {
        noteList.clear();
        noteList.addAll(newNotes);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem note = noteList.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.typeTextView.setText(note.getType());
        holder.dateTextView.setText(note.getDateTime());
        holder.contentTextView.setText(note.getContent());

        // 長按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(position); // 傳遞長按位置
            }
            return true;
        });
    }

    // 新增長按監聽器接口
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, typeTextView, dateTextView, contentTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            typeTextView = itemView.findViewById(R.id.note_type);
            dateTextView = itemView.findViewById(R.id.note_date_time);
            contentTextView = itemView.findViewById(R.id.note_content);
        }
    }
}
