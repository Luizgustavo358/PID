package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LinearizarProvaEmBranco extends AppCompatActivity {

    public Bitmap originalBitmap;
    public ImageView provaEmBranco;
    private double thresh = 170;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linearizar_prova_em_branco);


        initializeViews();

        this.seekBar.setProgress(170);
        colocaImagem();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LinearizarProvaEmBranco.this.thresh = progress;
                setBinary(LinearizarProvaEmBranco.this.provaEmBranco);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        provaEmBranco = (ImageView) findViewById(R.id.provaEmBranco);
        seekBar = findViewById(R.id.seekBar);
    }

    private void colocaImagem() {
        File file = (File) getIntent().getSerializableExtra("FILE");

        String filePath = file.getPath();

        originalBitmap = BitmapFactory.decodeFile(filePath);
        provaEmBranco.setImageBitmap(originalBitmap);

        setBinary(provaEmBranco);


    }

    public void selecionarProvaEmBranco(View view) {
        Intent intent = new Intent(this, SelecionaProva.class);
        startActivity(intent);
    }

    private Mat setBinary(ImageView imageView) {

        Mat source = new Mat();
        Utils.bitmapToMat(originalBitmap, source);

        Mat dest = new Mat();
        //Imgproc.adaptiveThreshold(destination, destination2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 7);
        Imgproc.threshold(source, dest, thresh, 255, Imgproc.THRESH_BINARY);
        Bitmap bitmap = convertMatToBitMap(dest);
        storeImage(bitmap);
        imageView.setImageBitmap(bitmap);
        return dest;
    }

    private File storeImage(Bitmap image) {

        try {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                System.out.println("******************* Error creating media file, check storage permissions: ");// e.getMessage());
                return null;
            }
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return pictureFile;
        } catch (FileNotFoundException e) {
            System.out.println("**************** File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("**************** Error accessing file: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() throws IOException {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "PID"
        );

        mediaStorageDir.mkdirs();

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

    private static Bitmap convertMatToBitMap(Mat input) {
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
        return bmp;
    }

    public void selecionaGabarito(View view) {
        Intent intent = new Intent(this, SelecionaGabarito.class);
        startActivity(intent);
    }
}
