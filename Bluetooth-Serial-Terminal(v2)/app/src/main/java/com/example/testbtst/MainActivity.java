package com.example.testbtst;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG_MA";
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1; // Or any other integer value

    // Declare cBTInitSendReceive as a field
    private classBTInitDataCommunication cBTInitSendReceive = null;

    Button buttonSendMessage;
    Button buttonBTConnect;
    Button buttonShare;
    Button buttonMemory1;
    Button buttonMemory2;
    Button buttonMemory3;
    Button buttonMemory4;

    Button buttonPopUpSave;
    Button buttonPopUpCancel;

    TextView tvMAReceivedMessage;
    EditText editTextSendMessage;

    EditText editTextPopUpLabel;
    EditText editTextPopUpData;

    LinearLayout linearLayoutPopupSaveData;
    PopupWindow popupWindowSaveData;
    Spinner spinnerBTPairedDevices;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket BTSocket = null;
    BluetoothAdapter BTAdaptor = null;
    Set<BluetoothDevice> BTPairedDevices = null;
    boolean bBTConnected = false;
    BluetoothDevice BTDevice = null;

    static public final int BT_CON_STATUS_NOT_CONNECTED = 0;
    static public final int BT_CON_STATUS_CONNECTING = 1;
    static public final int BT_CON_STATUS_CONNECTED = 2;
    static public final int BT_CON_STATUS_FAILED = 3;
    static public final int BT_CON_STATUS_CONNECTION_LOST = 4;
    static public int iBTConnectionStatus = BT_CON_STATUS_NOT_CONNECTED;
    static final int BT_STATE_LISTENING = 1;
    static final int BT_STATE_CONNECTING = 2;
    static final int BT_STATE_CONNECTED = 3;
    static final int BT_STATE_CONNECTION_FAILED = 4;
    static final int BT_STATE_MESSAGE_RECEIVED = 5;

    String sM1Index= "",sM1Data="";
    String sM2Index= "",sM2Data="";
    String sM3Index= "",sM3Data="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate-Start");

        tvMAReceivedMessage = findViewById(R.id.idMATextViewReceivedMessage);
        tvMAReceivedMessage.setMovementMethod(new ScrollingMovementMethod());



        tvMAReceivedMessage.setText("App Loaded");

        spinnerBTPairedDevices = findViewById(R.id.idMASpinnerBTPairedDevices);

        buttonSendMessage = findViewById(R.id.idMAButtonSendData);
        buttonBTConnect = findViewById(R.id.idMAButtonConnect);
        buttonShare = findViewById(R.id.idMAButtonShare);

        buttonMemory1 = findViewById(R.id.idMAButtonStoreData1);
        buttonMemory2 = findViewById(R.id.idMAButtonStoreData2);
        buttonMemory3 = findViewById(R.id.idMAButtonStoreData3);
        buttonMemory4 = findViewById(R.id.idMAButtonStoreData4);


        editTextSendMessage = findViewById(R.id.idMAEditTextSendMessage);
        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Send Button clicked");

                String sMessage = editTextSendMessage.getText().toString();
                tvMAReceivedMessage.append("\n->" + sMessage);

                sendMessage(sMessage);
            }
        });
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Button Click buttonShare");


                Log.d(TAG, "sharing  : " + tvMAReceivedMessage.getText().toString());
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_SUBJECT,"Share BTTerminal message");
                intentShare.putExtra(Intent.EXTRA_TEXT,tvMAReceivedMessage.getText().toString());
                startActivity(Intent.createChooser(intentShare, "Sharing BT Terminal"));


            }
        });
        buttonMemory1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Button Click buttonMemory1");
                sendMessage(sM1Data);
            }
        });
        buttonMemory1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "Button Long Press buttonMemory1");
                LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.layoutstoredata,null);
                buttonPopUpCancel = customView.findViewById(R.id.idSDButtonCancel);
                buttonPopUpSave = customView.findViewById(R.id.idSDButtonSave);
                editTextPopUpLabel = customView.findViewById(R.id.idSDEditTextLabel);
                editTextPopUpData = customView.findViewById(R.id.idSDEditTextData);
                editTextPopUpData.setText(sM1Data);
                editTextPopUpLabel.setText(sM1Index);
                linearLayoutPopupSaveData = customView.findViewById(R.id.idLLPopupStoreData);
                popupWindowSaveData = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindowSaveData.setFocusable(true);
                popupWindowSaveData.update();
                //display the popup window
                popupWindowSaveData.showAtLocation(linearLayoutPopupSaveData, Gravity.CENTER, 0, 0);
                buttonPopUpCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindowSaveData.dismiss();
                    }
                });
                buttonPopUpSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(editTextPopUpData.getText().length()<1 || editTextPopUpLabel.getText().length()<1)
                        {
                            Toast.makeText(getApplicationContext(), "Please enter both label and Data", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        storeData("1",editTextPopUpLabel.getText().toString(),editTextPopUpData.getText().toString());
                        popupWindowSaveData.dismiss();
                        readAllData();
                    }
                });
                return false;
            }
        });
        buttonMemory2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Send Button clicked buttonMemory2");
                sendMessage(sM2Data);
            }
        });
        buttonMemory2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Log.d(TAG, "Button Long Press buttonMemory2");
                LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.layoutstoredata,null);
                buttonPopUpCancel = customView.findViewById(R.id.idSDButtonCancel);
                buttonPopUpSave = customView.findViewById(R.id.idSDButtonSave);
                editTextPopUpLabel = customView.findViewById(R.id.idSDEditTextLabel);
                editTextPopUpData = customView.findViewById(R.id.idSDEditTextData);
                editTextPopUpData.setText(sM2Data);
                editTextPopUpLabel.setText(sM2Index);
                linearLayoutPopupSaveData = customView.findViewById(R.id.idLLPopupStoreData);
                popupWindowSaveData = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindowSaveData.setFocusable(true);
                popupWindowSaveData.update();
                //display the popup window
                popupWindowSaveData.showAtLocation(linearLayoutPopupSaveData, Gravity.CENTER, 0, 0);
                buttonPopUpCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindowSaveData.dismiss();
                    }
                });
                buttonPopUpSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(editTextPopUpData.getText().length()<1 || editTextPopUpLabel.getText().length()<1)
                        {
                            Toast.makeText(getApplicationContext(), "Please enter both label and Data", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        storeData("2",editTextPopUpLabel.getText().toString(),editTextPopUpData.getText().toString());
                        popupWindowSaveData.dismiss();
                        readAllData();
                    }
                });

                return false;
            }
        });

        buttonMemory3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Send Button clicked buttonMemory3");
                sendMessage(sM3Data);
            }
        });
        buttonMemory3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Log.d(TAG, "Button Long Press buttonMemory2");
                LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.layoutstoredata,null);
                buttonPopUpCancel = customView.findViewById(R.id.idSDButtonCancel);
                buttonPopUpSave = customView.findViewById(R.id.idSDButtonSave);
                editTextPopUpLabel = customView.findViewById(R.id.idSDEditTextLabel);
                editTextPopUpData = customView.findViewById(R.id.idSDEditTextData);
                editTextPopUpData.setText(sM3Data);
                editTextPopUpLabel.setText(sM3Index);
                linearLayoutPopupSaveData = customView.findViewById(R.id.idLLPopupStoreData);
                popupWindowSaveData = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindowSaveData.setFocusable(true);
                popupWindowSaveData.update();
                //display the popup window
                popupWindowSaveData.showAtLocation(linearLayoutPopupSaveData, Gravity.CENTER, 0, 0);
                buttonPopUpCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindowSaveData.dismiss();
                    }
                });
                buttonPopUpSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(editTextPopUpData.getText().length()<1 || editTextPopUpLabel.getText().length()<1)
                        {
                            Toast.makeText(getApplicationContext(), "Please enter both label and Data", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        storeData("3",editTextPopUpLabel.getText().toString(),editTextPopUpData.getText().toString());
                        popupWindowSaveData.dismiss();
                        readAllData();
                    }
                });

                return false;
            }
        });

        buttonMemory4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Send Button clicked CLear screen");
                tvMAReceivedMessage.setText("");
            }
        });

        buttonBTConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button clicked buttonBTConnect");
                if (bBTConnected == false) {
                    if (spinnerBTPairedDevices.getSelectedItemPosition() == 0) {
                        Log.d(TAG, "Please select BT device");
                        Toast.makeText(getApplicationContext(), "Please select Bluetooth Device", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String sSelectedDevices = spinnerBTPairedDevices.getSelectedItem().toString();
                    Log.d(TAG, "Selected device = " + sSelectedDevices);

                    for (BluetoothDevice BTDev : BTPairedDevices) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                            return;
                        }
                        if (sSelectedDevices.equals(BTDev.getName())) {
                            BTDevice = BTDev;
                            Log.d(TAG, "Selected device UUID = " + BTDevice.getAddress());

                            cBluetoothConnect cBTConnect = new cBluetoothConnect(BTDevice);
                            cBTConnect.start();

//                            try {
//                                Log.d(TAG, "Create socket, my uuid = " + MY_UUID);
//                                BTSocket = BTDevice.createRfcommSocketToServiceRecord(MY_UUID);
//                                Log.d(TAG, "Connecting to device");
//                                BTSocket.connect();
//                                Log.d(TAG, "Connected");
//                                buttonBTConnect.setText("Disconnect");
//                                bBTConnected = true;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Log.e(TAG, "Exception = " + e.getMessage());
//                                bBTConnected = false;
//                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Disconnecting BTConnection");
                    if (BTSocket != null && BTSocket.isConnected()) {
                        try {
                            BTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "BTDisconnect Exp" + e.getMessage());
                        }
                    }

                    buttonBTConnect.setText("Connect");
                    bBTConnected = false;

                }

            }
        });

    }

    void storeData(String sButtonNumber,String sIndex, String sValue){
        Log.d(TAG,"storeData : "+sButtonNumber+", Index : "+sIndex+", Value : "+sValue);
        try{
            SharedPreferences spSavedBluetoothDevice = getSharedPreferences("TERMINAL_STORED_DATA",this.MODE_PRIVATE);
            SharedPreferences.Editor editor = spSavedBluetoothDevice.edit();
            editor.putString("M"+sButtonNumber+"_INDEX",sIndex);
            editor.putString("M"+sButtonNumber+"_DATA",sValue);
            editor.commit();
        }catch (Exception exp){

        }
    }
    void readAllData(){
        Log.d(TAG,"readAllData");
        try{
            SharedPreferences spSavedBluetoothDevice = getSharedPreferences("TERMINAL_STORED_DATA",this.MODE_PRIVATE);
            sM1Index = spSavedBluetoothDevice.getString("M1_INDEX",null);
            if(sM1Index == null){
                Log.d(TAG,"storing Default Data");
                storeData("1","S1","M1 Data");
                storeData("2","S2","M2 Data");
                storeData("3","S3","M3 Data");
            }
            sM1Index = spSavedBluetoothDevice.getString("M1_INDEX",null);sM1Data = spSavedBluetoothDevice.getString("M1_DATA",null);
            sM2Index = spSavedBluetoothDevice.getString("M2_INDEX",null);sM2Data = spSavedBluetoothDevice.getString("M2_DATA",null);
            sM3Index = spSavedBluetoothDevice.getString("M3_INDEX",null);sM3Data = spSavedBluetoothDevice.getString("M3_DATA",null);


            buttonMemory1.setText(sM1Index);
            buttonMemory2.setText(sM2Index);
            buttonMemory3.setText(sM3Index);

        }catch (Exception exp){

        }
    }


    public class cBluetoothConnect extends Thread {
        private BluetoothDevice device;

        public cBluetoothConnect(BluetoothDevice BTDevice) {
            Log.i(TAG, "classBTConnect-start");

            device = BTDevice;
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
                BTSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (Exception exp) {
                Log.e(TAG, "classBTConnect-exp" + exp.getMessage());
            }
        }

        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request the missing permissions
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
                BTSocket.connect();
                Message message = Message.obtain();
                message.what = BT_STATE_CONNECTED;
                handler.sendMessage(message);
            }catch(IOException e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = BT_STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    public class classBTInitDataCommunication extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public classBTInitDataCommunication(BluetoothSocket socket){
            Log.i(TAG, "classBTInitDataCommunication-start");
            bluetoothSocket = socket;

            try{
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
                Log.e(TAG,"classBTInitDataCommunication-start, exp"+e.getMessage());
            }

        }
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(BTSocket.isConnected()){
                try{
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(BT_STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                }catch (IOException e){
                    e.printStackTrace();
                    Log.e(TAG,"BT disconnect from decide end, exp"+e.getMessage());
                    iBTConnectionStatus = BT_CON_STATUS_CONNECTION_LOST;
                    try{
                        Log.d(TAG,"Disconnecting BTConnection");
                        if(BTSocket!=null && BTSocket.isConnected()){
                            BTSocket.close();
                        }
                        buttonBTConnect.setText("Connect");
                        bBTConnected = false;
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }







//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
//            // Check if the permission is granted.
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with getting paired devices.
//                getBTPairedDevices();
//                populateSpinnerWithBTPairedDevices();
//            } else {
//                // Permission denied, inform the user or handle the denial gracefully.
//                editTextSendMessage.setText("\nBluetooth permission denied");
//            }
//        }
//    }

    Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg){
            switch(msg.what){
                case BT_STATE_LISTENING:
                    Log.d(TAG,"BT_STATE_LISTENING");
                    break;
                case BT_STATE_CONNECTING:
                    iBTConnectionStatus = BT_CON_STATUS_CONNECTING;
                    buttonBTConnect.setText("Connecting..");
                    Log.d(TAG,"BT_STATE_CONNECTING");
                    break;
                case BT_STATE_CONNECTED:
                    iBTConnectionStatus = BT_CON_STATUS_CONNECTED;
                    Log.d(TAG,"BT_STATE_CONNECTED");
                    buttonBTConnect.setText("Disconnect");

                    cBTInitSendReceive = new classBTInitDataCommunication(BTSocket);
                    cBTInitSendReceive.start();

                    bBTConnected = true;
                    break;
                case BT_STATE_CONNECTION_FAILED:
                    iBTConnectionStatus = BT_CON_STATUS_FAILED;
                    Log.d(TAG,"BT_STATE_CONNECTION_FAILED");
                    bBTConnected = false;
                    break;
                case BT_STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0 ,msg.arg1);
                    Log.d(TAG,"Message receive ( "+tempMsg.length()+" ) data : "+tempMsg);

                    tvMAReceivedMessage.append(tempMsg);
                    break;
            }
            return true;
        }
    });

    public void sendMessage(String sMessage)
    {
        if( BTSocket!= null && iBTConnectionStatus==BT_CON_STATUS_CONNECTED)
        {
            if(BTSocket.isConnected() )
            {
                try {
                    cBTInitSendReceive.write(sMessage.getBytes());
                    tvMAReceivedMessage.append("\r\n-> " + sMessage);
                }
                catch (Exception exp)
                {

                }
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Please connect to bluetooth", Toast.LENGTH_SHORT).show();
            tvMAReceivedMessage.append("\r\n Not connected to bluetooth");
        }

    }

    void getBTPairedDevices() {
        Log.d(TAG, "getBTPairedDevices - start");

        BTAdaptor = BluetoothAdapter.getDefaultAdapter();
        if (BTAdaptor == null) {
            Log.e(TAG, "getBTPairedDevices , BTAdaptor null");
            editTextSendMessage.setText("\nNo Bluetooth Device in the phone");
            return;
        } else if (!BTAdaptor.isEnabled()) {
            Log.e(TAG, "getBTPairedDevices , BT not enabled");
            editTextSendMessage.setText("\nPlease turn ON Bluetooth");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
            return;
        }
        BTPairedDevices = BTAdaptor.getBondedDevices();

        Log.d(TAG, "getBTPairedDevices , Paired devices cout = " + BTPairedDevices.size());


        for (BluetoothDevice BTDev : BTPairedDevices) {
            Log.d(TAG, BTDev.getName() + ", " + BTDev.getAddress());
        }
    }
    void populateSpinnerWithBTPairedDevices() {
        ArrayList<String> alPairedDevices = new ArrayList<>();
        alPairedDevices.add("Select");
        for (BluetoothDevice BTDev : BTPairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                return;
            }
            alPairedDevices.add(BTDev.getName());
        }
        final ArrayAdapter<String > aaPairedDevices = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,alPairedDevices);
        aaPairedDevices.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerBTPairedDevices.setAdapter((aaPairedDevices));
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume-Resume");

        getBTPairedDevices();
        populateSpinnerWithBTPairedDevices();

        readAllData();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause-Start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy-Start");
    }
}