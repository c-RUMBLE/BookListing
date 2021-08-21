package com.example.android.booklisting;

import android.graphics.Bitmap;

public class Book {

    private final String mName;

    private final String mAuthors;

    private final String mPrice;

    private final Bitmap mImageBitmap;

    private final String mUrl;

    public Book(String  name, String authors, String price,Bitmap imageUrl, String url)
    {
        mName = name;
        mAuthors = authors;
        mPrice = price;
        mImageBitmap = imageUrl;
        mUrl = url;
    }

    public String getmName() {
        return mName;
    }

    public String getmAuthors() {
        return mAuthors;
    }

    public String getmPrice() {
        return mPrice;
    }

    public Bitmap getmImageBitmap() {
        return mImageBitmap;
    }

    public String getmUrl() {
        return mUrl;
    }
}
