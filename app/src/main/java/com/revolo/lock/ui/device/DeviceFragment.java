package com.revolo.lock.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.HomeLockListAdapter;
import com.revolo.lock.bean.TestLockBean;
import com.revolo.lock.ui.MainActivity;
import com.revolo.lock.ui.TitleBar;
import com.revolo.lock.ui.device.add.AddDeviceActivity;
import com.revolo.lock.ui.device.lock.DeviceDetailActivity;

public class DeviceFragment extends Fragment {

    private DeviceViewModel mDeviceViewModel;
    private HomeLockListAdapter mHomeLockListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDeviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        ConstraintLayout clNoDevice = root.findViewById(R.id.clNoDevice);
        ConstraintLayout clHadDevice = root.findViewById(R.id.clHadDevice);
        mDeviceViewModel.getTestLockBeans().observe(getViewLifecycleOwner(), testLockBeans -> {
            if(testLockBeans != null) {
                if(testLockBeans.isEmpty()) {
                    clNoDevice.setVisibility(View.VISIBLE);
                    clHadDevice.setVisibility(View.GONE);
                } else {
                    clNoDevice.setVisibility(View.GONE);
                    clHadDevice.setVisibility(View.VISIBLE);
                }
                mHomeLockListAdapter.setList(testLockBeans);
            }
        });
        // 无设备的时候控件UI
        ImageView ivAdd = root.findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));

        // 有设备的时候控件UI
        if(getContext() != null) {
            new TitleBar(root).setTitle(getString(R.string.title_my_devices))
                    .setRight(ContextCompat.getDrawable(getContext(), R.drawable.ic_home_icon_add),
                            v -> startActivity(new Intent(getContext(), AddDeviceActivity.class)));
            RecyclerView rvLockList = root.findViewById(R.id.rvLockList);
            rvLockList.setLayoutManager(new LinearLayoutManager(getContext()));
            mHomeLockListAdapter = new HomeLockListAdapter(R.layout.item_home_lock_list_rv);
            mHomeLockListAdapter.setOnItemClickListener((adapter, view, position) -> {
                if(adapter.getItem(position) instanceof TestLockBean) {
                    if(position < 0 || position >= adapter.getData().size()) return;
                    TestLockBean testLockBean = (TestLockBean) adapter.getItem(position);
                    Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
                    intent.putExtra(Constant.LOCK_DETAIL, testLockBean);
                    startActivity(intent);
                }
            });
            rvLockList.setAdapter(mHomeLockListAdapter);
            if(getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).setStatusBarColor(R.color.white);
            }
        }
        return root;
    }

}