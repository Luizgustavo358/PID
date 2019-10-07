package com.example.pid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings({"unused", "ConstantConditions"})
public class MainActivity extends AppCompatActivity {

    private LinearLayout option1Layout;
    private LinearLayout option2Layout;
    private LinearLayout option3Layout;
    private LinearLayout subFab;

    private View shadowView;

    FloatingActionButton fabPhoto;

    Animation hideLayout;
    Animation hideShadow;
    Animation showLayout;
    Animation showShadow;

    Button button;

    private Bitmap bitmapProvaEmBranco;
    private ImageView imageViewProvaEmBranco;
    Button obterProvaBranco;
    File fileProvaEmBranco;
    static final int REQUEST_TAKE_PHOTO_PROVA_BRANCO = 1;
    private final static int IMAGE_RESULT_PROVA_BRANCO = 200;

    private Bitmap bitmapGabarito;
    private ImageView imageViewGabarito;
    Button obterGabarito;
    File fileGabarito;
    static final int REQUEST_TAKE_PHOTO_GABARITO = 2;
    private final static int IMAGE_RESULT_GABARITO = 201;

    private RecyclerView recyclerViewProvas;


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


    /**
     * Set the activity listener behaviours.
     */
    private void setListeners() {
        option1Layout.setOnClickListener(v -> {
            getAlertDialog(this::setFileProvaEmBranco, REQUEST_TAKE_PHOTO_PROVA_BRANCO, IMAGE_RESULT_PROVA_BRANCO).show();
        });

        option2Layout.setOnClickListener(v -> {
            getAlertDialog(this::setFileGabarito, REQUEST_TAKE_PHOTO_GABARITO, IMAGE_RESULT_GABARITO).show();
        });

        // TODO
        option3Layout.setOnClickListener(v -> {
            // getAlertDialog()
        });

//        obterProvaBranco.setOnClickListener(v -> {
//            getAlertDialog(this::setFileProvaEmBranco, REQUEST_TAKE_PHOTO_PROVA_BRANCO, IMAGE_RESULT_PROVA_BRANCO).show();
//        });
//        obterGabarito.setOnClickListener(v -> {
//            getAlertDialog(this::setFileGabarito, REQUEST_TAKE_PHOTO_GABARITO, IMAGE_RESULT_GABARITO).show();
//        });

        button.setOnClickListener(v -> {
            setBinary(bitmapProvaEmBranco, imageViewProvaEmBranco, setGreyScale(bitmapProvaEmBranco, imageViewProvaEmBranco));
            setBinary(bitmapGabarito, imageViewGabarito, setGreyScale(bitmapGabarito, imageViewGabarito));

        });
    }

    private AlertDialog getAlertDialog(Consumer<File> setFileProvaEmBranco, int resultTakePicture, int resultChoosePicture) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.modo)
                .setPositiveButton(R.string.camera, (dialog, id) -> {
                    dispatchTakePictureIntent(setFileProvaEmBranco, resultTakePicture);
                })
                .setNegativeButton(R.string.galeria, (dialog, id) -> {
                    choosePictureFromSomewhere(resultChoosePicture);
                })
                .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                })
                .setCancelable(true);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void setBinary(Bitmap bitmap, ImageView imageView, Mat destination) {
        Mat destination2 = new Mat();
        Imgproc.adaptiveThreshold(destination, destination2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 7);
        Utils.matToBitmap(destination2, bitmap);
        storeImage(bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private Mat setGreyScale(Bitmap bitmap, ImageView imageView) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat destination = new Mat();
        Imgproc.cvtColor(mat, destination, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(destination, bitmap);
        imageView.setImageBitmap(bitmap);
        return destination;
    }

    private void choosePictureFromSomewhere(int resultCode) {
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (Objects.requireNonNull(intent.getComponent()).getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Selecione fonte");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[0]));


        startActivityForResult(chooserIntent, resultCode);


    }

    public void setBitmapGabarito(Bitmap bitmapGabarito) {
        this.bitmapGabarito = bitmapGabarito;
    }

    public void setBitmapProvaEmBranco(Bitmap bitmapProvaEmBranco) {
        this.bitmapProvaEmBranco = bitmapProvaEmBranco;
    }

    public void setFileGabarito(File fileGabarito) {
        this.fileGabarito = fileGabarito;
    }

    public void setFileProvaEmBranco(File fileProvaEmBranco) {
        this.fileProvaEmBranco = fileProvaEmBranco;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO_PROVA_BRANCO && resultCode == RESULT_OK) {

            setPic(imageViewProvaEmBranco, this::setBitmapProvaEmBranco, fileProvaEmBranco.getAbsolutePath());
            galleryAddPic(fileProvaEmBranco.getAbsolutePath());
            if (bitmapGabarito != null && bitmapProvaEmBranco != null)
                button.setVisibility(View.VISIBLE);

        } else if (requestCode == IMAGE_RESULT_PROVA_BRANCO && resultCode == Activity.RESULT_OK) {

            String filePath = getImageFilePath(data);
            if (filePath != null) {
                bitmapProvaEmBranco = BitmapFactory.decodeFile(filePath);
                imageViewProvaEmBranco.setImageBitmap(bitmapProvaEmBranco);
            }
            if (bitmapGabarito != null && bitmapProvaEmBranco != null)
            button.setVisibility(View.VISIBLE);

        } else if (requestCode == REQUEST_TAKE_PHOTO_GABARITO && resultCode == RESULT_OK) {

            setPic(imageViewGabarito, this::setBitmapGabarito, fileGabarito.getAbsolutePath());
            galleryAddPic(fileGabarito.getAbsolutePath());
            if (bitmapGabarito != null && bitmapProvaEmBranco != null)
            button.setVisibility(View.VISIBLE);
        } else if (requestCode == IMAGE_RESULT_GABARITO && resultCode == RESULT_OK) {

            String filePath = getImageFilePath(data);
            if (filePath != null) {
                bitmapGabarito = BitmapFactory.decodeFile(filePath);
                imageViewGabarito.setImageBitmap(bitmapGabarito);
            }
            if (bitmapGabarito != null && bitmapProvaEmBranco != null)
            button.setVisibility(View.VISIBLE);
        }
    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }


    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }


    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "PID"
        );
        storageDir.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents

        return image;

    }


    private void dispatchTakePictureIntent(Consumer<File> setFile, int resultCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();
                setFile.accept(photoFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null && photoFile.exists()) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, resultCode);
            } else {
                System.out.println("NÃO EXISTE");
            }
        }


    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() throws IOException {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "PID"
        );

        mediaStorageDir.mkdirs();


        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist

        // Create a media file name
        File mediaFile;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";
        mediaFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                mediaStorageDir      /* directory */
        );
        return mediaFile;


    }

    private void storeImage(Bitmap image) {

        try {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG,
                        "******************* Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "**************** File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "**************** Error accessing file: " + e.getMessage());
        }
    }

    private void setPic(ImageView imageView, Consumer<Bitmap> setBitmap, String path) {
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

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        setBitmap.accept(bitmap);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        // FAB
        fabPhoto = findViewById(R.id.fab_photo);

        // Linear Layout
        subFab = findViewById(R.id.ls_layout);
        option1Layout = findViewById(R.id.option1);
        option2Layout = findViewById(R.id.option2);
        option3Layout = findViewById(R.id.option3);

        // View
        shadowView = findViewById(R.id.shadowView);
        imageViewProvaEmBranco = findViewById(R.id.imageView);
        imageViewGabarito = findViewById(R.id.imageView2);

        // Button
        button = findViewById(R.id.button);
//        obterProvaBranco = findViewById(R.id.button2);
//        obterGabarito = findViewById(R.id.button3);
    }


    private static final String TAG = "MYAPP::OPENCV";


    BaseLoaderCallback mCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mCallBack);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
