package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.requests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Constants;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.NodeNames;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private final Context context;
    private final List<RequestModel> requestModelList;
    private DatabaseReference databaseReferenceFriendRequests, databaseReferenceChats;
    private FirebaseUser currentUser;

    public RequestAdapter(Context context, List<RequestModel> requestModelList) {
        this.context = context;
        this.requestModelList = requestModelList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        RequestModel requestModel = requestModelList.get(position);

        holder.tvFullName.setText(requestModel.getUserName());
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER + "/" + requestModel.getPhotoName());
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);
        });

        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.btnAcceptRequest.setOnClickListener(view -> {
            holder.pbDecision.setVisibility(View.VISIBLE);
            holder.btnDenyRequest.setVisibility(View.GONE);
            holder.btnAcceptRequest.setVisibility(View.GONE);

            final String userId = requestModel.getUserId();

            databaseReferenceChats.child(currentUser.getUid()).child(userId)
                    .child(NodeNames.TIME_STAMP)
                    .setValue(ServerValue.TIMESTAMP)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            databaseReferenceChats.child(userId).child(currentUser.getUid())
                                    .child(NodeNames.TIME_STAMP)
                                    .setValue(ServerValue.TIMESTAMP)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            databaseReferenceFriendRequests.child(currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                                                    .setValue(Constants.REQUEST_STATUS_ACCEPTED)
                                                    .addOnCompleteListener(task3 -> {
                                                        if (task3.isSuccessful()) {
                                                            databaseReferenceFriendRequests.child(userId).child(currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                                                    .setValue(Constants.REQUEST_STATUS_ACCEPTED)
                                                                    .addOnCompleteListener(task4 -> {
                                                                        if (task4.isSuccessful()) {
                                                                            holder.pbDecision.setVisibility(View.GONE);
                                                                            holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                                                            holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                                                        } else {
                                                                            handleException(holder, task4.getException());
                                                                        }
                                                                    });
                                                        } else {
                                                            handleException(holder, task3.getException());
                                                        }
                                                    });


                                        } else {
                                            handleException(holder, task2.getException());
                                        }
                                    });


                        } else {
                            handleException(holder, task.getException());
                        }

                    });


        });

        holder.btnDenyRequest.setOnClickListener(view -> {
            holder.pbDecision.setVisibility(View.VISIBLE);
            holder.btnDenyRequest.setVisibility(View.GONE);
            holder.btnAcceptRequest.setVisibility(View.GONE);

            final String userId = requestModel.getUserId();

            databaseReferenceFriendRequests.child(currentUser.getUid()).child(userId)
                    .child(NodeNames.REQUEST_TYPE).setValue(null)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            databaseReferenceFriendRequests.child(userId).child(currentUser.getUid())
                                    .child(NodeNames.REQUEST_TYPE).setValue(null)
                                    .addOnCompleteListener(task2 -> {
                                        if (!task2.isSuccessful()) {
                                            Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task2.getException())
                                                    , Toast.LENGTH_SHORT).show();
                                        }
                                        holder.pbDecision.setVisibility(View.GONE);
                                        holder.btnDenyRequest.setVisibility(View.VISIBLE);
                                        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                                    });
                        } else {
                            Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException())
                                    , Toast.LENGTH_SHORT).show();
                            holder.pbDecision.setVisibility(View.GONE);
                            holder.btnDenyRequest.setVisibility(View.VISIBLE);
                            holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                        }
                    });

        });
    }

    private void handleException(@NonNull RequestAdapter.RequestViewHolder holder, Exception exception) {
        Toast.makeText(context, context.getString(R.string.failed_to_accept_request, exception), Toast.LENGTH_SHORT).show();
        holder.pbDecision.setVisibility(View.GONE);
        holder.btnDenyRequest.setVisibility(View.VISIBLE);
        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return requestModelList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvFullName;
        private final ImageView ivProfile;
        private final Button btnAcceptRequest, btnDenyRequest;
        private final ProgressBar pbDecision;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvfullName);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnDenyRequest = itemView.findViewById(R.id.btnDenyRequest);
            pbDecision = itemView.findViewById(R.id.pbDecision);
        }
    }
}
