package com.example.android.nsdchat;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;

@SuppressWarnings("deprecation")
public class CameraView extends Activity implements SurfaceHolder.Callback {

    private final String LOG_TAG = "CameraView";
    boolean mPreviewRunning;
    Camera mCamera;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    Button mCapture;
    int lastImageRotation;
    Camera.PictureCallback mPictureCallback;
    Camera.PreviewCallback mPreviewCallback;
    private OrientationEventListener mOrientationEventListener;
    private int mFrameNumber = 1;
    private long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SURFACE_TYPE_PUSH_BUFFERS);
        mPreviewRunning = false;

//        File mFolder = new File(path);
//        if (!mFolder.exists())
//            mFolder.mkdir();
        mCapture = (Button) findViewById(R.id.capture);

        mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                String date = dateFormat.format(new Date());
                String fName = "Image-" + date + ".jpg";

                File pictureFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/", fName);
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    Log.e(LOG_TAG, pictureFile.toString());
                    fos.write(bytes);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };


        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO) && mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    new PictureTask().execute();
                    Log.d(LOG_TAG, "No auto focus");
                } else if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                    new PictureTask().execute();
                } else {
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {
                            if (b) {
                                new PictureTask().execute();
                            } else {
                                Toast.makeText(getApplicationContext(), "Not Sharp Focussed", Toast.LENGTH_SHORT).show();
                            }
                            if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                mCamera.cancelAutoFocus();
                            }
                        }
                    });
                    Log.d(LOG_TAG, "auto focus");
                }
            }
        });
        mPreviewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mFrameNumber == 1) {
                    start = System.currentTimeMillis();
                    mFrameNumber = 2;
                    return;
                }
                long now = System.currentTimeMillis();
                float fps = (float) (now - start) / mFrameNumber;
                Log.d(LOG_TAG, "FPS: " + Float.toString(fps));
                mFrameNumber++;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open();
        if (mCamera != null) {
            //mCamera.setDisplayOrientation(90);
            mSurfaceHolder.addCallback(this);
            mSurfaceView.setVisibility(View.VISIBLE);

            mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

                public void onOrientationChanged(int orientation) {

                    if (orientation == ORIENTATION_UNKNOWN)
                        return;

                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(0, info);

                    orientation = (orientation + 45) / 90 * 90;
                    int rotation;

                    rotation = (info.orientation + orientation) % 360;


                    Camera.Parameters params = mCamera.getParameters();
                    if (lastImageRotation != rotation) {
                        params.setRotation(rotation);
                        mCamera.setParameters(params);

                        Log.d(LOG_TAG, "onOrientationChanged");
                        lastImageRotation = rotation;
                    }
                }

            };
        }

        if (mOrientationEventListener.canDetectOrientation())
            mOrientationEventListener.enable();

    }


    @Override
    protected void onPause() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mSurfaceHolder.removeCallback(this);
            mSurfaceView.setVisibility(View.GONE);
            mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            mOrientationEventListener.disable();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        mCamera = Camera.open();
//        mCamera.setDisplayOrientation(90);

//        try {
//            mCamera.setPreviewDisplay(mSurfaceHolder);
//        } catch (IOException e) {
//            mCamera.release();
//            mCamera = null;
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        Log.d(LOG_TAG, "Surface Changed");
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size mPreviewSize = getOptimalPreviewSize(previewSizes, w, h);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPreviewFpsRange(30000, 30000);
        if (parameters.getAutoExposureLock())
            parameters.setAutoExposureLock(true);
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            Log.d(LOG_TAG, Camera.Parameters.FOCUS_MODE_INFINITY);
        } else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            Log.d(LOG_TAG, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, camInfo);
        int cameraRotationOffset = camInfo.orientation;

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        int displayRotation;


        // back-facing
        displayRotation = (cameraRotationOffset - degrees + 360) % 360;
        mCamera.setDisplayOrientation(displayRotation);

        Log.d(LOG_TAG, "cameraRotationOffset:" + cameraRotationOffset);
        Log.d(LOG_TAG, "degrees:" + degrees);
        Log.d(LOG_TAG, "displayRotation:" + displayRotation);


        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(mPreviewCallback);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
        if (mCamera != null) {
            mCamera.startPreview();
        }
        mPreviewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mPreviewRunning = false;
            mCamera = null;
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    private class PictureTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mCamera.startPreview();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            mCamera.takePicture(null, null, mPictureCallback);

            try {
                Thread.sleep(1000);     //Captured Image Preview for 1 second
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, "Interrupted: " + ex);
            }

            return null;
        }
    }

}