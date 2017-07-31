package com.example.kevinjustinus.opencv;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "MainActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean permissionGranted = false;

    // check if OpenCV successfully loaded into project
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
        
    }

    // HSV values
    // H = hue (color), pure green is 120 but in OpenCV it's divided by 2
    // that's why the hue value is 45-75 or 60+/-15
    // S = saturation, currently takes almost the whole range
    // V = value, currently takes almost the whole range, see Wikipedia
    int iLowH = 45;
    int iHighH = 75;
    int iLowS = 20;
    int iHighS = 255;
    int iLowV = 10;
    int iHighV = 255;

    Mat imgHSV, imgThresholded; // image matrix input, output

    Scalar sc1, sc2;
    JavaCameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set activity layout to landscape in fullscreen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // check if permission to camera has been granted
        if (!permissionGranted) {
            checkPermissions();
        }

        // lower and upper limits to HSV filter
        sc1 = new Scalar(iLowH, iLowS, iLowV);
        sc2 = new Scalar(iHighH, iHighS, iHighV);

        // not needed in our app, just in this demo
        cameraView = (JavaCameraView) findViewById(R.id.cameraview);
        cameraView.setCameraIndex(0); // 0 for rear and 1 for front
        cameraView.setCvCameraViewListener(this);
        cameraView.enableView();
    }

    // don't worry about this
    @Override
    protected void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        imgHSV = new Mat(width, height, CvType.CV_16UC4);
        imgThresholded = new Mat(width, height, CvType.CV_16UC4);
    }

    // don't worry about this
    @Override
    public void onCameraViewStopped() {

    }

    // IMPORTANT PIECE HERE!!!
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // convert image type of source into a matrix (BGR to HSV)
        Imgproc.cvtColor(inputFrame.rgba(), imgHSV, Imgproc.COLOR_BGR2HSV);
        // return indices in source image which satisfies the lower & upper boundaries
        Core.inRange(imgHSV, sc1, sc2, imgThresholded);
        return imgThresholded;
    }

    // don't worry about this
    private boolean checkPermissions() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE);
            return false;
        } else {
            return true;
        }

    }

}