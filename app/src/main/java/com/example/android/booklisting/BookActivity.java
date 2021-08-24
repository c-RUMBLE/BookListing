package com.example.android.booklisting;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import androidx.loader.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class BookActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Book>> {

    public static final String LOG_TAG = BookActivity.class.getName();

    BookAdapter adapter;
    private TextView mEmptyStateTextView;
    EditText editText;
    ProgressBar mProgress;
    static String finalUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        mProgress = findViewById(R.id.progress);

        editText = findViewById(R.id.search_text);

        editText.setOnClickListener(v -> editText.setCursorVisible(true));

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                Log.i(LOG_TAG,"Search pressed");
                hideKeyboard(v);
                editText.setCursorVisible(false);
                goSearch();
                return true;
            }
            return false;
        });

        ImageView imgView = findViewById(R.id.search_icon);
        imgView.setClipToOutline(true);
        imgView.setOnClickListener(v -> {
            Log.v(LOG_TAG, "Search clicked");
            editText.clearFocus();
            hideKeyboard(v);
            editText.setCursorVisible(false);
            goSearch();
        });

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = findViewById(R.id.list);

        assert bookListView != null;
        bookListView.setEmptyView(mEmptyStateTextView);

        adapter = new BookAdapter(this, new ArrayList<>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(adapter);

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Book clickedBook = adapter.getItem(position);
            String clickedUrl = clickedBook.getmUrl();
            if(!clickedUrl.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(clickedUrl));
                startActivity(i);
            }
            else {
                Toast.makeText(BookActivity.this, R.string.no_info, Toast.LENGTH_SHORT).show();
            }
        });

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork =cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        mProgress.setVisibility(View.GONE);
        if(!isConnected)
        {
            mEmptyStateTextView.setText(R.string.connection_lost);
        }
        else {
            mEmptyStateTextView.setText(R.string.search_books);
            if(getLoaderManager().getLoader(0) != null) {
                mEmptyStateTextView.setVisibility(View.GONE);
                LoaderManager.getInstance(this).initLoader(0,null, this);
            }
            Log.e(LOG_TAG, "Init Loader");
        }

        }

    public void goSearch() {
        mProgress.setVisibility(View.VISIBLE);
        adapter.clear();
        mEmptyStateTextView.setText("");
        String queries = String.valueOf(editText.getText());
        try {
            String GOOGLE_BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=15&q=";
            finalUrl = GOOGLE_BOOKS_REQUEST_URL + URLEncoder.encode(queries,"UTF-8");
            Log.v(LOG_TAG,finalUrl);
            LoaderManager.getInstance(this).restartLoader(0,null,this);
            Log.v(LOG_TAG,"Init Loader on search");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"Problem encoding queries to Url :" + e);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @NonNull
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BookLoader(this, finalUrl);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Book>> loader, List<Book> books) {

        mProgress.setVisibility(View.GONE);

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected) {
            mEmptyStateTextView.setText(R.string.connection_lost);
        }
        else {
            mEmptyStateTextView.setText(R.string.no_books);
        }
        adapter.clear();

        Log.e(LOG_TAG,"On Load Finished");
        if(books != null && !books.isEmpty()){
            adapter.addAll(books);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Book>> loader) {
        Log.e(LOG_TAG,"On Loader Reset");
        adapter.clear();
    }
}