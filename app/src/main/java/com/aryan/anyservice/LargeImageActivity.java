package com.aryan.anyservice;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.timroes.axmlrpc.XMLRPCException;
import helper.OdooRPC;

public class LargeImageActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "Touch";
    OdooRPC odooRPC;
    ProgressBar progressBar;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    ImageView iv;
    boolean first=true;
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_image);
        iv=findViewById(R.id.image);
        progressBar=findViewById(R.id.progress);
        int image=getIntent().getIntExtra("image",0);
        int orderId=getIntent().getIntExtra("order_id",0);
        HashMap map = new HashMap<String, String>();
        map.put("method", "load_image");
        map.put("model", "anyservice.service");
        if(orderId>0){
            map.put("order_id", orderId);
        }else {
            map.put("id", image);
        }


        AsyncOdooRPCcall task = new AsyncOdooRPCcall();
        task.execute(map);


    }


    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                if(first) {
                    first=false;
                    matrix.set(view.getImageMatrix());
                    savedMatrix.set(matrix);
                }else{
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                }
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    float newDist = spacing(event);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        matrix.postScale(scale, scale,mid.x,mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen
        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
    }
    class AsyncOdooRPCcall extends AsyncTask<HashMap<String,Object>, ProgressBar,HashMap<String,Object>> {

        @Override
        protected HashMap<String,Object> doInBackground(HashMap<String, Object>... hashMaps) {
            HashMap<String, Object> result=null;
            try {
                if (odooRPC == null) {
                    odooRPC = new OdooRPC();
                    try {
                        odooRPC.login();
                    } catch (XMLRPCException e) {
                        e.printStackTrace();
                    }
                }
                String method = (String) hashMaps[0].get("method");
                String model = (String) hashMaps[0].get("model");
                hashMaps[0].remove("method");
                hashMaps[0].remove("model");
                result = (HashMap) odooRPC.callOdoo(model, method, hashMaps[0]);

                if (result.get("result").equals("Success")) {
                    if (method.equals("load_image")) {
                        String image = String.valueOf(result.get("image"));
                        try {
                            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                            final Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv.setImageBitmap(decodedBitmap);
                                    iv.setOnTouchListener(LargeImageActivity.this);
                                }
                            });
                        }catch(Exception e){
                            finish();
                        }
                    }
                }

            }catch (Exception e){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LargeImageActivity.this)
                                .setTitle("App Info")
                                .setMessage("No internet Connection Found.")
                                .setIcon(R.mipmap.any_service_icon_foreground)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finishAffinity();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                });
            }

            return result;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> stringObjectHashMap) {

                progressBar.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
        }
    }

}