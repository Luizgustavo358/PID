package com.example.pid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelecionaProva extends AppCompatActivity {
    // variaveis globais
    Button btnCamera, btnTiraFoto, limiariza;
    private File fileProvaEmBranco;
    static final int REQUEST_TAKE_PHOTO_PROVA_BRANCO = 1;
    private final static int IMAGE_RESULT_PROVA_BRANCO = 200;
    private Bitmap bitmapProvaEmBranco;
    private ImageView imageViewProvaEmBranco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_prova);

        Intent intent = getIntent();

        initializeViews();

        setListeners();
    }

    /**
      * Initializes the views of the activity
      */
    private void initializeViews() {
        btnCamera = findViewById(R.id.btnFotoProvaEmBranco);
        btnTiraFoto = findViewById(R.id.btnTirarFoto);
        limiariza = findViewById(R.id.btnLimiarizar);
        imageViewProvaEmBranco = findViewById(R.id.imgFoto);
    }

    public void setListeners() {
        btnTiraFoto.setOnClickListener(v -> {
            getAlertDialog(this::setFileProvaEmBranco, REQUEST_TAKE_PHOTO_PROVA_BRANCO, IMAGE_RESULT_PROVA_BRANCO).show();
            limiariza.setVisibility(v.VISIBLE);
        });
    }

    public void setFileProvaEmBranco(File fileProvaEmBranco) {
        this.fileProvaEmBranco = fileProvaEmBranco;
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

    private void dispatchTakePictureIntent(Consumer<File> setFile, int resultCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
                setFile.accept(photoFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    /**
     * Volta para tela inicial.
     * @param view
     */
    public void telaInicial(View view) {
        Intent intent = new Intent(this, TelaInicial.class);
        startActivity(intent);
    }

    /**
     *
     * @param view
     */
    public void telaLinearizaProva(View view) {

        Intent intent = new Intent(getApplicationContext(), LinearizarProvaEmBranco.class);

        intent.putExtra("prova", fileProvaEmBranco);

        startActivity(intent);
    }


    private void setPic(Consumer<Bitmap> setBitmap, String path) {
        int targetW = imageViewProvaEmBranco.getWidth();
        int targetH = imageViewProvaEmBranco.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        setBitmap.accept(bitmap);

    }
    private void setPic(ImageView imageView, Consumer<Bitmap> setBitmap, String path) {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        setBitmap.accept(bitmap);
        imageView.setImageBitmap(bitmap);
    }

    public void setBitmapProvaEmBranco(Bitmap bitmapProvaEmBranco) {
        this.bitmapProvaEmBranco = bitmapProvaEmBranco;
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);


        if (path != null) {
            bitmapProvaEmBranco = BitmapFactory.decodeFile(path);
            try {
                ExifInterface exif = new ExifInterface(path);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                Toast.makeText(this, "Branco Orientation: " + orientation, Toast.LENGTH_LONG).show();

                if (orientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmapProvaEmBranco = Bitmap.createBitmap(bitmapProvaEmBranco, 0, 0, bitmapProvaEmBranco.getWidth(), bitmapProvaEmBranco.getHeight(), matrix, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageViewProvaEmBranco.setImageBitmap(bitmapProvaEmBranco);
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




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO_PROVA_BRANCO && resultCode == RESULT_OK) {

            setPic(imageViewProvaEmBranco, this::setBitmapProvaEmBranco, fileProvaEmBranco.getAbsolutePath());
            galleryAddPic(fileProvaEmBranco.getAbsolutePath());

        } else if (requestCode == IMAGE_RESULT_PROVA_BRANCO && resultCode == Activity.RESULT_OK) {

            String filePath = getImageFilePath(data);
            if (filePath != null) {
                bitmapProvaEmBranco = BitmapFactory.decodeFile(filePath);
                changeOrientation(filePath);


                imageViewProvaEmBranco.setImageBitmap(bitmapProvaEmBranco);
            }


        }
    }

    private void changeOrientation(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Toast.makeText(this, "Branco Orientation: " + orientation, Toast.LENGTH_LONG).show();

            if (orientation == 6) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmapProvaEmBranco = Bitmap.createBitmap(bitmapProvaEmBranco, 0, 0, bitmapProvaEmBranco.getWidth(), bitmapProvaEmBranco.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
