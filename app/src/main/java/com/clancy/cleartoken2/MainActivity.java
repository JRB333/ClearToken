package com.clancy.cleartoken2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends Activity {

	int REQUEST_ENABLE_BT = 96;
	private BluetoothManager bluetoothManager = null;
	private BluetoothAdapter mBluetoothAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(this, "BLE not supported on this device!", Toast.LENGTH_LONG).show();
		    finish();
		}
        
		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
            	bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
				mBluetoothAdapter = bluetoothManager.getAdapter();
				if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) 
				{
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				Intent myIntent = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(myIntent);
                finish();
            }
        }, 2000);
		
		ImageButton btAdapter = (ImageButton) findViewById(R.id.imageButton1);
		btAdapter.setBackground(null);
		/*
		btAdapter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
				mBluetoothAdapter = bluetoothManager.getAdapter();
				if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) 
				{
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				else
				{
		   	   		  Intent myIntent = new Intent(getApplicationContext(), DeviceScanActivity.class);
		   	   		  startActivityForResult(myIntent, 0);
		   	   		  //finish();
		   	   		  overridePendingTransition(0, 0);    
				}
			}
		});
		*/
	}

}
