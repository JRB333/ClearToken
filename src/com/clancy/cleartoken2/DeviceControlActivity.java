

package com.clancy.cleartokendemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    int secondsDelay = 1000;
    String WhatToDo  = "";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mDataCode;
    
    private Button buttonSecond;    
    private Button buttonTwoMin;    
    private Button buttonReset;
    private Button buttonNear;
    private Button buttonMedium;
    private Button buttonFar;
    private EditText editURI;
    private Button buttonWriteURI;
    private Button buttonOn;
    private Button buttonOff;
        
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
    													new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    
    private Handler mHandler = new Handler(); 
    
    ProgressDialog mDialog = null;
    
	private Runnable runThanks = new Runnable() {     
		public void run() 
		{         			    	    
			Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
			if (danman)
			{
				WhatToDo = "THANKS";
			}
			else
				Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
			
		}
	}; 
	
	private Runnable runClear = new Runnable() {     
		public void run() 
		{         			    
			Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
			if (danman)
			{
				WhatToDo = "CLEAR";
			}
			else
				Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
		}
	}; 
	
	private Runnable runToken = new Runnable() {     
		public void run() 
		{         			    
			Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
			if (danman)
			{
				WhatToDo = "TOKEN";
			}
			else
				Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();

		}
	}; 
    private Runnable runConnecting =new Runnable() {
    	public void run()
    	{
    		mDialog = new ProgressDialog(DeviceControlActivity.this);
        	mDialog.setMessage("Connecting...");
        	mDialog.setCancelable(true);
        	mDialog.show();
    	}  ; 
    };
    private Runnable runCommunicating  =new Runnable() {
    	public void run()
    	{
    		mDialog = new ProgressDialog(DeviceControlActivity.this);
        	mDialog.setMessage("Talking to Unit...");
        	mDialog.setCancelable(true);
        	mDialog.show();
    	} ; 
    };
    
    //BluetoothGattCharacteristic characteristicHour;
    //UUID UUID_SERVICE;
    //UUID UUID_CHARACTERISTIC;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
                     
            Handler handler1 = new Handler();
            handler1.post(runConnecting);
            mBluetoothLeService.connect(mDeviceAddress);           
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, ">>>Action received.");
        if( BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){ 
            mConnected = true;
            updateConnectionState(R.string.connected);
            invalidateOptionsMenu();
            mDataField.setText(R.string.no_data);
            Log.d(TAG, ">>Gatt Connected Returned");
            if (mDialog != null) {mDialog.dismiss();mDialog=null;}
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
                Log.d(TAG, ">>Gatt Disconnected");
                if (mDialog != null) {mDialog.dismiss();mDialog=null;}
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                // Show all the supported services and characteristics on the user interface.
            	//displayGattServices(mBluetoothLeService.getSupportedGattServices());               
            	//commented by dan            	
                Log.d(TAG,">>>>Services Discovered Returned!!!!!!!!!!!!!");
                if (mDialog != null) {mDialog.dismiss();mDialog=null;}
               
                ProcessWhatToDo();
                              
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            	//intent.getStringExtra(BluetoothLeService.EXTRA_DATA) will return extended data "R\n52 01 00"
                Log.d(TAG, ">>>>Data Available");
                if (mDialog != null) {mDialog.dismiss();mDialog=null;}
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        if (android.os.Build.VERSION.SDK_INT > 9) {
        	StrictMode.ThreadPolicy policy = 
        	        new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy);
        	}
        
        
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mDataCode = (TextView) findViewById(R.id.data_code);
        
        buttonTwoMin = (Button) findViewById(R.id.buttonTwoMin);
        buttonSecond = (Button) findViewById(R.id.buttonSecond);        
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonNear = (Button) findViewById(R.id.buttonNear);
        buttonMedium = (Button) findViewById(R.id.buttonMedium);
        buttonFar = (Button) findViewById(R.id.buttonFar);
        editURI = (EditText) findViewById(R.id.editURI);
        buttonWriteURI = (Button) findViewById(R.id.buttonURI);
        buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOff = (Button) findViewById(R.id.buttonOff);
                    
        buttonTwoMin.setVisibility(View.INVISIBLE);
        buttonSecond.setVisibility(View.INVISIBLE);
        buttonReset.setVisibility(View.INVISIBLE);
        buttonNear.setVisibility(View.INVISIBLE);
        buttonMedium.setVisibility(View.INVISIBLE);
        buttonFar.setVisibility(View.INVISIBLE);
        editURI.setVisibility(View.INVISIBLE);
        buttonWriteURI.setVisibility(View.INVISIBLE);
        buttonOn.setVisibility(View.INVISIBLE);
        buttonOff.setVisibility(View.INVISIBLE);
                           
        buttonTwoMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
                if (danman) {
                    Handler handler1 = new Handler();
                    handler1.post(runCommunicating);
                    WhatToDo = "Parking2Minutes";
                    Log.d(TAG, "Setting 2 Min...");
                } else
                    Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
            }
        });
        buttonSecond.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{
					new Handler().post(runCommunicating);
					WhatToDo = "Vend1Second";
					Log.d(TAG, "Setting 1 Second...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
				
				//mHandler.postDelayed(showButtons, secondsDelay);
			}
		});
        buttonReset.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "Reset";
					Log.d(TAG, "Resetting Time...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonWriteURI.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "WriteURI";
					Log.d(TAG, "Writing URI...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonNear.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "Near";
					Log.d(TAG, "Setting Near...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonMedium.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "Medium";
					Log.d(TAG, "Setting Medium...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonFar.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "Far";
					Log.d(TAG, "Setting Far...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonOn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "BeaconOn";
					Log.d(TAG, "Beacon On...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        buttonOff.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
				if (danman)
				{	new Handler().post(runCommunicating);
					WhatToDo = "BeaconOff";
					Log.d(TAG, "Beacon On...");
				}
				else
					Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();				
			}
		});
        getActionBar().setTitle(R.string.app_name);
        getActionBar().setIcon(R.drawable.ct_generic);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:            	
            	Handler handler=new Handler();
            	handler.post(runConnecting);
            	mBluetoothLeService.connect(mDeviceAddress);                
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                if (mConnectionState.getText().equals("Connected"))
                {
                	 buttonTwoMin.setVisibility(View.VISIBLE);
                     buttonSecond.setVisibility(View.VISIBLE);                   
                     buttonReset.setVisibility(View.VISIBLE);
                     buttonNear.setVisibility(View.VISIBLE);
                     buttonMedium.setVisibility(View.VISIBLE);
                     buttonFar.setVisibility(View.VISIBLE);
                     editURI.setVisibility(View.VISIBLE);
                     buttonWriteURI.setVisibility(View.VISIBLE);
                     buttonOn.setVisibility(View.VISIBLE);
                     buttonOff.setVisibility(View.VISIBLE);              
                }
                else
                {
                    buttonTwoMin.setVisibility(View.INVISIBLE);
                    buttonSecond.setVisibility(View.INVISIBLE);
                    buttonReset.setVisibility(View.INVISIBLE);
                    buttonNear.setVisibility(View.INVISIBLE);
                    buttonMedium.setVisibility(View.INVISIBLE);
                    buttonFar.setVisibility(View.INVISIBLE);
                    editURI.setVisibility(View.INVISIBLE);
                    buttonWriteURI.setVisibility(View.INVISIBLE);
                    buttonOn.setVisibility(View.INVISIBLE);
                    buttonOff.setVisibility(View.INVISIBLE);                                           
                }
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) 
        	return;
        
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();           
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
      
    private void Write1Unit()
    {		
    	String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
    	String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
    	mDataCode.setText(strValue);
    	if (!strValue.startsWith("4"))
    	{  if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return; 
    	}
    	byte[] value = new byte[10];
    	int command_pos=0;
    	for (int x=0;x<strValue.length();x=x+2)
    	{	
    		value[command_pos] = (byte) Integer.parseInt(strValue.substring(x,x+2),16);	
    		//value[command_pos] = (byte) convert(Integer.valueOf(strValue.substring(x,x+2)));
    		++command_pos;
    	}
    	if( value[2] == 0x00)
    	{
    		mDataCode.setText("DEVICE NEEDS CONFIGURED IN DB");
    		 if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return;
    	}    		
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));
	
		mBluetoothLeService.disconnect();		
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
    }
    public static int convert(int n) {
    	  return Integer.valueOf(String.valueOf(n), 16);
    	}

    private void Write1Second()
    {
      try {
    	
    	String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
    	String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
    	mDataCode.setText(strValue);
    	if (!strValue.startsWith("4"))
    	{	if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return; }
    	byte[] value = new byte[10];
    	int command_pos=0;
                   
    	for (int x=0;x<strValue.length();x=x+2)
    	{	value[command_pos] = (byte) (Integer.parseInt(strValue.substring(x,x+2),16));    	
    		++command_pos;
    	}
    	if( value[2] == 0x00)
    	{
    		mDataCode.setText("DEVICE NEEDS CONFIGURED IN DB");
    		if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return;
    	}   
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));
		mBluetoothLeService.disconnect();
		if (mDialog != null) {mDialog.dismiss();mDialog=null;}
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
      }               
      catch(Throwable t) { 
    		Toast.makeText(getApplicationContext(), "GetFileSize Ex: "+t.toString(), 2000).show();
    		mDataField.setText("Write1Sec: "+t.toString());
      }   
    }
    private void WriteReset()
    {
    	String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
    	String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
    	mDataCode.setText(strValue);
    	if (!strValue.startsWith("4"))
    	{  if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return; 
    	}
    	byte[] value = new byte[10];
    	int command_pos=0;
    	for (int x=0;x<strValue.length();x=x+2)
    	{	
    		value[command_pos] = (byte) Integer.parseInt(strValue.substring(x,x+2),16);	    
    		++command_pos;
    	}
    	if( value[2] == 0x00)
    	{
    		mDataCode.setText("DEVICE NEEDS CONFIGURED IN DB");
    		 if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return;
    	} 
    	value[3] = (byte) 0x00;
	    value[4] = (byte) 0x00;	//			
	    value[5] = (byte) 0x01; //reset existing time if 1
	    
		
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));		
		mBluetoothLeService.disconnect();
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
    }
    private void WriteURI()
    {
		byte[] value = new byte[20];			
		byte[] theText = new byte[18];
		theText= editURI.getText().toString().getBytes();		
				
	    value[0] = (byte) 0x00;
	    int x;
	    for (x=1;x<=theText.length;++x)
	    {
	    	value[x] = (byte) theText[x-1];
	    }	    
	    value[x] = (byte) 0x00;
	    ++x;	
		for (int y=x;y<18;++y)
		{
			value[y] = (byte) 0x20;
		}
		
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b6-456f-4baa-ad5c-a3f14fd2653d"));  //<-- SECOND CHARACTERISTIC IN LIST
		mBluetoothLeService.disconnect();
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
    }
    private void SetAntenna(byte level)
    {		
    	String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
    	String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
    	mDataCode.setText(strValue);
    	if (!strValue.startsWith("4"))
    	{  if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return; 
    	}
    	byte[] value = new byte[8];
    	int command_pos=4;
    	for (int x=12;x<strValue.length()-1;x=x+2)
    	{	
    		value[command_pos] = (byte) Integer.parseInt(strValue.substring(x,x+2),16);	    
    		++command_pos;
    	}
    	value[0]=0x40;
    	value[1]=0x06;
    	value[2]=0x06;    	
    	value[3]=level;    	
    	    	
    	if( value[2] == 0x00)
    	{
    		mDataCode.setText("DEVICE NEEDS CONFIGURED IN DB");
    		 if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return;
    	}    		
    	
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));
	
		mBluetoothLeService.disconnect();		
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
    }     
    private void SetBeacon(byte level)
    {		
    	String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
    	String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
    	mDataCode.setText(strValue);
    	if (!strValue.startsWith("4"))
    	{  if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return; 
    	}
    	byte[] value = new byte[8];
    	int command_pos=4;
    	for (int x=12;x<strValue.length()-1;x=x+2)
    	{	
    		value[command_pos] = (byte) Integer.parseInt(strValue.substring(x,x+2),16);	    
    		++command_pos;
    	}
    	value[0]=0x40;
    	value[1]=0x06;
    	value[2]=0x05;    	
    	value[3]=level;    //on 1 or off 0    	
    	    	
    	if( value[2] == 0x00)
    	{
    		mDataCode.setText("DEVICE NEEDS CONFIGURED IN DB");
    		 if (mDialog != null) {mDialog.dismiss();mDialog=null;}
    		return;
    	}    		
    	
    	    	
		String returnString = mBluetoothLeService.writeCTCharacters(value,
				UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
				UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));
	
		mBluetoothLeService.disconnect();		
		if (!returnString.equals("SUCCESS"))
		{
			Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_SHORT).show();
		}
    }    
    void ProcessWhatToDo()
    {
    	if (WhatToDo.equals("Parking2Minutes"))
        {    		
    		Write1Unit();
        }
        if (WhatToDo.equals("Vend1Second"))
        {        	
        	Write1Second();
        }
        if (WhatToDo.equals("Reset"))
        {
        	WriteReset();
        }       
        if (WhatToDo.equals("WriteURI"))
        {
        	WriteURI();
        }                          
       
        if (WhatToDo.equals("Near"))
        {
        	SetAntenna((byte) 0);
        }                          
       
        if (WhatToDo.equals("Medium"))
        {
        	SetAntenna((byte) 1);
        }                          
        if (WhatToDo.equals("Far"))
        {
        	SetAntenna((byte) 2);
        }                          
        if (WhatToDo.equals("BeaconOn"))
        {
        	SetBeacon((byte) 1);
        }                          
        if (WhatToDo.equals("BeaconOff"))
        {
        	SetBeacon((byte) 0);
        }                          
       
        WhatToDo = "";          
    
    }
}