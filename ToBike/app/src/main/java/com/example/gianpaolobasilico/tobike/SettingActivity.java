package com.example.gianpaolobasilico.tobike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;

public class SettingActivity extends AppCompatActivity {


    Toolbar toolbar;
    private static final int REQUEST_CODE_TO_SETTING=1992;
    private ListView devices;
    private ArrayAdapter arrayAdapter;
    private Button cerca;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT=4321;
    private BroadcastReceiver broadcastReceiver;
    ArrayList<BluetoothDevice> avaibleDevices;
    ConnectThread connectThread;
    Context context;

   /**  00001101-0000-1000-8000-00805F9B34FB   */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context=this;
        toolbar=(Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        devices=(ListView)findViewById(R.id.listaDevices);
        avaibleDevices=new ArrayList<>();
        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UUID devUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                connectThread =new ConnectThread(avaibleDevices.get(position),devUUID,bluetoothAdapter);
                connectThread.run();
                Intent result=new Intent();
                result.putExtra("connectThread", (Parcelable) connectThread);
                setResult(RESULT_OK,result);
            }
        });
        cerca=(Button)findViewById(R.id.connetiti);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                Log.i("ric",intent.toString());
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    arrayAdapter.add(bluetoothDevice.getName()+"\n"+bluetoothDevice.getAddress());
                    avaibleDevices.add(bluetoothDevice);
                    Log.i("ric",bluetoothDevice.getName());



                }

            }
        };


        if(!bluetoothAdapter.isEnabled()){
            Intent enablebtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enablebtIntent,REQUEST_ENABLE_BT);
        }

        cerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapter=new ArrayAdapter<String>(context,R.layout.listdevices);
                bluetoothAdapter.startDiscovery();
                devices.setAdapter(arrayAdapter);
                Log.i("inizio","inizio ricerca");
            }
        });


    }

}
