package com.michael.mt_r_android;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    CustomViewCanvas customViewCanvas;
    private TextView textReceive = null;
    private EditText textSend = null;
    private Button btnConnect = null;
    private Button btnSend = null;
    private Button btnDraw = null;
    private Button btnUp = null;
    private Button btnDown = null;
    private Button btnLeft = null;
    private Button btnRight = null;
    private Button btnStop = null;
    private static final String ServerIP = "103.44.145.243";//"172.26.213.6";//"103.44.145.243";花生壳
    private static final int ServerPort = 25044;//3247;//25044;花生壳
    private Socket socket = null;
    private String strMessage;
    private boolean isConnect = false;
    private OutputStream outStream;
    private Handler myHandler = null;
    private ReceiveThread receiveThread = null;
    private boolean isReceive = false;
    protected int[] recBuffer =new int[362];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customViewCanvas = (CustomViewCanvas) findViewById(R.id.customViewC);

//        textReceive = (TextView)findViewById(R.id.textViewReceive);
        textSend = (EditText)findViewById(R.id.editTextSend);

        btnConnect = (Button)findViewById(R.id.buttonConnect);
        btnSend = (Button)findViewById(R.id.buttonSend);
        btnDraw = (Button)findViewById(R.id.buttonDraw);
        btnUp = (Button)findViewById(R.id.buttonUp);
        btnDown = (Button)findViewById(R.id.buttonDown);
        btnLeft = (Button)findViewById(R.id.buttonLeft);
        btnRight = (Button)findViewById(R.id.buttonRight);
        btnStop = (Button)findViewById(R.id.buttonStop);

        //buttonUp
        btnUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = "MT-R move forward";
                new Thread(sendThread).start();

            }
        });
        //buttonDown
        btnDown.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = "MT-R move back";
                new Thread(sendThread).start();

            }
        });
        //buttonLeft
        btnLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = "MT-R move left";
                new Thread(sendThread).start();

            }
        });
        //buttonRight
        btnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = "MT-R move right";
                new Thread(sendThread).start();

            }
        });
        //buttonStop
        btnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = "MT-R move stop";
                new Thread(sendThread).start();

            }
        });
        //连接按钮的监听器
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isConnect){
                    new Thread(connectThread).start();
                }

            }
        });

        //发送按钮的监听器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                strMessage = textSend.getText().toString();
                new Thread(sendThread).start();
            }
        });

        //DrawButton
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                for (int i=0;i<181;i++){
                    recBuffer[2*i]=0;
                    recBuffer[2*i+1]=i*100;
                }
                recBuffer[0]=-4000;
                recBuffer[1]=0;
                recBuffer[100]=-100;
                recBuffer[101]=2000;
                customViewCanvas.drawLaserMap(recBuffer);
            }
        });

        myHandler =new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what) {
                    case 0:
                                customViewCanvas.drawLaserMap(recBuffer);
                        break;
                    case 1:
                        break;
                }
            }
        };
    }

    //连接到服务器的接口
    Runnable connectThread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                //初始化Scoket，连接到服务器
                socket = new Socket(ServerIP, ServerPort);
                isConnect = true;
                //启动接收线程
                isReceive = true;
                receiveThread = new ReceiveThread(socket);
                receiveThread.start();
                System.out.println("----connected success----");
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("UnknownHostException-->" + e.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("IOException" + e.toString());
            }
        }
    };

    //发送消息的接口
    Runnable sendThread = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            byte[] sendBuffer = null;
            try {
                sendBuffer = strMessage.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                outStream = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                outStream.write(sendBuffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    //接收线程
    private class ReceiveThread extends Thread{
        private InputStream inStream = null;

        private byte[] buffer;

        ReceiveThread(Socket socket){
            try {
                inStream = socket.getInputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        @Override
        public void run(){
            while(isReceive){
                buffer = new byte[1448];
                try {
                    inStream.read(buffer);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for(int j=0;j<362;j++){
                    recBuffer[j] = (buffer[4*j+0] & 0xff) | ((buffer[4*j+1] << 8) & 0xff00)
                            | ((buffer[4*j+2] << 24) >>> 8) | (buffer[4*j+3] << 24);
                }
//                Message msg = new Message();
////                msg.obj = str;
//                myHandler.sendMessage(msg);
//                try {
//                    str = new String(recBuffer,"UTF-8").trim();
//
//                } catch (UnsupportedEncodingException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                Message msg = new Message();
                msg.what = 0;
                myHandler.sendMessage(msg);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if(receiveThread != null){
            isReceive = false;
            receiveThread.interrupt();
        }
    }

}
