package edu.uncc.cci.mobileapps.hw07_group25;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SelectUserAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<User> userList;

    public SelectUserAdapter(Activity activity, ArrayList<User> userList) {
        this.activity = activity;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_member, null);
        User user = userList.get(i);
        TextView tvMemberName = viewGroup.findViewById(R.id.tv_member_name);
        TextView tvMemberEmail = viewGroup.findViewById(R.id.tv_member_email);
        ImageView ivMemberLayout = viewGroup.findViewById(R.id.iv_member_layout);
        tvMemberName.setText(String.format("%s %s", user.getFname(), user.getLname()));
        tvMemberEmail.setText(user.getId());
        Picasso.get().load(user.getAvatarUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(ivMemberLayout);
        return viewGroup;
    }
}
