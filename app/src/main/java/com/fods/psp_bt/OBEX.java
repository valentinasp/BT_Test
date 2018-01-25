package com.fods.psp_bt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Arrays;

import static android.content.ContentValues.TAG;
import static com.fods.psp_bt.BluetoothConnectionService.byteArrayToHex;
import static com.fods.psp_bt.OBEX.BTCobexState_t.Cobex_Disconnect;
import static com.fods.psp_bt.OBEX.BTCobexState_t.Cobex_Receive_Packet;
import static com.fods.psp_bt.OBEX.BTCobexState_t.Cobex_ResivingFile;
import static com.fods.psp_bt.OBEX.BTCobexState_t.Cobex_WaitingConnect;
import static com.fods.psp_bt.OBEX.Constants.COBEX_PKT_SIZE;
import static com.fods.psp_bt.OBEX.Constants.OBEX_FINAL_BIT;
import static com.fods.psp_bt.OBEX.Constants.OBEX_OPCODE_CONNECT;
import static com.fods.psp_bt.OBEX.Constants.OBEX_OPCODE_DISCONNECT;
import static com.fods.psp_bt.OBEX.Constants.OBEX_OPCODE_PUT;
import static com.fods.psp_bt.OBEX.Constants.OBEX_RESPONSE_NOT_ACCEPTABLE;
import static com.fods.psp_bt.OBEX.ObexState_t.COBEX_STATE_CONNECTED;
import static com.fods.psp_bt.OBEX.ObexState_t.COBEX_STATE_NOT_CONNECTED;
import static com.fods.psp_bt.OBEX.ResiveSatus_t.COBEX_PACKED_WAITING;
import static com.fods.psp_bt.OBEX.SbObexError_T.SBOBEXERROR_FILENAME;
import static com.fods.psp_bt.OBEX.SbObexError_T.SBOBEXERROR_OK;
import static com.fods.psp_bt.OBEX.preview_mode_t.PREVIEW_GIF;
import static com.fods.psp_bt.OBEX.preview_mode_t.PREVIEW_JPG;
import static com.fods.psp_bt.OBEX.preview_mode_t.PREVIEW_OFF;
import static com.fods.psp_bt.SOR.sorReadTraceData;

/**
 * Created by Valentinas on 2017-10-31.
 */
public class OBEX {

    private static boolean sendingFile = false;

    enum cobex_send_state_t {
        codex_send_SendConnect,
        cobex_send_ResivePacket,
        cobex_send_ResivedConnnectHeader,
        cobex_send_HiPacket,
        cobex_ResivedAcknowledgement,
        cobex_send_FileData,
        cobex_send_EndOfTransferFile,
        cobex_ResivedEndOfTransferAck,
        cobex_send_Disconnect,
        cobex_ResivedDisconnectAck,
        cobex_Error,
        cobex_Disconnected,
        cobex_ResivedConnnectAck
    }

    enum preview_mode_t{
        PREVIEW_OFF,
        PREVIEW_JPG,
        PREVIEW_GIF
    }

    enum SbObexError_T
    {
        SBOBEXERROR_OK,
        SBOBEXERROR_INIT,
        SBOBEXERROR_FILENAME,
        SBOBEXERROR_OVERWRITE,
        SBOBEXERROR_FILESIZE,
        SBOBEXERROR_PACKETSEND,
        SBOBEXERROR_INIT_ACK,
        SBOBEXERROR_FILE_CREATE,
        SBOBEXERROR_SET_PATH,
        SBOBEXERROR_SEND_PUT_START,
        SBOBEXERROR_PUT_START_ACK,
        SBOBEXERROR_SEND_DATA,
        SBOBEXERROR_SEND_DATA_ACK,
        SBOBEXERROR_END_ACK,
        SBOBEXERROR_DISCONNECT_ACK,
        SBOBEXERROR_CANCEL_ACK,
        SBOBEXERROR_SEND_END
    }
    private static OBEX instace = null;
    private boolean _canceled = false;
    private boolean _done = false;
    cobex_send_state_t CobexSendState = cobex_send_state_t.cobex_send_ResivedConnnectHeader;
    long nBytesLeftToSend = 0;


    public class Constants {
        public static final byte MAX_SECONDS = (byte) 25;
        public static final byte OBEX_RESPONSE_CONTINUE = (byte) 0x10;

        public static final byte OBEX_RESPONSE_OK = (byte) 0x20;
        public static final byte OBEX_RESPONSE_CREATED = (byte) 0x21;
        public static final byte OBEX_RESPONSE_ACCEPTED = (byte) 0x22;
        public static final byte OBEX_RESPONSE_NONAUTH_INF = (byte) 0x23;
        public static final byte OBEX_RESPONSE_NO_CONTENT = (byte) 0x24;
        public static final byte OBEX_RESPONSE_RESET_CONTENT = (byte) 0x25;
        public static final byte OBEX_RESPONSE_PARTIAL_CONTENT = (byte) 0x26;

        public static final byte OBEX_RESPONSE_MULTIPLE_CHOICES = (byte) 0x30;
        public static final byte OBEX_RESPONSE_MOVED_PERMANENTLY = (byte) 0x31;
        public static final byte OBEX_RESPONSE_MOVED_TEMPORARILY = (byte) 0x32;
        public static final byte OBEX_RESPONSE_SEE_OTHER = (byte) 0x33;
        public static final byte OBEX_RESPONSE_NOT_MODIFIED = (byte) 0x34;
        public static final byte OBEX_RESPONSE_USE_PROXY = (byte) 0x35;

        public static final byte OBEX_RESPONSE_BAD_REQUEST = (byte) 0x40;
        public static final byte OBEX_RESPONSE_UNAUTHORIZED = (byte) 0x41;
        public static final byte OBEX_RESPONSE_PAYMENT_REQUIRED = (byte) 0x42;
        public static final byte OBEX_RESPONSE_FORBIDDEN = (byte) 0x43;
        public static final byte OBEX_RESPONSE_NOT_FOUND = (byte) 0x44;
        public static final byte OBEX_RESPONSE_METHOD_NOT_ALLOWED = (byte) 0x45;
        public static final byte OBEX_RESPONSE_NOT_ACCEPTABLE = (byte) 0x46;
        public static final byte OBEX_RESPONSE_PROXY_AUTH_REQ = (byte) 0x47;
        public static final byte OBEX_RESPONSE_REQUEST_TIMED_OUT = (byte) 0x48;
        public static final byte OBEX_RESPONSE_CONFLICT = (byte) 0x49;
        public static final byte OBEX_RESPONSE_GONE = (byte) 0x4a;
        public static final byte OBEX_RESPONSE_LENGTH_REQUIRED = (byte) 0x4b;
        public static final byte OBEX_RESPONSE_PRECONDITION_DAILED = (byte) 0x4c;
        public static final byte OBEX_RESPONSE_REQ_ENT_TOO_LARGE = (byte) 0x4d;
        public static final byte OBEX_RESPONSE_REQ_URL_TOO_LARGE = (byte) 0x4e;
        public static final byte OBEX_RESPONSE_UNSUPPORTED_MEDIA_TYPE = (byte) 0x4f;

        public static final byte OBEX_RESPONSE_INTERNAL_SERVER_ERROR = (byte) 0x50;
        public static final byte OBEX_RESPONSE_NOT_IMPLEMENTED = (byte) 0x51;
        public static final byte OBEX_RESPONSE_BAD_GATEWAY = (byte) 0x52;
        public static final byte OBEX_RESPONSE_SERVICE_UNAVAILABLE = (byte) 0x53;
        public static final byte OBEX_RESPONSE_GATEWAY_TIMEOUT = (byte) 0x54;
        public static final byte OBEX_RESPONSE_HTTP_VERSION_NOT_SUPPORTED = (byte) 0x55;

        public static final byte OBEX_RESPONSE_DATABASE_FULL = (byte) 0x60;
        public static final byte OBEX_RESPONSE_DATABASE_LOCKED = (byte) 0x61;
        public static final byte OBEX_RESPONSE_COONECT_OK = (byte) 0xA0;

        public static final byte OBEX_FINAL_BIT = (byte) 0x80;

        public static final byte OBEX_OPCODE_CONNECT = (byte) 0x80;
        public static final byte OBEX_OPCODE_DISCONNECT = (byte) 0x81;
        public static final byte OBEX_OPCODE_PUT = (byte) 0x02;
        public static final byte OBEX_OPCODE_GET = (byte) 0x03;
        public static final byte OBEX_OPCODE_SETPATH = (byte) 0x85;
        public static final byte OBEX_OPCODE_SESSION = (byte) 0x87;
        public static final byte OBEX_OPCODE_ABORT = (byte) 0xff;

        public static final byte OBEX_HI_COUNT = (byte) 0xc0;
        public static final byte OBEX_HI_NAME = (byte) 0x01;
        public static final byte OBEX_HI_TYPE = (byte) 0x42;
        public static final byte OBEX_HI_LENGTH = (byte) 0xC3;
        public static final int OBEX_HI_TIME_ISO8601 = (byte) 0x44;
        public static final int OBEX_HI_TIME_COMP = (byte) 0xc4;
        public static final int OBEX_HI_DESCRIPTION = (byte) 0x05;
        public static final int OBEX_HI_TARGET = (byte) 0x46;
        public static final int OBEX_HI_HTTP = (byte) 0x47;
        public static final byte OBEX_HI_BODY = (byte) 0x48;
        public static final byte OBEX_HI_END_OF_BODY = (byte) 0x49;
        public static final byte OBEX_HI_WHO = (byte) 0x4a;
        public static final byte OBEX_HI_CONNECTION_ID = (byte) 0xcb;
        public static final byte OBEX_HI_APP_PARAMETERS = (byte) 0x4c;
        public static final byte OBEX_HI_AUTH_CHALLANGE = (byte) 0x4d;
        public static final byte OBEX_HI_AUTH_RESPONSE = (byte) 0x4e;
        public static final byte OBEX_HI_CREATOR_ID = (byte) 0xcf;
        public static final byte OBEX_HI_WAN_UUID = (byte) 0x50;
        public static final byte OBEX_HI_OBJECT_CLASS = (byte) 0x51;
        public static final byte OBEX_HI_SESSION_PARAMS = (byte) 0x52;
        public static final byte OBEX_HI_SESSION_SEQ_NUM = (byte) 0x93;

        public static final int OBEX_VERSION = (byte) 0x10;

        //#define COBEX_FILE_DATA_BUF_N   (4*1024)
        //#define COBEX_FILE_DATA_BUF_N   (2048)
        public static final int COBEX_FILE_DATA_BUF_N = 30720;
        public static final int COBEX_PKT_SIZE = (COBEX_FILE_DATA_BUF_N + 96);
        public static final int OBEX_DEFAULT_TIMEOUT = 5000;
    }

    obex_packet outPacket = new obex_packet();
    obex_packet inPacket = new obex_packet();

    public OBEX() {

    }

    public static OBEX getInstance() {
        if (instace == null)
            instace = new OBEX();
        return instace;
    }

    public obex_packet newPacket() {
        return new obex_packet();
    }

    public static class obex_packet {
        public int max = 0;
        public int l = 0;
        public byte[] buffer = new byte[COBEX_PKT_SIZE];

        public obex_packet() {

        }

        public int getL() {
            return l;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public void setL(int l) {
            this.l = l;
        }

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

    }

    public void obex_opcode_connect() {
        outPacket.l = 0;
        outPacket.buffer[outPacket.l++] = OBEX_OPCODE_CONNECT;
        outPacket.buffer[outPacket.l++] = 0x00;
        outPacket.buffer[outPacket.l++] = 0x07;
        outPacket.buffer[outPacket.l++] = 0x10;
        outPacket.buffer[outPacket.l++] = 0x00;
        outPacket.buffer[outPacket.l++] = (byte) ((inPacket.max >> 8) & 0xff);
        outPacket.buffer[outPacket.l++] = (byte) (inPacket.max & 0xff);
    }

    public void obex_hi_name(byte[] msg, int lgt) {
        int i;

        if (outPacket.max < (outPacket.l + (lgt * 2) + 3)) {
            return;
        }
        outPacket.buffer[outPacket.l++] = Constants.OBEX_HI_NAME;
        outPacket.buffer[outPacket.l++] = (byte) (((2 * lgt + 3) >> 8) & 0xff);
        outPacket.buffer[outPacket.l++] = (byte) ((2 * lgt + 3) & 0xff);

        for (i = 0; i < lgt; i++) {
            outPacket.buffer[outPacket.l++] = 0x00;
            outPacket.buffer[outPacket.l++] = msg[i];
        }
    }

    public void obex_hi_body() {
        outPacket.buffer[outPacket.l++] = Constants.OBEX_HI_BODY;
        outPacket.buffer[outPacket.l++] = (byte) (((3) >> 8) & 0xff);
        outPacket.buffer[outPacket.l++] = (byte) ((3) & 0xff);
    }

    // When the packet is ready, run this to set the correct length
    public void cobex_packlgt() {
        outPacket.buffer[1] = (byte) ((outPacket.l >> 8) & 0xff);
        outPacket.buffer[2] = (byte) (outPacket.l & 0xff);

        return;
    }

    public void obex_opcode_put() {
        outPacket.l = 0;
        outPacket.buffer[outPacket.l++] = OBEX_OPCODE_PUT;
        outPacket.buffer[outPacket.l++] = 0x00;
        outPacket.buffer[outPacket.l++] = 0x00;
    }

    // Length, however, is a 32 bit integer.
    public void obex_hi_length(long length) {
        if (outPacket.max < (outPacket.l + 5)) {
            return;
        }
        outPacket.buffer[outPacket.l++] = Constants.OBEX_HI_LENGTH;
        outPacket.buffer[outPacket.l++] = (byte) ((length >> 24) & 255);
        outPacket.buffer[outPacket.l++] = (byte) ((length >> 16) & 255);
        outPacket.buffer[outPacket.l++] = (byte) ((length >> 8) & 255);
        outPacket.buffer[outPacket.l++] = (byte) (length & 255);
    }

    public void obex_hi_endofbody() {
        if (outPacket.max < (outPacket.l + 3)) {
            return;
        }
        outPacket.buffer[outPacket.l++] = Constants.OBEX_HI_END_OF_BODY;
        outPacket.buffer[outPacket.l++] = 0x00;
        outPacket.buffer[outPacket.l++] = 0x03;
    }

    public void obex_opcode_disconnect() {
        outPacket.l = 0;
        outPacket.buffer[outPacket.l++] = OBEX_OPCODE_DISCONNECT;
        outPacket.buffer[outPacket.l++] = 0x00;
        outPacket.buffer[outPacket.l++] = 0x03;
    }

    // Set "final bit" in a packet operation code.
    public void cobex_set_final_bit() {
        if (outPacket.l < 3) {
            return;
        }
        outPacket.buffer[0] = (byte) (outPacket.buffer[0] | OBEX_FINAL_BIT);
    }

    //
    // Functions related to send
    //
    //
    private ObexSendThread mObexSendThread = null;

    public void cobex_packet_send() {
        //UPLOAD buffer to comport
        //com_port.Write(outPacket.buffer, 0, outPacket.l);
        if(sendingFile == true) {
            MainActivity.getInstance().getBtConService().write(outPacket.buffer, outPacket.l);
        }else{
            MainActivity.getInstance().write(outPacket.buffer, outPacket.l);
        }

    }


    void obex_send_file(File file) throws IOException {
        if (mObexSendThread != null) {
            //mObexSendThread.destroy();
            mObexSendThread = null;
        }
        mObexSendThread = new ObexSendThread(file);
        mObexSendThread.start();
    }

    class ObexSendThread extends Thread {
        private long fsize;
        private int Receiver_MaxBufferSize;
        private FileInputStream fis;
        long startTime;

        public ObexSendThread(File file) throws IOException {
            fis = null;
            fis = new FileInputStream(file);
            Log.d(TAG, "Total file size to send (in bytes) : " + fis.available());
            Receiver_MaxBufferSize = 0;
            fsize = fis.available();
            nBytesLeftToSend = fsize;
            MainActivity.getInstance().progressBar.setProgress(0);
            CobexSendState = cobex_send_state_t.codex_send_SendConnect;
            startTime = System.currentTimeMillis();// nanoTime();
            sendingFile = true;
        }

        public void run() {
            while (true) {
                switch (CobexSendState) {
                    case cobex_send_ResivePacket:
                        break;
                    case codex_send_SendConnect:
                        //fill packet
                        obex_opcode_connect();
                        cobex_packet_send();
                        CobexSendState = cobex_send_state_t.cobex_ResivedConnnectAck;
                        break;
                    case cobex_ResivedConnnectAck:
                        if (MainActivity.getInstance().getBtConService().IncominingObexPacked.l != 0) {

                            if (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[0] == Constants.OBEX_RESPONSE_COONECT_OK) {
                                Receiver_MaxBufferSize = (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[5] << 8) + MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[6];
                                if (Receiver_MaxBufferSize >= 128)//cia logiskas dydis
                                {
                                    //if (Receiver_MaxBufferSize < outPacket.max)
                                    {
                                        outPacket.max = Receiver_MaxBufferSize;
                                    }
                                    CobexSendState = cobex_send_state_t.cobex_send_HiPacket;
                                } else {
                                    _canceled = true;
                                }
                            }
                            MainActivity.getInstance().getBtConService().IncominingObexPacked.l = 0;
                        }
                        break;
                    case cobex_send_ResivedConnnectHeader:
                        if (MainActivity.getInstance().getBtConService().IncominingObexPacked.l != 0) {
                            int len = MainActivity.getInstance().getBtConService().IncominingObexPacked.l;

                            byte[] text = new byte[10];
                            text[0] = (byte) 0x80;//connect
                            text[1] = (byte) 0x00;//
                            text[2] = (byte) 0x07;//message size
                            text[3] = (byte) 0x01;
                            text[4] = (byte) 0x00;

                            text[5] = (byte) 0xff;//buffer size
                            text[6] = (byte) 0xff;
                            if (Equality(MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer, len - 2, text, 5)) {
                                Receiver_MaxBufferSize = (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[5] << 8) + MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[6];
                                CobexSendState = cobex_send_state_t.cobex_send_HiPacket;
                            }
                            MainActivity.getInstance().getBtConService().IncominingObexPacked.l = 0;
                        }
                        break;
                    case cobex_send_HiPacket:
                        //fill packet
                        obex_opcode_put();
                        obex_hi_length(fsize);
                        String fileName = "PREVIEW.JPG";
                        //watch = System.Diagnostics.Stopwatch.StartNew();
                        obex_hi_name(fileName.getBytes(StandardCharsets.US_ASCII), fileName.length());
                        obex_hi_body();
                        cobex_packlgt();
                        cobex_packet_send();
                        CobexSendState = cobex_send_state_t.cobex_ResivedAcknowledgement;
                        break;
                    case cobex_ResivedAcknowledgement:
                        if (MainActivity.getInstance().getBtConService().IncominingObexPacked.l != 0) {

                            if (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[0] == (Constants.OBEX_RESPONSE_CONTINUE | OBEX_FINAL_BIT)) {
                                CobexSendState = cobex_send_state_t.cobex_send_FileData;
                            /*
                            if (Pck_watch != null)
                            {
                                Pck_watch.Stop();
                                long Pck_elapsedMs = Pck_watch.ElapsedMilliseconds;
                                PrintMs(Pck_elapsedMs);
                            }
                            */
                            }
                            MainActivity.getInstance().getBtConService().IncominingObexPacked.l = 0;
                        }
                        break;
                    case cobex_send_FileData:
                        int toTransfer;

                        if (nBytesLeftToSend == 0) {
                            CobexSendState = cobex_send_state_t.cobex_send_EndOfTransferFile;
                            break;
                        }

                        if (nBytesLeftToSend > (outPacket.max - 6)) {
                            toTransfer = (outPacket.max - 6);
                        } else {
                            toTransfer = (int) nBytesLeftToSend;
                        }
                        outPacket.l = 0;
                        outPacket.buffer[outPacket.l++] = OBEX_OPCODE_PUT;
                        outPacket.buffer[outPacket.l++] = (byte) ((toTransfer + 6) >> 8);//visas ilgis H
                        outPacket.buffer[outPacket.l++] = (byte) ((toTransfer + 6) & 0xFF);//visas ilgis L
                        outPacket.buffer[outPacket.l++] = Constants.OBEX_HI_BODY;
                        outPacket.buffer[outPacket.l++] = (byte) ((toTransfer + 3) >> 8);//data ilgis H;
                        outPacket.buffer[outPacket.l++] = (byte) ((toTransfer + 3) & 0xFF);//data ilgis L;
                        try {
                            if (fis.read(outPacket.buffer, outPacket.l, toTransfer) != -1) {
                                nBytesLeftToSend -= toTransfer;
                                outPacket.l = outPacket.l + toTransfer;
                                //Pck_watch = System.Diagnostics.Stopwatch.StartNew();
                                cobex_packet_send();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //ProgressBarChanged(true, (int)((fsize- nBytesLeftToSend) * 100 / fsize));
                        MainActivity.getInstance().progressBar.setProgress((int) ((fsize - nBytesLeftToSend) * 100 / fsize));
                        CobexSendState = cobex_send_state_t.cobex_ResivedAcknowledgement;
                        break;
                    case cobex_send_EndOfTransferFile:
                        obex_opcode_put();
                        obex_hi_endofbody();
                        cobex_packlgt();
                        cobex_set_final_bit();
                        cobex_packet_send();
                        CobexSendState = cobex_send_state_t.cobex_ResivedEndOfTransferAck;
                        break;
                    case cobex_ResivedEndOfTransferAck:
                        if (MainActivity.getInstance().getBtConService().IncominingObexPacked.l != 0) {

                            if (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[0] == (Constants.OBEX_RESPONSE_OK | OBEX_FINAL_BIT)) {
                                CobexSendState = cobex_send_state_t.cobex_send_Disconnect;
                            }
                            MainActivity.getInstance().getBtConService().IncominingObexPacked.l = 0;
                        }
                        break;
                    case cobex_Error:
                        break;
                    case cobex_send_Disconnect:
                        obex_opcode_disconnect();
                        cobex_packet_send();
                        CobexSendState = cobex_send_state_t.cobex_ResivedDisconnectAck;
                        break;
                    case cobex_ResivedDisconnectAck:
                        if (MainActivity.getInstance().getBtConService().IncominingObexPacked.l != 0) {
                            if (MainActivity.getInstance().getBtConService().IncominingObexPacked.buffer[0] == (Constants.OBEX_RESPONSE_OK | OBEX_FINAL_BIT)) {
                                //send completed
                                MainActivity.getInstance().runOnUiThread(new Runnable() {
                                    public void run() {
                                        long endTime = (System.currentTimeMillis() - startTime);//nanoTime();
                                        float MethodeDuration = (float) endTime / 1000;
                                        NumberFormat formatter = NumberFormat.getNumberInstance();
                                        formatter.setMinimumFractionDigits(1);
                                        formatter.setMaximumFractionDigits(1);

                                        Toast.makeText(MainActivity.getInstance(), "Send Completed " + formatter.format(MethodeDuration) + "s", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                CobexSendState = cobex_send_state_t.cobex_Disconnected;
                            }
                            MainActivity.getInstance().getBtConService().IncominingObexPacked.l = 0;
                        }
                        break;
                    case cobex_Disconnected:
                        _done = true;
                        break;
                }
                if (_canceled || _done) {
                    try {
                        fis.close();
                        cancel();
                        _canceled = false;
                        _done = false;
                        MainActivity.getInstance().getBtConService().resetConnection();
                        MainActivity.getInstance().runOnUiThread(new Runnable() {
                            public void run() {
                                MainActivity.getInstance().btnSend.setEnabled(true);
                            }
                        });
                        sendingFile = true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel() {
            _canceled = true;
        }
    }

    public boolean Equality(byte[] a1, int len_a, byte[] b1, int len_b) {
        int i;
        boolean bEqual = false;
        if (len_a == len_b) {
            i = 0;
            while ((i < len_a) && (a1[i] == b1[i])) {
                i++;
            }

            if (i == len_a) {
                bEqual = true;
            }
        }
        return bEqual;
    }

    //public void obex_prepare_to_send()
    {
        outPacket.buffer = new byte[COBEX_PKT_SIZE];
        inPacket.buffer = new byte[1024];
        inPacket.max = inPacket.buffer.length;
        BluetoothConnectionService.IncominingObexPacked = inPacket;
        MainActivity.getInstance().progressBar.setProgress(0);
        //ProgressBarChanged(true, 0);
    }

    public void obex_cancel_sending() {

    }


    enum BTCobexState_t {
        Cobex_Receive_Packet,
        Cobex_WaitingConnect,
        Cobex_ResivingFile,
        Cobex_SendFile,
        Cobex_Disconnect
    }

    enum ResiveSatus_t {
        COBEX_PACKED_RESIVE_ERROR,
        COBEX_PACKED_WAITING,
        COBEX_PACKED_RESIVED
    }

    enum ObexState_t {
        COBEX_STATE_NOT_CONNECTED,
        COBEX_STATE_CONNECTED
    }


    ObexState_t cobex_state = COBEX_STATE_NOT_CONNECTED;
    int lenght = 0;

    static class ObexResiveThread extends Thread {

        private static long rx_timeout;
        private static long curr_bt_tick;
        ResiveSatus_t rx_err = COBEX_PACKED_WAITING;
        BTCobexState_t BTResiveState = Cobex_WaitingConnect;
        BTCobexState_t BTNextResiveState = Cobex_WaitingConnect;
        ObexState_t cobex_state = COBEX_STATE_NOT_CONNECTED;

        //String btsb_app_preview_fname_jpg = "PREVIEW.JPG";
        //String btsb_app_preview_fname_jpg2 = "PREVIEW2.JPG";
        //String btsb_app_preview_fname_gif = "PREVIEW.GIF";
        preview_mode_t obex_preview_mode = PREVIEW_OFF;
        SbObexError_T cobex_error_status = SBOBEXERROR_OK;
        int obex_received_files = 0;
        boolean FileResived = false;


        class obex_in {
            long file_size;
            long file_size_done;
            long body_len;
            long body_len_done;
            int packet_len;
            int packet_len_done;
            short last_body;
            //char * buf;
            String name;
            int fp;

            void obex_in() {
            }
        }

        obex_in obex_in = new obex_in();

        public ObexResiveThread() {
            getInstance().inPacket.buffer = new byte[COBEX_PKT_SIZE];
            getInstance().inPacket.max = getInstance().inPacket.buffer.length;
            //BluetoothConnectionService.IncominingObexPacked = inPacket;

            BTResiveState = Cobex_Receive_Packet;
            BTNextResiveState = Cobex_WaitingConnect;
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    MainActivity.getInstance().btnSend.setEnabled(false);
                }
            });
            FileResived = false;
        }

        static void setTimeout(long timeout){
            rx_timeout = timeout;
            curr_bt_tick = System.currentTimeMillis();
        }

        public void run() {
            while (true) {
                switch (BTResiveState) {
                    case Cobex_Receive_Packet:
                        boolean rx_done = false;
                        setTimeout(3000);
                        while (true) {
                            switch (MainActivity.getInstance().getOBEXPacketStatus()) {
                                case COBEX_PACKED_WAITING:
                                    //Packet Waiting...
                                    if ((System.currentTimeMillis() - curr_bt_tick) > rx_timeout) {
                                        BTResiveState = Cobex_Disconnect;
                                        rx_done = true;
                                    }
                                    break;
                                case COBEX_PACKED_RESIVED:
                                    BTResiveState = BTNextResiveState;
                                    rx_done = true;
                                    break;
                                case COBEX_PACKED_RESIVE_ERROR:
                                    BTResiveState = Cobex_Disconnect;
                                    rx_done = true;
                                    break;
                            }
                            if(rx_done)
                            break;
                        }
                        break;
                    case Cobex_WaitingConnect:
                        if ((getInstance().inPacket.l != 0) && (cobex_state == COBEX_STATE_NOT_CONNECTED)) {
                            try {
                                int lenght = getInstance().inPacket.l;
                                cobex_process_data(getInstance().inPacket.buffer, lenght);
                                MainActivity.getInstance().setReadyToRecive();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (cobex_state == COBEX_STATE_NOT_CONNECTED) {
                                BTResiveState = Cobex_Disconnect;
                                break;
                            } else {
                                BTResiveState = Cobex_Receive_Packet;
                                BTNextResiveState = Cobex_WaitingConnect;
                                break;
                            }
                        }
                        if(getInstance().inPacket.l > 0){//resive file name
                            try {
                                int lenght = getInstance().inPacket.l;
                                cobex_process_data(getInstance().inPacket.buffer, lenght);
                                MainActivity.getInstance().setReadyToRecive();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(cobex_state == COBEX_STATE_NOT_CONNECTED){
                                BTResiveState = Cobex_Disconnect;
                                break;
                            }
                            BTResiveState = Cobex_Receive_Packet;
                            BTNextResiveState = Cobex_ResivingFile;
                            //Log.d(TAG, "file_size: " + obex_in.file_size + "file_size_done" +obex_in.file_size_done);
                            MainActivity.getInstance().progressBar.setProgress(0);
                        }else{
                            BTResiveState = Cobex_Disconnect;
                        }
                        break;
                    case Cobex_ResivingFile:
                        if(getInstance().inPacket.l > 0){
                            try {
                                int length = getInstance().inPacket.l;
                                byte[] dst = Arrays.copyOf(getInstance().inPacket.buffer, length);
                                cobex_process_data(dst, length);
                                MainActivity.getInstance().progressBar.setProgress((int) ((obex_in.file_size - (obex_in.file_size - obex_in.file_size_done)) * 100 / obex_in.file_size));
                                MainActivity.getInstance().setReadyToRecive();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(cobex_state == COBEX_STATE_NOT_CONNECTED){
                                cobex_state = COBEX_STATE_NOT_CONNECTED;

                                BTResiveState = Cobex_Disconnect;
                                //IsBTResivedFiles = true;
                                break;
                            }else{
                                BTResiveState = Cobex_Receive_Packet;
                                BTNextResiveState = Cobex_ResivingFile;
                            }
                        }else{
                            BTResiveState = Cobex_Disconnect;
                        }
                        break;
                    case Cobex_Disconnect:
                        BTResiveState = Cobex_Receive_Packet;
                        BTNextResiveState = Cobex_WaitingConnect;
                        //FileResived = true;
                        //cancel();
                        break;
                }
                if(FileResived)
                    break;
            }
            try {
                sleep(3000);
                cancel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() throws InterruptedException {
            MainActivity.getInstance().resetObexConnection();
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    MainActivity.getInstance().btnSend.setEnabled(true);
                }
            });
            /*
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.getInstance(), "Resive Completed ", Toast.LENGTH_SHORT).show();
                }
            });
            */
        }

        static String getNewFileName(String oldName,int fileNr) {
            String fileName = "";
            String extension = "";
            String name = "";
            int idxOfDot = oldName.lastIndexOf('.');   //Get the last index of . to separate extension
            int idxOfBkt = oldName.indexOf('(');
            extension = oldName.substring(idxOfDot);
            if(idxOfBkt == -1) {
                name = oldName.substring(0, idxOfDot);
            }else{
                name = oldName.substring(0, idxOfBkt);
            }
            //Log.d(TAG, "File name: " + name);
            //Log.d(TAG, "File extension: " + extension);
            fileName = (name + "(" +Integer.toString(fileNr)+ ")" + extension);
            return fileName;
        }


        void cobex_process_data(byte buf[], int len) throws IOException, InterruptedException {
            int i, hdr_len, ptr = 0, data_len = 0, err;
            String path = "/sdcard/";
            //String f_name;

            if (cobex_state == COBEX_STATE_NOT_CONNECTED){

                if (buf[0] == OBEX_OPCODE_CONNECT){
                    getInstance().outPacket.buffer[0] = (byte)0xA0; /* response opcode */
                    getInstance().outPacket.buffer[1] = (byte)0x00; /* length 7 */
                    getInstance().outPacket.buffer[2] = (byte)0x07;
                    getInstance().outPacket.buffer[3] = (byte)0x10; /* obex version */
                    getInstance().outPacket.buffer[4] = (byte)0x00; /* flags */
                    getInstance().outPacket.buffer[5] = (byte)((getInstance().inPacket.max >> 8) & 0xff);
                    getInstance().outPacket.buffer[6] = (byte)(getInstance().inPacket.max & 0xff);
                    getInstance().outPacket.l = 7;
                    getInstance().cobex_packet_send();
                    cobex_state = COBEX_STATE_CONNECTED;
                }else if(buf[0] == OBEX_OPCODE_DISCONNECT){
                    getInstance().outPacket.buffer[0] = (byte)0xA0;
                    getInstance().outPacket.buffer[1] = (byte)0x00;
                    getInstance().outPacket.buffer[2] = (byte)0x03;
                    getInstance().outPacket.l = 3;
                    cobex_state = COBEX_STATE_NOT_CONNECTED;
                    OBEX.getInstance().cobex_packet_send();
                    FileResived = true;
                }
            }else{
                if(buf[0] == OBEX_OPCODE_DISCONNECT){
                    getInstance().outPacket.buffer[0] = (byte)0xA0;
                    getInstance().outPacket.buffer[1] = (byte)0x00;
                    getInstance().outPacket.buffer[2] = (byte)0x03;
                    getInstance().outPacket.l = 3;
                    cobex_state = COBEX_STATE_NOT_CONNECTED;
                    OBEX.getInstance().cobex_packet_send();
                    FileResived = true;
                }else
                if(obex_in.packet_len_done == 0 || obex_in.packet_len_done == obex_in.packet_len){
                    if((buf[ptr] == OBEX_OPCODE_PUT || buf[ptr] == (OBEX_OPCODE_PUT|OBEX_FINAL_BIT))){
                        ptr++;
                        //obex_in.packet_len = (buf[ptr]<<8) + buf[ptr+1];
                        byte[] arr = new byte[] {buf[ptr],buf[ptr+1]};
                        ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
                        obex_in.packet_len = wrapped.getShort();
                        obex_in.packet_len_done = 0;
                        ptr+=2;

                        while(ptr < len){
                            switch(buf[ptr]){
                                case 0x01:
                                { /* HI for name header */
                                    ptr++;

                                    hdr_len = (buf[ptr]<<8) + buf[ptr+1];
                                    ptr+=2;
                                    obex_in.name = "";
                                    for (i = 0; i < (hdr_len-3)/2; i++){
                                        char ch = (char)((buf[ptr]<<8)+buf[ptr+1]);
                                        if(ch != 0x00) {
                                            String character = Character.toString(ch);
                                            obex_in.name = obex_in.name + character;
                                        }
                                        ptr+=2;
                                    }
                                    File PrewiewFile = new File(path+obex_in.name);
                                    int nr = 0;
                                    while (PrewiewFile.exists()) {
                                        nr++;
                                        obex_in.name = getNewFileName(obex_in.name,nr);
                                        PrewiewFile = new File(path+obex_in.name);
                                    }
                                    PrewiewFile.createNewFile();
                                    FileOutputStream wFile = new FileOutputStream(PrewiewFile, true);
                                    wFile.close();
                                    break;
                                }
                                case (byte)0xc3:
                                { /* HI for length header */
                                    ptr++;
                                    obex_in.file_size_done = 0;
                                    //ByteBuffer file_size_arr = ByteBuffer.wrap(new byte[] {0, 0, 0, 0,buf[ptr],buf[ptr+1],buf[ptr+2],buf[ptr+3]});
                                    //obex_in.file_size = file_size_arr.getLong();
                                    obex_in.file_size = (((long)buf[ptr])<<24)+(((long)buf[ptr+1])<<16)+(buf[ptr+2]<<8)+buf[ptr+3];
                                    ptr+=4;
                                    break;
                                }
                                case (byte)0x48:
                                case (byte)0x49:
                                { /* HI for length header */
                                    if (buf[ptr] == 0x48){
                                        obex_in.last_body = 0;
                                    }else{
                                        obex_in.last_body = 1;
                                    }
                                    ptr++;
                                    //byte[] body_len_arr = new byte[] {buf[ptr],buf[ptr+1]};
                                    //ByteBuffer wrapped_len = ByteBuffer.wrap(body_len_arr); // big-endian by default
                                    //obex_in.body_len = wrapped_len.getShort()-3;
                                    obex_in.body_len = (buf[ptr]<<8) + buf[ptr+1] - 3;
                                    obex_in.body_len_done = 0;

                                    ptr+=2;

                                    obex_in.body_len_done += len-ptr;
                                    obex_in.file_size_done += len-ptr;
                                    data_len = len-ptr;
                                    if (cobex_error_status == SBOBEXERROR_OK){
                                        //if (obex_preview_mode == PREVIEW_OFF){

                                       //}else{ /* preview mode */
                                            if(data_len != 0){
                                                //if(obex_preview_mode == PREVIEW_JPG){
                                                    //f_name = mem_calloc(DIR_BUFFER_SZ + 1,sizeof(char));
                                                    //assert_m(f_name);
                                                    //snprintf(f_name,DIR_BUFFER_SZ,"%s/%s",WRK_FILE_PATH,JPG_PREVIEW);
                                                    //FileHandler = FileIOOpen(f_name,"a+");
                                                    //mem_free(f_name);

                                                //}else if(obex_preview_mode == PREVIEW_GIF){
                                                    //f_name = mem_calloc(DIR_BUFFER_SZ + 1,sizeof(char));
                                                    //assert_m(f_name);
                                                    //snprintf(f_name,DIR_BUFFER_SZ,"%s/%s",WRK_FILE_PATH,GIF_PREVIEW);
                                                    //FileHandler = FileIOOpen(f_name,"a+");
                                                    //mem_free(f_name);
                                                //}
                                                File PrewiewFile = new File(path+obex_in.name);
                                                PrewiewFile.createNewFile();
                                                FileOutputStream wFile = new FileOutputStream(PrewiewFile, true);
                                                wFile.write(buf,ptr,data_len);
                                                wFile.flush();
                                                ptr+=data_len;
                                                wFile.close();
                                            }
                                        //}
                                    }else{
                                        ptr += data_len;
                                    }

                                    if (data_len > 0 && obex_in.file_size_done == obex_in.file_size){
                                        //if (obex_preview_mode == PREVIEW_OFF){
                                        //    obex_in.name = null;
                                        //}
                                    }

                                    break;
                                }
                                default:
                                    ptr++;
                                    if(((buf[ptr]<<8) + buf[ptr+1] - 1)>0){
                                        ptr += (buf[ptr]<<8) + buf[ptr+1] - 1;
                                    }
                                    break;
                            }//switch(buf[ptr]){
                        }//while(ptr < len){
                        obex_in.packet_len_done += len;

                        if (obex_in.packet_len_done == obex_in.packet_len){
                            //outPacket.l = 3;
                            if (cobex_error_status != SBOBEXERROR_OK){
                                if (cobex_error_status == SBOBEXERROR_FILENAME){
                                    getInstance().outPacket.buffer[0] = OBEX_RESPONSE_NOT_ACCEPTABLE;
                                }else{
                                    getInstance().outPacket.buffer[0] = (byte)0xff;
                                }
                            }
                            else if (buf[0] == (OBEX_OPCODE_PUT|OBEX_FINAL_BIT)){
                                getInstance().outPacket.buffer[0] = (byte)0xA0;//Send Done

                                final File file = new File(path+obex_in.name);
                                new Thread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // Do network action in this function
                                        try {
                                            //MainActivity.FTPUploader ftp = new MainActivity.FTPUploader("ftp2.lifodas.com","fod","LifodaS41");

                                            SharedPreferences prefs = MainActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
                                            String host = prefs.getString("host", null);
                                            String hostDir = prefs.getString("hostdir", null);
                                            String user = prefs.getString("user", null);
                                            String psw = prefs.getString("pass", null);

                                            if((psw != null) && (user != null) && (hostDir != null) && (host != null)){
                                                MainActivity.FTPUploader ftp = new MainActivity.FTPUploader(host, user, psw);
                                                ftp.uploadFile(file.getAbsolutePath(), file.getName(), hostDir);
                                            }else{
                                                MainActivity.getInstance().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(MainActivity.getInstance(), "Send to default host", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                MainActivity.FTPUploader ftp = new MainActivity.FTPUploader("ftp2.lifodas.com", "afl", "s4cb5C");
                                                ftp.uploadFile(file.getAbsolutePath(), file.getName(), "/AFL upload/FS200/");
                                            }

                                                //ftp.uploadFile("/sdcard/test2.jpg","test2.jpg","up/FOD_upload/FS200/");
                                                //ftp.uploadFile(file.getAbsolutePath(),file.getName(),"/FOD upload/FS200/");
                                                //ftp.uploadFile(file.getAbsolutePath(), file.getName(), "/AFL upload/FS200/");
                                                //FTPUploader ftp = new FTPUploader("192.168.1.1","user","user");
                                                //ftp.uploadFile(file.getAbsolutePath(),file.getName(),"/up/FOD_upload/fs200/");

                                        } catch (final Exception ex) {
                                            System.err.println(ex);
                                        }

                                    }
                                }).start();

/*
                                try {
                                    int data[] = new int[300000];
                                    sorReadTraceData(file, data, 0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
*/
                                new Thread(new Runnable(){
                                    @Override
                                    public void run() {
                                        try {
                                            String path = "/sdcard/";
                                            sor_send_file(path+obex_in.name);
                                        } catch (final Exception ex) {
                                            System.err.println(ex);
                                        }


                                    }
                                }).start();


                            }else{
                                getInstance().outPacket.buffer[0] = (byte)0x90;
                            }
                            getInstance().outPacket.buffer[1] = (byte)0x00;
                            getInstance().outPacket.buffer[2] = (byte)0x03;
                            getInstance().outPacket.l = 3;
                            OBEX.getInstance().cobex_packet_send();
                        }
                    }
                    else if(buf[ptr] == OBEX_OPCODE_DISCONNECT){
                        //outPacket.l = 3;
                        getInstance().outPacket.buffer[0] = (byte)0xA0;
                        getInstance().outPacket.buffer[1] = (byte)0x00;
                        getInstance().outPacket.buffer[2] = (byte)0x03;
                        cobex_state = COBEX_STATE_NOT_CONNECTED;
                        getInstance().outPacket.l = 3;
                        OBEX.getInstance().cobex_packet_send();
                        FileResived = true;
                        if(obex_preview_mode != PREVIEW_OFF){
                        }
                    }
                }else{
                    if (obex_in.body_len_done < obex_in.body_len){
                        if (cobex_error_status == SBOBEXERROR_OK){
                            if(obex_preview_mode == PREVIEW_OFF){

                            }else{
                                if(obex_preview_mode == PREVIEW_JPG){
                                    //f_name = mem_calloc(DIR_BUFFER_SZ + 1,sizeof(char));
                                }else if(obex_preview_mode == PREVIEW_GIF){
                                    //f_name = mem_calloc(DIR_BUFFER_SZ + 1,sizeof(char));
                                }
                                File PrewiewFile = new File(path+obex_in.name);
                                PrewiewFile.createNewFile();
                                FileOutputStream wFile = new FileOutputStream(PrewiewFile, true);
                                wFile.write(buf,ptr,data_len);
                                wFile.flush();
                                ptr+=data_len;
                                wFile.close();
                            }
                        }
                        obex_in.body_len_done += len;
                        obex_in.file_size_done += len;
                        obex_in.packet_len_done += len;

                        if (obex_in.body_len_done == obex_in.body_len){
                            if (cobex_error_status != SBOBEXERROR_OK){
                                if (cobex_error_status == SBOBEXERROR_FILENAME){
                                    getInstance().outPacket.buffer[0] = OBEX_RESPONSE_NOT_ACCEPTABLE;
                                }else{
                                    getInstance().outPacket.buffer[0] = (byte)0xff;
                                }
                            }
                            else if (obex_in.last_body != 0){
                                getInstance().outPacket.buffer[0] = (byte)0xA0;
                            }
                            else {
                                getInstance().outPacket.buffer[0] = (byte)0x90;
                            }
                            getInstance().outPacket.buffer[1] = (byte)0x00;
                            getInstance().outPacket.buffer[2] = (byte)0x03;
                            getInstance().outPacket.l = 3;
                            OBEX.getInstance().cobex_packet_send();
                            obex_in.body_len_done = 0;
                            if (obex_in.file_size_done == obex_in.file_size){
                                if(obex_preview_mode == PREVIEW_OFF){
                                }
                                obex_received_files++;
                            }
                        }
                    }
                }
            }
            return;
        }

        private void sor_send_file(String s) throws IOException {
            MainActivity.getInstance().sor_send_file(s);
        }
    }

}
