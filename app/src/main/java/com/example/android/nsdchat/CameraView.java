package com.example.android.nsdchat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
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
import java.util.Random;

import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;


@SuppressWarnings("deprecation")
public class CameraView extends Activity implements SurfaceHolder.Callback {
    private final String LOG_TAG = "CameraView";
    boolean mPreviewRunning;
    Camera mCamera;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    Bitmap bitmap;
    Button mCapture;
    public String path = Environment.getDataDirectory().getAbsolutePath() + "/storage/emulated/0/Pictures/Cam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SURFACE_TYPE_PUSH_BUFFERS);
        mPreviewRunning = false;

        File mFolder = new File(path);
        if(!mFolder.exists())
            mFolder.mkdir();
        mCapture = (Button) findViewById(R.id.capture);
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        Random generator = new Random();
                        int n = 1000;
                        n = generator.nextInt(n);
                        String fName = "Image-" + n + ".jpg";
                        File pictureFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/", fName);
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            Log.e(LOG_TAG, pictureFile.toString());
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
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
                });
            }
        });
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
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
//        if (mPreviewRunning){
//            mCamera.stopPreview();
//        }
//        Camera.Parameters p = mCamera.getParameters();
//        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
//        Camera.Size previewSize = previewSizes.get(0);
//        p.setPreviewSize(previewSize.width, previewSize.height);
//        mCamera.setParameters(p);
//        try{
//            mCamera.setPreviewDisplay(surfaceHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
        mPreviewRunning = false;
        mCamera = null;
    }
}
