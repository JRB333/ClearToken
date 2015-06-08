

package com.clancy.cleartokendemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.clancy.cleartokendemo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.clancy.cleartokendemo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.clancy.cleartokendemo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.clancy.cleartokendemo.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.clancy.cleartokendemo.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override        
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                broadcastUpdate(intentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            //At this point we need to change a flag of some sort to continue the Write Routine
            Log.d(TAG,">>>>Service Discovered!!!!!!!!");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            byte[] retValue = new byte[9];
            retValue=characteristic.getValue();
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                if (android.os.Build.VERSION.SDK_INT >= 19 ) 
                {
                	//DO THIS STUPID CRAP TO TRIGGER A CALLBACK FOR KITKAT
                	Log.i(TAG, "**********4.4 Workaround" + mBluetoothGatt.discoverServices());                
                }
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        if (mBluetoothGatt.connect()) {
            mConnectionState = STATE_CONNECTING;
            if (android.os.Build.VERSION.SDK_INT >= 19 ) 
            {
            	//DO THIS STUPID CRAP TO TRIGGER A CALLBACK FOR KITKAT
            	Log.i(TAG, "**********4.4 Workaround" + mBluetoothGatt.discoverServices());                
            }
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
      
   
    public String writeCTCharacters(byte[] value, UUID UUID_LUM_APP, UUID UUID_LUM_CHAR) 
    {
    	//DeviceControlActivity.Messagebox("Begin Write", getApplicationContext());
    	String returnVal = "SUCCESS";
    	Log.d(TAG,">>>>Begin Routine");
    	BluetoothGattService ctService = mBluetoothGatt.getService(UUID_LUM_APP);
    	List<BluetoothGattService> junkList = mBluetoothGatt.getServices(); // just put this here for delay
    	if (ctService == null) 
    	{
    		Log.d(TAG,"ctService null");
    		return "Gatt Service Failed";
    	}
    	Log.d(TAG,">>>>After getService");
		/*try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//DeviceControlActivity.Messagebox("GetDescriptor sleep error", getApplicationContext());
			e.printStackTrace();
		}  */
    	BluetoothGattCharacteristic ctChar = ctService.getCharacteristic(UUID_LUM_CHAR);
    	if (ctChar == null) 
    	{
    		Log.d(TAG,"ctChar null");
    		return "Get Characteristic Failed";
    	}
    	Log.d(TAG,">>>>After getCharacteristic");
    	
    	List<BluetoothGattDescriptor> descList = ctChar.getDescriptors();
   
    	///MY NEW STUFF    	
    	BluetoothGattDescriptor descr=ctChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
    	try {
    		Thread.sleep(500);
    	} catch (InterruptedException e) {
		// TODO Auto-generated catch block		
    		e.printStackTrace();
		}
    	if (UUID_LUM_CHAR.toString().contains("a3f14fd2653c"))
    	{
    	//THIS IS ONLY FOR NOTIFICATION CHARACTERISTICS - SKIP FOR WRITE ONLY.
    	descr.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    	try {
    		Thread.sleep(200);
    	} catch (InterruptedException e) {
		// TODO Auto-generated catch block		
    		e.printStackTrace();
		}
    	mBluetoothGatt.writeDescriptor(descr);    	
    	try {
    		Thread.sleep(200);
    	} catch (InterruptedException e) {
		// TODO Auto-generated catch block		
    		e.printStackTrace();
		}
    	//NED OF NOTIFICATION CHARACTERISTIC
    }
    	ctChar.setValue(value);
    	try {
    		Thread.sleep(200);
    	} catch (InterruptedException e) {
		// TODO Auto-generated catch block		//
    		e.printStackTrace();
		}
    	mBluetoothGatt.setCharacteristicNotification(ctChar, true);
    	
    	mBluetoothGatt.writeCharacteristic(ctChar);    	    	    
    	try {
    		Thread.sleep(200);
    	} catch (InterruptedException e) {
		// TODO Auto-generated catch block		
    		e.printStackTrace();
		}
    	
    	//mBluetoothGatt.disconnect();
    	if(true==true){return "SUCCESS";}
    	    	
    	Log.d(TAG,">>>>After getDescriptors");
    	UUID descUUID = null;
    	if(descList.size()>=1)
    	{
        	descUUID = descList.get(0).getUuid() ;
        	if (descUUID.toString().equals("00002902-0000-1000-8000-00805f9b34fb"))        	
        	{
            	BluetoothGattDescriptor descriptor = ctChar.getDescriptor(descUUID); 
        	    byte[] blah = {0x01, 0x00};
        	    if (descriptor == null)
        	    {
        	    	Log.d(TAG,"Get Descriptor NULL");
        	    	return "Get Descriptor NULL";
        	    }
        	    else
        	    {
        	    	descriptor.setValue(blah);
        	    	boolean wroteit = mBluetoothGatt.writeDescriptor(descriptor);
        	    	if (wroteit == false)
        	    	{
        	    		Log.d(TAG,"Write Descriptor False!");
        	    		return "Write Descriptor False!";
        	    		
        	    	}
        	    	else
        	    	{
        	    		//DeviceControlActivity.Messagebox("Write Descriptor Success - Opening!", getApplicationContext());
        	    		/*try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
						//	DeviceControlActivity.Messagebox("Thread Sleep error", getApplicationContext());
							e.printStackTrace();
						}*/
        	    	}        	    	
        	    }        		
        	}
    	}
    	else
    	{
    		Log.d(TAG,"Empty Descriptor List");
    		return "Empty Descriptor List";
    	}

    	ctChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    	boolean writeval = ctChar.setValue(value);
    	boolean status = mBluetoothGatt.writeCharacteristic(ctChar);    	 
    	if (status == false)
    	{
    		Log.d(TAG,"Write Characteristic False!");
    		return "Write Characteristic False!";
    	}
    	else
    	{
            if (android.os.Build.VERSION.SDK_INT >= 4.4 ) 
            {
            	//DO THIS STUPID CRAP TO TRIGGER A CALLBACK FOR KITKAT
        		try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    			//	DeviceControlActivity.Messagebox("Thread Sleep error", getApplicationContext());
    				e.printStackTrace();
    			}                
            }
    	}    
    	Log.w(TAG,"Write Status: " + status);
    	if (descUUID == null)
    	{
    		Log.d(TAG,"descUUID is NULL");
    		return "descUUID is NULL";
    	}
    	else
    	{    
        	if (descUUID.toString().equals("00002902-0000-1000-8000-00805f9b34fb"))
        	{
            	BluetoothGattDescriptor descriptor = ctChar.getDescriptor(descUUID); 
        	    byte[] blah = {0x00, 0x00};
        	    if (descriptor.equals(null))
        	    {
        	    	Log.d(TAG,"Closing get Descriptor failed");
        	    	return "Closing get Descriptor failed";
        	    }
        	    else
        	    {
        	    	descriptor.setValue(blah);
        	    	boolean wroteit = mBluetoothGatt.writeDescriptor(descriptor);
        	    	if (wroteit == false)
        	    	{
        	    		Log.d(TAG,"Write Descriptor False!");
        	    		return "Write Descriptor False!";
        	    	}
        	    	else
        	    	{
        	    		//DeviceControlActivity.Messagebox("Write Descriptor Success - Closing!", getApplicationContext());        	    		
        	    	}        	    	
        	    }        		
        	} 
        	else
        	{ 
        		Log.d(TAG,"Trying to close but UUID is not what was expected.");
        		return "Trying to close but UUID is not what was expected.";
        	}
    	}    	
    	return returnVal;
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
