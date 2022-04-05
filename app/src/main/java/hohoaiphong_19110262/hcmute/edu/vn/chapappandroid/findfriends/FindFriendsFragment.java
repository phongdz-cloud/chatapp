package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.findfriends;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.Constants;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.Common.NodeNames;
import hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.R;

public class FindFriendsFragment extends Fragment {
    private FindFriendAdapter findFriendAdapter;
    private List<FindFriendModel> findFriendModelList;
    private TextView tvEmptyFriendsList;

    private DatabaseReference databaseReferenceFriendRequests;
    private FirebaseUser currentUser;
    private View progressBar;


    public FindFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFindFriends = view.findViewById(R.id.rvFindFriends);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyFriendsList = view.findViewById(R.id.tvEmptyFriendsList);

        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendModelList = new ArrayList<>();
        findFriendAdapter = new FindFriendAdapter(getActivity(), findFriendModelList);
        rvFindFriends.setAdapter(findFriendAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.USERS);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS).child(currentUser.getUid());

        tvEmptyFriendsList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                findFriendModelList.clear();
                dataSnapshot.getChildren()
                        .forEach(ds -> {
                            if (!Objects.equals(ds.getKey(), currentUser.getUid()) && ds.child(NodeNames.NAME).getValue() != null) {
                                final String userId = ds.getKey();

                                if (userId.equals(currentUser.getUid()))
                                    return;

                                final String fullName = Objects.requireNonNull(ds.child(NodeNames.NAME).getValue()).toString();
                                String photoName = "";
                                if (ds.child(NodeNames.PHOTO).getValue() != null) {
                                    photoName = Objects.requireNonNull(ds.child(NodeNames.PHOTO).getValue()).toString();
                                }

                                String finalPhotoName = photoName;
                                databaseReferenceFriendRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String requestType = Objects.requireNonNull(snapshot.child(NodeNames.REQUEST_TYPE).getValue()).toString();
                                            if (requestType.equals(Constants.REQUEST_STATUS_SENT)) {
                                                findFriendModelList.add(new FindFriendModel(fullName, finalPhotoName, ds.getKey(), true));
                                            }
                                        } else {
                                            findFriendModelList.add(new FindFriendModel(fullName, finalPhotoName, ds.getKey(), false));
                                        }
                                        findFriendAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                                tvEmptyFriendsList.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_friends), Toast.LENGTH_SHORT).show();
            }
        });
    }
}