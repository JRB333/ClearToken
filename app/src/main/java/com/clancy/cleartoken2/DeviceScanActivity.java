
package com.clancy.cleartoken2;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private static final String CT_UUID = "c9cab9b8-3abf-4043-a5af-9ad00c6074d5";
    public ArrayList<String> foundDevices = new ArrayList<String>();  // Define array

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 30 seconds.
    private static final long SCAN_PERIOD = 30000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setTitle(R.string.title_devices);
        getActionBar().setTitle(R.string.app_name);
        getActionBar().setIcon(R.drawable.ct_generic);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;

        final Intent intent = new Intent(this, DeviceControlActivity.class);
        String deviceName = device.getName();
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, deviceName);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        String deviceColor = foundDevices.get((position * 2) + 1);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_COLOR, deviceColor);

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // --- Determine Version of Android OS ---
            String myOSVersion = android.os.Build.VERSION.RELEASE; // e.g. myOSVersion := "4.2"
            float myOSVersionNum = Float.valueOf(myOSVersion.substring(0,3));

            // --- Determine Device Make & Model ---
            String manufacturer = Build.MANUFACTURER;
            String brand        = Build.BRAND;
            String product      = Build.PRODUCT;
            String model        = Build.MODEL;
            //int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion := 8;

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            foundDevices.clear();  // Initialize 'Found' Device Array
            mScanning = true;
			
            // --- Android Version-Specific Bluetooth Scan ---

            if (myOSVersionNum < 4.4) {
                // --- Android 4.3 (NO UUID Filtering) ---
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                // --- Android 4.4 or Later (With UUID Filtering) ---
                UUID[] uuids = new UUID[1];   // JRB
                uuids[0] = UUID.fromString(CT_UUID);
                mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            BluetoothDevice device = mLeDevices.get(i);

            final String deviceName = device.getName().toUpperCase();
            final String deviceFirstChar = deviceName.substring(0, 1);
            String cColorDigit = deviceName.substring(deviceName.length()-1);
            String deviceColor = "";
            int deviceNameOK = -99;

            //if ((deviceName.matches("[A-Z]{2}[0-9]{3}") || deviceName.matches("[A-Z]{3}[0-9]{2}")) && deviceName.length() == 5)
            if ((deviceFirstChar.matches("[A-Z]") || deviceFirstChar.matches("[0-9]")) && cColorDigit.matches("[0-9]") && deviceName.length() == 5) {
                deviceNameOK = 99;   // Device Name OK

                // General ListView optimization code.
                if (view == null) {
                    view = mInflator.inflate(R.layout.listitem_device, null);
                    viewHolder = new ViewHolder();
                    viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                    viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }

                try{
                    int  colorDigit = Integer.valueOf(cColorDigit);
                    switch (colorDigit)
                    {	case 0:
                        case 5:
                            deviceColor =  "#FFBB33";
                            break;

                        case 1:
                        case 6:
                            deviceColor =  "#AA66CC";
                            break;

                        case 2:
                        case 7:
                            deviceColor =  "#99CC00";
                            break;

                        case 3:
                        case 8:
                            deviceColor =  "#33B5E5";
                            break;

                        case 4:
                        case 9:
                            deviceColor =  "#FF4444";
                            break;

                        default:
                            deviceColor =  "#FFFFFF";
                    }
                }catch(Exception ex)
                {
                    //Toast.makeText(DeviceScanActivity.this, "Unknown Device", Toast.LENGTH_LONG).show();
                }

                view.setBackgroundColor(Color.parseColor(deviceColor));

                if (deviceName != null && deviceName.length() > 0  && deviceNameOK > 0) {
                    viewHolder.deviceName.setText(deviceName);

                    // Add This Device To Reference ArrayList
                    if (!foundDevices.contains(deviceName)) {
                        foundDevices.add(deviceName);
                        foundDevices.add(deviceColor);

                        viewHolder.deviceName.setText(deviceName);
                        viewHolder.deviceAddress.setText("");
                    }
                } else {
                    //viewHolder.deviceName.setText(R.string.unknown_device);
                }

                //viewHolder.deviceAddress.setText(device.getAddress());
            } else {
                // viewHolder.deviceName.setText(R.string.unknown_device);
            }

            if (deviceNameOK > 0) {
                return view;
            } else {
                return null;
            }
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }catch(Exception ex)
                            {
                                Toast.makeText(DeviceScanActivity.this, "ERR ADDING DEVICE", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}