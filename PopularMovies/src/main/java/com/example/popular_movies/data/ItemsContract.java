package com.example.popular_movies.data;

import android.net.Uri;

public class ItemsContract {
	public static final String CONTENT_AUTHORITY = "com.example.popular_movies";
	public static final Uri BASE_URI = Uri.parse("content://com.example.popular_movies");

	interface ItemsColumns {
		/** Type: INTEGER PRIMARY KEY AUTOINCREMENT */
		String _ID = "id";
		String TITLE = "title";
        String VOTE_AVERAGE = "vote_average";
		String POPULARITY = "popularity";
		String RELEASE_DATE = "release_date";
		String SAVE_AS_FAVORITE = "save_as_favorite";
		String OVERVIEW = "overview";
		String POSTER_URL = "poster_path";
		String PHOTO_URL = "backdrop_path";
		int IS_NOT_FAVORITE = 0;
		int IS_FAVORITE = 1;
	}

	public static class Items implements ItemsColumns {
		public static final String CONTENT_TYPE 				= "vnd.android.cursor.dir/vnd.com.example.popular_movies.items";
		public static final String CONTENT_ITEM_TYPE 			= "vnd.android.cursor.item/vnd.com.example.popular_movies.items";
		public static final String CONTENT_FULTER_FAVORITE_TYPE = "vnd.android.cursor.favorite/vnd.com.example.popular_movies.items";

        public static final String DEFAULT_SORT = RELEASE_DATE + " DESC";

		/** Matches: /items/ */
		public static Uri buildDirUri() {
			return BASE_URI.buildUpon().appendPath(ItemsProvider.Tables.ITEMS).build();
		}

		/** Matches: /items/[_id]/ */
		public static Uri buildItemUri(long _id) {
			return BASE_URI.buildUpon().appendPath(ItemsProvider.Tables.ITEMS).appendPath(Long.toString(_id)).build();
		}

        /** Matches: /items/[_id]/ */
        public static Uri buildFavoriteUri(int favoriteVal) {
			Uri uri = BASE_URI.buildUpon()
				.appendPath(ItemsProvider.Tables.FAVORITE)
				.appendPath(favoriteVal+"")
				.build();
			return uri;
        }
        /** Read item ID item detail URI. */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
	}

	private ItemsContract() {
	}
}
