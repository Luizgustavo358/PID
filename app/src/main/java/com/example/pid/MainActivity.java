package com.example.pid;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {


    Animation hideLayout;
    Animation hideShadow;

    Animation showLayout;
    Animation showShadow;
    private FloatingActionButton fabPhoto;
    private View shadowView;
    private LinearLayout subFab;
    private LinearLayout option1Layout;
    private LinearLayout option2Layout;
    private LinearLayout option3Layout;

    private ImageView imageView;

    private static final int CAMERA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();

        initializeViews();
        subFab.setVisibility(View.GONE);
        setFabAnimations();
        setListeners();

    }


    /**
     * Set the activity listener behaviours.
     */
    private void setListeners() {
        option1Layout.setOnClickListener(view -> {
            dispatchTakePictureIntent();
        });

        option2Layout.setOnClickListener(view ->
                Toast.makeText(this, "Option 2", Toast.LENGTH_SHORT).show()
        );

        option3Layout.setOnClickListener(view ->
                Toast.makeText(this, "Option 3", Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);*/
            setPic();
            galleryAddPic();
        }
    }

    String currentPhotoPath;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("ERRRRRRRRRRRRRRRRRRRROU");
                throw new RuntimeException(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null && photoFile.exists()) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } else {
                System.out.println("NÃO EXISTE");
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        subFab = findViewById(R.id.ls_layout);
        shadowView = findViewById(R.id.shadowView);
        option1Layout = findViewById(R.id.option1);
        option2Layout = findViewById(R.id.option2);
        option3Layout = findViewById(R.id.option3);
        fabPhoto = findViewById(R.id.fab_photo);
        imageView = findViewById(R.id.imageView);
    }


    /**
     * Set Floating Action button animations.
     */
    private void setFabAnimations() {
        hideLayout = AnimationUtils.loadAnimation(this, R.anim.hide_layout);
        hideFabAnimation(hideLayout);
        hideShadow = AnimationUtils.loadAnimation(this, R.anim.hide_shadow);
        hideShadowAnimation(hideShadow);

        showLayout = AnimationUtils.loadAnimation(this, R.anim.show_layout);
        showShadow = AnimationUtils.loadAnimation(this, R.anim.show_shadow);

        fabPhoto.setOnClickListener(toggleFab());

        shadowView.setOnClickListener(setShadowViewClick());
    }


    /**
     * Starts the animations when clicked on the shadow view.
     *
     * @return lambda with the animations started if shadowView is visible
     */
    private View.OnClickListener setShadowViewClick() {
        return v -> {
            if (shadowView.getVisibility() == View.VISIBLE) {
                shadowView.startAnimation(hideShadow);
                subFab.startAnimation(hideLayout);
            }
        };
    }

    /**
     * Set animation listener of the fab
     *
     * @param hideShadow is the animation used on the shadowView
     */
    private void hideShadowAnimation(Animation hideShadow) {
        hideShadow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shadowView.clearAnimation();
                shadowView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Creates an onClickListener that toggles the floating action button between being visible or not.
     *
     * @return a click listener view
     */
    private View.OnClickListener toggleFab() {
        return v -> {
            if (subFab.getVisibility() == View.VISIBLE) {
                shadowView.startAnimation(hideShadow);
                subFab.startAnimation(hideLayout);
            } else {
                subFab.setVisibility(View.VISIBLE);
                shadowView.setVisibility(View.VISIBLE);
                shadowView.startAnimation(showShadow);
                subFab.startAnimation(showLayout);
            }
        };
    }

    /**
     * Set animation listener of the fab
     *
     * @param hideLayout is the animation used on the shadowView
     */
    private void hideFabAnimation(Animation hideLayout) {
        hideLayout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                subFab.clearAnimation();
                subFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
