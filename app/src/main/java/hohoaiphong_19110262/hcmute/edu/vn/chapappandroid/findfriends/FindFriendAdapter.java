package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.findfriends;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Constants;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.NodeNames;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {
    private Context context;
    private List<FindFriendModel> findFriendModelList;

    private DatabaseReference friendRequestDatabase;
    private FirebaseUser currentUser;
    private String userId;

    public FindFriendAdapter(Context context, List<FindFriendModel> findFriendModelList) {
        this.context = context;
        this.findFriendModelList = findFriendModelList;
    }

    @NonNull
    @Override
    public FindFriendAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout, parent, false);

        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendAdapter.FindFriendViewHolder holder, int position) {
        FindFriendModel findFriendModel = findFriendModelList.get(position);


        holder.tvFullName.setText(findFriendModel.getUserName());
        StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                .child(Constants.IMAGES_FOLDER + "/" + findFriendModel.getPhotoName());

        fileRef.getDownloadUrl().addOnCompleteListener(uri -> {
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);
        });

        friendRequestDatabase = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (findFriendModel.isRequestSend()) {
            holder.btnSendRequest.setVisibility(View.GONE);
            holder.btnCancelRequest.setVisibility(View.VISIBLE);
        } else {
            holder.btnSendRequest.setVisibility(View.VISIBLE);
            holder.btnCancelRequest.setVisibility(View.GONE);
        }

        holder.btnSendRequest.setOnClickListener(view -> {
            holder.btnSendRequest.setVisibility(View.GONE);
            holder.pbRequest.setVisibility(View.VISIBLE);

            userId = findFriendModel.getUserId();

            friendRequestDatabase.child(currentUser.getUid())
                    .child(userId).child(NodeNames
                    .REQUEST_TYPE)
                    .setValue(Constants.REQUEST_STATUS_SENT)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            friendRequestDatabase.child(userId)
                                    .child(currentUser.getUid()).child(NodeNames
                                    .REQUEST_TYPE).setValue(Constants.REQUEST_STATUS_RECEIVED)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(context, R.string.request_sent_successfully, Toast.LENGTH_SHORT).show();
                                            holder.btnSendRequest.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(context,
                                                    context.getString(R.string.failed_to_send_request
                                                            , task2.getException())
                                                    , Toast.LENGTH_SHORT).show();
                                            holder.btnSendRequest.setVisibility(View.VISIBLE);
                                        }
                                        holder.pbRequest.setVisibility(View.GONE);
                                        holder.btnCancelRequest.setVisibility(View.VISIBLE);
                                    });
                        } else {
                            Toast.makeText(context,
                                    context.getString(R.string.failed_to_send_request
                                            , task.getException())
                                    , Toast.LENGTH_SHORT).show();
                            holder.btnSendRequest.setVisibility(View.VISIBLE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.btnCancelRequest.setVisibility(View.GONE);
                        }
                    });


        });

        holder.btnCancelRequest.setOnClickListener(view -> {

            holder.btnCancelRequest.setVisibility(View.GONE);
            holder.pbRequest.setVisibility(View.VISIBLE);

            userId = findFriendModel.getUserId();

            friendRequestDatabase.child(currentUser.getUid())
                    .child(userId).child(NodeNames
                    .REQUEST_TYPE)
                    .setValue(null)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            friendRequestDatabase.child(userId)
                                    .child(currentUser.getUid()).child(NodeNames
                                    .REQUEST_TYPE).setValue(null)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(context, R.string.request_cancelled_successfully, Toast.LENGTH_SHORT).show();
                                            holder.btnSendRequest.setVisibility(View.VISIBLE);
                                            holder.btnCancelRequest.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(context,
                                                    context.getString(R.string.failed_to_cancelled_request
                                                            , task2.getException())
                                                    , Toast.LENGTH_SHORT).show();
                                            holder.btnSendRequest.setVisibility(View.GONE);
                                            holder.btnCancelRequest.setVisibility(View.VISIBLE);
                                        }
                                        holder.pbRequest.setVisibility(View.GONE);

                                    });
                        } else {
                            Toast.makeText(context,
                                    context.getString(R.string.failed_to_cancelled_request
                                            , task.getException())
                                    , Toast.LENGTH_SHORT).show();
                            holder.btnSendRequest.setVisibility(View.GONE);
                            holder.pbRequest.setVisibility(View.GONE);
                            holder.btnCancelRequest.setVisibility(View.VISIBLE);
                        }
                    });


        });
    }

    @Override
    public int getItemCount() {
        return findFriendModelList.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfile;
        private TextView tvFullName;
        private Button btnSendRequest, btnCancelRequest;
        private ProgressBar pbRequest;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            pbRequest = itemView.findViewById(R.id.pbRequest);
        }
    }
}
