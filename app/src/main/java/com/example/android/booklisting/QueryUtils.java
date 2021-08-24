package com.example.android.booklisting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    public static List<Book> fetchBookData(String requestUrl) {

        // Create URL object
        Log.e(LOG_TAG,"Fetch Book Data");
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        return extractBooks(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    public static List<Book> extractBooks(String bookJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the bookJSON. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // build up a list of Book objects with the corresponding data.

            JSONObject jsonRootObject = new JSONObject(bookJSON);

            int totalBooks = jsonRootObject.optInt("totalItems");
            if(totalBooks == 0) {
                return null;
            }

            JSONArray bookArray = jsonRootObject.optJSONArray("items");
            for(int i=0;i<bookArray.length();i++) {
                JSONObject currentBook = bookArray.getJSONObject(i);

                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                String name = volumeInfo.optString("title");
                if (name.equals("")) {
                    name += "Unidentified title";
                }
                JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                StringBuilder auth = new StringBuilder();
                if (authorsArray == null) {
                    auth.append("Anonymous");
                } else {
                    for (int j = 0; j < authorsArray.length(); j++) {
                        auth.append(authorsArray.optString(j));
                        if (j != (authorsArray.length() - 1))
                            auth.append(", ");
                    }
                }
                String authors = auth.toString();
                String imgUrl;
                JSONObject imgLinks = volumeInfo.optJSONObject("imageLinks");
                if (imgLinks == null) {
                    imgUrl = "https://static.thenounproject.com/png/1554490-200.png";
                }
                else {
                    imgUrl = imgLinks.optString("smallThumbnail");
                    if (imgUrl.equals("")) {
                        imgUrl = "https://static.thenounproject.com/png/1554490-200.png";
                    }
                }
                URL imUrl = new URL(imgUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(imUrl.openConnection().getInputStream());

                String url = volumeInfo.optString("infoLink");

                JSONObject saleInfo = currentBook.getJSONObject("saleInfo");

                String saleability = saleInfo.optString("saleability");
                String price;
                if(saleability.compareTo("FOR_SALE") == 0) {
                    JSONObject listPrice = saleInfo.getJSONObject("listPrice");
                    String currency = listPrice.optString("currencyCode");
                    String amount = String.valueOf(listPrice.optDouble("amount"));
                    price = currency + " " + amount;
                        }
                else {
                    price = "NOT_FOR_SALE";
                }

                books.add(new Book(name, authors, price, bitmap, url));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        } catch (IOException e) {
            Log.e("QueryUtils", "Problem loading Image from url", e);
        }

        // Return the list of books
        return books;
    }
}