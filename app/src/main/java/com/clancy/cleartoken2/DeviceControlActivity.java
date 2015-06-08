
package com.clancy.cleartoken2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;

import com.squareup.picasso.Picasso;

import com.clancy.cleartoken2.data.model.ItemDto;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    protected static final int TIMER_RUNTIME = 10000; // in ms --> 10s
    protected boolean mbActive;
    protected ProgressBar mProgressBar;

    int secondsDelay = 1000;
    String WhatToDo  = "";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_COLOR = "DEVICE_COLOR";

    public int nBaseIncrements;
    public String cBaseLocation;
    public String cBaseUnits;
    public Float nBaseFee;
    public String cBaseType;
    public int nBaseMaxInc;
    public String cBaseImage;
    public Float nFinalPaidAmt;
    public int nFinalIncrements;
    public String mProgressMsg;

    private View thisView;
    private View info_view;
    private View result_view;
    private ViewGroup mContainerView;
    private final String DeviceInfoURL = "http://www.cleartoken.com/services/?func=info&device=";
    private final String DeviceIconURL = "http://www.cleartoken.com/appicons/";
    private final String TokenURLBase = "http://www.cleartoken.com/services/?phone=";

    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceColor;
    private String mDeviceMax;
    private String URLFile;
    private String ClearToken;

    private TextView mDataField;
    private TextView mDataCode;
    private TextView editURI;
    private TextView mConnectionState;

    // Display_Device_Info Layout 'Objects'
    private android.widget.LinearLayout centerLayout;
    private android.widget.ImageView deviceIcon;
    private TextView mHiddenReset;
    private TextView mThisDeviceName;
    private TextView mDeviceLocation;
    private TextView mDeviceRate;
    private String mDeviceFee;
    private TextView mDeviceIncMax;
    private TextView mDeviceType;
    private TextView mDeviceIncrements;
    private TextView mIncrementDown;
    private TextView mIncrementUp;
    private TextView mIncrements;
    private TextView mAmountDue;
    private ImageView payBtnImg;
    private TextView payBtnText;
    private ImageView exitBtnImg;
    private TextView exitBtnText;

    // Additional Display_Device_Result Layout 'Objects'
    private TextView resultMsg;

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
            BackgroundTask task = new BackgroundTask(DeviceControlActivity.this);
            task.ProgMsg = "Connecting...";
            task.execute();

            //mDialog = new ProgressDialog(DeviceControlActivity.this);
            //mDialog.setMessage("Connecting...");
            //mDialog.setCancelable(true);
            //mDialog.show();
        }  ;
    };

    private Runnable runCommunicating  =new Runnable() {
        public void run()
        {
            BackgroundTask task = new BackgroundTask(DeviceControlActivity.this);
            task.ProgMsg = "Talking to Unit...";
            task.execute();

            //mDialog = new ProgressDialog(DeviceControlActivity.this);
            //mDialog.setMessage("Attempting to Talk to Unit...");
            //mDialog.setCancelable(true);
            //mDialog.show();
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
        //mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // JRB - Set up to utilize this Layout
        setContentView(R.layout.display_device_info);
        //setContentView(R.layout.gatt_services_characteristics);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceColor = intent.getStringExtra(EXTRAS_DEVICE_COLOR);

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mHiddenReset = (TextView) findViewById(R.id.hidden_reset);
        centerLayout = (LinearLayout) findViewById(R.id.center_layout);
        mThisDeviceName = (TextView) findViewById(R.id.device_name);
        mDeviceLocation = (TextView) findViewById(R.id.device_location);
        mDeviceRate = (TextView) findViewById(R.id.device_rate);
        mDeviceIncMax = (TextView) findViewById(R.id.increment_maximum);
        mDeviceType = (TextView) findViewById(R.id.device_type);
        mDeviceIncrements = (TextView) findViewById(R.id.device_increments);

        mIncrementDown = (TextView) findViewById(R.id.price_iterator_minus_btn);
        mIncrementUp = (TextView) findViewById(R.id.price_iterator_plus_btn);
        mIncrements = (TextView) findViewById(R.id.price_iterator_value);

        mAmountDue = (TextView) findViewById(R.id.amount_due);
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        deviceIcon = (ImageView) findViewById(R.id.device_icon);

        payBtnImg = (ImageView) findViewById(R.id.paybtnimg);
        ImageView entryBtnImgView = (ImageView) findViewById((R.id.paybtnimg));
        entryBtnImgView.setBackgroundResource(R.drawable.roundedbutton);

        mIncrementDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cIncrements =  mIncrements.getText().toString();
                String cNewIncrements = cIncrements;
                int nNewIncrements = Integer.valueOf(cIncrements);
                if (nNewIncrements > 1) {
                    nNewIncrements = nNewIncrements - 1;
                } else {
                    nNewIncrements = 1;
                }
                nFinalIncrements = nNewIncrements;
                cNewIncrements = String.valueOf(nNewIncrements);
                mIncrements.setText(cNewIncrements);

                CalcNewRate(nNewIncrements);
            }
        });

        mIncrementUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cIncrements =  mIncrements.getText().toString();
                String cNewIncrements = cIncrements;
                int nNewIncrements = Integer.valueOf(cIncrements);
                if (nNewIncrements < nBaseMaxInc) {
                    nNewIncrements = nNewIncrements + 1;
                } else {
                    nNewIncrements = nBaseMaxInc;
                }
                nFinalIncrements = nNewIncrements;
                cNewIncrements = String.valueOf(nNewIncrements);
                mIncrements.setText(cNewIncrements);

                CalcNewRate(nNewIncrements);
            }
        });

        mHiddenReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
                if (danman)
                {
                    new Handler().post(runCommunicating);
                    WhatToDo = "Reset";
                    Log.d(TAG, "Entering RESET...");
                } else {
                    Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
                    //mHandler.postDelayed(showButtons, secondsDelay);
                }
            }
        });


        payBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
                if (danman)
                {
                    // Load the ImageView that will host the animation
                    ImageView entryBtnImgView = (ImageView) findViewById((R.id.paybtnimg));
                    // set its background to our AnimationDrawable XML resource.
                    entryBtnImgView.setBackgroundResource(R.drawable.roundedbutton);

                    /*
                    * Get the background, which has been compiled to an AnimationDrawable
                    * object.
                    */
                    AnimationDrawable frameAnimation = (AnimationDrawable) entryBtnImgView.getBackground();

                    // Start the animation (looped playback by default).
                    frameAnimation.start();

                    new Handler().post(runCommunicating);
                    WhatToDo = "PayNow";
                    Log.d(TAG, "Entering PayNow...");
                } else {
                    Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
                    //mHandler.postDelayed(showButtons, secondsDelay);
                }
            }
        });

        URLFile =  DeviceInfoURL + mDeviceName;
        String mDeviceInfo = HTTPFileTransfer.HTTPGetPageContent(URLFile, getApplicationContext());
        int nDeviceInfoLen = mDeviceInfo.length();
        if (nDeviceInfoLen > 0) {
            // Parse Device Info and Display on Layout
            parseDeviceInfo(mDeviceInfo);
        }

        getActionBar().setTitle(R.string.app_name);
        getActionBar().setIcon(R.drawable.ct_generic);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void CalcNewRate(int nNewIncrements) {
        // Determine if Units Needs To Be Plural Or Not
        String cNewIncrements = String.valueOf(nNewIncrements);
        String cCurrentIncrements = mDeviceIncrements.getText().toString();
        int nUnitsLen = cCurrentIncrements.length();
        String cLastChar = cCurrentIncrements.substring(nUnitsLen - 1, nUnitsLen);
        if (nNewIncrements == 1 && cLastChar.equals("s")) {
            cCurrentIncrements = cCurrentIncrements.substring(0,nUnitsLen - 1);  // Trim Off "s"  (not plural)
        }
        if (nNewIncrements > 1 && !cLastChar.equals("s")) {
            cCurrentIncrements = cCurrentIncrements + "s";  // Add "s"   (plural)
        }
        mDeviceIncrements.setText(cCurrentIncrements);

        // Determine new Amount_Due
        String cRate = mDeviceRate.getText().toString();
        int nloc = cRate.indexOf("/");
        cRate = cRate.substring(1, nloc);   //  Do not include the '$'
        float nRate = Float.valueOf(cRate);
        float nAmtDue = nRate * nNewIncrements;
        nFinalPaidAmt = nAmtDue;
        String cAmtPaid = String.valueOf(nAmtDue);
        String cAmtDue = DecimalFormat.getCurrencyInstance().format(nAmtDue);
        cAmtDue = "Charge = " + cAmtDue;
        mAmountDue.setText(cAmtDue);
    }

    private void PayNow() {
        // Get SIM Card Info
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String DeviceID = telemamanger.getDeviceId();
        String SimSerialNumber = telemamanger.getSimSerialNumber();
        String SimPhoneNumber = telemamanger.getLine1Number();

        // Get Internet Account Info
        /*AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();

        ArrayList<String> internetAccounts = new ArrayList<String>();
        for (Account ac : accounts) {
            String acname = ac.name;
            String actype = ac.type;
        }*/

        String ThisID = null;
        // Get Phone Info
        String phone = HTTPFileTransfer.GetPhoneNumber(getApplicationContext());

        if (phone != null) {
            // Phone Number OK
            ThisID = phone;

        } else if (SimSerialNumber != null) {
            // SIM Card Number OK
            ThisID = SimSerialNumber;

        } else if (DeviceID != null) {
            // DeviceID OK
            ThisID = DeviceID;
        }

        String cIncrements = String.valueOf(nFinalIncrements);
        String DeviceType = mDeviceType.getText().toString();
        String Charge = String.valueOf(nFinalPaidAmt);

        // Save For Subsequent Use
        nFinalPaidAmt = Float.valueOf(Charge.replace("$",""));
        nFinalIncrements = Integer.valueOf(cIncrements);

        String TokenURL = "";
        if (DeviceType.equals("Meter")) {
            // Meter
            TokenURL = TokenURLBase + ThisID + "&device=" + mDeviceName + "&increments=" + cIncrements + "&fee=" + Charge.replace("$","");
        } else {
            // Vending
            TokenURL = TokenURLBase + ThisID + "&device=" + mDeviceName + "&increments=1&fee=" + Charge.replace("$","");
        }
        ClearToken = HTTPFileTransfer.HTTPGetPageContent(TokenURL, getApplicationContext());

        String ResultMsg = "";
        String returnString = "";
        if (!ClearToken.substring(0,2).equals("40") || !(ClearToken.length() == 20 || ClearToken.length() == 18)) {
            // --- Bad Token Reply ---
            // --- Display on Device ---
            if (!(ClearToken.length() == 20 || ClearToken.length() == 18)) {
                // --- Bad Token Length ---
                String crlf = System.getProperty("line.separator");
                String BadToken = "";
                for (int j=0;j<ClearToken.length();j+=2) {
                    if (j+2 > ClearToken.length()) {
                        BadToken = BadToken + ClearToken.substring(j,j+1);
                        break;
                    } else if(j+2 == ClearToken.length()) {
                        BadToken = BadToken + ClearToken.substring(j,j+2);
                        break;
                    } else {
                        BadToken = BadToken + ClearToken.substring(j,j+2) + "-";
                    }
                }

                ResultMsg = "Bad Token Length (" + String.valueOf(ClearToken.length())  + "): " + crlf + BadToken;
            } else {
                ResultMsg = ClearToken;
            }

            if (ClearToken.equals("PHONE_NUMBER_FORMAT_INVALID")) {
                // *** Invalid Phone String Format ***
                Log.d(TAG, "PHONE_NUMBER_FORMAT_INVALID: " + phone);
                ResultMsg = "PHONE_FORMAT_INVALID: " + phone;
            }

            if (ClearToken.equals("NOT_AUTHORIZED")) {
                // *** Invalid Phone String Format ***
                Log.d(TAG, "NOT_AUTHORIZED: " + phone);
                ResultMsg = "NOT_AUTHORIZED: " + phone;
            }

            /* if (ClearToken.equals("ERROR CONNECTING") || ClearToken.equals("UNKNOWN DEVICE")) {
                Log.d(TAG, "writeTimeToDevice()_ERROR_CONNECTING" + ClearToken);
                return;
            }  */
        } else {
            // --- Good ClearToken Returned ---
            byte[] value = new byte[10];
            int command_pos = 0;
            for (int x = 0; x < ClearToken.length(); x = x + 2) {
                value[command_pos] = (byte) Integer.parseInt(ClearToken.substring(x, x + 2), 16);
                //value[command_pos] = (byte) convert(Integer.valueOf(strValue.substring(x,x+2)));
                ++command_pos;
            }
            if (value[2] == 0x00) {
                return;
            }

/*
        value[0] = (byte) 0x40;
        value[1] = (byte) 0x08;
        value[2] = (byte) 0x03;
        value[3] = (byte) 0x00;
        value[4] = (byte) 0x02;
        value[5] = (byte) 0x01;
        value[6] = (byte) 0x00;
        value[7] = (byte) 0x2B;
        value[8] = (byte) 0xE9;
        value[9] = (byte) 0xBC;
*/

            Log.d(TAG, "writeTimeToDevice() DEVICECMD: " + value);
            returnString = mBluetoothLeService.writeCTCharacters(value,
                    UUID.fromString("c9cab9b8-3abf-4043-a5af-9ad00c6074d5"),
                    UUID.fromString("d5dee9b5-456f-4baa-ad5c-a3f14fd2653c"));

            Log.d(TAG, "After sending CT command, return from Device value: " + returnString);

            mBluetoothLeService.disconnect();
            try {
                Thread.sleep(1000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            mBluetoothLeService.close();  // ##########
        }

        if (returnString.equals("SUCCESS")) {
            Log.d(TAG, "Received SUCCESS response from CT device, so calling the fragment to do confirm action");
            //df.onPayConfirm(mDeviceName, "" + mFee, "" + mIncrements, "" + phone, mUnits);
            onSuccess();
        } else {
            // TODO, we should handle this in some way
            Log.d(TAG, "FAILURE: " + ResultMsg);
            onFailure(ResultMsg);
        }
    }

    private void parseDeviceInfo(String deviceInfo){
        // Parse Out Individual Part of Device Info
        ItemDto tempItem = new ItemDto();
        try {
            JSONObject obj = new JSONObject(deviceInfo.toString());
            mThisDeviceName.setText (obj.getString("DeviceId"));
            cBaseLocation = obj.getString("Location").trim();
            mDeviceLocation.setText(cBaseLocation);
            TextView payBtnText = (TextView) findViewById(R.id.paybtntext);

            String deviceType = obj.getString("Type");
            if (deviceType.equals("M")) {
                mDeviceType.setText("Meter");
                payBtnText.setText("PAY");
            } else {
                mDeviceType.setText("Vending");
                payBtnText.setText("VEND");
            }

            String units = obj.getString("Units");
            String increments = obj.getString("Increments").trim();
            String thisUnits = "";
            if (units.equals("S")) {
                thisUnits = "Second";
            }
            if (units.equals("M")) {
                thisUnits = "Minute";
            }
            if (units.equals("H")) {
                thisUnits = "Hour";
            }
            String cIncrements = "";
            if (increments.equals("1")) {
                cIncrements = thisUnits;
            } else {
                cIncrements = (increments + " " + thisUnits);
            }

            float nFee = Float.valueOf(obj.getString("Fee"));
            String cFee = String.valueOf(nFee);
            String Fee = DecimalFormat.getCurrencyInstance().format(nFee);
            String DevRate = Fee + " / " + cIncrements;
            int len = DevRate.length();
            // Do We Need To Abbreviate?
            if (len > 14) {
                DevRate = DevRate.replace("Hour","Hr");
                DevRate = DevRate.replace("Minute","Min");
                DevRate = DevRate.replace("Second","Sec");
            }

            // Save 'Base' Values For Subsequent Reference
            //final Intent intent = new Intent(this, DeviceControlActivity.class);
            nBaseIncrements = Integer.valueOf(increments);
            cBaseUnits = units;
            nBaseFee = nFee;
            cBaseType = deviceType;

            String maxInc = obj.getString("MaxIncrements");
            nBaseMaxInc = Integer.valueOf(maxInc);
            String cMaxInc = maxInc + " " + thisUnits + " Maximum";
            // Do We Need To Abbreviate?
            len = cMaxInc.length();
            if (len > 16) {
                cMaxInc = cMaxInc.replace("Maximum", "Max");
            }

            // Do We Need To Abbreviate Even More?
            len = cMaxInc.length();
            if (len > 16) {
                cMaxInc = cMaxInc.replace("Seconds", "Sec");
                cMaxInc = cMaxInc.replace("Hours", "Hr");
                cMaxInc = cMaxInc.replace("Minutes", "Min");
            }

            if (deviceType.equals("M")) {
                // Meter Displays More Information
                mDeviceIncrements.setText(cIncrements);
                mDeviceRate.setText(DevRate);
                mDeviceIncMax.setText(cMaxInc);
                mIncrementUp.setEnabled(true);
                mIncrementUp.setVisibility(View.VISIBLE);
                mIncrementDown.setEnabled(true);
                mIncrementDown.setVisibility(View.VISIBLE);
            } else {
                // Vending Displays Less Information
                mDeviceIncrements.setText("");
                mDeviceRate.setText("");
                mDeviceIncMax.setText("");
                mIncrementUp.setEnabled(false);
                mIncrementUp.setVisibility(View.INVISIBLE);
                mIncrementUp.setBackgroundColor(Color.parseColor(mDeviceColor));
                mIncrementDown.setEnabled(false);
                mIncrementDown.setVisibility(View.INVISIBLE);
                mIncrementDown.setBackgroundColor(Color.parseColor(mDeviceColor));
            }

            nFinalIncrements = 1;
            mIncrements.setText("1");
            mAmountDue.setText("Charge = " + Fee);
            nFinalPaidAmt = nBaseFee;

            // Set Layout Background Color
            centerLayout.setBackgroundColor(Color.parseColor(mDeviceColor));

            // Get Device Icon Info and Display It
            cBaseImage = obj.getString("PhotoFile").trim();
            len = cBaseImage.length();
            if (len == 0) {
                cBaseImage = "DEFAULT";
            }

            final String iconURL = DeviceIconURL + cBaseImage + ".png";
            Picasso.with(this)
                    .load(iconURL)
                    .into(deviceIcon);

        } catch (JSONException e) {
            Log.d(TAG, "ERROR: " + e, e);
        }
    }

    private void onSuccess() {
        // Successfully Wrote To ClearToken Device
        Log.d(TAG, "Successful Write To ClearToken Device: " + mDeviceName);

        // Get 'Base' Values
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceColor = intent.getStringExtra(EXTRAS_DEVICE_COLOR);

        int BaseInc = nBaseIncrements;
        String BaseUnits = cBaseUnits;
        Float BaseFee = nBaseFee;
        Float FinalPaidAmt = nFinalPaidAmt;
        int FinalIncrements = nFinalIncrements;

        // Line #1 of Result Message
        String cPaidAmt = DecimalFormat.getCurrencyInstance().format(FinalPaidAmt);

        String ResultMsg1 = "";
        String ResultMsg2 = "";
        if (FinalPaidAmt > 0) {
            ResultMsg1 = cPaidAmt + " has been charged to your account.";
        }
        if (ResultMsg1.length() > 39) {
            // Abbreviate Line In Order To Fit Onto Screen
            ResultMsg1 = ResultMsg1.replace("account","acct");
        }

        String DeviceType = "";
        String ResultMsg = "";
        if (cBaseType.equals("M")) {
            DeviceType = "Meter";

            // Meter requires calculation
            // Calculate Paid-Until Time

            // Line #2 of Result Message - Calculate Purchased Minutes
            int purchasedMin = 0;
            if (BaseUnits.equals("S")) {
                purchasedMin = FinalIncrements * (BaseInc / 60);
            }
            if (BaseUnits.equals("M")) {
                purchasedMin = FinalIncrements * (BaseInc);
            }
            if (BaseUnits.equals("H")) {
                purchasedMin = FinalIncrements * (BaseInc * 60);
            }

            // Add Minutes To Current DateTime
            Calendar now = Calendar.getInstance();  // Get Current DateTime
            now.add(Calendar.MINUTE, purchasedMin);    // Add nn Minutes To Current DateTime

            int hr = now.get(Calendar.HOUR_OF_DAY);
            int hr_ampm = hr;
            String TimeSuffix = "AM";
            if (hr > 12) {
                hr_ampm = hr_ampm - 12;
                TimeSuffix = "PM";
            } else if (hr == 12) {
                TimeSuffix = "PM";
            }
            int min = now.get(Calendar.MINUTE);
            String cMin = String.valueOf(min);
            if (cMin.length() == 1){
                cMin = "0" + cMin;
            }

            String PaidUntilTime = String.valueOf(hr_ampm) + ":" + cMin + " " + TimeSuffix;
            ResultMsg2 = "You are paid until: " + PaidUntilTime;
        } else {
            // Vending has no calculations
            DeviceType = "Vending";
            ResultMsg2 = "Done";
        }

        ResultMsg = "";
        if (ResultMsg1.length() > 0) {
            ResultMsg = ResultMsg1 + "\n" + "\n" + ResultMsg2;
        } else {
            ResultMsg = ResultMsg2;
        }

        // Display Results Layout
        // JRB - Set up to utilize this Layout
        setContentView(R.layout.display_device_result);
        //setContentView(R.layout.gatt_services_characteristics);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

        centerLayout = (LinearLayout) findViewById(R.id.center_layout);
        mThisDeviceName = (TextView) findViewById(R.id.device_name);
        mDeviceType = (TextView) findViewById(R.id.device_type);
        mDeviceType.setText(DeviceType);
        mDeviceLocation = (TextView) findViewById(R.id.device_location);
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        exitBtnImg = (ImageView) findViewById(R.id.exitbtnimg);
        ImageView exitBtnImg = (ImageView) findViewById((R.id.exitbtnimg));
        exitBtnImg.setBackgroundResource(R.drawable.roundedbutton);
        exitBtnText = (TextView) findViewById(R.id.exitbtntext);

        deviceIcon = (ImageView) findViewById(R.id.device_icon);
        resultMsg = (TextView) findViewById(R.id.result_message);

        mThisDeviceName.setText(mDeviceName);
        mDeviceLocation.setText(cBaseLocation);
        centerLayout.setBackgroundColor(Color.parseColor(mDeviceColor));

        if (ResultMsg.equals("Done")) {
            resultMsg.setGravity(Gravity.CENTER);
            resultMsg.setTextSize(28);
        }
        resultMsg.setText(ResultMsg);

        final String iconURL = DeviceIconURL + cBaseImage + ".png";
        Picasso.with(this)
                .load(iconURL)
                .into(deviceIcon);

        // --------  EXIT on Success  --------
        exitBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
                if (danman) {
                    // Load the ImageView that will host the animation
                    ImageView exitBtnImg = (ImageView) findViewById((R.id.exitbtnimg));
                    // set its background to our AnimationDrawable XML resource.
                    exitBtnImg.setBackgroundResource(R.drawable.roundedbutton);
                    /*
                    * Get the background, which has been compiled to an AnimationDrawable
                    * object.
                    */
                    AnimationDrawable frameAnimation = (AnimationDrawable) exitBtnImg.getBackground();

                    // Start the animation (looped playback by default).
                    frameAnimation.start();

                    //new Handler().post(runCommunicating);
                    WhatToDo = "Quit";
                    Log.d(TAG, "Entering QUIT...");
                } else {
                    Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
                    //mHandler.postDelayed(showButtons, secondsDelay);
                }
            }
        });

        getActionBar().setTitle(R.string.app_name);
        getActionBar().setIcon(R.drawable.ct_generic);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void onFailure(String ResultMsg) {
        // Failure To Write ClearToken Device
        Log.d(TAG, "Failure To Write To ClearToken Device: " + mDeviceName);

        // Get 'Base' Values
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceColor = intent.getStringExtra(EXTRAS_DEVICE_COLOR);

        String ResultMsg1 = "";
        String ResultMsg2 = ResultMsg;

        ResultMsg = "";
        if (ResultMsg1.length() > 0) {
            ResultMsg = ResultMsg1 + "\n" + "\n" + ResultMsg2;
        } else {
            ResultMsg = ResultMsg2;
        }

        // Display Results Layout
        // JRB - Set up to utilize this Layout
        setContentView(R.layout.display_device_result);
        //setContentView(R.layout.gatt_services_characteristics);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

        centerLayout = (LinearLayout) findViewById(R.id.center_layout);
        mThisDeviceName = (TextView) findViewById(R.id.device_name);
        mDeviceLocation = (TextView) findViewById(R.id.device_location);
        mDeviceType = (TextView) findViewById(R.id.device_type);
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        exitBtnImg = (ImageView) findViewById(R.id.exitbtnimg);
        ImageView exitBtnImg = (ImageView) findViewById((R.id.exitbtnimg));
        exitBtnImg.setBackgroundResource(R.drawable.roundedbutton);
        exitBtnText = (TextView) findViewById(R.id.exitbtntext);

        deviceIcon = (ImageView) findViewById(R.id.device_icon);
        resultMsg = (TextView) findViewById(R.id.result_message);

        mThisDeviceName.setText(mDeviceName);
        mDeviceLocation.setText(cBaseLocation);
        if (cBaseType.equals("M")) {
            mDeviceType.setText("Meter");
        } else {
            mDeviceType.setText("Vending");
        }
        centerLayout.setBackgroundColor(Color.parseColor(mDeviceColor));

        resultMsg.setGravity(Gravity.CENTER);
        if (ResultMsg.length() <= 34) {
            resultMsg.setTextSize(20);
        } else {
            resultMsg.setTextSize(16);
        }
        resultMsg.setText(ResultMsg);

        final String iconURL = DeviceIconURL + cBaseImage + ".png";
        Picasso.with(this)
                .load(iconURL)
                .into(deviceIcon);

        // --------  EXIT on Failure  --------
        exitBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean danman = mBluetoothLeService.connect(mDeviceAddress);
                if (danman) {
                    // Load the ImageView that will host the animation
                    ImageView exitBtnImg = (ImageView) findViewById((R.id.exitbtnimg));
                    // set its background to our AnimationDrawable XML resource.
                    exitBtnImg.setBackgroundResource(R.drawable.roundedbutton);
                    /*
                    * Get the background, which has been compiled to an AnimationDrawable
                    * object.
                    */
                    AnimationDrawable frameAnimation = (AnimationDrawable) exitBtnImg.getBackground();

                    // Start the animation (looped playback by default).
                    frameAnimation.start();

                    //new Handler().post(runCommunicating);
                    WhatToDo = "Quit";
                    Log.d(TAG, "Entering QUIT...");
                } else {
                    Toast.makeText(getApplicationContext(), "No Connect!", Toast.LENGTH_LONG).show();
                    //mHandler.postDelayed(showButtons, secondsDelay);
                }
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
/*                  buttonTwoMin.setVisibility(View.VISIBLE);
                    buttonSecond.setVisibility(View.VISIBLE);
                    buttonReset.setVisibility(View.VISIBLE);
                    buttonNear.setVisibility(View.VISIBLE);
                    buttonMedium.setVisibility(View.VISIBLE);
                    buttonFar.setVisibility(View.VISIBLE);
                    editURI.setVisibility(View.VISIBLE);
                    buttonWriteURI.setVisibility(View.VISIBLE);
                    buttonOn.setVisibility(View.VISIBLE);
                    buttonOff.setVisibility(View.VISIBLE);
*/
                }
                else
                {
/*                  buttonTwoMin.setVisibility(View.INVISIBLE);
                    buttonSecond.setVisibility(View.INVISIBLE);
                    buttonReset.setVisibility(View.INVISIBLE);
                    buttonNear.setVisibility(View.INVISIBLE);
                    buttonMedium.setVisibility(View.INVISIBLE);
                    buttonFar.setVisibility(View.INVISIBLE);
                    editURI.setVisibility(View.INVISIBLE);
                    buttonWriteURI.setVisibility(View.INVISIBLE);
                    buttonOn.setVisibility(View.INVISIBLE);
                    buttonOff.setVisibility(View.INVISIBLE);
*/
                }
                mConnectionState.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            //mDataField.setText(data);
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
/*
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
*/

    public static int convert(int n) {
        return Integer.valueOf(String.valueOf(n), 16);
    }

/*
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
*/

    private void WriteReset()
    {
        String phone=HTTPFileTransfer.GetPhoneNumber(getApplicationContext());
        String strValue=HTTPFileTransfer.HTTPGetPageContent("http://www.cleartoken.com/services/default.aspx?phone="+phone+"&device="+mDeviceName+"&duration=1", getApplicationContext());
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

/*
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
*/

/*
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
*/

    private void updateItem() {
/*
        View newView;

        Drawable rndBtnDark = getResources().getDrawable(R.drawable.roundedbutton);
        Drawable rndBtnLight = getResources().getDrawable(R.drawable.roundedbutton2);

        buttonPay = (Button) findViewById(R.id.pay_button);
        Drawable bkgnd = buttonPay.getBackground(getResources().getDrawable(R.id.pay_button));

        if (bkgnd == fndBtnDark) {
            // Change from Dark to Light
            buttonPay.setBackground(R.drawable.roundedbutton2);
        } else {
            // Change from Light to Dark
            buttonPay.setBackground(R.drawable.roundedbutton);
        }

        mContainerView.updateViewLayout(newView, 0);
*/
    }


    public void quit() {
        finishAffinity();  // Completely close the app
    }

    void ProcessWhatToDo()
    {
        if (WhatToDo.equals("PayNow"))
        {
            PayNow();
        }
        if (WhatToDo.equals("Parking2Minutes"))
        {
            //Write1Unit();
        }
        if (WhatToDo.equals("Vend1Second"))
        {
            //Write1Second();
        }
        if (WhatToDo.equals("Quit"))
        {
           quit();
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
            //SetAntenna((byte) 0);
        }

        if (WhatToDo.equals("Medium"))
        {
            //SetAntenna((byte) 1);
        }
        if (WhatToDo.equals("Far"))
        {
            //SetAntenna((byte) 2);
        }
        if (WhatToDo.equals("BeaconOn"))
        {
            //SetBeacon((byte) 1);
        }
        if (WhatToDo.equals("BeaconOff"))
        {
            //SetBeacon((byte) 0);
        }

        WhatToDo = "";

    }




}
