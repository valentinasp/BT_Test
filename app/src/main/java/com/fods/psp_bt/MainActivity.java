package com.fods.psp_bt;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

//import static com.fods.psp_bt.ClassPdfCreater.draw_dec;
import static com.fods.psp_bt.OBEX.ResiveSatus_t.COBEX_PACKED_RESIVED;
import static com.fods.psp_bt.OBEX.ResiveSatus_t.COBEX_PACKED_RESIVE_ERROR;
import static com.fods.psp_bt.OBEX.ResiveSatus_t.COBEX_PACKED_WAITING;
import static com.fods.psp_bt.SOR.sorReadTraceData;
//import static com.fods.psp_bt.SOR.sorCheckCrc;
//import static com.fods.psp_bt.SOR.sorReadTraceData;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static MainActivity instance = null;
    //ClassPdfCreater creator = new ClassPdfCreater();


    //private BluetoothConnectionService mBluetoothConnection = null;
    BluetoothConnectionService mBluetoothConnection = null;
    OBEX mObexConnection = null;

    private static final String TAG = "bluetooth1";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inputStream = null;
    public ProgressBar progressBar;
    ThreadBeConnected myThreadBeConnected;

    BluetoothDevice mBTDevice;

    final String LOG_TAG = "myLogs"; //test

    final String FILENAME = "file";

    final String DIR_SD = "MyFiles";
    final String FILENAME_SD = "fileSD";
    public Button btnSend;
    public Button btnCreatePDF;

    private String myName = "SamsungNote3";
    String filename = "test2.jpg";
    //OBEX.obex_packet inPacket;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-адрес Bluetooth модуля
    private static String address = "00:04:3E:08:C1:11";

    private ConnectedThread mConnectedThread;
    OBEX.ObexResiveThread mObexResiveThread;
    final int PORT = 21;

    public Typeface fontello;


    public static byte[] toBytes(short s) {
        return new byte[]{0, 0, (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF)};
    }

    public static int[] toIntArray(short short_arr[]) {
        int[] int_arr = new int[short_arr.length];
        for(int index=0;index<short_arr.length;index++) {
            byte[] arr = toBytes(short_arr[index]);
            //ByteBuffer buf = ByteBuffer.wrap(arr); // big-endian by default
            ByteBuffer buf = ByteBuffer.wrap(arr);
            int_arr[index] = buf.getInt();
        }
        return int_arr;
    }

    public static int toInt(short short_arr) {
        byte[] arr = toBytes(short_arr);
        //ByteBuffer buf = ByteBuffer.wrap(arr); // big-endian by default
        ByteBuffer buf = ByteBuffer.wrap(arr);
        return buf.getInt();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //fontello = Typeface.createFromAsset(getAssets(), "Fonts/fontello.ttf");
        // Enable Android-style asset loading (highly recommended)
        PDFBoxResourceLoader.init(getApplicationContext());
/*
        short[] short_arr = new short[] {(short)65535,(short)65280,(short)255,(short)15,(short)240,(short)61440};
        int[] int_arr = toIntArray(short_arr);
       int Scale = 7000;
        int Pcnt = 1250;
       int finc = getIncFp(Scale,Pcnt);
        int sinc = getIncSp(finc,Scale,Pcnt);

        System.out.println("FirstInc:"+ String.valueOf(finc));
        System.out.println("SecInc:"+ String.valueOf(sinc));

        float Coefficient = getCoefficient(7000,1250);

        draw_inc(Coefficient,7000,1250);

        for(int index=0;index<int_arr.length;index++) {
            System.out.println("IntArray["+index+"]:"+ String.valueOf(int_arr[index]));
        }
*/
        final EditText pass = findViewById(R.id.editUserPsw);
        final EditText name = findViewById(R.id.editUserName);
        final EditText hostdir = findViewById(R.id.editHostDir);
        final EditText host = findViewById(R.id.editHost);

        loadFTPparams();

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putString("user",name.getText().toString());//name.getText().toString());
                editor.putString("pass",pass.getText().toString());
                editor.putString("host",host.getText().toString());
                editor.putString("hostdir",hostdir.getText().toString());
                editor.apply();

                Toast.makeText(MainActivity.getInstance(), "new value saved", Toast.LENGTH_SHORT).show();
            }
        });


        //draw_dec((float) (100000.0/1000.0), (float) 1000.0,  100000.0);
/*
        final File file = new File("/sdcard/END200.SOR");
        try {
            int data[] = new int[300000];
            //SOR.EvEventsS Events = new SOR.EvEventsS();
            sorReadTraceData(file, data, 0);

            Toast.makeText(MainActivity.getInstance(), ".SOR read complite", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */


/*
        new Thread(new Runnable(){
            @Override
            public void run() {
                // Do network action in this function
                try {
                    //FTPUploader ftp = new FTPUploader("ftp2.lifodas.com","fod","LifodaS41");
                    FTPUploader ftp = new FTPUploader("ftp2.lifodas.com","afl","s4cb5C");
                    //ftp.uploadFile("/sdcard/test2.jpg","test2.jpg","up/FOD_upload/FS200/");
                    //ftp.uploadFile(file.getAbsolutePath(),file.getName(),"/FOD upload/FS200/");
                    ftp.uploadFile(file.getAbsolutePath(),file.getName(),"/AFL upload/FS200/");
                    //FTPUploader ftp = new FTPUploader("192.168.1.1","user","user");
                    //ftp.uploadFile(file.getAbsolutePath(),file.getName(),"/up/FOD_upload/fs200/");
                    ftp.disconnect();
                } catch (Exception ex) {
                    System.err.println(ex);
                }

            }
        }).start();

*/

        btnSend = (Button) findViewById(R.id.button2);
        btnSend.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   btnSend.setEnabled(false);
                   mBluetoothConnection = new BluetoothConnectionService(MainActivity.this, MY_UUID);
                   mObexConnection = new OBEX();
                   mBluetoothConnection.setIncominingObexPacked(mObexConnection.newPacket());
                   mBTDevice = btAdapter.getRemoteDevice(address);
                   startConnection();
                   while (mBluetoothConnection.isDeviceConnected() == false);
                       try {
                           File file = new File("/sdcard/test2.jpg");
                           //File file = new File("/sdcard/END100-After-C001_003.JPG");
                           mObexConnection.obex_send_file(file);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
           }
        );

        btnCreatePDF = (Button) findViewById(R.id.create_pdf_button);
        btnCreatePDF.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       DoSavePdfFile();
                   }
               }
        );


    }


    public void loadFTPparams(){
        EditText editPass = (EditText)findViewById(R.id.editUserPsw);
        EditText editName = (EditText)findViewById(R.id.editUserName);
        EditText editHostdir = (EditText)findViewById(R.id.editHostDir);
        EditText editHost = (EditText)findViewById(R.id.editHost);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String host = prefs.getString("host", null);
        String hostDir = prefs.getString("hostdir", null);
        String user = prefs.getString("user", null);
        String psw = prefs.getString("pass", null);

        if((psw != null) && (user != null) && (hostDir != null) && (host != null)){
            editPass.setText(psw);
            editName.setText(user);
            editHostdir.setText(hostDir);
            editHost.setText(host);
        }else{
            editPass.setText("s4cb5C");
            editName.setText("afl");
            editHostdir.setText("/AFL upload/FS200/");
            editHost.setText("ftp2.lifodas.com");
        }
    }


    //create method for starting connection
    //***remember the conncction will fail and app will crash if you haven't paired first
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public BluetoothConnectionService getBtConService(){
        return mBluetoothConnection;
    }

    public static MainActivity getInstance(){
        return instance;
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



    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void bluetoothadd(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth

            Log.d("Bluetooth ","not found");
        }

        if (mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {

                    Log.d("Device name:",device.getName());
                    Log.d("Mac Addressess", String.valueOf(mBluetoothAdapter.getRemoteDevice(device.getAddress())));
                }
            }
        }

    }




    void writeFileSD() {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // создаем каталог
        sdPath.mkdirs();
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
        // открываем поток для записи
        BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
        // пишем данные
        bw.write("Содержимое файла на SD");
        // закрываем поток
        bw.close();
        Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    void readFileSD() {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)){
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath. getAbsolutePath() + "/" + DIR_SD);
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try{
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
            Log.d(LOG_TAG, str);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(hasPermissions()){
            Toast.makeText(MainActivity.getInstance(), "app has permissions", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.getInstance(), "app doesn't have permissions", Toast.LENGTH_SHORT).show();
            requestPermissionWithRationale();
        }

        //Turn ON BlueTooth if it is OFF
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        //textStatus.setText("setup()");
        myThreadBeConnected = new ThreadBeConnected();
        myThreadBeConnected.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadBeConnected!=null){
            myThreadBeConnected.cancel();
        }
    }



    private class ThreadBeConnected extends Thread {

        private BluetoothServerSocket bluetoothServerSocket = null;

        public ThreadBeConnected() {
            try {
                bluetoothServerSocket =
                        btAdapter.listenUsingRfcommWithServiceRecord(myName, MY_UUID);
/*
                textStatus.setText("Waiting\n"
                        + "bluetoothServerSocket :\n"
                        + bluetoothServerSocket);
                        */
                Toast.makeText(MainActivity.getInstance(), "Waiting\n"
                        + "bluetoothServerSocket :\n"
                        + bluetoothServerSocket, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket bluetoothSocket = null;

            if(bluetoothServerSocket!=null){
                try {
                    while (true) {
                        bluetoothSocket = bluetoothServerSocket.accept();

                        BluetoothDevice remoteDevice = bluetoothSocket.getRemoteDevice();

                        final String strConnected = "Connected:\n" +
                                remoteDevice.getName() + "\n" +
                                remoteDevice.getAddress();

                        //connected
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //textStatus.setText(strConnected);
                                Toast.makeText(MainActivity.getInstance(), strConnected, Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (bluetoothSocket != null) {
                            connected(bluetoothSocket);
                        }
                        Log.d(TAG, "connected: Closed");
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String eMessage = e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            //textStatus.setText("something wrong: \n" + eMessage);
                            Toast.makeText(MainActivity.getInstance(), "something wrong: \n" + eMessage, Toast.LENGTH_SHORT).show();
                        }});
                }
            }else{
                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        //textStatus.setText("bluetoothServerSocket == null");
                        Toast.makeText(MainActivity.getInstance(), "bluetoothServerSocket == null", Toast.LENGTH_SHORT).show();
                    }});
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
                Log.d(TAG, "close bluetoothServerSocket");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");
        //inPacket = new OBEX.obex_packet();
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        ResiveImage();
    }

    private void ResiveImage() {
        Log.d(TAG, "Resiver: Starting.");
        mObexResiveThread = new OBEX.ObexResiveThread();
        mObexResiveThread.start();
        this.setReadyToRecive();
    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int packetLength;
        private int resivedBytes;
        private int leftBytes;
        private OBEX.ResiveSatus_t packetResived;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            packetLength = 0;
            resivedBytes = 0;
            leftBytes = 0;
            packetResived = COBEX_PACKED_RESIVED;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {

                    if(packetResived == COBEX_PACKED_WAITING) {
                        if (packetLength == 0 && mmInStream.available() >= 3) {
                            mmInStream.read(OBEX.getInstance().inPacket.buffer, 0, 3);
                            resivedBytes = 3;
                            byte[] arr = new byte[] {OBEX.getInstance().inPacket.buffer[1],OBEX.getInstance().inPacket.buffer[2]};
                            ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
                            packetLength = wrapped.getShort();
                            if(packetLength > OBEX.getInstance().inPacket.max){
                                packetResived = COBEX_PACKED_RESIVE_ERROR;
                                continue;
                            }
                            leftBytes = packetLength - 3;
                            OBEX.getInstance().inPacket.l = packetLength;
                            if (packetLength == 3) {
                                packetLength = 0;
                                packetResived = COBEX_PACKED_RESIVED;
                                resivedBytes = 0;
                                leftBytes = 0;
                            }
                        } else if (packetLength != 0 && packetResived == COBEX_PACKED_WAITING) {
                            int availableBytes = mmInStream.available();
                            if (availableBytes > 0) {
                                if (availableBytes >= leftBytes) {
                                    //Log.d(TAG, "availableBytes: " + availableBytes);
                                    //Log.d(TAG, "resivedBytes: " + resivedBytes);
                                    //Log.d(TAG, "leftBytes: " + leftBytes);
                                    int bytes_read = mmInStream.read(OBEX.getInstance().inPacket.buffer, resivedBytes, leftBytes);
                                    leftBytes -= bytes_read;
                                    //Log.d(TAG, "leftBytes2: " + leftBytes);
                                    resivedBytes += bytes_read;
                                    if (leftBytes == 0) {
                                        packetResived = COBEX_PACKED_RESIVED;
                                        resivedBytes = 0;
                                        packetLength = 0;
                                        //Log.d(TAG, "InputStream: " + byteArrayToHex(OBEX.getInstance().inPacket.buffer, OBEX.getInstance().inPacket.l));
                                    }
                                } else {//availableBytes < leftBytes
                                    int bytes_read = mmInStream.read(OBEX.getInstance().inPacket.buffer, resivedBytes, availableBytes);
                                    leftBytes -= bytes_read;
                                    //Log.d(TAG, "availableBytes: " + availableBytes);
                                    //Log.d(TAG, "leftBytes3: " + leftBytes);
                                    resivedBytes += bytes_read;
                                    if (leftBytes == 0) {
                                        packetResived = COBEX_PACKED_RESIVED;
                                        resivedBytes = 0;
                                        packetLength = 0;
                                        //Log.d(TAG, "InputStream: " + byteArrayToHex(OBEX.getInstance().inPacket.buffer, OBEX.getInstance().inPacket.l));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "read: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
                //Log.d(TAG, "_ConnectedThread_RUN" );//i
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes, int len) {
            //String text = new String(bytes, Charset.defaultCharset());
            //Log.d(TAG, "write: Writing to outputstream: " + byteArrayToHex(bytes,len) );//text);
            try {
                mmOutStream.write(bytes,0,len);
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        public void setReady(){
            packetResived = COBEX_PACKED_WAITING;
            OBEX.getInstance().inPacket.l = 0;
        }

        public OBEX.ResiveSatus_t getPacketStatus() {
            return packetResived;
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                //while (mmSocket.isConnected());
                mmSocket.close();
                Log.d(TAG, "_ConnectedThread_STOP" );
            } catch (IOException e) { }
        }
        public boolean isConnected() {
            if(mmOutStream == null)
                return false;
            return true;
        }
    }

    public void resetObexConnection() {
        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(mObexResiveThread != null) {
            //mObexResiveThread.cancel();
            //mObexResiveThread = null;
        }

    }

    public void setReadyToRecive() {
        mConnectedThread.setReady();
    }

    public OBEX.ResiveSatus_t getOBEXPacketStatus() {
        if(mConnectedThread == null){
            return COBEX_PACKED_RESIVE_ERROR;
        }
        return mConnectedThread.getPacketStatus();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread# write(byte[])
     */
    public void write(byte[] out,int len) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        //Log.d(TAG, "Sending data: Write Called.");
        //Log.d(TAG, "Sending data: " + out + "...");
        //perform the write
        mConnectedThread.write(out,len);
        //mConnectedThread.wait(10);
    }


    private class UploadFile extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            FTPClient client = new FTPClient();
            try {
                client.connect(params[1], PORT);
                client.login(params[2], params[3]);
                client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
                return client.storeFile(filename, new FileInputStream(new File(
                        params[0])));

            } catch (Exception e) {
                Log.d("FTP", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean sucess) {
            if (sucess)
                Toast.makeText(instance, "File Sent", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(instance, "Error", Toast.LENGTH_LONG).show();
        }

    }

    public class FileUpload {

        /**
         * Upload a file to a FTP server. A FTP URL is generated with the
         * following syntax:
         * ftp://user:password@host:port/filePath;type=i.
         *
         * @param ftpServer , FTP server address (optional port ':portNumber').
         * @param user      , Optional user name to login.
         * @param password  , Optional password for user.
         * @param fileName  , Destination file name on FTP server (with optional
         *                  preceding relative path, e.g. "myDir/myFile.txt").
         * @param source    , Source file to upload.
         * @throws MalformedURLException, IOException on error.
         */
        public void upload(String ftpServer, String user, String password,
                           String fileName, File source) throws MalformedURLException,
                IOException {
            if (ftpServer != null || fileName != null || source != null)
            {
                StringBuffer sb = new StringBuffer("ftp://");
                // check for authentication else assume its anonymous access.
                if ( user != null || password != null)
                {
                    sb.append(user);
                    sb.append(':');
                    sb.append(password);
                    sb.append('@');
                }
                sb.append(ftpServer);
                sb.append('/');
                sb.append(fileName);
         /*
          * type ==&gt; a=ASCII mode, i=image (binary) mode, d= file directory
          * listing
          */
                sb.append(";type=i");

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                try {
                    URL url = new URL(sb.toString());
                    URLConnection urlc = url.openConnection();

                    bos = new BufferedOutputStream(urlc.getOutputStream());
                    bis = new BufferedInputStream(new FileInputStream(source));

                    int i;
                    // read byte by byte until end of stream
                    while ((i = bis.read()) != -1) {
                        bos.write(i);
                    }
                } finally {
                    if (bis != null)
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    if (bos != null)
                        try {
                            bos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                }
            }
      else
            {
                System.out.println("Input not available.");
            }
        }
    }

    public static class FTPUploader {

        FTPClient ftp = null;

        public FTPUploader(String host, String user, String pwd) throws Exception {
            ftp = new FTPClient();
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
            int reply;

            //ftp.connect( host);
            InetAddress ia = InetAddress.getByName(host);
            System.out.println("host:" + ia.getHostName());
            ftp.connect(ia);

            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new Exception("Exception in connecting to FTP Server");
            }
            ftp.login(user, pwd);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
        }

        public boolean uploadFile(String localFileFullName, String fileName, String hostDir)
                throws Exception {
            try (InputStream input = new FileInputStream(new File(localFileFullName))) {
                this.ftp.changeWorkingDirectory(hostDir);
                return this.ftp.storeFile(hostDir + fileName, input);
            }
        }

        public void disconnect() {
            if (this.ftp.isConnected()) {
                try {
                    this.ftp.logout();
                    this.ftp.disconnect();
                } catch (IOException f) {
                    // do nothing as file is already saved to server
                }
            }
        }
    }


    File DoSavePdfFile (){
        String fileNamePdf = "/sdcard/test.pdf";
        File testPdf = new File(fileNamePdf);
        if (testPdf.exists()) testPdf.delete();

        ClassPdfCreater creator = new ClassPdfCreater();
        creator.createPdf(fileNamePdf);
        return new File(fileNamePdf);
    }


    @SuppressLint("WrongConstant")
    private boolean hasPermissions(){
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        for (String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    public void requestPermissionWithRationale() {
        /*
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final String message = "Storage permission is needed to show files count";
            Snackbar.make(MainActivity.this.findViewById(R.id.include), message, Snackbar.LENGTH_LONG)
                    .setAction("GRANT", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPerms();
                        }
                    })
                    .show();
        } else {

        }
        */
        requestPerms();
    }

    private void requestPerms(){
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions,PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode){
            case PERMISSION_REQUEST_CODE:

                for (int res : grantResults){
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed){
            //user granted all permissions we can perform our task.
            //makeFolder();
        }
        else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Storage Permissions denied.", Toast.LENGTH_SHORT).show();
                    killApp(false);
                } else {
                    showNoStoragePermissionSnackbar();
                }
            }
        }

    }

    public void showNoStoragePermissionSnackbar() {
        Snackbar.make(MainActivity.this.findViewById(R.id.include), "Storage permission isn't granted" , Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings();

                        Toast.makeText(getApplicationContext(),
                                "Open Permissions and grant the Storage permission",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }
    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, PERMISSION_REQUEST_CODE);
    }


    /**
     * Kill the app either safely or quickly. The app is killed safely by
     * killing the virtual machine that the app runs in after finalizing all
     * {@link Object}s created by the app. The app is killed quickly by abruptly
     * killing the process that the virtual machine that runs the app runs in
     * without finalizing all {@link Object}s created by the app. Whether the
     * app is killed safely or quickly the app will be completely created as a
     * new app in a new virtual machine running in a new process if the user
     * starts the app again.
     *
     * <P>
     * <B>NOTE:</B> The app will not be killed until all of its threads have
     * closed if it is killed safely.
     * </P>
     *
     * <P>
     * <B>NOTE:</B> All threads running under the process will be abruptly
     * killed when the app is killed quickly. This can lead to various issues
     * related to threading. For example, if one of those threads was making
     * multiple related changes to the database, then it may have committed some
     * of those changes but not all of those changes when it was abruptly
     * killed.
     * </P>
     *
     * @param killSafely
     *            Primitive boolean which indicates whether the app should be
     *            killed safely or quickly. If true then the app will be killed
     *            safely. Otherwise it will be killed quickly.
     */
    public static void killApp(boolean killSafely) {
        if (killSafely) {
            /*
             * Notify the system to finalize and collect all objects of the app
             * on exit so that the virtual machine running the app can be killed
             * by the system without causing issues. NOTE: If this is set to
             * true then the virtual machine will not be killed until all of its
             * threads have closed.
             */
            System.runFinalizersOnExit(true);

            /*
             * Force the system to close the app down completely instead of
             * retaining it in the background. The virtual machine that runs the
             * app will be killed. The app will be completely created as a new
             * app in a new virtual machine running in a new process if the user
             * starts the app again.
             */
            System.exit(0);
        } else {
            /*
             * Alternatively the process that runs the virtual machine could be
             * abruptly killed. This is the quickest way to remove the app from
             * the device but it could cause problems since resources will not
             * be finalized first. For example, all threads running under the
             * process will be abruptly killed when the process is abruptly
             * killed. If one of those threads was making multiple related
             * changes to the database, then it may have committed some of those
             * changes but not all of those changes when it was abruptly killed.
             */
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    private ProgressDialog pDialog = null;

    private class SorSendThread extends Thread {
        final File sorfile;
        final File genPdf;
        String fileNamePdf;
        private boolean _canceled = false;

        public SorSendThread(String sor_pathname) {//"/sdcard/END200.SOR"
            sorfile = new File(sor_pathname);
            String fileNameWithOutExt = sorfile.getName().replaceFirst("[.][^.]+$", "");
            fileNamePdf = "/sdcard/" + fileNameWithOutExt + ".pdf";
            genPdf = new File(fileNamePdf);

            if(pDialog == null) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    public void run() {
                        pDialog = ProgressDialog.show(MainActivity.this, "Generating Report", "Please wait...", true);
                    }
                });
            }
            _canceled = false;
        }

        public void run(){

            MainActivity.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.getInstance(), "Start generate report", Toast.LENGTH_LONG).show();
                }
            });

            try {
                //int data[] = new int[300000];
                sorReadTraceData(sorfile);
            } catch (IOException e) {
                e.printStackTrace();
                cancel();
            }

            if (genPdf.exists()) genPdf.delete();

            if(_canceled == false){

                ClassPdfCreater creator = new ClassPdfCreater();
                creator.createPdf(fileNamePdf);

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        // Do network action in this function
                        try {
                            SharedPreferences prefs = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
                            String host = prefs.getString("host", null);
                            String hostDir = prefs.getString("hostdir", null);
                            String user = prefs.getString("user", null);
                            String psw = prefs.getString("pass", null);

                            if((psw != null) && (user != null) && (hostDir != null) && (host != null)){
                                MainActivity.FTPUploader ftp = new MainActivity.FTPUploader(host, user, psw);
                                if(ftp.uploadFile(genPdf.getAbsolutePath(), genPdf.getName(), hostDir))
                                {
                                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.getInstance(), "Report send: complete", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else{
                                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.getInstance(), "Report send: error", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }else{
                                /*
                                MainActivity.getInstance().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.getInstance(), "Send to default host", Toast.LENGTH_LONG).show();
                                    }
                                });
                                */
                                MainActivity.FTPUploader ftp = new MainActivity.FTPUploader("ftp2.lifodas.com", "afl", "s4cb5C");
                                if(ftp.uploadFile(genPdf.getAbsolutePath(), genPdf.getName(), "/AFL upload/FS200/")){
                                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.getInstance(), "Report send comlite", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else{
                                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.getInstance(), "Report send error", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        } catch (final Exception ex) {
                            System.err.println(ex);
                        }
                        cancel();
                    }
                }).start();
            }else{
                _canceled = false;
            }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            if(pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }
            _canceled = true;
        }
    }

    private SorSendThread mSorSendThread = null;

    public void sor_send_file(String sor_name) throws IOException {
        if (mSorSendThread != null) {
            //mObexSendThread.destroy();
            mSorSendThread = null;
        }
        mSorSendThread = new SorSendThread(sor_name);
        mSorSendThread.start();
    }

}


