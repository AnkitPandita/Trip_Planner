package edu.uncc.cci.mobileapps.hw07_group25;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Message> messageList;
    FirebaseUser currentUser;

    public ChatAdapter(Activity activity, ArrayList<Message> messageList, FirebaseUser currentUser) {
        this.activity = activity;
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int i) {
        return messageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Message message = messageList.get(i);
        TextView tvBody;
        TextView tvDateTime;
        ImageView ivMessage;
        if (message.getSenderEmail().equals(currentUser.getEmail())) {
            viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_my_message, null);
            tvBody = viewGroup.findViewById(R.id.tv_body);
            tvDateTime = viewGroup.findViewById(R.id.tv_date_time);
            ivMessage = viewGroup.findViewById(R.id.iv_message);
        } else {
            viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_other_message, null);
            TextView tvSender = viewGroup.findViewById(R.id.tv_sender);
            tvBody = viewGroup.findViewById(R.id.tv_body_other);
            tvDateTime = viewGroup.findViewById(R.id.tv_date_time_other);
            ivMessage = viewGroup.findViewById(R.id.iv_message_other);
            tvSender.setText(message.getSenderEmail());
        }
        if (message.isPhoto()) {
            tvBody.setVisibility(View.GONE);
            ivMessage.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getBody())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(ivMessage);
        } else {
            tvBody.setVisibility(View.VISIBLE);
            ivMessage.setVisibility(View.GONE);
            tvBody.setText(message.getBody());
        }
        if (message.getTimestamp() != null) {
            tvDateTime.setText(message.getTimestamp().toDate().toString());
        } else {
            tvDateTime.setText("Just now");
        }
        return viewGroup;
    }
}


