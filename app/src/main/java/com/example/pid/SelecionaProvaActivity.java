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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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

public class SelecionaProvaActivity extends AppCompatActivity {
    // variaveis globais
    Button btnTiraFoto, btnTonsDeCinza, btnRedimensionar, btnLimiarizar;
    private File file;
    static final int REQUEST_TAKE_PHOTO_PROVA_BRANCO = 1;
    private final static int IMAGE_RESULT_PROVA_BRANCO = 200;
    private Bitmap bitmapProva;
    private ImageView imageViewProva;
    private final static int THRESHOLD = 30;
    private TextView titleTextView;
    private TipoProva tipoProva;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_prova);
        OpenCVLoader.initDebug();

        Intent intent = getIntent();
        String title = intent.getStringExtra("TITLE");
        tipoProva = (TipoProva) intent.getSerializableExtra("TIPO_PROVA");
        files = (File[]) intent.getSerializableExtra("FILE");


        System.out.println(this.getClass().getCanonicalName());
        System.out.println(tipoProva.name());
        printFiles(files);
        System.out.println();

        initializeViews();

        titleTextView.setText(title);

        setListeners();
    }

    /**
      * Initializes the views of the activity
      */
    private void initializeViews() {
        titleTextView = findViewById(R.id.title);
        btnTiraFoto = findViewById(R.id.btnTirarFoto);
        btnTonsDeCinza = findViewById(R.id.btnTonsDeCinza);
        imageViewProva = findViewById(R.id.imgFoto);
        btnRedimensionar = findViewById(R.id.btnRedimensionar);
        btnLimiarizar = findViewById(R.id.btnLimiarizar);
    }

    public void setListeners() {
        btnTiraFoto.setOnClickListener(v -> {
            getAlertDialog(this::setFile, REQUEST_TAKE_PHOTO_PROVA_BRANCO, IMAGE_RESULT_PROVA_BRANCO).show();

        });

        btnTonsDeCinza.setOnClickListener(v -> {
            setGreyScale(bitmapProva, imageViewProva);
            btnTonsDeCinza.setVisibility(View.GONE);
            btnRedimensionar.setVisibility(View.VISIBLE);
        });

        btnRedimensionar.setOnClickListener(v -> {
            long begin = System.nanoTime();
            Mat a = new Mat();
            Bitmap bmp32 = bitmapProva.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, a);

            MatOfPoint2f finalCorners = getCorners(a);
            if (finalCorners.toArray().length == 4) {
                Mat result = warpPerspective(a, finalCorners);

                Utils.matToBitmap(result, bitmapProva);

                imageViewProva.setImageBitmap(bitmapProva);
                btnRedimensionar.setVisibility(View.GONE);
                btnLimiarizar.setVisibility(View.VISIBLE);

               file = storeImage(bitmapProva);

            } else {
                System.out.println("ERROR");
                Toast.makeText(this, "Folha não identificada - primeira foto", Toast.LENGTH_LONG).show();
            }
            long end = System.nanoTime();
            System.out.printf("EXECUTION TIME - PERSPECTIVE: %d\n", end - begin);
        });

        btnLimiarizar.setOnClickListener(v -> {
            Intent intent = new Intent(this, LimiarizarActivity.class);

            files[tipoProva.ordinal()] = file;
            intent.putExtra("FILE", files);
            intent.putExtra("TIPO_PROVA", tipoProva);
            startActivity(intent);
        });
    }




    private static double getDistance(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
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


    private static Size getRectangleSize(MatOfPoint2f rectangle) {
        Point[] corners = rectangle.toArray();

        double top = getDistance(corners[0], corners[1]);
        double right = getDistance(corners[1], corners[2]);
        double bottom = getDistance(corners[2], corners[3]);
        double left = getDistance(corners[3], corners[0]);

        double averageWidth = (top + bottom) / 2f;
        double averageHeight = (right + left) / 2f;

        return new Size(new Point(averageWidth, averageHeight));
    }

    private Mat warpPerspective(Mat mat, MatOfPoint2f finalCorners) {
        Size size = getRectangleSize(finalCorners);
        Mat result = Mat.zeros(size, mat.type());
        // STEP 10: Homography: Use findHomography to find the affine transformation of your paper sheet
        Mat homography;
        MatOfPoint2f dstPoints = new MatOfPoint2f();
        Point[] arrDstPoints = {new Point(result.cols(), result.rows()), new Point(0, result.rows()), new Point(0, 0), new Point(result.cols(), 0)};
        dstPoints.fromArray(arrDstPoints);
        homography = Calib3d.findHomography(finalCorners, dstPoints);

        // STEP 11: Warp the input image using the computed homography matrix
        Imgproc.warpPerspective(mat, result, homography, size);

        Size out = new Size(bitmapProva.getWidth(), bitmapProva.getHeight());
        Imgproc.resize(result, result, out);
        return result;
    }

    private MatOfPoint2f getCorners(Mat mat) {
        final double DOWNSCALE_IMAGE_SIZE = 1000;
        // STEP 1: Resize input image to img_proc to reduce computation
        double ratio = DOWNSCALE_IMAGE_SIZE / Math.max(mat.width(), mat.height());
        Size downscaledSize = new Size(mat.width() * ratio, mat.height() * ratio);
        Mat dst = new Mat(downscaledSize, mat.type());
        Imgproc.resize(mat, dst, downscaledSize);
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // STEP 2: convert to grayscale
        Imgproc.cvtColor(dst, grayImage, Imgproc.COLOR_BGR2GRAY);

        // STEP 3: try to filter text inside document
        Imgproc.medianBlur(grayImage, detectedEdges, 9);

        // STEP 4: Edge detection
        Mat edges = new Mat();
        // Imgproc.erode(edges, edges, new Mat());
        // Imgproc.dilate(edges, edges, new Mat(), new Point(-1, -1), 1); // 1
        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, edges, THRESHOLD, THRESHOLD * 3, 3, true);

        // STEP 5: makes the object in white bigger to join nearby lines
        Imgproc.dilate(edges, edges, new Mat(), new Point(-1, -1), 1); // 1


        //Utils.matToBitmap(edges, bitmapProva);
        //imageViewProva.setImageBitmap(bitmapProva);
        //Image imageToShow = Utils.mat2Image(edges);
        //updateImageView(cannyFrame, imageToShow);

        // STEP 6: Compute the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // STEP 7: Sort the contours by length and only keep the largest one
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint matOfPoint = contours.get(i);

        }
        MatOfPoint largestContour = getMaxContour(contours);

        // STEP 8: Generate the convex hull of this contour
        Mat convexHullMask = Mat.zeros(mat.rows(), mat.cols(), mat.type());
        MatOfInt hullInt = new MatOfInt();
        Imgproc.convexHull(largestContour, hullInt);
        MatOfPoint hullPoint = getNewContourFromIndices(largestContour, hullInt);

        // STEP 9: Use approxPolyDP to simplify the convex hull (this should give a quadrilateral)
        MatOfPoint2f polygon = new MatOfPoint2f();
        Imgproc.approxPolyDP(convert(hullPoint), polygon, 20, true);
        List<MatOfPoint> tmp = new ArrayList<>();
        tmp.add(convert(polygon));
        restoreScaleMatOfPoint(tmp, ratio);
        Imgproc.drawContours(convexHullMask, tmp, 0, new Scalar(25, 25, 255), 2);
        // Image extractImageToShow = Utils.mat2Image(convexHullMask);
        // updateImageView(extractFrame, extractImageToShow);
        MatOfPoint2f finalCorners = new MatOfPoint2f();
        Point[] tmpPoints = polygon.toArray();
        for (Point point : tmpPoints) {
            point.x = point.x / ratio;
            point.y = point.y / ratio;
        }
        finalCorners.fromArray(tmpPoints);
        boolean clockwise = true;
        double currentThreshold = THRESHOLD;
        return finalCorners;
    }

    private static MatOfPoint getMaxContour(List<MatOfPoint> contours) {
        double maxVal = 0;
        MatOfPoint largestContour = null;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                largestContour = contours.get(contourIdx);
            }
        }
        return largestContour;
    }


    /**
     * Creates a new simplified contour from an original contour by extracting points defined by their indices in the original contour
     *
     * @param origContour The original contour
     * @param indices     Indices of points to extract
     */
    public static MatOfPoint getNewContourFromIndices(MatOfPoint origContour, MatOfInt indices) {
        int height = (int) indices.size().height;
        MatOfPoint2f newContour = new MatOfPoint2f();
        newContour.create(height, 1, CvType.CV_32FC2);
        for (int i = 0; i < height; ++i) {
            int index = (int) indices.get(i, 0)[0];
            double[] point = new double[]{
                    origContour.get(index, 0)[0],
                    origContour.get(index, 0)[1]
            };
            newContour.put(i, 0, point);
        }
        return convert(newContour);
    }

    /**
     * Converts from MatOfPoint to MatOfPoint2f and vice versa
     *
     * @param mat Input Mat
     * @return Converted Mat
     */
    public static MatOfPoint2f convert(MatOfPoint mat) {
        return new MatOfPoint2f(mat.toArray());
    }

    public static MatOfPoint convert(MatOfPoint2f mat) {
        return new MatOfPoint(mat.toArray());
    }

    public void setFile(File file) {
        this.file = file;
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



    private static void restoreScaleMatOfPoint(List<MatOfPoint> tmp, double ratio) {
        for (MatOfPoint matOfPoint : tmp) {
            for (Point point : matOfPoint.toList()) {
                point.x = point.x / ratio;
                point.y = point.y / ratio;
            }
        }
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
        Intent intent = new Intent(this, TelaInicialActivity.class);
        startActivity(intent);
    }

    /**
     *
     * @param view
     */
    public void telaLinearizaProva(View view) {

        Intent intent = new Intent(getApplicationContext(), LimiarizarActivity.class);

        intent.putExtra("prova", file);

        startActivity(intent);
    }


    private void setPic(Consumer<Bitmap> setBitmap, String path) {
        int targetW = imageViewProva.getWidth();
        int targetH = imageViewProva.getHeight();

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

    public void setBitmapProva(Bitmap bitmapProva) {
        this.bitmapProva = bitmapProva;
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);


        if (path != null) {
            bitmapProva = BitmapFactory.decodeFile(path);
            try {
                ExifInterface exif = new ExifInterface(path);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);


                if (orientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmapProva = Bitmap.createBitmap(bitmapProva, 0, 0, bitmapProva.getWidth(), bitmapProva.getHeight(), matrix, true);
                } else if(orientation == 8){
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    bitmapProva = Bitmap.createBitmap(bitmapProva, 0, 0, bitmapProva.getWidth(), bitmapProva.getHeight(), matrix, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageViewProva.setImageBitmap(bitmapProva);
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

            setPic(imageViewProva, this::setBitmapProva, file.getAbsolutePath());
            galleryAddPic(file.getAbsolutePath());
            btnTonsDeCinza.setVisibility(View.VISIBLE);
            btnRedimensionar.setVisibility(View.GONE);
            btnLimiarizar.setVisibility(View.GONE);
        } else if (requestCode == IMAGE_RESULT_PROVA_BRANCO && resultCode == Activity.RESULT_OK) {

            String filePath = getImageFilePath(data);
            if (filePath != null) {
                bitmapProva = BitmapFactory.decodeFile(filePath);
                changeOrientation(filePath);


                imageViewProva.setImageBitmap(bitmapProva);
            }

            btnTonsDeCinza.setVisibility(View.VISIBLE);
            btnRedimensionar.setVisibility(View.GONE);
            btnLimiarizar.setVisibility(View.GONE);
        }
    }

    private void changeOrientation(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            if (orientation == 6) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmapProva = Bitmap.createBitmap(bitmapProva, 0, 0, bitmapProva.getWidth(), bitmapProva.getHeight(), matrix, true);
            } else if(orientation == 8){
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                bitmapProva = Bitmap.createBitmap(bitmapProva, 0, 0, bitmapProva.getWidth(), bitmapProva.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Mat setGreyScale(Bitmap bitmap, ImageView imageView) {
        long begin = System.nanoTime();
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat destination = new Mat();
        Imgproc.cvtColor(mat, destination, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(destination, bitmap);
        imageView.setImageBitmap(bitmap);
        long end = System.nanoTime();
        System.out.printf("EXECUTION TIME - GREYSCALE: %d\n", end - begin);
        return destination;
    }

    public static void printFiles(File[] files) {
        System.out.println("FILE ARRAY: ");
        int i = 0;
        for (File file : files) {
            System.out.println("" + i++ +": " + (file == null ? "FILE NULO" : "FILE NAO NULO"));

        }
    }
}
