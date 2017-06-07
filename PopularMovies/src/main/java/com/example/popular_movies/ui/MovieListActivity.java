package com.example.popular_movies.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.popular_movies.R;
import com.example.popular_movies.data.ItemsContract;
import com.example.popular_movies.data.MovieLoader;
import com.example.popular_movies.data.UpdaterService;
import com.example.popular_movies.remote.Config;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link MovieDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class MovieListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private static final String SAVE_STATE = "SAVE_STATE";
    private int sort = Config.POPULAR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            sort = savedInstanceState.getInt(SAVE_STATE);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(sort == Config.FAVORITE){
                        mSwipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                    refresh();
                }
            }
        );

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        //if (savedInstanceState == null) {
            //refresh();
        //}
    }

    private void refresh() {
        Intent i = new Intent(this, UpdaterService.class);
        i.putExtra(UpdaterService.SORT_TYPE,sort);
        startService(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.popular:
                if(sort == Config.POPULAR){
                    break;
                }else{
                    sort = Config.POPULAR;
                    refresh();
                }
                break;
            case R.id.topRated:
                if(sort == Config.TOP_RATE){
                    break;
                }else{
                    sort = Config.TOP_RATE;
                    refresh();
                }
                break;
            case R.id.favorite:
                if(sort == Config.FAVORITE){
                    break;
                }
                sort = Config.FAVORITE;
                getLoaderManager().restartLoader(0, null, this);
                break;
        }
        return true;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putInt(SAVE_STATE, sort);
        super.onSaveInstanceState(outState);
    }
    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
        if(!mIsRefreshing){
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e("onCreateLoader","sort: "+sort);
        if(sort==Config.POPULAR || sort==Config.TOP_RATE ){
            return MovieLoader.newAllMovie(this);
        }else{
            return MovieLoader.newInstanceForFavorite(this,ItemsContract.Items.IS_FAVORITE);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.e("onCreateLoader","cursor.getCount(): "+cursor.getCount());
        if(cursor.getCount()<1){
            if(sort==Config.FAVORITE){
                Toast.makeText(this,R.string.no_favorite,Toast.LENGTH_SHORT).show();
                sort = Config.POPULAR;
                getLoaderManager().restartLoader(0, null, this);
                return;
            }
            refresh();
        }

        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);

            TransitionManager.beginDelayedTransition(mRecyclerView, slide);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        private Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(MovieLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()));
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);
                    /*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vh.thumbnailView.setTransitionName(getString(R.string.activity_image_trans));
                        vh.titleView.setTransitionName(getString(R.string.activity_title_trans));

                        Pair<View,String> pair1 = Pair.create((View)vh.thumbnailView, vh.thumbnailView.getTransitionName());
                        Pair<View,String> pair2 = Pair.create((View)vh.titleView, vh.titleView.getTransitionName());

                        ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(MovieListActivity.this,pair1,pair2);
                        startActivity(intent, options.toBundle());
                    } else {
                        startActivity(intent);
                    }*/
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            final String IMAGE_URL = Config.BASE_IMAGE_URL+mCursor.getString(MovieLoader.Query.POSTER_URL);
            Picasso.with(MovieListActivity.this).load(IMAGE_URL).into(holder.thumbnailView);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnailView;

        private  ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }
}
