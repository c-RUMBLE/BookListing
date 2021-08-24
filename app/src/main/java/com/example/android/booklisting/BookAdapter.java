package com.example.android.booklisting;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(@NonNull Context context, @NonNull List<Book> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_item, parent, false);
        }

        Book currentBook = getItem(position);
        assert currentBook != null;
        TextView nameView = listItemView.findViewById(R.id.book_name);
        TextView authorsView = listItemView.findViewById(R.id.authors_view);
        TextView priceView = listItemView.findViewById(R.id.price);
        ImageView imageView = listItemView.findViewById(R.id.book_image);

        String name = currentBook.getmName();
        nameView.setText(name);

        String authors = currentBook.getmAuthors();
        authorsView.setText(authors);

        String price = currentBook.getmPrice();
        priceView.setText(price);

        Bitmap img_bmap = currentBook.getmImageBitmap();
        imageView.setImageBitmap(img_bmap);
        imageView.setClipToOutline(true);

        return listItemView;
    }
}
