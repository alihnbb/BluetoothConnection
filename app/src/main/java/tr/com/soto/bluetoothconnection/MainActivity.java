package tr.com.soto.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Switch bluetoothOnOff;
    private Button makeVisibleButton;
    private CheckBox cbMakeVisible;
    private BluetoothAdapter bluetoothAdapter;

    public void makeVisible(View view) {

        Toast toast = Toast.makeText(getApplicationContext(), "Searching Peripherals", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothOnOff = (Switch) findViewById(R.id.btSwitch);
        makeVisibleButton = (Button) findViewById(R.id.makeVisibleButton);
        cbMakeVisible = (CheckBox) findViewById(R.id.cbMakeVisible);

        PackageManager packageManager = getBaseContext().getPackageManager();

        if(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter != null) {

                bluetoothOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        Intent turnOnOffIntent = null;

                        if (isChecked) {
                            //give a permission from AndroidManifest.xml file

                            turnOnOffIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivity(turnOnOffIntent);

                            Toast.makeText(getApplicationContext(), "Bluetooth is turning on!", Toast.LENGTH_LONG).show();
                            makeVisibleButton.setEnabled(true);

                        } else {

                            if (bluetoothAdapter.disable()) {

                                if (bluetoothAdapter.isEnabled()) {

                                    Toast.makeText(getApplicationContext(), "Bluetooth couldn't be disabled!", Toast.LENGTH_LONG).show();

                                } else {

                                    Toast.makeText(getApplicationContext(), "Bluetooth is turned off sucessfully!", Toast.LENGTH_LONG).show();
                                    makeVisibleButton.setEnabled(false);

                                }
                            }
                        }
                    }
                });

                cbMakeVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        Intent intent = null;
                        if(isChecked) {

                            if(!bluetoothAdapter.isDiscovering()) {

                                intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

                            }

                        } else {

                            //TODO: cancel discovering

                        }

                        startActivity(intent);

                    }
                });

            } else {

                Toast.makeText(getApplicationContext(), "Bluetooth crashed!", Toast.LENGTH_SHORT).show();

            }
        } else {

            Toast.makeText(getApplicationContext(), "Device can not support Bluetooth!", Toast.LENGTH_LONG).show();

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
}
