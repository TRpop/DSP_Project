package idiots.com.dsp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static String TAG = "MainActivity";

    private int mSensorX;
    private int mSensorY;
    private int mOldSensorX = -1;
    private int mOldSensorY = -1;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final static int SCALE = 50;
    private float mWidth;
    private float mHeight;

    private Bluetooth bluetooth;

    private Vibrator v;

    private ImageView go, gameover, retry, ship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        go = findViewById(R.id.main_go_img);
        gameover = findViewById(R.id.main_gameover_img);
        retry = findViewById(R.id.main_retry_img);
        ship = findViewById(R.id.main_ship_img);

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);

        mHeight = displaySize.y;
        mWidth = displaySize.x;

        go.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                if (bluetooth.isEnabled()) {
                    bluetooth.showPairedDevicesListDialog();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, Bluetooth.REQUEST_BLUETOOTH_ENABLE);
                }

                return false;
            }
        });

        retry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gaming();
                return false;
            }
        });

        init();

        bluetooth = new Bluetooth(this, null) {
            @Override
            protected void onReceive(String s) {
                Log.d(TAG, s);
                gameover();
                vibrate(100);

            }

            @Override
            protected void onConnected(String deviceName) {
                MainActivity.this.msg(deviceName);
                gaming();
            }
        };

        /*
        if (!bluetooth.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Bluetooth.REQUEST_BLUETOOTH_ENABLE);
        }
        else {
            Log.d(TAG, "Initialisation successful.");

            bluetooth.showPairedDevicesListDialog();
        }*/

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void init(){
        go.setVisibility(View.VISIBLE);
        gameover.setVisibility(View.INVISIBLE);
        retry.setVisibility(View.INVISIBLE);
        ship.setVisibility(View.INVISIBLE);
    }

    private void gaming(){
        startSimulation();
        go.setVisibility(View.INVISIBLE);
        gameover.setVisibility(View.INVISIBLE);
        retry.setVisibility(View.INVISIBLE);
        ship.setVisibility(View.VISIBLE);
    }

    private void gameover(){
        stopSimulation();
        go.setVisibility(View.INVISIBLE);
        gameover.setVisibility(View.VISIBLE);
        retry.setVisibility(View.VISIBLE);
        ship.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onPause() {
        super.onPause();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bluetooth.cancel();
        v.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_start_opt:
                startSimulation();
                return true;
            case R.id.main_stop_opt:
                stopSimulation();
                return true;

            case R.id.main_pair_opt:
                if (bluetooth.isEnabled()) {
                    bluetooth.showPairedDevicesListDialog();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, Bluetooth.REQUEST_BLUETOOTH_ENABLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float xData = sensorEvent.values[0];
        float yData = sensorEvent.values[1];

        mSensorX = (int) ((((-xData / 9.8f) / 2) + 0.5f) * SCALE);
        mSensorY = (int) ((((-yData / 9.8f) / 2) + 0.5f) * SCALE);

        ship.setX(mWidth/2 - ship.getWidth()/2  + 50*yData);
        ship.setY(mHeight/2 - ship.getHeight()/2  + 50*xData);

        if (mOldSensorX != mSensorX || mOldSensorY != mSensorY) {
            //TODO send data & update old value
            if (bluetooth.isConnected()) {
                bluetooth.sendMessage("@#x" + Character.toString((char) mSensorX) + "y" + Character.toString((char) mSensorY) + "b&*");
            }

            mOldSensorX = mSensorX;
            mOldSensorY = mSensorY;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Bluetooth.REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                //BlueTooth is now Enabled
                bluetooth.showPairedDevicesListDialog();
            }
            if (resultCode == RESULT_CANCELED) {
                bluetooth.showQuitDialog("You need to enable bluetooth");
            }
        }
    }

    private void startSimulation() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d("now", "registered");
    }

    public void stopSimulation() {
        mSensorManager.unregisterListener(this);
        Log.d("now", "unregistered");
    }

    private void vibrate(int lenth){
        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, lenth};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        v.vibrate(pattern, -1);
    }

    private void msg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
