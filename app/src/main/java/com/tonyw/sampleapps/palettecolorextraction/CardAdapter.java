package com.tonyw.sampleapps.palettecolorextraction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Parcelable;

import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import java.io.File;
import java.util.ArrayList;

/**
 * Adapter for the GridView that displays the cards.
 */
public class CardAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> mBitmaps;
    private static final String DIR_NAME_FOR_IMAGE = "Images";
    /**
     * The GridView using this adapter. Used as a horrible hack to scroll to a position.
     */
    private GridView mGridView;

    // Variables for undo feature.
//    private Bitmap mDismissedBitmap;
//    private int mDismissedPosition;
//    OnClickWrapper onUndoClickWrapper = new OnClickWrapper("undoclickwrapper",
//            new SuperToast.OnClickListener() {
//        @Override
//        public void onClick(View view, Parcelable token) {
//            if (mDismissedBitmap != null) {
//                mBitmaps.add(mDismissedPosition, mDismissedBitmap);
//                notifyDataSetChanged();
//                mGridView.smoothScrollToPosition(mDismissedPosition);
//                mDismissedBitmap = null;
//            }
//        }
//    });

    public CardAdapter(Context context, ArrayList<Bitmap> bitmaps, GridView gridView) {
        mContext = context;
        mBitmaps = bitmaps;
        mGridView = gridView;
    }

    public void remove(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage("Are you sure to delete the image?");
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // delete file too
                        File directory = new File(mContext.getFilesDir(), "Palette" + File.separator + DIR_NAME_FOR_IMAGE);
                        directory.listFiles()[position].delete();
                        Toast.makeText(mContext, "Photo deleted", Toast.LENGTH_LONG).show();

                        mBitmaps.remove(position);

                        notifyDataSetChanged();
                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public int getCount() {
        return mBitmaps.size();
    }

    @Override
    public Object getItem(int position) {
        // Not used.
        return null;
    }

    @Override
    public long getItemId(int position) {
        // Not used.
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CardView cardView;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cardView = (CardView) inflater.inflate(R.layout.card_layout, null);
        } else {
            cardView = (CardView) convertView;
        }
        Bitmap bitmap = mBitmaps.get(position);
        ((ImageView) cardView.findViewById(R.id.card_image)).setImageBitmap(bitmap);

        // Extract prominent colors asynchronously and then update the card.
        new ExtractPaletteColorsAsyncTask(mContext, cardView).execute(bitmap);

        return cardView;
    }
}
