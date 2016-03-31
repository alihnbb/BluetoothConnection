package tr.com.soto.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Switch bluetoothOnOff;
    private Button scanButton;
    private CheckBox cbMakeVisible;
    private BluetoothAdapter bluetoothAdapter;
    private ListView btListView;
    private static final int BT_REQUEST_ENABLE = 1;
    private int btResultCode;
    private ArrayList<String> devicesList;
    private ArrayAdapter arrayAdapter;
    private int osVersion = BuildConfig.VERSION_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothOnOff = (Switch) findViewById(R.id.btSwitch);
        scanButton = (Button) findViewById(R.id.scanButton);
        cbMakeVisible = (CheckBox) findViewById(R.id.cbMakeVisible);
        btListView = (ListView) findViewById(R.id.btListView);

        /*
            when running on JELLY_BEAN_MR1 and below, call the static getDefaultAdapter() method;
            when running on JELLY_BEAN_MR2 and higher, retrieve it through getSystemService(Class)
            with BLUETOOTH_SERVICE.
        */
        Log.i("ANDROID OS VERSION", String.valueOf(osVersion));
        if(osVersion > 17) {

            bluetoothAdapter = (BluetoothAdapter)getSystemService(BLUETOOTH_SERVICE);

        } else if(osVersion <= 17) {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        }

        if(bluetoothAdapter != null) {

            if(bluetoothAdapter.isEnabled()) {

                bluetoothOnOff.setChecked(true);
                scanButton.setEnabled(true);

            } else {

                bluetoothOnOff.setChecked(false);
                scanButton.setEnabled(false);

            }

            bluetoothOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    btListView.setAdapter(null);

                    if (isChecked) {

                        //give a permission from AndroidManifest.xml file
                        //BLUETOOTH permission for use Bluetooth features.
                        //BLUETOOTH_ADMIN permission for initiate device discovery or manipulate Bluetooth settings.
                        if(!bluetoothAdapter.isEnabled()) {

                            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBTIntent, BT_REQUEST_ENABLE);
                            onActivityResult(BT_REQUEST_ENABLE, btResultCode, enableBTIntent);

                            Log.i("BT PROCESS RESULT CODE", String.valueOf(btResultCode));

                            Toast.makeText(getApplicationContext(), "Bluetooth is turning on!", Toast.LENGTH_LONG).show();
                            scanButton.setEnabled(true);
                        }

                    } else {

                        if (bluetoothAdapter.disable()) {

                            if (bluetoothAdapter.isEnabled()) {

                                Toast.makeText(getApplicationContext(), "Bluetooth couldn't be disabled, try again!", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(getApplicationContext(), "Bluetooth is turned off sucessfully!", Toast.LENGTH_LONG).show();
                                scanButton.setEnabled(false);

                            }
                        }
                    }
                }
            });

            cbMakeVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        startActivity(intent);

                    } else {

                        //TODO: cancel discovering
                    }
                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "The Device does not support Bluetooth!", Toast.LENGTH_SHORT).show();

        }
    }

    public void scanForPairedDevices(View view) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {

            for(BluetoothDevice device : pairedDevices) {
                devicesList.add(device.getName() + "\n" + device.getAddress() + "(paired)");
                btListView.setAdapter(arrayAdapter);
                btListView.refreshDrawableState();

            }

        } else {

            Toast.makeText(getApplicationContext(), "No paired device found!", Toast.LENGTH_SHORT);

        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //When finding a device
        if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Get the BluetoothDevice object from intent
            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            devicesList.add(btDevice.getName() + "\n" + btDevice.getAddress());
            btListView.setAdapter(arrayAdapter);
            btListView.refreshDrawableState();
        }
        }
    };

    public void searchForDevices(View view) {

        if(bluetoothAdapter.isDiscovering()) {

            bluetoothAdapter.cancelDiscovery();

        }
        btListView.setAdapter(null);

        Toast toast = Toast.makeText(getApplicationContext(), "Searching for devices...", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        devicesList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesList);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();

    }

    private void searchForLEDevice() {

        //TODO: coding for LE

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