package idiots.com.dsp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;

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
    private GraphView graph;
    private CircularQueue q;
    private final static int SCALE = 50;

    private Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetooth = new Bluetooth(this, null);

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
        graph = findViewById(R.id.main_graph);
        q = new CircularQueue(1);

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
                if(bluetooth.isEnabled()){
                    bluetooth.showPairedDevicesListDialog();
                }else{
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
        mSensorX = (int)((((-sensorEvent.values[0]/9.8f)/2)+0.5f)*SCALE);
        mSensorY = (int)((((-sensorEvent.values[1]/9.8f)/2)+0.5f)*SCALE);

        if(mOldSensorX != mSensorX || mOldSensorY != mSensorY){
            //TODO send data & update old value
            if(bluetooth.isConnected()){
                bluetooth.sendMessage("@#x"+Character.toString((char) mSensorX) + "y" + Character.toString((char) mSensorY) + "b&*");
            }

            mOldSensorX = mSensorX;
            mOldSensorY = mSensorY;
        }

        q.add(new DataPoint(mSensorX, mSensorY));

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(this.q.getArray());
        series.setShape(PointsGraphSeries.Shape.POINT);

        series.setColor(Color.BLUE);
        series.setSize(10);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.getViewport().setMaxX(SCALE*2);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxY(SCALE*2);
        graph.getViewport().setMinY(0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == Bluetooth.REQUEST_BLUETOOTH_ENABLE){
            if (resultCode == RESULT_OK){
                //BlueTooth is now Enabled
                bluetooth.showPairedDevicesListDialog();
            }
            if(resultCode == RESULT_CANCELED){
                bluetooth.showQuitDialog( "You need to enable bluetooth");
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
}
