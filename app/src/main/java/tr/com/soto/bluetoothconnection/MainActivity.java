package tr.com.soto.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Switch bluetoothOnOff;
    private Button scanButton;
    private Button openSocketButton;
    private CheckBox cbMakeVisible;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private ListView btListView;
    private ProgressBar socketProgress;
    private static final int BT_REQUEST_ENABLE = 1;
    private static final int BT_VISIBLE_ENABLE = 13;
    private static final int BONDED_DEVICE = 12;
    private static final int DISCOVERABILITY_DURATION = 300;
    private boolean isPaired = false;
    private ArrayList<String> devicesList;
    private ArrayList<BluetoothDevice> btDeviceList;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothOnOff = (Switch) findViewById(R.id.btSwitch);
        scanButton = (Button) findViewById(R.id.scanButton);
        openSocketButton = (Button) findViewById(R.id.openSocketButton);
        cbMakeVisible = (CheckBox) findViewById(R.id.cbMakeVisible);
        btListView = (ListView) findViewById(R.id.btListView);
        socketProgress = (ProgressBar) findViewById(R.id.socketProgress);

        /*
            when running on JELLY_BEAN_MR1 and below, call the static getDefaultAdapter() method;
            when running on JELLY_BEAN_MR2 and higher, retrieve it through getSystemService(Class)
            with BLUETOOTH_SERVICE.
        */
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter != null) {

            if(bluetoothAdapter.isEnabled()) {

                bluetoothOnOff.setChecked(true);
                scanButton.setEnabled(true);
                openSocketButton.setEnabled(true);
                cbMakeVisible.setEnabled(true);

            } else {

                bluetoothOnOff.setChecked(false);
                scanButton.setEnabled(false);
                openSocketButton.setEnabled(false);
                cbMakeVisible.setEnabled(false);

            }




            //Bluetooth On/Off
            bluetoothOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    btListView.setAdapter(null);

                    if (isChecked) {

                        //give a permission from AndroidManifest.xml file
                        //BLUETOOTH permission for use Bluetooth features.
                        //BLUETOOTH_ADMIN permission for initiate device discovery or manipulate Bluetooth settings.
                        if (!bluetoothAdapter.isEnabled()) {

                            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBTIntent, BT_REQUEST_ENABLE);

                            Toast.makeText(getApplicationContext(), "Bluetooth is turning on!", Toast.LENGTH_LONG).show();
                            scanButton.setEnabled(true);
                            openSocketButton.setEnabled(true);
                            cbMakeVisible.setEnabled(true);

                            if(bluetoothAdapter.isDiscovering()) {

                                cbMakeVisible.setChecked(true);

                            }
                        }

                    } else {

                        if (bluetoothAdapter.disable()) {

                            Toast.makeText(getApplicationContext(), "Bluetooth is turned off sucessfully!", Toast.LENGTH_LONG).show();
                            scanButton.setEnabled(false);
                            openSocketButton.setEnabled(false);
                            cbMakeVisible.setEnabled(false);

                        }
                    }
                }
            });





            //make device discoverable to other devices
            cbMakeVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                if (isChecked) {

                    //default discoverable duration 120 seconds
                    //specific duration can be defined
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABILITY_DURATION);
                    startActivityForResult(discoverableIntent, BT_VISIBLE_ENABLE);

                }
                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "The Device does not support Bluetooth!", Toast.LENGTH_LONG).show();

        }
    }

    //Find for other bluetooth devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //When finding a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                String deviceInfo = "";
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

                //Get the BluetoothDevice object from intent
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceInfo += btDevice.getName() + "\n" + btDevice.getAddress();
                btDeviceList.add(btDevice);

                if(btDevice.getBondState() == BONDED_DEVICE) {

                    Log.d("BluetoothDevice", "Paired device found.");
                    deviceInfo += " (PAIRED)";
                    isPaired = true;

                }

                devicesList.add(deviceInfo);
                btListView.setAdapter(arrayAdapter);
                btListView.refreshDrawableState();
            }
        }
    };


    public void openSocketAsServer(View view) {
        socketProgress.setVisibility(view.VISIBLE);

        Log.d("BluetoothDevice", "Server connection starting...");
        Thread serverThread = new ServerBTConnection(bluetoothAdapter);
        serverThread.run();
        Log.d("BluetoothDevice", "Server connection started!");

    }

    //Scan button performed
    public void searchForDevices(View view) {

        Log.d("BluetoothDevice","Searching devices...");

        if(bluetoothAdapter.isDiscovering()) {

            Log.d("BluetoothDevice", "Cancel discoverying...");
            bluetoothAdapter.cancelDiscovery();

        }
        btListView.setAdapter(null);

        Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_LONG).show();

        devicesList = new ArrayList<String>();
        btDeviceList = new ArrayList<BluetoothDevice>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesList);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        Log.d("BluetoothDevice", "Starting discovery...");
        bluetoothAdapter.startDiscovery();



        //found device click operation
        btListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (btDeviceList.size() > 0) {
                bluetoothDevice = btDeviceList.get(position);

                Log.d("BluetoothDevice", bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                Log.d("BluetoothDevice", "Client connection starting...");
                Thread clientThread = new ClientBTConnection(bluetoothDevice);
                clientThread.run();
                Log.d("BluetoothDevice", "Client connection started!");

            }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BT_REQUEST_ENABLE) {

            if(bluetoothAdapter.isEnabled()) {

                Log.i("Bluetooth Status", "Status: Enabled");

            }  else if (requestCode == BT_VISIBLE_ENABLE) {

                if(resultCode == DISCOVERABILITY_DURATION) {

                    Log.i("Bluetooth Status", "Status: Discoverable Mode: On");

                } else {

                    Log.e("Bluetooth Status", "Fail to enable discoverability on the device!");

                }

            }  else {

                Log.i("Bluetooth Status", "Status: Disabled");
                bluetoothOnOff.setChecked(false);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}