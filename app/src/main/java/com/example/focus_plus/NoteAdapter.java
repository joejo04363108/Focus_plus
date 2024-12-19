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

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem note = noteList.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.typeTextView.setText(note.getType());
        holder.dateTextView.setText(note.getDateTime());
        holder.contentTextView.setText(note.getContent());
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
