package com.example.android.nsdchat;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    //    public String path = Environment.getDataDirectory().getAbsolutePath() + "/storage/emulated/0/Pictures/Cam";
    Camera.PictureCallback mPictureCallback;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
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

//                    mBitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                    if(mBitmap!=null){
//                        Log.e(LOG_TAG,"Bitmap not null.");
//                        if (mBitmap.getWidth() > mBitmap.getHeight()) {
//                            Matrix matrix = new Matrix();
//                            matrix.postRotate(90);
//                            mBitmap = Bitmap.createBitmap(mBitmap , 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
//
//                            bytes=mBitmap.get
//                        }
//                    }
//                    else
//                        Log.e(LOG_TAG, "Bitmap not null.");

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

                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        if (b == true) {
//                            mCamera.takePicture(null, null, mPictureCallback);
//                            mCamera.startPreview();

                        } else {
                            Toast.makeText(getApplicationContext(), "Not Sharp Focussed", Toast.LENGTH_SHORT).show();
                        }

                        if (mCamera.getParameters().getFocusMode() == Camera.Parameters.FOCUS_MODE_AUTO) {
                            mCamera.cancelAutoFocus();
                        }

                    }
                });
                new PictureTask().execute();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open();
        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT)
            mCamera.setDisplayOrientation(90);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
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
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size mPreviewSize = getOptimalPreviewSize(previewSizes, w, h);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FLASH_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
        mCamera.startPreview();
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
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

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

    //temporary method put here
    public int getOrientation() {
        if (getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels) {
            Toast t = Toast.makeText(this, "LANDSCAPE", Toast.LENGTH_SHORT);
            t.show();
            return Configuration.ORIENTATION_LANDSCAPE;
        }

        Toast t = Toast.makeText(this, "PORTRAIT", Toast.LENGTH_SHORT);
        t.show();
        return Configuration.ORIENTATION_PORTRAIT;
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