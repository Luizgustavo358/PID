package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
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

import static com.example.pid.SelecionaProvaActivity.printFiles;

public class LimiarizarActivity extends AppCompatActivity {

    Button btnVisualizar, btnCancelar, btnOk;
    public Bitmap originalBitmap;
    public ImageView provaEmBranco;
    private double thresh = 170;
    private SeekBar seekBar;
    TextView limiar;
    TipoProva tipoProva;
    File [] files;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limiarizar);
        OpenCVLoader.initDebug();


        Intent intent = this.getIntent();
        tipoProva = (TipoProva) intent.getSerializableExtra("TIPO_PROVA");
        files = (File[]) intent.getSerializableExtra("FILE");
        file = files[tipoProva.ordinal()];

        System.out.println(this.getClass().getCanonicalName());
        System.out.println(tipoProva.name());
        printFiles(files);
        System.out.println();

        initializeViews();

        this.seekBar.setProgress(170);
        colocaImagem();
        setListeners();
    }

    private void setListeners() {
        btnVisualizar.setOnClickListener(v -> setBinary(LimiarizarActivity.this.provaEmBranco));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LimiarizarActivity.this.thresh = progress;
                LimiarizarActivity.this.limiar.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnOk.setOnClickListener(v -> {
            if(tipoProva == TipoProva.PROVA_EM_BRANCO) {
                Intent intent = new Intent(this, SelecionaProvaActivity.class);
                intent.putExtra("TITLE", "Gabarito");
                intent.putExtra("TIPO_PROVA", TipoProva.GABARITO);
                intent.putExtra("FILE", files);
                startActivity(intent);
            } else if (tipoProva == TipoProva.GABARITO){
                Intent intent = new Intent(this, SelecionaProvaActivity.class);
                intent.putExtra("TITLE", "Resposta");
                intent.putExtra("TIPO_PROVA", TipoProva.RESPOSTA);
                intent.putExtra("FILE", files);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, NotaAlunoActivity.class);
                intent.putExtra("FILE", files);
                startActivity(intent);
            }
        });
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        provaEmBranco = findViewById(R.id.provaEmBranco);
        seekBar = findViewById(R.id.seekBar);
        btnOk = findViewById(R.id.btnOk);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnVisualizar = findViewById(R.id.btnVisualizar);
        limiar = findViewById(R.id.limiar);
    }

    private void colocaImagem() {


        String filePath = file.getPath();

        originalBitmap = BitmapFactory.decodeFile(filePath);
        provaEmBranco.setImageBitmap(originalBitmap);

        setBinary(provaEmBranco);


    }

    public void retornar(View view) {
        finish();
    }

    private Mat setBinary(ImageView imageView) {

        Mat source = new Mat();
        Utils.bitmapToMat(originalBitmap, source);

        Mat dest = new Mat();
        Imgproc.threshold(source, dest, thresh, 255, Imgproc.THRESH_BINARY);
        Bitmap bitmap = convertMatToBitMap(dest);
        file = storeImage(bitmap);
        System.out.println(file == null ? "FILE NULO" : "FILE NAO NULO");
        files[tipoProva.ordinal()] = file;
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


}
