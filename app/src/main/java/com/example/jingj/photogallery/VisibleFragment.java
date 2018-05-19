package com.example.jingj.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class VisibleFragment extends Fragment {

    private static final String TAG = "visibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        //注册广播接收器
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        //解除广播接收器
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果我们收到了这个广播，说明界面是可见的，取消通知
            Log.i(TAG, "cancel notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}

