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
import android.media.Image;
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
import androidx.core.app.CoreComponentFactory;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings({"unused", "ConstantConditions"})
public class MainActivity extends AppCompatActivity {


    private LinearLayout subFab;

    private View shadowView;

    private FloatingActionButton fabPhoto;

    private Animation hideLayout;
    private Animation hideShadow;
    private Animation showLayout;
    private Animation showShadow;

    private Button button;

    private Bitmap bitmapProvaEmBranco;
    private ImageView imageViewProvaEmBranco;
    private File fileProvaEmBranco;
    private LinearLayout option1Layout;
    static final int REQUEST_TAKE_PHOTO_PROVA_BRANCO = 1;
    private final static int IMAGE_RESULT_PROVA_BRANCO = 200;

    private Bitmap bitmapGabarito;
    private ImageView imageViewGabarito;
    private LinearLayout option2Layout;
    private File fileGabarito;
    static final int REQUEST_TAKE_PHOTO_GABARITO = 2;
    private final static int IMAGE_RESULT_GABARITO = 201;
    private RecyclerView recyclerViewProvas;

    private RecyclerView recyclerView;
    private ImagensAdapter adapter;
    private LinearLayout option3Layout;
    private static final int RESQUEST_TAKE_PHOTO_ALUNO = 3;
    private final static int IMAGE_RESULT_ALUNO = 202;


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
     * Creates a new simplified contour from an original contour by extracting points defined by their indices in the original contour
     * @param origContour The original contour
     * @param indices Indices of points to extract
     */
    public static MatOfPoint getNewContourFromIndices(MatOfPoint origContour, MatOfInt indices) {
        int height = (int) indices.size().height;
        MatOfPoint2f newContour = new MatOfPoint2f();
        newContour.create(height, 1, CvType.CV_32FC2);
        for (int i = 0; i < height; ++i) {
            int index = (int) indices.get(i, 0)[0];
            double[] point = new double[] {
                    origContour.get(index, 0)[0],
                    origContour.get(index, 0)[1]
            };
            newContour.put(i, 0, point);
        }
        return convert(newContour);
    }

    /**
     * Converts from MatOfPoint to MatOfPoint2f and vice versa
     * @param mat Input Mat
     * @return Converted Mat
     */
    public static MatOfPoint2f convert(MatOfPoint mat) {
        return new MatOfPoint2f(mat.toArray());
    }
    public static MatOfPoint convert(MatOfPoint2f mat) {
        return new MatOfPoint(mat.toArray());
    }

    private static double getDistance(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
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

    private static void restoreScaleMatOfPoint(List<MatOfPoint> tmp, double ratio) {
        for (MatOfPoint matOfPoint : tmp) {
            for (Point point : matOfPoint.toList()) {
                point.x = point.x / ratio;
                point.y = point.y / ratio;
            }
        }
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

        option3Layout.setOnClickListener(v -> {
            ImageItem item = new ImageItem();
            adapter.getImageItems().add(item);
            getAlertDialog(item::setFile, RESQUEST_TAKE_PHOTO_ALUNO, IMAGE_RESULT_ALUNO).show();
        });

        button.setOnClickListener(v -> {


            Mat mat = new Mat();
            Bitmap bmp32 = bitmapProvaEmBranco.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, mat);




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
            Imgproc.Canny(detectedEdges, edges, 30, 30 * 3, 3, true);

            // STEP 5: makes the object in white bigger to join nearby lines
            Imgproc.dilate(edges, edges, new Mat(), new Point(-1, -1), 1); // 1

            //Utils.matToBitmap(edges, bitmapProvaEmBranco);
            //imageViewProvaEmBranco.setImageBitmap(bitmapProvaEmBranco);
            //Image imageToShow = Utils.mat2Image(edges);
            //updateImageView(cannyFrame, imageToShow);

            // STEP 6: Compute the contours
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
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
            double currentThreshold = 30;
            if (finalCorners.toArray().length == 4) {
                Size size = getRectangleSize(finalCorners);
                Mat result = Mat.zeros(size, mat.type());
                // STEP 10: Homography: Use findHomography to find the affine transformation of your paper sheet
                Mat homography;
                MatOfPoint2f dstPoints = new MatOfPoint2f();
                Point[] arrDstPoints = { new Point(result.cols(), result.rows()), new Point(0, result.rows()), new Point(0, 0), new Point(result.cols(), 0) };
                dstPoints.fromArray(arrDstPoints);
                homography = Calib3d.findHomography(finalCorners, dstPoints);

                // STEP 11: Warp the input image using the computed homography matrix
                Imgproc.warpPerspective(mat, result, homography, size);

                Size out = new Size(bitmapProvaEmBranco.getWidth(), bitmapProvaEmBranco.getHeight());
                Imgproc.resize(result, result, out);





                Utils.matToBitmap(result, bitmapProvaEmBranco);

                Mat branco = setBinary(bitmapProvaEmBranco, imageViewProvaEmBranco, setGreyScale(bitmapProvaEmBranco, imageViewProvaEmBranco));
                Mat gabarito = setBinary(bitmapGabarito, imageViewGabarito, setGreyScale(bitmapGabarito, imageViewGabarito));
                Utils.matToBitmap(branco, bitmapProvaEmBranco);
                imageViewProvaEmBranco.setImageBitmap(bitmapProvaEmBranco);


                storeImage(bitmapProvaEmBranco);
            }










            //Mat branco = setBinary(bitmapProvaEmBranco, imageViewProvaEmBranco, setGreyScale(bitmapProvaEmBranco, imageViewProvaEmBranco));
            //Mat bin = setBinary(bitmapGabarito, imageViewGabarito, setGreyScale(bitmapGabarito, imageViewGabarito));
            for (ImageItem imageItem : adapter.getImageItems()) {
                setBinary(imageItem.getBitmap(), setGreyScale(imageItem.getBitmap()));
                adapter.notifyDataSetChanged();
            }
        });
    }



    private static MatOfPoint getMaxContour(List<MatOfPoint> contours) {
        double maxVal = 0;
        MatOfPoint largestContour = null;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++){
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea){
                maxVal = contourArea;
                largestContour = contours.get(contourIdx);
            }
        }
        return largestContour;
    }


    /**
     * Object that encapsulates the contour and 4 points that makes the larger
     * rectangle on the image
     */
    public static class Quadrilateral {
        public MatOfPoint contour;
        public Point[] points;

        public Quadrilateral(MatOfPoint contour, Point[] points) {
            this.contour = contour;
            this.points = points;
        }
    }

    public static Quadrilateral findDocument(Mat inputRgba) {
        ArrayList<MatOfPoint> contours = findContours(inputRgba);
        Quadrilateral quad = getQuadrilateral(contours);
        return quad;
    }

    private static ArrayList<MatOfPoint> findContours(Mat src) {

       /* double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width, height);*/

        Mat resizedImage = new Mat(src.size(), CvType.CV_8UC4);
        Mat grayImage = new Mat(src.size(), CvType.CV_8UC4);
        Mat cannedImage = new Mat(src.size(), CvType.CV_8UC1);

        Imgproc.resize(src, resizedImage, src.size());
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 75, 200);

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        Collections.sort(contours, (lhs, rhs) -> Double.compare(Imgproc.contourArea(rhs), Imgproc.contourArea(lhs)));

        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    private static Quadrilateral getQuadrilateral(ArrayList<MatOfPoint> contours) {

        for (MatOfPoint c : contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            // select biggest 4 angles polygon
            if (points.length == 4) {
                Point[] foundPoints = sortPoints(points);

                return new Quadrilateral(c, foundPoints);
            }
        }

        return null;
    }

    private static Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = (lhs, rhs) -> Double.compare(lhs.y + lhs.x, rhs.y + rhs.x);

        Comparator<Point> diffComparator = (lhs, rhs) -> Double.compare(lhs.y - lhs.x, rhs.y - rhs.x);

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
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

    private Mat setBinary(Bitmap bitmap, ImageView imageView, Mat destination) {
        Mat destination2 = new Mat();
        Imgproc.adaptiveThreshold(destination, destination2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 7);
        Utils.matToBitmap(destination2, bitmap);
        storeImage(bitmap);
        imageView.setImageBitmap(bitmap);
        return destination2;
    }

    private Mat setBinary(Bitmap bitmap, Mat destination) {
        Mat destination2 = new Mat();
        Imgproc.adaptiveThreshold(destination, destination2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 7);
        Utils.matToBitmap(destination2, bitmap);
        storeImage(bitmap);
        return destination2;
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


    private Mat setGreyScale(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat destination = new Mat();
        Imgproc.cvtColor(mat, destination, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(destination, bitmap);
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
        } else if (requestCode == RESQUEST_TAKE_PHOTO_ALUNO && resultCode == RESULT_OK) {
            ImageItem last = adapter.last();
            setPic(last::setBitmap, last.getFile().getAbsolutePath());
            adapter.notifyDataSetChanged();
            galleryAddPic(last.getFile().getAbsolutePath());
            if (bitmapGabarito != null && bitmapProvaEmBranco != null)
                button.setVisibility(View.VISIBLE);
        } else if (requestCode == IMAGE_RESULT_ALUNO && resultCode == RESULT_OK) {
            ImageItem last = adapter.last();
            String filePath = getImageFilePath(data);
            if (filePath != null) {
                last.setBitmap(BitmapFactory.decodeFile(filePath));
                adapter.notifyDataSetChanged();
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


        return image;

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
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImagensAdapter();
        recyclerView.setAdapter(adapter);

        // Button
        button = findViewById(R.id.button);
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
