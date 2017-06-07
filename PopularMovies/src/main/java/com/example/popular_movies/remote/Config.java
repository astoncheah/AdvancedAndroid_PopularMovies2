package com.example.popular_movies.remote;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL POPULAR_URL;
    public static final URL TOP_RATED_URL;

    public static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w500/";//"w92", "w154", "w185", "w342", "w500", "w780", or "original"
    public static final String BASE_MOVIE_URL = "https://www.themoviedb.org/movie/";
    public static final String BASE_API_URL = "https://api.themoviedb.org/3/movie/";
    public static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=";
    public static final String END_TRAILER_URL = "/videos?api_key="+API_KEY+"&language=en-US";
    public static final String END_REVIEW_URL = "/reviews?api_key="+API_KEY+"&language=en-US";
    public static final int POPULAR = 0;
    public static final int TOP_RATE = 1;
    public static final int FAVORITE = 2;


    //https://api.themoviedb.org/3/movie/popular?api_key=98b8694252f0acbb8f656a9c87f06ead&language=en-US&page=1
    //https://api.themoviedb.org/3/movie/top_rated?api_key=98b8694252f0acbb8f656a9c87f06ead&language=en-US&page=1
    //https://api.themoviedb.org/3/movie/{movie_id}/reviews?api_key=<<api_key>>&language=en-US&page=1
    //https://api.themoviedb.org/3/movie/{movie_id}/videos?api_key=<<api_key>>&language=en-US
    //https://www.youtube.com/watch?v=fBNpSRtfIUA
    static {
        URL url = null;
        try {
            url = new URL("https://api.themoviedb.org/3/movie/popular?api_key="+API_KEY+"&language=en-US&page=1");
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();
        }

        POPULAR_URL = url;
    }
    static {
        URL url = null;
        try {
            url = new URL("https://api.themoviedb.org/3/movie/top_rated?api_key="+API_KEY+"&language=en-US&page=1");
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();
        }

        TOP_RATED_URL = url;
    }
}
