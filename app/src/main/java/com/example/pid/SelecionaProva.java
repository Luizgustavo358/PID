package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

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
    Button btnCamera;
    private File fileProvaEmBranco;
    static final int REQUEST_TAKE_PHOTO_PROVA_BRANCO = 1;
    private final static int IMAGE_RESULT_PROVA_BRANCO = 200;

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
    }

    public void setListeners() {
        btnCamera.setOnClickListener(v -> {
            getAlertDialog(this::setFileProvaEmBranco, REQUEST_TAKE_PHOTO_PROVA_BRANCO, IMAGE_RESULT_PROVA_BRANCO).show();
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
        Intent intent = new Intent(this, LinearizarProvaEmBranco.class);
        startActivity(intent);
    }
}
