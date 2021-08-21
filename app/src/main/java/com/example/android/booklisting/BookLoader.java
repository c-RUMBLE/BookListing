package com.example.android.booklisting;

import androidx.loader.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>>
{
    private final String mUrl;

    public static final String LOG_TAG = BookLoader.class.getName();


    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.e(LOG_TAG,"On Start Loading");
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        Log.e(LOG_TAG,"Load In Background");
        if (mUrl == null) {
            return null;
        }

        // Create a fake list of earthquake locations.
        return QueryUtils.fetchBookData(mUrl);
    }
}
