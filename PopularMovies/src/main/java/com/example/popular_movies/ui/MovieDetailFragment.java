package com.example.popular_movies.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.popular_movies.R;
import com.example.popular_movies.data.ItemsContract;
import com.example.popular_movies.data.MovieLoader;
import com.example.popular_movies.remote.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link MovieListActivity} in two-pane mode (on
 * tablets) or a {@link MovieDetailActivity} on handsets.
 */
public class MovieDetailFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MovieDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private CheckBox cBoxFavorite;
    private int mScrollY;
    private boolean mIsCard = false;
    private boolean hasDataChanged = false;
    private int mStatusBarFullOpacityBottom;
    private ProgressDialog mProgressDialog;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    public static MovieDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
            R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public MovieDetailActivity getActivityCast() {
        return (MovieDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
            mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });

        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mScrollView.getScrollY();
                Log.e("onScrollChanged","mScrollY: "+mScrollY);
                getActivityCast().onUpButtonFloorChanged(mItemId, MovieDetailFragment.this);
                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCursor != null) {
                    String link = Config.BASE_MOVIE_URL+mCursor.getString(MovieLoader.Query._ID);
                    String title = mCursor.getString(MovieLoader.Query.TITLE);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT,title);
                    i.putExtra(Intent.EXTRA_TEXT, link);
                    startActivity(Intent.createChooser(i,getString(R.string.action_share)));
                }
            }
        });

        bindViews();
        updateStatusBar();
        return mRootView;
    }
    @Override
    public void onDestroy(){
        Log.e("onDestroy","mItemId: "+mItemId);
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }
    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                mStatusBarFullOpacityBottom - mTopInset * 3,
                mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                (int) (Color.red(mMutedColor) * 0.9),
                (int) (Color.green(mMutedColor) * 0.9),
                (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView textReleaseDate = (TextView) mRootView.findViewById(R.id.textReleaseDate);
        TextView textRating = (TextView) mRootView.findViewById(R.id.textRating);
        cBoxFavorite = (CheckBox) mRootView.findViewById(R.id.cBoxFavorite);
        TextView playTrailer = (TextView) mRootView.findViewById(R.id.playTrailer);
        TextView readReview = (TextView) mRootView.findViewById(R.id.readReview);

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            final String id = mCursor.getString(MovieLoader.Query._ID);

            int val = mCursor.getInt(MovieLoader.Query.FAVORITE);
            if(val==ItemsContract.Items.IS_FAVORITE){
                cBoxFavorite.setChecked(true);
            }else{
                cBoxFavorite.setChecked(false);
            }

            cBoxFavorite.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(cBoxFavorite.isChecked()){
                        hasDataChanged = true;
                        Uri dirUri = ItemsContract.Items.buildDirUri();
                        ContentValues values = new ContentValues();
                        values.put(ItemsContract.Items.SAVE_AS_FAVORITE,ItemsContract.Items.IS_FAVORITE);
                        getActivity().getContentResolver().update(dirUri, values, ItemsContract.Items._ID+"=?", new String[] {id});
                    }else{
                        hasDataChanged = true;
                        Uri dirUri = ItemsContract.Items.buildDirUri();
                        ContentValues values = new ContentValues();
                        values.put(ItemsContract.Items.SAVE_AS_FAVORITE,ItemsContract.Items.IS_NOT_FAVORITE);
                        getActivity().getContentResolver().update(dirUri, values, ItemsContract.Items._ID+"=?", new String[] {id});
                    }
                }
            });
            playTrailer.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(isInternetConnected()){
                        new LoadURL(true).execute(Config.BASE_API_URL+id+Config.END_TRAILER_URL);
                    }else{
                        Toast.makeText(getActivity(),R.string.no_internet,Toast.LENGTH_SHORT).show();
                    }
                }
            });
            readReview.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(isInternetConnected()){
                        new LoadURL(false).execute(Config.BASE_API_URL+id+Config.END_REVIEW_URL);
                    }else{
                        Toast.makeText(getActivity(),R.string.no_internet,Toast.LENGTH_SHORT).show();
                    }
                }
            });

            textRating.setText(
                mCursor.getString(MovieLoader.Query.VOTE_AVERAGE));
                    //+"/10\n"+
                //mCursor.getString(MovieLoader.Query.POPULARITY));
            titleView.setText(mCursor.getString(MovieLoader.Query.TITLE));
            textReleaseDate.setText(DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(MovieLoader.Query.RELEASE_DATE)));
            bodyView.setText(Html.fromHtml(mCursor.getString(MovieLoader.Query.OVERVIEW)));

            final String IMAGE_URL = Config.BASE_IMAGE_URL+mCursor.getString(MovieLoader.Query.POSTER_URL);
            //Picasso.with(getActivity()).load(IMAGE_URL).into(holder.thumbnailView);

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                .get(IMAGE_URL, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = Palette.generate(bitmap, 12);
                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            mPhotoView.setImageBitmap(imageContainer.getBitmap());
                            mRootView.findViewById(R.id.meta_bar)
                                .setBackgroundColor(mMutedColor);
                            updateStatusBar();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        } else {
            mRootView.setVisibility(View.GONE);
            textReleaseDate.setText("N/A" );
            textRating.setText("N/A" );
            titleView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return MovieLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        Log.e("onLoadFinished","hasDataChanged: "+hasDataChanged);
        if(hasDataChanged){
            hasDataChanged = false;
            return;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
            ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
            : mPhotoView.getHeight() - mScrollY;
    }
    private boolean isInternetConnected(){
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return false;
        }
        return true;
    }
    private class LoadURL extends AsyncTask<String,Void,String>{
        boolean isTrailer;
        private LoadURL(boolean isTrailer){
            this.isTrailer = isTrailer;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.connecting));
            mProgressDialog.show();
        }
        @Override
        public String doInBackground(String... url) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return extractKey(url[0]);
        }
        public String extractKey(String url) {
            try {
                //JSONObject json = new JSONObject(SAMPLE_JSON_RESPONSE);
                JSONObject json = readJsonFromUrl(url);
                JSONArray jsonArray = json.optJSONArray("results");

                if(jsonArray!=null){
                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String key;
                        if(isTrailer){
                            key = jsonObject.optString("key");
                        }else{
                            key = jsonObject.optString("url");
                        }
                        if(key!=null){
                            return key;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Log.e("onPostExecute",""+s);
            if(s!=null){
                if(isTrailer){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.YOUTUBE_LINK+s)));
                }else{
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(s)));
                }
            }else{
                Toast.makeText(getActivity(),R.string.no_result,Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss();
        }
        private JSONObject readJsonFromUrl(String url) throws IOException, JSONException{
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            Log.e("readJsonFromUrl",""+url);
            try {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setReadTimeout(5000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                    String jsonText = readAll(rd);
                    return new JSONObject(jsonText);
                } else {
                    Log.e("readJsonFromUrl", "Error response code: " + urlConnection.getResponseCode());
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return null;
        }
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
    }
}
