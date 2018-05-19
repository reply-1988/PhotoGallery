package com.example.jingj.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.util.List;

public class PollServiceHigh extends JobService {

    private PollTask mCurrentTask;
    final static int JOB_ID = 12345;
    private static final String TAG = "PollServiceHigh";
    public static final String ACTION_SHOW_NOTIFICATION = "com.jingj.android.photogallery.SHOW_NOTIFICATION";


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "开始后台程序");
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "开始后台程序");
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return false;
    }

    //新建线程用来运行jobService
    private class PollTask extends AsyncTask<JobParameters, Void, Void>{

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParam = params[0];

            pollFlick();
            jobFinished(jobParam, false);
            return null;
        }
    }


    @TargetApi(Build.VERSION_CODES.O_MR1)
    private void pollFlick(){
        if (!isNetworkAvailableAndConnected()) {
            Log.i(TAG, "一个萝卜");
            return;
        }
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultedID = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null) {
            items = new FlickerFetcher().fetchRecentPhotos(1);
        } else {
            items = new FlickerFetcher().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getmID();
        if (resultId == lastResultedID) {
            Log.i(TAG, "Got an old result");
        } else {
            Log.i(TAG, "Got an new result");
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent ps = PendingIntent.getActivity(this, 0, i, 0);

            //Android8.0之后要添加NotificationChannel
            final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    "test", "test", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, "test")
                    .setTicker(resources.getString(R.string.new_picture_title))
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setContentText(resources.getString(R.string.new_picture_text))
                    .setContentTitle(resources.getString(R.string.new_picture_title))
                    .setContentIntent(ps)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(111111, notification);
            Log.i(TAG, "已经成功开启通知");
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));
        }
        QueryPreferences.setLastResultId(this, resultId);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    //判断已经计划好了任务
    public static boolean isScheduled(Context context) {

        boolean hasBeenScheduled = false;
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }

    //新建JobInfo并运行Service
    public static void startService(Context context, boolean isOn) {

        boolean should_create = !isOn;
        if (should_create) {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollServiceHigh.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    //在Android7.0及以上的版本setPeriodic有一个最小值为15min，小于15min的值都会被强制设置成15min！！！！！！
                    //所以服务开启的特别慢！！！！！！
                    .setPeriodic(1000 * 60 * 15)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
            Log.i(TAG, "成功设置定时器");
        } else {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.cancel(JOB_ID);
            Log.i(TAG, "取消定时器设置");
        }
        QueryPreferences.setAlarmOn(context, isOn);
    }
}
