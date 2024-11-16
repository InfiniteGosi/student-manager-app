package com.example.studentmanagement.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.studentmanagement.R;
import com.example.studentmanagement.activities.ChangePasswordActivity;
import com.example.studentmanagement.activities.LoginActivity;
import com.example.studentmanagement.activities.MainActivity;
import com.example.studentmanagement.activities.UploadProfileImageActivity;
import com.example.studentmanagement.activities.ViewCertificatesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private DatabaseReference userRef;
    private Button logoutButton, changepwButton, viewCertificate;
    private TextView usernameText;
    private ImageView changeIcon, avatarImage;
    private ActivityResultLauncher<Intent> uploadImageLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the ActivityResultLauncher for handling result from UploadProfileImageActivity
        uploadImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String imageUrl = result.getData().getStringExtra("imageUrl");
                        if (imageUrl != null) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(avatarImage);
                            avatarImage.setTag(imageUrl);
                            userRef.child("profileImageUrl").setValue(imageUrl);
                        }
                    }
                }
        );
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutButton = view.findViewById(R.id.logout_button);
        changepwButton = view.findViewById(R.id.changepw_button);
        viewCertificate = view.findViewById(R.id.viewCertificate);
        usernameText = view.findViewById(R.id.text_username);
        changeIcon = view.findViewById(R.id.change_icon);
        avatarImage = view.findViewById(R.id.avatar_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        usernameText.setText(user.getEmail());

        userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        viewCertificate.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ViewCertificatesActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        changepwButton.setOnClickListener(view1 -> {
            Intent i = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(i);
        });

        changeIcon.setOnClickListener(view12 -> {
            Intent i = new Intent(getActivity(), UploadProfileImageActivity.class);
            String currentImageUrl = (avatarImage.getTag() != null) ? avatarImage.getTag().toString() : null;
            i.putExtra("currentImageUrl", currentImageUrl);
            uploadImageLauncher.launch(i);  // Use the registered launcher instead of startActivityForResult
        });

        userRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null && getActivity() != null) {  // Check if fragment is attached
                    Glide.with(getActivity())
                            .load(imageUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(avatarImage);
                    avatarImage.setTag(imageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return view;
    }
}