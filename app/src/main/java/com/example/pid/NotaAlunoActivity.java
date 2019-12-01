package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NotaAlunoActivity extends AppCompatActivity {

    File files[];
    ImageView imageView;
    Button btnComecarDeNovo, btnProximaProva;
    Mat branco, gabarito, resposta;
    private final static int THRESHOLD = 30;
    TextView txtErradas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);
        OpenCVLoader.initDebug();


        Intent intent = getIntent();
        files = (File[]) intent.getSerializableExtra("FILE");

        initializeViews();
        setListeners();
        try {
            setMats();

            long begin = System.nanoTime();

            int kernelSize = 4;
            Mat element4 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                    new Point(kernelSize, kernelSize));

            kernelSize = 2;
            Mat element2 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                    new Point(kernelSize, kernelSize));

            dilate(gabarito, gabarito, element4, 1);
            dilate(resposta, resposta, element4, 1);

            Core.subtract(branco, gabarito, gabarito);
            Core.subtract(branco, resposta, resposta);
            Mat result = new Mat();
            erode(gabarito, gabarito, element2, 1);
            dilate(gabarito, gabarito, element4, 2);
            dilate(resposta, resposta, element4, 2);
            erode(resposta, resposta, element4, 2);
            erode(resposta, resposta, element2, 3);
            Core.subtract(resposta, gabarito, result);

            dilate(result, result, element4, 2);




            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();

            Mat edges = new Mat();
            Imgproc.Canny(result, edges, THRESHOLD, THRESHOLD * 3, 3, true);

            Imgproc.dilate(edges, edges, new Mat(), new Point(-1, -1), 1); // 1

            Bitmap bmp = convertMatToBitMap(edges);
            imageView.setImageBitmap(bmp);

            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            System.out.println("Width: " + edges.width() + " Height: " + edges.height());

            int objects = 0;
            int margin = 50;
            for (MatOfPoint contour : contours) {
                Rect rect = Imgproc.boundingRect(contour);
                int x = rect.x;
                int y = rect.y;
                System.out.println("RECT X: " + x + " Y: " + y);
                if(x >= margin && x <= edges.width() - margin && y >= margin && y <= edges.height() - margin){
                    objects++;
                }
            }
            long end = System.nanoTime();
            System.out.printf("EXECUTION TIME - COMPUTE ERROS: %d\n", end - begin);


            Toast.makeText(this, "Objetos (Questões erradas): " + objects, Toast.LENGTH_LONG).show();
            txtErradas.setText("" + objects);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void erode(Mat src, Mat dst, Mat kernel, int iterations){
        for (int i = 0; i < iterations; i++) {
            Imgproc.erode(src, dst, kernel);
        }
    }

    private static void dilate(Mat src, Mat dst, Mat kernel, int iterations){
        for (int i = 0; i < iterations; i++) {
            Imgproc.dilate(src, dst, kernel);
        }
    }

    private void setListeners() {
        btnProximaProva.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelecionaProvaActivity.class);
            intent.putExtra("TITLE", "Resposta");
            intent.putExtra("TIPO_PROVA", TipoProva.RESPOSTA);
            intent.putExtra("FILE", files);
            startActivity(intent);
        });
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

    private void setMats() {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filePath = file.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            if (i == TipoProva.PROVA_EM_BRANCO.ordinal()) {
                branco = mat;
            } else if (i == TipoProva.GABARITO.ordinal()) {
                gabarito = mat;
            } else {
                resposta = mat;
            }
        }
    }


    private void initializeViews() {
        imageView = findViewById(R.id.imageView5);
        btnComecarDeNovo = findViewById(R.id.btnComecar);
        btnProximaProva = findViewById(R.id.btnProximaProva);
        txtErradas = findViewById(R.id.txtErradas);
    }

    /**
     * Volta para tela inicial.
     *
     * @param view
     */
    public void telaInicial(View view) {
        Intent intent = new Intent(this, TelaInicialActivity.class);
        startActivity(intent);
    }


}
