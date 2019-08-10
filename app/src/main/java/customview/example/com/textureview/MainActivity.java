package customview.example.com.textureview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Size previewsize;
    Size[] jpegSizes;
    String cameraID;

    TextureView textureView;
    ImageButton imageButton;
    CameraManager cameraManager;
    CameraCharacteristics cameraCharacteristics;
    CameraDevice cameraDevice;
    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraCaptureSessions;
    ImageReader imageReader;

    HandlerThread handlerThread;
    Handler handler;


    int rotation;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }


    float x = 0;
    float y = 0;
    private byte[] const_byte;
    Canvas mCanvas;
    Bitmap mBitmap;

    public class CustomView extends View {


        Paint paint;
        private Bitmap bmp;

        public CustomView(Context context) {
            super(context);
            mBitmap = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888);
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

//            if (const_byte != null)
//            {
//                BitmapFactory.Options optio = new BitmapFactory.Options();
//                optio.inSampleSize = 1;
//
//                bmp = BitmapFactory.decodeByteArray(const_byte, 0,
//                        const_byte.length, optio);
//            }


//            final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

            canvas.drawCircle(x, y, 50, paint);



//            mCanvas = canvas
//            canvas.drawBitmap(bmp, rect, rect, paint);
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                x = event.getX();
                y = event.getY();
                invalidate();
            }
            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);



        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.hardware.camera2.full","android.hardware.camera2.autofocus",
                "android.permission.READ_EXTERNAL_STORAGE"}, 100);

        textureView = (TextureView) findViewById(R.id.textureView);
        imageButton = (ImageButton) findViewById(R.id.imageButton);

        textureView.setSurfaceTextureListener(surfaceTextureListener);

        RelativeLayout parent_layout = (RelativeLayout) findViewById(R.id.parent_layout);
        parent_layout.addView(new CustomView(MainActivity.this));

        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x = event.getX();
                    y = event.getY();

                }

                getPicture();
                return false;
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


    }


    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };


    private void openCamera() {

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);


        try {

            cameraID = cameraManager.getCameraIdList()[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            previewsize = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(cameraID, stateCallback, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startCamera();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int i) {
            cameraDevice.close();
            cameraDevice = null;

        }
    };


    private void startCamera() {

        try {

            if (cameraDevice == null || !textureView.isAvailable() || previewsize == null)
                return;

            SurfaceTexture texture = textureView.getSurfaceTexture();

            if (texture == null)
                return;

            texture.setDefaultBufferSize(previewsize.getWidth(), previewsize.getHeight());
            Surface surface = new Surface(texture);


            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);


            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    if (null == cameraDevice)
                        return;

//                    L.get().toast(MainActivity.this, "On ConFig");

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();


                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();


                }
            }, handler);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void updatePreview() {

        if (null == cameraDevice)
            return;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {

            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, handler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private void getPicture() {

        if (cameraDevice == null)
            return;

//        CameraManager cameraManager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);




        try {

//        CameraCharacteristics cameraCharacteristics=cameraManager.getCameraCharacteristics(cameraDevice.getId());

            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());

            if (cameraCharacteristics != null)
                jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);


//            int width = 640, height = 480;
            int width = 1920, height = 1080;

            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }


//            ImageReader imageReader=ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);


            List<Surface> outputSurfaces = new ArrayList<Surface>(2);

            outputSurfaces.add(imageReader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            rotation = getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));

//            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
            ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {

                    Image image = null;


                    try {
                        image = imageReader.acquireLatestImage();

                        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();

                        byte[] bytes = new byte[byteBuffer.capacity()];
                        const_byte = bytes;

                        byteBuffer.get(bytes);

//                        Log.d("",bytes.toString());
//                      OutputStream  outputStream = new FileOutputStream(file);
//                        outputStream.write(bytes);

                        save(bytes);

                    } catch (Exception e) {

                    } finally {
                        if (image != null)
                            image.close();
                    }



                }






                public void save(byte[] bytes) {

                    File file = getOutputMediaFile();

                    OutputStream outputStream = null;

                    try {




                BitmapFactory.Options optio = new BitmapFactory.Options();
                optio.inSampleSize = 1;

                        mBitmap = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888);


                        mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, optio);

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);



                        Canvas canvas = new Canvas(mBitmap);
                        canvas.drawCircle(60, 50, 25, paint);


                        SessionData.getInstance().setImgBitmap(mBitmap);

                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);

                        Log.d("Image", bytes.toString());

                        startActivity(new Intent(MainActivity.this, ImageActivity.class));


                    } catch (IOException e) {

                        e.printStackTrace();

                    } finally {

                        try {

                            if (outputStream != null)
                                outputStream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                }


            };


            imageReader.setOnImageAvailableListener(imageAvailableListener, handler);

            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    startCamera();
                }

                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);


                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureCallback, handler);
                    } catch (Exception e) {

                    }


                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, handler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "TextureView");
        mediaStorageDir.mkdirs();

        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                L.get().toast(MainActivity.this, "failed to create directory");

                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (cameraDevice != null) {
            cameraDevice.close();
        }

        stopbackgroundhandler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startbackgroundhandler();
    }

    private void startbackgroundhandler(){

        handlerThread  = new HandlerThread("Camera Background");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    private void stopbackgroundhandler(){

        handlerThread.quitSafely();

        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }







}
