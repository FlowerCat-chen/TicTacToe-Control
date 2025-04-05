package com.flowercat.chessControl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.widget.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.graphics.Color;

public class MainActivity extends Activity {
    // 蓝牙适配器
    private BluetoothAdapter bluetoothAdapter = null;
    // 已配对的蓝牙设备列表
    private List<String> pairedDevicesList = new ArrayList<String>();
    // 已配对的蓝牙设备
    private Set<BluetoothDevice> pairedDevicesSet;
    // UI组件
    private Spinner deviceSpinner;
    private EditText sendData;
    private EditText receivedData;
    private Button connectButton;
    private Button sendButton;
    private TextView textView1;
    private TextView textView2;
	private Button bt_bottom_add,bt_bottom_dis,bt_top_add,bt_top_dis;
	
	private Button bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9;
	
	private Button bt_stroke,bt_chess,bt_lay,bt_clear,bt_initpos,bt_straight;//走棋，描边，放棋,清空,归位
	
	private Button enable_scroll,disable_scroll,bt_drawing;//
	
	private ScrollView mScrollView;
	
	private int selectedgrid;

    // 选中的设备名称和地址
    private String selectedDeviceName = "";
    private String selectedDeviceAddress = "";
    // BluetoothService
    private BluetoothService bluetoothService = null;
    // 标志位，用于判断是否已经连接上蓝牙设备
    private boolean isConnected = false;

	
	public int degree_bottom = 0;
	public int degree_top = 0;
	
	public TextView bottom_num,top_num;
    private DrawBoardView drawingView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 如果获取失败，说明此设备不支持蓝牙
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 获取UI组件
        deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
        sendData = (EditText) findViewById(R.id.editText1);
        receivedData = (EditText) findViewById(R.id.receivedData);
        connectButton = (Button) findViewById(R.id.connectButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

		bt_bottom_add = findViewById(R.id.bt_bottom_add);
		bt_bottom_dis = findViewById(R.id.bt_bottom_dis);
		bt_top_add = findViewById(R.id.bt_top_add);
		bt_top_dis = findViewById(R.id.bt_top_dis);

        bottom_num = findViewById(R.id.tv_bottom);
		top_num = findViewById(R.id.tv_top);
		
		mScrollView = findViewById(R.id.scrollview);
		
		enable_scroll = findViewById(R.id.enable_scroll);
		disable_scroll = findViewById(R.id.disable_scroll);
		
		bt_stroke = findViewById(R.id.bt_stroke);//描边
		bt_chess = findViewById(R.id.bt_chess);//下棋
		
		bt_lay = findViewById(R.id.bt_lay);
		
		bt_clear = findViewById(R.id.bt_clear);
		
		bt_initpos = findViewById(R.id.bt_initpos);
		
		bt_straight = findViewById(R.id.bt_straight);
		
		drawingView = findViewById(R.id.drawBoardView);
		
		bt_drawing = findViewById(R.id.bt_drawing);
		
		//9个格子
		bt1 = findViewById(R.id.bt1);
		bt2 = findViewById(R.id.bt2);
		bt3 = findViewById(R.id.bt3);
		bt4 = findViewById(R.id.bt4);
		bt5 = findViewById(R.id.bt5);
		bt6 = findViewById(R.id.bt6);
		bt7 = findViewById(R.id.bt7);
		bt8 = findViewById(R.id.bt8);
		bt9 = findViewById(R.id.bt9);
		
		
        // 检查蓝牙是否开启
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
		initLogic();
	}


   public void setScrollEnabled(boolean enable){
	   if(!enable){
		   mScrollView.setOnTouchListener(new OnTouchListener() {
				   @Override
				   public boolean onTouch(View v, MotionEvent event) {
					   return true;
				   }
			   });
	   } else {
		   mScrollView.setOnTouchListener(null);
	   }
   }

   
   
   
   
   public void handleSingle(String mes){
	   //处理返回的信号
	   Toast.makeText(MainActivity.this,mes,Toast.LENGTH_LONG).show();
	   
	   // 按 | 分割字符串
	   String[] keyValuePairs = mes.split("\\|");

	   // 遍历每个 key:value 对
	   for (String pair : keyValuePairs) {
		   // 按 : 分割每个 key:value 对
		   String[] keyVal = pair.split(":");

		   // 获取 key 和 value
		   String key = keyVal[0];
		   String value = keyVal[1];

		   // 打印 key 和 value
	   
		   switch(key){
			   case "user":
				   setGrid("user",value);
				   break;

			   case "ai":
				   setGrid("ai",value);
				   break;
		   }
		   
	   
	   }
	   
	   
	   
	   
	  
   }


   public void setGrid(String role,String id){
	   
	   if(role.equals("user")){
		   //绿色
		   switch(id){
			   
			   case "1":
				   bt1.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "2":
				   bt2.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "3":
				   bt3.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "4":
				   bt4.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "5":
				   bt5.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "6":
				   bt6.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "7":
				   bt7.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "8":
				   bt8.setBackgroundColor(Color.GREEN);
				   break;
				   
			   case "9":
				   bt9.setBackgroundColor(Color.GREEN);
				   break;
		   }
	   }
	   
	   if(role.equals("ai")){
		   //红色
		   
		   switch(id){

			   case "1":
				   bt1.setBackgroundColor(Color.RED);
				   break;

			   case "2":
				   bt2.setBackgroundColor(Color.RED);
				   break;

			   case "3":
				   bt3.setBackgroundColor(Color.RED);
				   break;

			   case "4":
				   bt4.setBackgroundColor(Color.RED);
				   break;

			   case "5":
				   bt5.setBackgroundColor(Color.RED);
				   break;

			   case "6":
				   bt6.setBackgroundColor(Color.RED);
				   break;

			   case "7":
				   bt7.setBackgroundColor(Color.RED);
				   break;

			   case "8":
				   bt8.setBackgroundColor(Color.RED);
				   break;

			   case "9":
				   bt9.setBackgroundColor(Color.RED);
				   break;
		   }
	   }
   }
   
   
	public void initLogic(){

		
		// 将按钮存入数组，并按顺序设置Tag为1-9
		final Button[] buttons = {bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9};
		
		
		
        // 获取已配对的蓝牙设备列表并显示到下拉框中
        pairedDevicesSet = bluetoothAdapter.getBondedDevices();
        if (pairedDevicesSet.size() > 0) {
            for (BluetoothDevice device : pairedDevicesSet) {
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pairedDevicesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(adapter);

        // 监听下拉框选择事件
        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					String selectedDevice = adapterView.getItemAtPosition(i).toString();
					selectedDeviceName = selectedDevice.substring(0, selectedDevice.indexOf("\n"));
					selectedDeviceAddress = selectedDevice.substring(selectedDevice.indexOf("\n") + 1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {

				}
			});

        // 连接按钮点击事件
        connectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// 如果已经连接上了蓝牙设备，再次点击连接按钮将断开连接
					if (isConnected) {
						bluetoothService.disconnect();
						connectButton.setText("Connect");
						isConnected = false;
						receivedData.setText("");
						return;
					}
					// 如果还未连接上蓝牙设备，开始连接
					if (selectedDeviceAddress.length() > 0) {
						bluetoothService = new BluetoothService(handler);
						BluetoothDevice device = bluetoothAdapter.getRemoteDevice(selectedDeviceAddress);
						bluetoothService.connect(device);
					}
				}
			});

        // 发送按钮点击事件
        sendButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String message = sendData.getText().toString();
					if (message.length() > 0) {
						bluetoothService.write(message.getBytes());
						sendData.setText("");
					}
				}
			});

		
		bt_bottom_add.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					degree_bottom ++;
					bottom_num.setText(String.valueOf(degree_bottom));
					bluetoothService.write("@bottom_add$".getBytes());
					sendData.setText("");
					
				}
				
			});
			
		bt_bottom_dis.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					degree_bottom --;
					bottom_num.setText(String.valueOf(degree_bottom));
					bluetoothService.write("@bottom_dis$".getBytes());
					sendData.setText("");

				}

			});
			
			
		bt_top_add.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					degree_top ++;
					top_num.setText(String.valueOf(degree_top));
					bluetoothService.write("@top_add$".getBytes());
					sendData.setText("");

				}

			});
			
			
		bt_top_dis.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					degree_top--;
					top_num.setText(String.valueOf(degree_top));
					bluetoothService.write("@top_dis$".getBytes());
					sendData.setText("");

				}

			});
			
			
		enable_scroll.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					setScrollEnabled(true);

				}

			});
		
			
			
		disable_scroll.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					setScrollEnabled(false);

				}
			});
			
			
		bt_stroke.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//描边
					bluetoothService.write("@mode2$".getBytes());
				}
			});
			
		bt_chess.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//下棋
					bluetoothService.write("@mode3$".getBytes());
					
				}
			});
			
			
		
		for (int i = 0; i < buttons.length; i++) {
			Button btn = buttons[i];
			btn.setTag(i + 1); // 设置Tag对应1-9

			btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 重置所有按钮背景
						for (Button b : buttons) {
							b.setBackgroundResource(android.R.drawable.btn_default);
						}
						// 设置当前按钮背景为蓝色
						v.setBackgroundColor(Color.BLUE);
						// 更新选中格子编号
						selectedgrid = (Integer) v.getTag();
						
					}
					
			});
		}
			
			
		bt_lay.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//放置
					bluetoothService.write(("@" + selectedgrid + "$").getBytes());
				}
			});
			
		bt_clear.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					selectedgrid = 0;
					
					for (int i = 0; i < buttons.length; i++) {
						Button btn = buttons[i];
						btn.setBackgroundResource(android.R.drawable.btn_default);
						}
				}
			});
			
			
			
		bt_initpos.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					bluetoothService.write(("@" + "mode1" + "$").getBytes());
				}
			});
		
			
		bt_straight.setOnClickListener(new OnClickListener(){
				//描边，直走
				@Override
				public void onClick(View v) {
					bluetoothService.write(("@" + "straight" + "$").getBytes());
				}
			});
			
			
			
		bt_drawing.setOnClickListener(new OnClickListener(){
				//描画
				@Override
				public void onClick(View v) {
					String path = drawingView.getToSend(320,240);
					
					bluetoothService.write(("@" + path + "$").getBytes());
					//bluetoothService.write(("@" + "straight" + "$").getBytes());
				}
			});
    }


	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        initLogic();
    }

    // 处理蓝牙连接和数据传输的消息
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            textView1.setText("Bluetooth Device: " + selectedDeviceName);
                            connectButton.setText("Disconnect");
                            isConnected = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            connectButton.setText("Connecting...");
                            break;
                        case BluetoothService.STATE_DISCONNECTED:
                            textView1.setText("Bluetooth Device:");
                            connectButton.setText("Connect");
                            isConnected = false;
                            receivedData.setText("");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
					handleSingle(readMessage);
                    receivedData.append(readMessage);
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    selectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to " + selectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(BluetoothService.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}

