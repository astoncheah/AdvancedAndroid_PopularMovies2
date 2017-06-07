package com.example.popular_movies.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.example.popular_movies.remote.Config;
import com.example.popular_movies.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String SORT_TYPE = "SORT_TYPE";
    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.popular_movies.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.popular_movies.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        Cursor cursor = getContentResolver().query(dirUri,MovieLoader.Query.PROJECTION,null,null,null);
        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            int sort = intent.getIntExtra(UpdaterService.SORT_TYPE,Config.POPULAR);
            URL url = Config.POPULAR_URL;
            switch(sort){
                case Config.POPULAR:
                    url = Config.POPULAR_URL;
                    break;
                case Config.TOP_RATE:
                    url = Config.TOP_RATED_URL;
                    break;
            }
            JSONArray array = RemoteEndpointUtil.fetchJsonArray(url);
            if (array == null) {
                throw new JSONException("Invalid parsed item array" );
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                values.put(ItemsContract.Items._ID, object.getString(ItemsContract.Items._ID));
                values.put(ItemsContract.Items.TITLE, object.getString(ItemsContract.Items.TITLE));
                values.put(ItemsContract.Items.VOTE_AVERAGE, object.getString(ItemsContract.Items.VOTE_AVERAGE));
                values.put(ItemsContract.Items.POPULARITY, object.getString(ItemsContract.Items.POPULARITY));
                time.parse3339(object.getString(ItemsContract.Items.RELEASE_DATE));
                values.put(ItemsContract.Items.RELEASE_DATE, time.toMillis(false));
                values.put(ItemsContract.Items.SAVE_AS_FAVORITE,getFavorite(cursor,object.getString(ItemsContract.Items._ID)));
                values.put(ItemsContract.Items.OVERVIEW, object.getString(ItemsContract.Items.OVERVIEW));
                values.put(ItemsContract.Items.POSTER_URL, object.getString(ItemsContract.Items.POSTER_URL));
                values.put(ItemsContract.Items.PHOTO_URL, object.getString(ItemsContract.Items.PHOTO_URL));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }
            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        } finally{
            cursor.close();
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }
    private int getFavorite(Cursor cursor,String id){
        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(MovieLoader.Query._ID).equals(id) && cursor.getInt(MovieLoader.Query.FAVORITE)==ItemsContract.Items.IS_FAVORITE){
                    return ItemsContract.Items.IS_FAVORITE;
                }
            }while(cursor.moveToNext());
        }

        return ItemsContract.Items.IS_NOT_FAVORITE;
    }
}
