package com.example.popular_movies.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Helper for loading a list of articles or a single article.
 */
public class MovieLoader extends CursorLoader {
    public static MovieLoader newAllMovie(Context context) {
        return new MovieLoader(context, ItemsContract.Items.buildDirUri());
    }

    public static MovieLoader newInstanceForItemId(Context context, long itemId) {
        return new MovieLoader(context, ItemsContract.Items.buildItemUri(itemId));
    }

    public static MovieLoader newInstanceForFavorite(Context context, int favoriteVal) {
        return new MovieLoader(context, ItemsContract.Items.buildFavoriteUri(favoriteVal));
    }

    private MovieLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, ItemsContract.Items.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                ItemsContract.Items._ID,
                ItemsContract.Items.TITLE,
                ItemsContract.Items.VOTE_AVERAGE,
                ItemsContract.Items.POPULARITY,
                ItemsContract.Items.RELEASE_DATE,
                ItemsContract.Items.SAVE_AS_FAVORITE,
                ItemsContract.Items.OVERVIEW,
                ItemsContract.Items.POSTER_URL,
                ItemsContract.Items.PHOTO_URL,
        };

        int _ID             = 0;
        int TITLE           = 1;
        int VOTE_AVERAGE    = 2;
        int POPULARITY      = 3;
        int RELEASE_DATE    = 4;
        int FAVORITE        = 5;
        int OVERVIEW        = 6;
        int POSTER_URL      = 7;
        int PHOTO_URL       = 8;
    }
}
