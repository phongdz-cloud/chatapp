package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Constants;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Extras;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.NodeNames;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Util;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivSend;
    private EditText etMessage;
    private DatabaseReference mRootRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId, chatUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ivSend = findViewById(R.id.ivSend);
        etMessage = findViewById(R.id.etMessage);

        ivSend.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        if (getIntent().hasExtra(Extras.USER_KEY)) {
            chatUserId = getIntent().getStringExtra(Extras.USER_KEY);
        }

    }

    private void sendMessage(String msg, String msgType, String pushId) {
        try {
            if (!msg.equals("")) {
                HashMap messageMap = new HashMap();
                messageMap.put(NodeNames.MESSAGE_ID, pushId);
                messageMap.put(NodeNames.MESSAGE, msg);
                messageMap.put(NodeNames.MESSAGE_TYPE, msgType);
                messageMap.put(NodeNames.MESSAGE_FROM, currentUserId);
                messageMap.put(NodeNames.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId;

                HashMap messageUserMap = new HashMap();
                messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                etMessage.setText("");


                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_send_message, error.getMessage()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Toast.makeText(this, getString(R.string.failed_to_send_message, ex.getMessage()), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivSend:
                if (Util.connectionAvailable(this)) {
                    DatabaseReference userMessagePush = mRootRef.child(NodeNames.MESSAGES)
                            .child(currentUserId).child(chatUserId).push();
                    String pushId = userMessagePush.getKey();
                    sendMessage(etMessage.getText().toString().trim(), Constants.MESSAGE_TYPE_TEXT, pushId);
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}