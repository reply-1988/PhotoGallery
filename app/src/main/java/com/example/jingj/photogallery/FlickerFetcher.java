package com.example.jingj.photogallery;

import android.net.ProxyInfo;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickerFetcher {

    private static final String TAG = "FlickrFetcher";
    //此处的key是从GitHub上找到的，惭愧，日本雅虎注册不了
    private static final String API_KEY = "1e303cc783909781d2c8a75bdc25f100";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT  = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            //自动转义查询字符串
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        //url返回的是URLConnection对象，但是连接的是httpURL，所以需要将其转换为HttpURLConnection.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //从Url中获取得到的items
    private List<GalleryItem> downloadGalleryItems(String url) {

        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
            Logger.addLogAdapter(new AndroidLogAdapter());
            Logger.json(jsonString);

        } catch (IOException e) {
            Log.e(TAG, "fail to fetch items", e);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    //构建不同的url
    private String buildUrl(String method, String query) {
        Uri.Builder builder = ENDPOINT.buildUpon().appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            builder.appendQueryParameter("text", query);
        } else {
            builder.appendQueryParameter("page", query);
        }
        return builder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos(Integer page) {
        String url = buildUrl(FETCH_RECENT_METHOD, String.valueOf(page));
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    //解析json数据，并将其带入模型中
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException{

        //获取jsonObject中的jsonObject
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        String jsonPhotosString = photoJsonArray.toString();

        Gson gson = new Gson();
        Type galleryItemType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();
        List<GalleryItem> galleryItems = gson.fromJson(jsonPhotosString, galleryItemType);
        items.addAll(galleryItems);

    }
}
