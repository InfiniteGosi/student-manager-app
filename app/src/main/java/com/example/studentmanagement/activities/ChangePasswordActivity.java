package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentmanagement.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    ImageView goBackBtn ;
    TextInputEditText oldPassword, newPassword, confirmedPassword;
    Button changePasswordBtn;

    FirebaseUser user;
    AuthCredential authCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        goBackBtn = findViewById(R.id.goBackBtn);
        oldPassword = findViewById(R.id.oldPasswordId);
        newPassword = findViewById(R.id.newPasswordId);
        confirmedPassword = findViewById(R.id.confirmedPasswordId);
        changePasswordBtn = findViewById(R.id.btnChangPasswordId);

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChangePasswordActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPasswordContent = oldPassword.getText().toString();
                String newPasswordContent = newPassword.getText().toString();
                String confirmedPasswordContent = confirmedPassword.getText().toString();

                if(oldPasswordContent.isEmpty()) {
                    showToast("Hãy nhập mật khẩu cũ của bạn");
                    oldPassword.requestFocus();
                    return;
                }

                if(newPasswordContent.isEmpty()){
                    showToast("Hãy nhập mật khẩu mới của bạn");
                    newPassword.requestFocus();
                    return;
                }

                if (newPasswordContent.length() < 6) {
                    showToast("Mật khẩu mởi phải ít nhất 6 ký tự");
                    newPassword.requestFocus();
                    return;
                }

                if(confirmedPasswordContent.isEmpty()){
                    showToast("Hãy nhập lại mật khẩu mới");
                    confirmedPassword.requestFocus();
                    return;
                }

                if(!newPasswordContent.equals(confirmedPasswordContent)){
                    showToast("Mật khẩu xác minh không trùng mật khẩu mới");
                    confirmedPassword.requestFocus();
                    return;
                }

                updatePassword(oldPasswordContent, newPasswordContent);
            }
        });
    }

    private void updatePassword(String oldPasswordContent, String newPasswordContent){
        user = FirebaseAuth.getInstance().getCurrentUser();
        authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPasswordContent);

        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPasswordContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast("Mật khẩu cập nhật thành công");

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getMessage());
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast(e.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}