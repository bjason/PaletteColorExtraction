package com.tonyw.sampleapps.palettecolorextraction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {
    private static final int REQUEST_CODE_ACTION_ADD_FROM_STORAGE = 0;
    private static final int REQUEST_CODE_ACTION_ADD_FROM_CAMERA = 1;
    private static final String BUNDLE_SAVED_BITMAPS = "bitmaps";
    private static final String DIR_NAME_FOR_IMAGE = "Images";


    private ArrayList<Bitmap> mBitmaps;
    private GridView mGridView;
    private CardAdapter mCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
//            mBitmaps = savedInstanceState.getParcelableArrayList(BUNDLE_SAVED_BITMAPS);
            mBitmaps = (ArrayList<Bitmap>) WeakDataHolder.getInstance().getData("1");
        } else {
            mBitmaps = new ArrayList<>();
        }

        mGridView = (GridView) findViewById(R.id.color_background);
        mCardAdapter = new CardAdapter(this, mBitmaps, mGridView);
        mGridView.setAdapter(mCardAdapter);

        // Make cards dismissible.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mGridView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(AbsListView view, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mCardAdapter.remove(position);
                                }
                            }
                        });
        mGridView.setOnTouchListener(touchListener);
        // Set this scroll listener to ensure that we don't look for swipes during scrolling.
        mGridView.setOnScrollListener(touchListener.makeScrollListener());

        if (savedInstanceState == null) {
            try {
                addCards();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedState) {
        super.onSaveInstanceState(savedState);

//        savedState.putParcelableArrayList(BUNDLE_SAVED_BITMAPS, mBitmaps);
        WeakDataHolder.getInstance().saveData("1", mBitmaps);
    }

    /**
     * Adds cards with the default images stored in assets.
     */
    private void addCards() throws IOException {
        // load sample images
//        AssetManager assetManager = getAssets();
//        for (String assetName : assetManager.list("sample_images")) {
//            InputStream assetStream = assetManager.open("sample_images/" + assetName);
//            Bitmap bitmap = BitmapFactory.decodeStream(assetStream);
//            assetStream.close();
//            addCard(bitmap);
//        }
        File directory = new File(getApplicationContext().getFilesDir(), "Palette" + File.separator + DIR_NAME_FOR_IMAGE);
        if (directory.listFiles() == null) {
//            TextView tv = findViewById(R.id.centerText);
//            tv.setVisibility(View.VISIBLE);
        } else {
            for (File image : directory.listFiles()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(image));
                addCard(b);
            }
        }
    }

    /**
     * Adds the provided bitmap to a list, and repopulates the main GridView with the new card.
     */
    private void addCard(Bitmap bitmap) {
        mBitmaps.add(bitmap);
        mCardAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Methods to open the Gallery
    private void openGallery() {
        //Ask for permission to access/read storage for marshmallow and greater here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   //Requesting the permission with the appropriate request code
                        REQUEST_CODE_ACTION_ADD_FROM_STORAGE);
            } else {
                //If the permission was already granted the first time it will run the method to open the gallery intent
                getPhotoFromGallery();
            }
        }
    }

    private void openCamera() {
        //Ask for permission to access/read storage for marshmallow and greater here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},   //Requesting the permission with the appropriate request code
                        REQUEST_CODE_ACTION_ADD_FROM_CAMERA);
            } else {
                //If the permission was already granted the first time it will run the method to open the gallery intent
                getPhotoFromCamera();
            }
        }
    }

    private void getPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_ACTION_ADD_FROM_STORAGE);  //Check onActivityResult on how to handle the photo selected}
    }

    private void getPhotoFromCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CODE_ACTION_ADD_FROM_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACTION_ADD_FROM_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to access gallery
                getPhotoFromGallery();
            } else {
                Toast.makeText(getApplicationContext(), "Please grant permission to proceed", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CODE_ACTION_ADD_FROM_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to access the Camera
                getPhotoFromCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Please grant permission to proceed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_add_from_camera == item.getItemId()) {
            // Start Intent to retrieve an image (see OnActivityResult).
            openCamera();
            return true;
        } else if (R.id.action_add_from_storage == item.getItemId()) {
            // Start Intent to retrieve an image (see OnActivityResult).
            openGallery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (Activity.RESULT_OK == resultCode) {
            if (REQUEST_CODE_ACTION_ADD_FROM_STORAGE == requestCode) {
                try {
                    InputStream stream = getContentResolver().openInputStream(
                            data.getData());
                    bitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (REQUEST_CODE_ACTION_ADD_FROM_CAMERA == requestCode) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data"); // Just a thumbnail, but works okay for this.
            }
        }
        if (bitmap != null) {
            addCard(bitmap);
            mGridView.smoothScrollToPosition(mBitmaps.size() - 1);

            // save the image to internal memory
            try {
                File path = new File(getApplicationContext().getFilesDir(), "Palette" + File.separator + DIR_NAME_FOR_IMAGE);
                if (!path.exists()) {
                    path.mkdirs();
                }
                // use current time to name the picture
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssz");
                String imageName = sdf.format(new Date());

                Log.d(TAG, "onActivityResult() returned: " + sdf);

                File outFile = new File(path, imageName + ".jpeg");
                FileOutputStream outputStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Saving received message failed with", e);
            } catch (IOException e) {
                Log.e(TAG, "Saving received message failed with", e);
            }
        }
    }
}


