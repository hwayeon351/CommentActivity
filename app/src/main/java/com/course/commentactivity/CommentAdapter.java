package com.course.commentactivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<CommentAdapter.CommentItem> data = new ArrayList<>();
    private int  visible_cmt;
    private ArrayList<CommentAdapter.CommentHolder> itemController = new ArrayList<>();

    public static class CommentItem{
        private String date; // 댓글 작성 날짜
        private String comment; // 댓글 내용
        private String user_name; // 유저 닉네임

        public CommentItem(){}
        public CommentItem(String user_name, String date, String comment){
            this.user_name = user_name;
            this.date = date;
            this.comment = comment;
        }

        public String getComment() {
            return comment;
        }

        public String getDate() {
            return date;
        }

        public String getUser_name() {
            return user_name;
        }
    }

    public class CommentHolder extends RecyclerView.ViewHolder{
        private TextView username_tv;
        private TextView date_tv;
        private TextView comment_tv;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            this.username_tv = itemView.findViewById(R.id.username_tv);
            this.comment_tv = itemView.findViewById(R.id.comment_tv);
            this.date_tv = itemView.findViewById(R.id.date_tv);
        }
    }

    public CommentAdapter(ArrayList<CommentAdapter.CommentItem> data){
        super();
        this.data = data;
        this.visible_cmt = 0;
    }

    public int getVisible_cmt() {
        return visible_cmt;
    }

    public void setVisible_cmt(int visible_cmt) {
        this.visible_cmt = visible_cmt;
    }

    public int getDataSize(){
        return this.data.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.comment_item, parent, false);
        CommentAdapter.CommentHolder holder = new CommentAdapter.CommentHolder(view);
        itemController.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("viewHolder", "position " + position + " data size = " + data.size());
        CommentItem item = data.get(position);
        CommentAdapter.CommentHolder itemController = (CommentAdapter.CommentHolder) holder;
        itemController.date_tv.setText(item.getDate().split(" ")[0]);
        itemController.comment_tv.setText(item.getComment());
        itemController.username_tv.setText(item.getUser_name());
    }

    @Override
    public int getItemCount() {
        return visible_cmt;
    }
}
