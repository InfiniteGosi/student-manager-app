package com.example.studentmanagement.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.studentmanagement.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class UploadProfileImageActivity extends AppCompatActivity {
    final int PICK_IMAGE_REQUEST = 71;
    ImageView profileImageView, chooseImage;
    Button gobackBtn, saveBtn;
    Uri imageUrl;

    FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_image);

        profileImageView = findViewById(R.id.profileImageId);
        gobackBtn = findViewById(R.id.goBackBtnId);
        saveBtn = findViewById(R.id.btnSaveId);
        chooseImage = findViewById(R.id.chooseImageId);

        storage = FirebaseStorage.getInstance();

        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UploadProfileImageActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfileImage();
            }
        });

        Intent i = getIntent();
        String currentImageUrl = i.getStringExtra("currentImageUrl");
        if (currentImageUrl != null) {
            Glide.with(this).load(currentImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImageView);
            Log.d("uploadprofileimg", "ok");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadProfileImage() {
        if (imageUrl != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Generate a reference for the new image
            StorageReference ref = storage.getReference().child("images/" + UUID.randomUUID().toString());

            ref.putFile(imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadProfileImageActivity.this, "Tải lên thành công", Toast.LENGTH_SHORT).show();

                            // Retrieve the download URL
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // Display the uploaded image using Glide
                                    Glide.with(UploadProfileImageActivity.this)
                                            .load(downloadUri)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(profileImageView);

                                    // Store the download URL in Firebase Database under nested structure
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                                    // Setting the profileImageUrl inside the ired structure
                                    userRef.child("profileImageUrl").setValue(downloadUri.toString());

                                    // Return the URL as a result to the ProfileFragment
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("imageUrl", downloadUri.toString());
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UploadProfileImageActivity.this, "Tải lên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUrl = data.getData(); // Get image URI from intent
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUrl);
                profileImageView.setImageBitmap(bitmap); // Display the chosen image
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}