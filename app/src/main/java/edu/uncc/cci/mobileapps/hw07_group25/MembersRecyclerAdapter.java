package edu.uncc.cci.mobileapps.hw07_group25;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MembersRecyclerAdapter extends RecyclerView.Adapter<MembersRecyclerAdapter.ViewHolder> {
    private Activity activity;
    private ArrayList<User> userList;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String tripId;
    boolean isAdmin;

    public MembersRecyclerAdapter(Activity activity, ArrayList<User> userList, FirebaseFirestore db, FirebaseUser currentUser, String tripId, boolean isAdmin) {
        this.activity = activity;
        this.userList = userList;
        this.db = db;
        this.currentUser = currentUser;
        this.tripId = tripId;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.user = user;
        holder.tvMemberName.setText(String.format("%s %s", user.getFname(), user.getLname()));
        holder.tvMemberEmail.setText(user.getId());
        Picasso.get().load(user.getAvatarUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(holder.avatarMember);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarMember;
        TextView tvMemberName;
        TextView tvMemberEmail;
        User user;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarMember = itemView.findViewById(R.id.iv_member_layout);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberEmail = itemView.findViewById(R.id.tv_member_email);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (isAdmin && !user.getId().equals(currentUser.getEmail())) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                        alert.setTitle("Remove member");
                        alert.setMessage("Do you want to remove " + user.getFname() + " " + user.getLname() + "?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection(CreateTripActivity.TRIP_COLLECTION)
                                        .document(tripId)
                                        .update("users", FieldValue.arrayRemove(user.getId()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(activity, "Participant removed!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(activity, e + "", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        alert.show();
                    }
                    return false;
                }
            });
        }
    }
}


