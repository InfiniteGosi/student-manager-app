package com.example.studentmanagement.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studentmanagement.fragments.ProfileFragment;
import com.example.studentmanagement.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Mn tự tạo fragment r thay đổi ProfileFragment() bằng cái fragment tương ứng
        // Mình để vậy để không bị lỗi
        switch (position) {
            case 0:
                return new ProfileFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new ProfileFragment();
            case 3:
                return new UserFragment();
            case 4:
                return new ProfileFragment();
            default:
                return new ProfileFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
