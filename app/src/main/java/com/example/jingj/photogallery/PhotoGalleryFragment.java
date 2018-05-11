package com.example.jingj.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int page = 1;
    private FetchItemTask fetchItemTask;
    private ThumbnailDownload<PhotoHolder> mThumbnailDownload;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //启动AsyncTask，进而触发后台线程并调用doInBackGround()
        fetchItemTask = new FetchItemTask();
        fetchItemTask.execute(page);

        Handler responseHandler = new Handler();
        //mThumbnailDownload为HandlerThread线程
        mThumbnailDownload = new ThumbnailDownload<>(responseHandler);
        mThumbnailDownload.setmThumbnailDownloadListener(new ThumbnailDownload.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                photoHolder.bindDrawable(drawable);
                Log.i(TAG, "Holder bind success");
            }
        });
        mThumbnailDownload.start();
        mThumbnailDownload.getLooper();
        Log.i(TAG, "BackGround thread start");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //这样就能够保证每次旋转屏幕，重新生成RecyclerView时候能够重新配置Adapter，此外当模型层对象发生变化时
        //也应该调用这个方法
        setupAdapter();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mRecyclerView.canScrollVertically(1)) {
                    page = page + 1;
                    fetchItemTask.cancel(false);
                    fetchItemTask = new FetchItemTask();
                    fetchItemTask.execute(page);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownload.clearQueue();
        Log.i(TAG, "Background Thread destroyed");
    }

    private void setupAdapter() {
        /**
         * Return true if the fragment is currently added to its activity.
         */
        //保证getActivity()方法不为空
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
//            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
//            holder.bindDrawable(placeholder);
            mThumbnailDownload.queueThumbnail(holder, galleryItem.getmUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;
        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView;
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    //此类用来新建后台线程
    //第三个参数是doInBackGround的输出以及onPostExecute的输入
    private class FetchItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Integer... voids) {
            return new FlickerFetcher().fetchItems(voids[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            setupAdapter();
        }
    }
}
