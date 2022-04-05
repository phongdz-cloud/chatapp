package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.chats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.ChatActivity;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Constants;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Extras;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private final Context context;
    private final List<ChatListModel> chatListModelList;

    public ChatListAdapter(Context context, List<ChatListModel> chatListModelList) {
        this.context = context;
        this.chatListModelList = chatListModelList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        ChatListModel chatListModel = chatListModelList.get(position);

        holder.tvFullName.setText(chatListModel.getUserName());
        StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                .child(Constants.IMAGES_FOLDER + "/" + chatListModel.getPhotoName());

        fileRef.getDownloadUrl().addOnCompleteListener(uri -> {
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);
        });

        holder.llChatList.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(Extras.USER_KEY, chatListModel.getUserId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return chatListModelList.size();
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout llChatList;
        private final TextView tvFullName, tvLastMessage, tvLastMessageTime, tvUnreadCount;
        private final ImageView ivProfile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            llChatList = itemView.findViewById(R.id.llChatList);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }
}
