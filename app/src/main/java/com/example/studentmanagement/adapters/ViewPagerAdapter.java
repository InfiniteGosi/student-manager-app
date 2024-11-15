package com.example.studentmanagement.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studentmanagement.fragments.HomeFragment;
import com.example.studentmanagement.fragments.ProfileFragment;
import com.example.studentmanagement.fragments.StudentListFragment;
import com.example.studentmanagement.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private String userRole;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String userRole) {
        super(fragmentManager, lifecycle);
        this.userRole = userRole; // Truyền vai trò vào adapter
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Mn tự tạo fragment r thay đổi ProfileFragment() bằng cái fragment tương ứng
        // Mình để vậy để không bị lỗi
        if ("Employee".equals(userRole)) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new StudentListFragment();
                default:
                    return new ProfileFragment();
            }
        } else if ("Manager".equals(userRole)) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new StudentListFragment();
                case 2:
                    return new ProfileFragment();
                default:
                    return new ProfileFragment();
            }
        } else {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new StudentListFragment();
                case 2:
                    return new ProfileFragment();
                case 3:
                    return new UserFragment();
                default:
                    return new ProfileFragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
