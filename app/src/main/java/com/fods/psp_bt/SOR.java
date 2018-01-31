package com.fods.psp_bt;


import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;
import static com.fods.psp_bt.SOR.Constants.BELL_FODPARAMS_MAGIC;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_EG;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_EN;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_GE;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_GM;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_GS;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_MB;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_MS;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_SG;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_SP;
import static com.fods.psp_bt.SOR.Constants.EV_CODE_ST;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_GR_END;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_GR_MIDDLE;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_GR_START;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_LINK_END;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_LINK_START;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_MACROBEND;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_SIMPLE;
import static com.fods.psp_bt.SOR.Constants.EV_CTYPEN_MSK_SPLITTER;
import static com.fods.psp_bt.SOR.Constants.FIBER_TYPE_SMF28E;
import static com.fods.psp_bt.SOR.Constants.FIBER_TYPE_USER1;
import static com.fods.psp_bt.SOR.Constants.FIBER_TYPE_USER2;
import static com.fods.psp_bt.SOR.Constants.LightVelocity;
import static com.fods.psp_bt.SOR.Constants.MAX_BLOCKSIZE;
import static com.fods.psp_bt.SOR.Constants.MAX_TNDP;
import static com.fods.psp_bt.SOR.Constants.MAX_TNKE;
import static com.fods.psp_bt.SOR.Constants.NETWORK_TYPE_POINT_TO_POINT;
import static com.fods.psp_bt.SOR.Constants.NETWORK_TYPE_PON;
import static com.fods.psp_bt.SOR.Constants.PASSFAIL_TYPE_ITUG671;
import static com.fods.psp_bt.SOR.Constants.PASSFAIL_TYPE_TIA568_3D;
import static com.fods.psp_bt.SOR.Constants.PASSFAIL_TYPE_USER1;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_ACI;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_CONN_LOSS;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_CONN_REFL;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_END_REFL;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLICE_LOSS;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLITTER1_LOSS_TOLERANCE;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLITTER1_RATIO_INDX;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLITTER2_LOSS_TOLERANCE;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLITTER2_RATIO_INDX;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_EVENT_SPLITTER_REFL;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_LINK_ACI;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_LINK_LENGTH;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_LINK_LENGTH_TOLERANCE;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_LINK_LOSS;
import static com.fods.psp_bt.SOR.Constants.PFT_THRESH_DFLT_LINK_ORL;
import static com.fods.psp_bt.SOR.Constants.TEST_TYPE_LOCATE_END_AND_FAULTS;
import static com.tom_roush.pdfbox.cos.COSName.DS;
import static com.tom_roush.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes.RT;
import static org.apache.commons.net.telnet.TelnetCommand.AO;
import static org.apache.commons.net.telnet.TelnetCommand.EL;

/**
 * Created by Valentinas on 2018-01-15.
 */

public class SOR {

    private static FileInputStream fis;
    private static long fsize;
    static TestParams_T TestParamS = new TestParams_T();

    public static int[] sk;
    public static EvEventsS Events = new SOR.EvEventsS();

    public class Constants {
        public static final int TESTP_FPO = (2987);
        /* max. number of possible wavelength values in Test Settings menu */
        public static final int TESTSET_WL_NMAX = 7;
        /* max number of configurable splitters */


        public static final int TEST_TYPE_NMAX = (11);

        public static final int TEST_SMART_AUTO_TEST_TYPES_NMAX = (2);

        public static final int NETWORK_TYPE_NMAX = (2);
        public static final int MAX_NUMBER_OF_SUPPORTED_WL = (3);

        public static final int FIBER_USER_TYPES_NMAX = (2);
        public static final int FIBER_TYPES_NMAX = (3);

        public static final int PASSFAIL_USER_TYPES_NMAX = (1);
        public static final int PASSFAIL_TYPES_NMAX = (3);

        public static final int TEST_TYPE_LOCATE_END_AND_FAULTS	= 10;

        public static final int FIBER_TYPE_SMF28E = 0;
        public static final int FIBER_TYPE_USER1 = 30;
        public static final int FIBER_TYPE_USER2 = 31;

        public static final int PASSFAIL_TYPE_ITUG671 = 0;
        public static final int PASSFAIL_TYPE_TIA568_3D = 1;
        public static final int PASSFAIL_TYPE_USER1 = 30;

        public static final int NETWORK_TYPE_POINT_TO_POINT = 0;
        public static final int NETWORK_TYPE_PON = 1;

        public static final int PFT_THRESH_DFLT_LINK_LENGTH	= (10000);

        public static final int  PFT_THRESH_DFLT_LINK_LENGTH_TOLERANCE =(10);
        public static final int  PFT_THRESH_DFLT_LINK_LOSS =(5000);
        public static final int  PFT_THRESH_DFLT_LINK_ACI =(500);
        public static final int  PFT_THRESH_DFLT_LINK_ORL =(24000);


        public static final int PFT_THRESH_DFLT_EVENT_CONN_LOSS =(750);
        public static final int PFT_THRESH_DFLT_EVENT_CONN_REFL =(-260);
        public static final int PFT_THRESH_DFLT_EVENT_SPLICE_LOSS =(300);
        public static final int PFT_THRESH_DFLT_EVENT_SPLITTER1_LOSS_TOLERANCE =(1000);
        public static final int PFT_THRESH_DFLT_EVENT_SPLITTER2_LOSS_TOLERANCE =(1000);
        public static final int PFT_THRESH_DFLT_EVENT_SPLITTER1_RATIO_INDX =(4);
        public static final int PFT_THRESH_DFLT_EVENT_SPLITTER2_RATIO_INDX =(0);
        public static final int PFT_THRESH_DFLT_EVENT_END_REFL =(-260);
        public static final int PFT_THRESH_DFLT_EVENT_ACI =(500);

        public static final int PFT_THRESH_DFLT_EVENT_SPLITTER_REFL =(-550);

        public static final int TEST_DBUF_NMAX = (300000 + 512);

        public static final int  MAX_TNDP = (TEST_DBUF_NMAX);

        public static final int MAX_BLOCKSIZE = (2*MAX_TNDP+22);


        public static final int EV_SPL_CFG_MAX = (3);

        public static final int EV_CODE_MB = 0x424D;
        public static final int EV_CODE_SP = 0x5053;
        public static final int EV_CODE_GS = 0x5347;
        public static final int EV_CODE_GM = 0x4D47;
        public static final int EV_CODE_GE = 0x4547;
        public static final int EV_CODE_ST = 0x5453;
        public static final int EV_CODE_SG = 0x4753; /* link start group */
        public static final int EV_CODE_EG = 0x4745; /* link end group */
        public static final int EV_CODE_EN = 0x4E45;
        public static final int EV_CODE_SI = 0x4953;
        public static final int EV_CODE_MS = 0x534D; /* macrobend at splitter */

        public static final int BELL_FODPARAMS_MAGIC = 0x5fa5;
        public static final int MAX_TNKE = (80);

        public static final double LightVelocity = 1.49896229e4;

        public static final int EV_CTYPEN_MSK_SIMPLE     = 0x0000;

        public static final int EV_CTYPEN_MSK_MACROBEND  = 0x0001;

        public static final int EV_CTYPEN_MSK_SPLITTER   = 0x0002;

        public static final int EV_CTYPEN_MSK_GR_START   = 0x0004;

        public static final int EV_CTYPEN_MSK_GR_MIDDLE  = 0x0008;

        public static final int EV_CTYPEN_MSK_GR_END     = 0x0010;

        public static final int EV_CTYPEN_MSK_IS_GROUP   = 0x001C;

        public static final int EV_CTYPEN_MSK_LINK_START = 0x0020;

        public static final int EV_CTYPEN_MSK_LINK_END   = 0x0040;
    };
    public SOR (){

    }

    enum EvSplCfgTypes_t{
        EvSplCfgType_Auto,
        EvSplCfgType_None,
        EvSplCfgType_R1_1,
        EvSplCfgType_R1_2,
    }

    public static class EvSplCfg_t{
        int r1[] = new int[3]; /* r1:r2, -1:Auto, 0:None, 1, 2.. */
        int r2[] = new int[3]; /* r1:r2 */
    };

    public static int C2X_FUNC(char c1, char c2){
        int retval = ((c2 << 8) | c1);
        return retval;
    }


    public static class TestComParams_T{
        public int TNDP; /* Total Number of Data Points */
        public int AO;	/* int32_t Acquisition Offset */
        public int DS; 	/* Data Spacing */
        public int NPPW; /* Number of Data Points for Each Pulse Width */
        public int AR; 	/* Acquisition Range */
        public int TPW; 	/* Total Number of Pulse Widths Used */
        public int PWU; 	          /* Pulse Widths Used */
        public int Test_Mode;      /* FullAuto, EndLocate, Live, Expert */
        public int Test_RangeIndx; /* aqcuisition */
        public int Test_PwIndx;
        public int Test_RangeChangedFlag;
        public int Fiber_Type;     /* SMF-28e, User */
        public int Network_Type;     /* Point-to-Point, PON */
        public int PassFail_Type;  /* ITU G.671, User1 */
        public int Live_Fiber;     /* indicates live fiber mode */
        public int Event_Mode;     /* Off, Auto, EndLocate */
        public int TestGain;
        public int TestTime; /* test time in seconds x 100 */
        public int RefreshTime; /* trace refresh time in seconds * 100 */
        public int Resolution;
        public int marker1_val; /* marker 1 location */
        public int marker2_val; /* marker 2 location */
        public int  Cable_Launch_cm;  /* Launch  cables in cm */
        public int  Cable_Receive_cm; /* Receive cables in cm */
        public int MacrobendDetection; /* Macrobend detection on/off */
        public int LQC_Result; /* 0 -off, 1 - OK, 2 - poor */
        public EvSplCfg_t ev_spl_cfg = new EvSplCfg_t();
        public int otdr_script_id;
        public int otdr_script_state; /* 0 - idle, 1 - busy, 2 - done */
        public int otdr_script_status; /* 0 - OK, >0 error */

        public int[] LinkPassFailThresholdsOnOff = new int[2]; /* 0b000000000000dcba, link thresholds on/off bits 0-off,1-on: a-length, b-loss, c-ACI, d-ORL */
        public int[] EventPassFailThresholdsOnOff = new int[2]; /* 0b00000000hgfedcba, event thresholds on/off bits 0-off,1-on: a-event, b-conn.loss, c-conn.refl., d-splice loss, e-splitter1 loss, f-splitter2 loss, g-end refl, h-fiber sec.*/
        public int[] LinkPassThrLength = new int[2]; /* Link length pass threshold in meters */
        public int[] LinkPassThrLengthTolerance = new int[2]; /* Link length tolerance pass threshold in percent */
        public int[] LinkPassThrLoss = new int[2]; /* Link Loss pass threshold in dB*100 */
        public int[] LinkPassThrACI = new int[2]; /* Link Loss/Distance pass threshold in dB/km*100 */
        public int[] LinkPassThrORL = new int[2]; /* Link ORL pass threshold in dB*100 */
        public int[] EventPassThrConnLoss = new int[2]; /* Event Connector Loss pass threshold in dB*100 */
        public int[] EventPassThrConnRefl = new int[2]; /* Event Connector Reflectance pass threshold in dB*10 */
        public int[] EventPassThrSpliceLoss = new int[2]; /* Event Splice Loss pass threshold in dB*100 */
        public int[] EventPassThrSplitter1Ratio = new int[2]; /* Event Splitter 1 Ratio  pass threshold (2, 4, 8, 16, 32, 64, 128) */
        public int[] EventPassThrSplitter2Ratio = new int[2]; /* Event Splitter 2 Ratio  pass threshold (2, 4, 8, 16, 32, 64, 128) */
        public int[] EventPassThrSplitter1LossTolerance = new int[2]; /* Event Splitter 1 Loss Tolerance pass threshold in dB*100 */
        public int[] EventPassThrSplitter2LossTolerance = new int[2]; /* Event Splitter 2 Loss Tolerance pass threshold in dB*100 */
        public int[] EventPassThrEndRefl = new int[2]; /* Event End Reflectance pass threshold in dB*100 */
        public int[] EventPassThrACI = new int[2]; /* Event Fiber Section (ACI) pass threshold in dB/km*100 */
        public int EventPassThrSplitterRefl; /* Event Splitter Reflectance pass threshold in dB*100 */
    }

    public static class EvEventPS{
        public int Location; //int32_t
        public int End; //int32_t
        public int Type; //uint16_t
        public int Max; //int32_t
        public int CustomType;//uint16_t
        public int PassFail;//uint16_t
        public int ACI;//int16_t
        public int Refl;//int32_t
        public int Loss;//int32_t
        public int StartLevel;//int32_t
        public int LocStart_mm;//int32_t
        public int LocMax_mm;//int32_t
        public int LocEnd_mm;//int32_t
        public int PW_mm;//int32_t
        public int MaxLevel;//int32_t
        public int EndLevel;//int32_t
        public int SegmentID;//int16_t
        public int Flags;//int16_t
        public int SplitterRatio; /*uint16_t 1:x, 0 - auto */
    }

    public static class ldiv_t
    {       /* result of long divide */
        long quot;
        long rem;
    }

    public static class TestParams_T{
        public int  UO; 	/*int32_t User Offset */
        public int  GI; 	/*int32_t Group Index */
        public int  NAV; /*int32_t Number of Averages */
        public int  NW; 	/*int16_t Nominal Wavelength */
        public int  BC; 	/*int16_t Backscater Coefficient */
        public int  TNKE;   /*int16_t Number of Key Events */
        public int LT; 	/*uint16_t Loss Threshold */
        public int RT; 	/*uint16_t Reflectance Threshold */
        public int ET;	/*uint16_t End-of-Fiber Threshold */
        public int NF;	/*uint16_t Noise Floor Level */
        public int  FPO; /*int32_t Front Panel Offset */

        public String CID;
        public String FID;
        public String OL;
        public String TL;
        public String CCD;

        public String SN;
        public String MFID;
        public String OTDR;
        public String OMID;
        public String OMSN;
        public String SR;
        public String OT;

        public int  TraceValid; //int16_t
        public int  EventValid; //int16_t

        public int  EEL;	/*int32_t End to End Loss */
        public int  ELMP1;	/*int32_t End loss marker 1 position */
        public int  ELMP2;	/*int32_t End loss marker 2 position */
        public int ORL;   /*uint16_t Optical return loss */
        public int  RLMP1;     /*int32_t ORL marker 1 position */
        public int  RLMP2;     /*int32_t ORL marker 2 position */
        public int  EEACI;     /*int16_t End to End ACI */

        public int LinkPassFailResult; /*uint16_t 0b00000000ddccbbaa, aa - link length pf status, bb - Loss pf status, cc - ACI pf status, dd - ORL pf status */
        public int TimeStamp; /*uint32_t unix time since 1970-01-01 */
        public int DefaultACI; //uint16_t

        public int[] EventPassThrSplitterLossMin = new int[12]; /*uint16_t Event Splitter Loss Min pass threshold in dB*100 */
        public int[] EventPassThrSplitterLossMax = new int[12]; /*uint16_t Event Splitter Loss Max pass threshold in dB*100 */

        TestComParams_T c = new TestComParams_T(); /* common paramters */  //Bandyk dabar.
    }

    static class EvEventsS{
        /*int16_t */int N; 		        /* number of events */
        /*int32_t */int LocationIndx[] = new int[MAX_TNKE+2];
        /*int32_t */int EndIndx[] = new int[MAX_TNKE+2];
        /*int32_t */int MaxIndx[] = new int[MAX_TNKE+2];
        /*uint16_t*/int Type[] = new int[MAX_TNKE+2];
        /*uint16_t*/int CustomType[] = new int[MAX_TNKE+2];
        /*uint16_t*/int PassFail[] = new int[MAX_TNKE+2];
        /*int16_t */int ACI[] = new int[MAX_TNKE+2];
        /*int32_t */int ReflectionLevel[] = new int[MAX_TNKE+2];
        /*int32_t */int InsertionLoss[] = new int[MAX_TNKE+2];
        /*int32_t */int StartLevel[] = new int[MAX_TNKE+2];
        /*int32_t */int LocStart_mm[] = new int[MAX_TNKE+2];
        /*int32_t */int LocMax_mm[] = new int[MAX_TNKE+2];
        /*int32_t */int LocEnd_mm[] = new int[MAX_TNKE+2];
        /*int32_t */int PW_mm[] = new int[MAX_TNKE+2];
        /*int32_t */int MaxLevel[] = new int[MAX_TNKE+2];
        /*int32_t */int EndLevel[] = new int[MAX_TNKE+2];
        /*int16_t */int SegmentID[] = new int[MAX_TNKE+2];
        /*int16_t */int Flags[] = new int[MAX_TNKE+2];
        /*uint16_t*/int SplitterRatio[] = new int[MAX_TNKE+2];
    };



// test types
    enum TestType_t{
        TEST_TYPE_NONE,
        TEST_TYPE_EXPERT,
        TEST_TYPE_FULLAUTO,
        TEST_TYPE_FILEOPEN,
        TEST_TYPE_REALTIME,
        TEST_TYPE_PMOTDR,
        TEST_TYPE_OTDRONLY,
        TEST_TYPE_ENDLOCATE,
        TEST_TYPE_PMOTDR_SPLITTER,
        TEST_TYPE_CHARACTERIZE,
        TEST_TYPE_LOCATE_END_AND_FAULTS
    }

    enum FiberType_t{
        FIBER_TYPE_SMF28E,
        FIBER_TYPE_USER1,
        FIBER_TYPE_USER2
    }

    enum PassFailType_t{
        PASSFAIL_TYPE_ITUG671,
        PASSFAIL_TYPE_TIA568_3D,
        PASSFAIL_TYPE_USER1,
    }

    enum NetworkType_t{
        NETWORK_TYPE_POINT_TO_POINT,
        NETWORK_TYPE_PON,
    }


    boolean IS_TEST_TYPE_VALID(int t){ return (t <= TEST_TYPE_LOCATE_END_AND_FAULTS);}
    boolean IS_FIBER_TYPE_VALID(int t){ return (t == FIBER_TYPE_SMF28E || t == FIBER_TYPE_USER1 || t == FIBER_TYPE_USER2);}
    boolean IS_PASSFAIL_TYPE_VALID(int t){ return (t == PASSFAIL_TYPE_ITUG671 || t == PASSFAIL_TYPE_TIA568_3D || t == PASSFAIL_TYPE_USER1);}
    boolean IS_NETWORK_TYPE_VALID(int t){ return (t == NETWORK_TYPE_POINT_TO_POINT || t == NETWORK_TYPE_PON);}


    /**
     * @brief  Sor file read and parse function
     * @param  fp     - file pointer,
     *         data   - unsigned 16-bit integer data buffer
     *         tp     - TestParams structure,
     *         Events - structure for KeyEvents
     * @retval signed 8-bit integer read operation result 0-Pass, 1-Fail
     */

    //int sorReadTraceData(FIL *fp, int data[], TestParamS * tp, EvEventsS * Events, int16_t wave)




    static int crc16_table[] = {
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
        0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
        0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
        0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
        0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
        0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
        0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
        0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
        0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
        0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
        0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
        0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
        0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
        0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
        0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
        0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
        0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
        0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
        0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
        0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
        0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
        0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
        0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
    };

/**
 * @brief  A CRC-CCITT, 16-bit checksum calculation function.
 * @param  crc - already calculated CRC value, buf - new data buffer, n - word count to calculate
 * @retval unsigned 16-bit integer - new crc value
 */
    static int sorCalcCrc(int crc,byte buf[], int n)
    {
        int i;
        //uint8_t hbit, ptr = ;
        int hbit;
        int table_idx;

        for (i = 0; i < n; i++ ) {
            hbit = (crc & 0xff00) >> 8;
            crc <<= 8;
            crc = (crc & 0xffff);
            table_idx = hbit ^ (buf[i] & 0xff);
            crc ^= crc16_table [table_idx];
        }
        return crc;
    }

    /**
     * @brief  A CRC-CCITT, 16-bit checksum check funtion, reads entire file and compares calculated CRC value with value from file.
     * @retval signed 8-bit integer CRC check result 0-Pass, 1-Fail
     */

    static int sorCheckCrc(File file) throws IOException {
        int crc,r_crc;
        long n, br;
        int r_crc_idx = 0;

        fis = null;
        fis = new FileInputStream(file);
        Log.d(TAG, "Total file size (in bytes) : " + fis.available());
        fsize = fis.available();

        fis.getChannel().position(0);

        if(fsize < 4)
        {
            return 1; /* return ERROR */
        }
        else
        {
            n = fsize;
            n >>= 1;
            n -= 1;
            r_crc_idx = (int) (fsize-2);
        }
        crc = 0xffff;

        //byte[] buf = new byte[(int)fsize];

        byte[] buf = new byte[32767];
        int r_bytes = 0;
        long rb_left = fsize-2;
        try{

            while( rb_left != 0){
                r_bytes = fis.read(buf, 0, buf.length);
                if(r_bytes != -1){
                    if(r_bytes >= rb_left) {
                        crc = sorCalcCrc(crc, buf, (int)rb_left);
                        rb_left -= rb_left;
                    }else {
                        crc = sorCalcCrc(crc, buf, r_bytes);
                        rb_left -= r_bytes;
                    }
                }
            }
        }catch (IOException e){
            Log.e(TAG, "CRC: Read error: " + e.getMessage() );
        }

        fis.getChannel().position(fsize-2);
        fis.read(buf, 0, 2);
        byte[] arr = new byte[] {buf[0],buf[1]};
        //ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
        //r_crc = bytesToShort(arr);//wrapped.getShort(); bytesToShort(arr);//
        r_crc = toInt(bytesToShort(arr));

        if(crc != r_crc)
        {
            return 1; /* return ERROR */
        }
        return 0;
    }

    public static short bytesToShort(byte[] bytes) {
        short retval = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        retval &= 0x0000FFFF;
        return retval;
    }
    public byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    // packing an array of 4 bytes to an int, big endian
    public static int bytesToInt(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }



    static int sorReadTraceData(File file) throws IOException {
        int error;
        int dw; //uint32_t
        int w; //uint16_t
        byte[] BlockName = new byte[20];
        //String BlockName = new String();
        byte[] BlockRev = new byte[2];
        byte[] BlockSize= new byte[4];
        byte[] bNumberOfBlocks = new byte[2];
        int AllSize;

        int flg_DataPts, flg_GenParams,flg_SupParams, flg_FxdParams, flg_FodParams,
                flg_Fod02Params, flg_Fod03Params, flg_Fod04Params, flg_Fod05Params, flg_KeyEvents;

        int size_DataPts = 0;
        int size_GenParams = 0;
        int size_SupParams = 0;
        int size_FxdParams = 0;
        int size_FodParams = 0;
        int size_Fod02Params = 0;
        int size_Fod03Params = 0;
        int size_Fod04Params = 0;
        int size_Fod05Params = 0;
        int size_KeyEvents = 0;

        int rev_DataPts = 0;
        int rev_GenParams = 0;
        int rev_SupParams = 0;
        int rev_FxdParams= 0;
        int rev_FodParams= 0;
        int rev_Fod02Params= 0;
        int rev_Fod03Params = 0; 
        int    rev_Fod04Params= 0;
        int    rev_Fod05Params= 0;
        int    rev_KeyEvents= 0;

        int EPT; 	/*int32_t Event Propagation Time */
        int ER; 	/*int32_t Event Reflectance */
        int EN; 	/*int16_t Event Number */
        int EL; 	/*int16_t Event Loss */
        int ACI;	/*int16_t Event Attenuation Coefficient */
        byte EC[] = new byte[7];	    /* Event Code */

        int fod_ver = 0;

        fis = new FileInputStream(file);

        error = sorCheckCrc(file);
        //error = 0;
        if(error != 0){
            fis.close();
            return 1;   /* return ERROR */
        }
        fis.getChannel().position(0);

        TestParamS.c.LQC_Result = 0; /* initialize LQC Result */
        TestParamS.EEACI = 0;
        TestParamS.DefaultACI = 0;
        TestParamS.c.RefreshTime = 100; /* 1 second */

        TestParamS.c.LinkPassFailThresholdsOnOff[0] = 0;
        TestParamS.c.EventPassFailThresholdsOnOff[0] = 0;
        TestParamS.c.LinkPassThrLength[0] = PFT_THRESH_DFLT_LINK_LENGTH;
        TestParamS.c.LinkPassThrLengthTolerance[0] = PFT_THRESH_DFLT_LINK_LENGTH_TOLERANCE;
        TestParamS.c.LinkPassThrLoss[0] = PFT_THRESH_DFLT_LINK_LOSS;
        TestParamS.c.LinkPassThrACI[0] = PFT_THRESH_DFLT_LINK_ACI;
        TestParamS.c.LinkPassThrORL[0] = PFT_THRESH_DFLT_LINK_ORL;
        TestParamS.c.EventPassThrConnLoss[0] = PFT_THRESH_DFLT_EVENT_CONN_LOSS;
        TestParamS.c.EventPassThrConnRefl[0] = PFT_THRESH_DFLT_EVENT_CONN_REFL;
        TestParamS.c.EventPassThrSpliceLoss[0] = PFT_THRESH_DFLT_EVENT_SPLICE_LOSS;
        TestParamS.c.EventPassThrSplitter1Ratio[0] = 2 << PFT_THRESH_DFLT_EVENT_SPLITTER1_RATIO_INDX;
        TestParamS.c.EventPassThrSplitter2Ratio[0] = 2 << PFT_THRESH_DFLT_EVENT_SPLITTER2_RATIO_INDX;
        TestParamS.c.EventPassThrSplitter1LossTolerance[0] = PFT_THRESH_DFLT_EVENT_SPLITTER1_LOSS_TOLERANCE;
        TestParamS.c.EventPassThrSplitter2LossTolerance[0] = PFT_THRESH_DFLT_EVENT_SPLITTER2_LOSS_TOLERANCE;
        TestParamS.c.EventPassThrEndRefl[0] = PFT_THRESH_DFLT_EVENT_END_REFL;
        TestParamS.c.EventPassThrACI[0] = PFT_THRESH_DFLT_EVENT_ACI;

        TestParamS.c.LinkPassFailThresholdsOnOff[1] = 0;
        TestParamS.c.EventPassFailThresholdsOnOff[1] = 0;
        TestParamS.c.LinkPassThrLength[1] = PFT_THRESH_DFLT_LINK_LENGTH;
        TestParamS.c.LinkPassThrLengthTolerance[1] = PFT_THRESH_DFLT_LINK_LENGTH_TOLERANCE;
        TestParamS.c.LinkPassThrLoss[1] = PFT_THRESH_DFLT_LINK_LOSS;
        TestParamS.c.LinkPassThrACI[1] = PFT_THRESH_DFLT_LINK_ACI;
        TestParamS.c.LinkPassThrORL[1] = PFT_THRESH_DFLT_LINK_ORL;
        TestParamS.c.EventPassThrConnLoss[1] = PFT_THRESH_DFLT_EVENT_CONN_LOSS;
        TestParamS.c.EventPassThrConnRefl[1] = PFT_THRESH_DFLT_EVENT_CONN_REFL;
        TestParamS.c.EventPassThrSpliceLoss[1] = PFT_THRESH_DFLT_EVENT_SPLICE_LOSS;
        TestParamS.c.EventPassThrSplitter1Ratio[1] = 2 << PFT_THRESH_DFLT_EVENT_SPLITTER1_RATIO_INDX;
        TestParamS.c.EventPassThrSplitter2Ratio[1] = 2 << PFT_THRESH_DFLT_EVENT_SPLITTER2_RATIO_INDX;
        TestParamS.c.EventPassThrSplitter1LossTolerance[1] = PFT_THRESH_DFLT_EVENT_SPLITTER1_LOSS_TOLERANCE;
        TestParamS.c.EventPassThrSplitter2LossTolerance[1] = PFT_THRESH_DFLT_EVENT_SPLITTER2_LOSS_TOLERANCE;
        TestParamS.c.EventPassThrEndRefl[1] = PFT_THRESH_DFLT_EVENT_END_REFL;
        TestParamS.c.EventPassThrACI[1] = PFT_THRESH_DFLT_EVENT_ACI;

        TestParamS.c.EventPassThrSplitterRefl = PFT_THRESH_DFLT_EVENT_SPLITTER_REFL;

        TestParamS.LinkPassFailResult = 0;


        f_gets(BlockName, 5);
        if (!((BlockName[0] == 'M') && (BlockName[1] == 'a') && (BlockName[2] == 'p')))
        {
            fis.getChannel().position(0);/* if no 'Map\0' BLOCK ID then it's SOR1, so read from beginning */
        }

        fis.read(BlockRev, 0, BlockRev.length);/* Map Block revision */
        fis.read(BlockSize, 0, BlockSize.length);/* Map Block size */
        fis.read(bNumberOfBlocks, 0, bNumberOfBlocks.length);/* Number of Blocks */
        int NumberOfBlocks = bytesToShort(bNumberOfBlocks);

        if (bytesToInt(BlockSize) > MAX_BLOCKSIZE) return 1;
        AllSize = bytesToInt(BlockSize);

        flg_DataPts   = 0;
        flg_GenParams = 0;
        flg_SupParams = 0;
        flg_FxdParams = 0;
        flg_FodParams = 0;
        flg_Fod02Params = 0;
        flg_Fod03Params = 0;
        flg_Fod04Params = 0;
        flg_Fod05Params = 0;
        flg_KeyEvents = 0;

        for (int i = 0; i < NumberOfBlocks - 1; i++)
        {

            String strBlockName = f_gets();;

            fis.read(BlockRev, 0, BlockRev.length);  /* Block Revision No */
            fis.read(BlockSize, 0, BlockSize.length); /* Block size */

            if (bytesToInt(BlockSize) > MAX_BLOCKSIZE) return 1;


            if (strBlockName.compareTo("DataPts") == 0){
                flg_DataPts = 1;
                size_DataPts = AllSize;
                rev_DataPts = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("GenParams") == 0){
                flg_GenParams = 1;
                rev_GenParams = bytesToShort(BlockRev);
                size_GenParams = AllSize;
            }else if (strBlockName.compareTo("SupParams") == 0){
                flg_SupParams = 1;
                size_SupParams = AllSize;
                rev_SupParams = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("FxdParams") == 0){
                flg_FxdParams = 1;
                size_FxdParams = AllSize;
                rev_FxdParams = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("FodParams") == 0){
                flg_FodParams = 1;
                size_FodParams = AllSize;
                rev_FodParams = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("Fod02Params") == 0){
                flg_Fod02Params = 1;
                size_Fod02Params = AllSize;
                rev_Fod02Params = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("Fod03Params") == 0){
                flg_Fod03Params = 1;
                size_Fod03Params = AllSize;
                rev_Fod03Params = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("Fod04Params") == 0){
                flg_Fod04Params = 1;
                size_Fod04Params = AllSize;
                rev_Fod04Params = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("Fod05Params") == 0){
                flg_Fod05Params = 1;
                size_Fod05Params = AllSize;
                rev_Fod05Params = bytesToShort(BlockRev);
            }else if (strBlockName.compareTo("KeyEvents") == 0){
                flg_KeyEvents = 1;
                size_KeyEvents = AllSize;
                rev_KeyEvents = bytesToShort(BlockRev);
            }

            AllSize = AllSize + bytesToInt(BlockSize);
        }

        if (flg_DataPts == 1)
        {
            /* read data points */
            fis.getChannel().position(size_DataPts);
            if (rev_DataPts >= 200){
                fis.getChannel().position(fis.getChannel().position()+8);
            }
            TestParamS.c.TNDP = f_read_int32();  /* Total Number of Data Points */
            if (TestParamS.c.TNDP > MAX_TNDP){
                TestParamS.c.TNDP = MAX_TNDP;
            }
            fis.getChannel().position(fis.getChannel().position()+8);
            int DataSize = TestParamS.c.TNDP;
            int DataPointIdx = 0;
            sk = new int[TestParamS.c.TNDP];
            //int skIdx = 0;
            while(DataSize-- != 0){
                //data[DataPointIdx++] = f_read_unt16(); /* read data points */
                //sk[skIdx] = toInt((short)data[skIdx]);
                //skIdx++;
                sk[DataPointIdx++] = toInt((short) f_read_unt16());
            }
        }

        if (flg_GenParams == 1)
        {
            fis.getChannel().position(size_GenParams + 2);

            if (rev_GenParams >= 0xC8) {
                fis.getChannel().position(fis.getChannel().position()+10);
            }

            TestParamS.CID = f_gets();
            TestParamS.FID = f_gets();

            if (rev_GenParams >= 0xC8){
                fis.getChannel().position(fis.getChannel().position()+2); /* Fiber Type */
            }
            TestParamS.NW = f_read_unt16(); /* Nominal Wavelength */

            TestParamS.OL = f_gets();
            TestParamS.TL = f_gets();
            TestParamS.CCD = f_gets();

            fis.getChannel().position(fis.getChannel().position()+2);
            int uo = f_read_int32(); /* User Offset */
            TestParamS.UO = uo; /* User Offset */
        }

        if (flg_SupParams == 1)
        {
            fis.getChannel().position(size_SupParams);
            if (rev_SupParams >= 0xC8) {
                fis.getChannel().position(fis.getChannel().position()+10);
            }

            TestParamS.SN = f_gets();
            TestParamS.MFID = f_gets();
            TestParamS.OTDR = f_gets();
            TestParamS.OMID = f_gets();
            TestParamS.OMSN = f_gets();
            TestParamS.SR = f_gets();
            TestParamS.OT = f_gets();
        }

        if (flg_FxdParams == 1)
        {
            int it;
            if (rev_FxdParams >= 0xC8) {
                fis.getChannel().position(size_FxdParams + 10);
            }else {
                fis.getChannel().position(size_FxdParams);
            }
            dw = f_read_int32();
            TestParamS.TimeStamp = dw;
            w = f_read_unt16();                 /* Units of Distance */
            w = f_read_unt16();                 /* Actual Wavelength */
            TestParamS.c.AO = f_read_int32();   /* Acquisition Offset */
            if (rev_FxdParams >= 0xC8){
                fis.getChannel().position(fis.getChannel().position() + 4);/* Acquisition Offset Distance */
            }
            TestParamS.c.TPW = f_read_unt16(); /* Total Number of Pulse Widths Used */

            if (TestParamS.c.TPW  > 1){
                return 1;
            }

            w = f_read_unt16();            /* Pulse Widths Used */

            if ((w==101)||(w==301)||(w==1001)||(w==3001)) w--;
            TestParamS.c.PWU = w;
            //it = hostifPwCheck(TestParamS.c.PWU);
            //if(it == 0){return 1;}                 /* return ERROR */

            //TestParamS.c.Test_PwIndx = it-1;
            TestParamS.c.Test_PwIndx = 0;

            if (TestParamS.c.TPW > 2)
            {
                fis.getChannel().position(fis.getChannel().position() + 2 * (TestParamS.c.TPW - 1));
            }
            TestParamS.c.DS = f_read_int32();             /* Data Spacing */

            if (TestParamS.c.TPW > 2)
            {
                fis.getChannel().position(fis.getChannel().position() + 4 * (TestParamS.c.TPW - 1));
            }
            TestParamS.c.NPPW = f_read_int32();        /* Number of Data Points for Each Pulse Width */

            if (TestParamS.c.TPW > 2)
            {
                fis.getChannel().position(fis.getChannel().position() + 4 * (TestParamS.c.TPW - 1));
            }

            TestParamS.GI = f_read_int32();           /* Group Index */
            TestParamS.BC = f_read_unt16();          /* Backscater Coefficient */
            TestParamS.NAV = f_read_int32();         /* Number of Averages */
            if (rev_FxdParams >= 0xC8){
                fis.getChannel().position(fis.getChannel().position() + 2); /* Acquisition Time */
            }
            TestParamS.c.AR = f_read_int32();           /* Acquisition Range */
            if (rev_FxdParams >= 0xC8){
                fis.getChannel().position(fis.getChannel().position() + 4);/* Acquisition Range Distance */
            }
            TestParamS.FPO = f_read_int32();          /* Front Panel Offset */
            TestParamS.NF = f_read_unt16();          /* Noise Floor Level */
            fis.getChannel().position(fis.getChannel().position() + 4);
            TestParamS.LT = f_read_unt16();          /* Loss Threshold */
            TestParamS.RT = f_read_unt16();          /* Reflectance Threshold */
            TestParamS.ET = f_read_unt16();           /* End-of-Fiber Threshold */
        }

        if (flg_FodParams == 1) {
        /* Fod Parameters Block */
            fis.getChannel().position(size_FodParams);
            if (rev_FodParams >= 0xC8){
                fis.getChannel().position(fis.getChannel().position() + 10);
            }

            w = f_read_unt16();            /* Magic number */
            if(w != BELL_FODPARAMS_MAGIC)
            {
                return 0;                              /* return */
            }
            w = f_read_unt16();            /* Block Version */
            if(w >= 6) {
                fod_ver = w;
                //BellFodParamsVerOK = 1;
            }
            else{ return 1; /* return ERROR */
            }
            w = f_read_unt16();            /* Resolution */
            if (fod_ver >= 0x0010){
                TestParamS.c.Resolution = w;
            }else{
                TestParamS.c.Resolution = 0xffff;
            }

            //i = testPFilterIndxCheck(w);
            //if(i==0){return 1;}                      /* return ERROR */
            //tp->Test_FilterIndx = w;
            w = f_read_unt16();          /* Average time in 100 ms */
            //i = testPAvgTimeCheck(w);
            //if(i==0){return 1;}                      /* return ERROR */
            //tp->Test_AvgIndx = i-1;
            TestParamS.c.TestTime = w;
            w = f_read_unt16();            /* Test Range Index */
            //i = testPRangeIndxCheck(w);
            //if(i==0){return 1;}                      /* return ERROR */
            TestParamS.c.Test_RangeIndx = w;
            w = f_read_unt16();           /* Test Mode */
            //i = testPTestModeCheck(w);
            //if(i==0){return 1;}                      /* return ERROR */
            TestParamS.c.Test_Mode = w;
            w = f_read_unt16();           /* Event Mode */
            //i = testPEventModeCheck(w);
            //if(i==0){return 1;}                      /* return ERROR */
            TestParamS.c.Event_Mode = w;
            w = f_read_unt16();          /* Front Panel Offset */
            //i = testPFrontPanelOffsetCheck(dw);
            //if(i==0){return 1;}                      /* return ERROR */
            //tp->c->FPO = dw;
            //BellFodParamsFPO = dw/2;
            dw = f_read_int32();          /* Launch Cable */
            //i = testPLaunchCableCheck(dw);
            //if(i==0){return 1;}                      /* return ERROR */
            TestParamS.c.Cable_Launch_cm = dw;
            dw = f_read_int32();         /* Receive Cable */
            //i = testPReceiveCableCheck(dw);
            //if(i==0){return 1;}                      /* return ERROR */
            TestParamS.c.Cable_Receive_cm = dw;

            if (fod_ver >= 7)
            {
                w = f_read_unt16();          /* Live Fiber */
                TestParamS.c.Live_Fiber = w;

                if (fod_ver >= 8)
                {
                    int indx;
                    ldiv_t res;
                    dw = f_read_int32(); /* Marker 1 location in 100 ps */
                    dw += TestParamS.FPO + TestParamS.UO;
                    dw *= 100;
                    res = ldiv(dw, (TestParamS.c.DS / 100));
                    indx = (int) res.quot;
                    if(res.rem == 1){
                        indx++;
                    }

                    TestParamS.c.marker1_val = indx;

                    dw = f_read_int32();  /* Marker 2 location in 100 ps */
                    dw += TestParamS.FPO + TestParamS.UO;
                    dw *= 100;
                    res = ldiv(dw, (TestParamS.c.DS / 100));
                    indx = (int) res.quot;
                    if(res.rem == 1)
                    {
                        indx++;
                    }
                    TestParamS.c.marker2_val = indx;

                    if (fod_ver >= 17)
                    {
                        w = f_read_unt16();    /* LQC Result */
                        TestParamS.c.LQC_Result = w;

                        if (fod_ver >= 18)
                        {
                            w = f_read_unt16();  /* Network type */
                            TestParamS.c.Network_Type = w;
                            w = f_read_unt16();  /* Pass/Fail type */
                            TestParamS.c.PassFail_Type = w;
                            if (fod_ver >= 19)
                            {
                                w = f_read_unt16();  /* End to End ACI */
                                TestParamS.EEACI = w;
                                if (fod_ver >= 20){
                                    w = f_read_unt16();  /* Default ACI */
                                    TestParamS.DefaultACI = w;
                                }
                            }
                        }
                        else
                        {
                            TestParamS.c.Network_Type = 0;
                            TestParamS.c.PassFail_Type = 0;
                        }
                    }
                }
                else{
                    TestParamS.c.marker1_val = 0;
                    TestParamS.c.marker2_val = 0;
                }
            }
            else
            {
                TestParamS.c.Live_Fiber = 0;
            }
        }

        TestParamS.EEL = 0;
        TestParamS.ELMP1 = 0;
        TestParamS.ELMP2 = 0;
        TestParamS.ORL = 0;
        TestParamS.RLMP1 = 0;
        TestParamS.RLMP2 = 0;

        if ((flg_KeyEvents == 1) && (Events != null))
        {
            EvEventPS ev;
            int indx; //int32_t
            ldiv_t res;

            fis.getChannel().position(size_KeyEvents);
            if (rev_KeyEvents >= 0xC8){
                fis.getChannel().position(fis.getChannel().position() + 10);
            }
            TestParamS.TNKE = f_read_unt16();               /* Number of Key Events */
            if (TestParamS.TNKE > MAX_TNKE){return 1;}
            Events.N = 0;

            for (int i = 0; i < TestParamS.TNKE; i++)
            {
                EN = f_read_unt16(); /* Event Number */
                EPT = f_read_int32();                     /* Event Propagation Time */
                ACI = f_read_unt16();                     /* Event Attenuation Coefficient */
                EL = f_read_unt16();                       /* Event Loss */
                ER = f_read_int32();                       /* Event Reflectance */

                fis.read(EC,0,EC.length);
                fis.getChannel().position(fis.getChannel().position() + 2);
                if (rev_KeyEvents >= 0xC8){
                    fis.getChannel().position(fis.getChannel().position() + 20); /*Marker locations*/
                }

                f_gets();

                //EPT += tp->c->FPO + tp->UO;
                //EPT *= 100;
                res = ldiv(EPT, (TestParamS.c.DS / 100));
                indx = (int) res.quot;
                if(res.rem == 1)
                {
                    indx++;
                }

                //ev.LocStart_mm = SamplesToRangemm(indx - (tp->c->FPO + tp->UO)*10000/tp->c->DS, tp->c->DS, tp->GI);
                Events.LocStart_mm[i] = Time100psToRangemm(2*(EPT + TestParamS.UO), TestParamS.GI);
                Events.LocEnd_mm [i] =  Events.LocStart_mm[i];
                Events.LocationIndx[i] =  indx;
                Events.InsertionLoss [i] =  EL;
                Events.ReflectionLevel [i] =  ER;
                Events.ACI [i] =  ACI;
                Events.Type [i] =  (EC[1] << 8) + EC[0];
                Events.PassFail [i] =  0;
                Events.PW_mm [i] =  0;
                Events.SplitterRatio [i] =  0;

                if (EN == i + 1)
                {
                    Events.N++;
                }
                else
                {
                    break;
                }
            }
            TestParamS.EEL = f_read_int32();     /* End to End loss (EEL) */
            TestParamS.ELMP1 = f_read_int32(); /* EEL marker 1 */
            TestParamS.ELMP2 = f_read_int32(); /* EEL marker 2 */
            TestParamS.ORL = f_read_unt16();     /* optical return loss (ORL) */
            TestParamS.RLMP1 = f_read_int32(); /* ORL marker 1 */
            TestParamS.RLMP2 = f_read_int32(); /* ORL marker 2 */

            /*FOD02Params start finish*/
            if ((flg_Fod02Params==1) && (Events != null))
            {
                int n_ev, ev_code; //uint16_t
                //int EN; //uint16_t

            /* Fod Parameters Block */
                fis.getChannel().position(size_Fod02Params);
                if (rev_Fod02Params >= 0xC8){
                    fis.getChannel().position(fis.getChannel().position() + 12);
                }

                w = f_read_unt16();         /* Number of events */
                n_ev = w;

                for(int i = 0; i < n_ev; i++)
                {
                    w = f_read_unt16();           /* Event number */
                    EN = w;
                    dw = f_read_int32();          /* Event Tail Propagation Time*/
                    EPT = dw;
                    //EPT += tp->c->FPO + tp->UO;
                    //EPT *= 100;
                    res = ldiv(EPT, (TestParamS.c.DS/100));
                    indx = (int) res.quot;
                    if(res.rem == 1){indx++;}

                    w = f_read_unt16();            /* Custom Event Code */
                    ev_code = w;

                    if (EN-1 < TestParamS.TNKE){
                        Events.EndIndx[EN-1] = indx;
                        Events.LocEnd_mm[EN-1] = Time100psToRangemm(2*(EPT + TestParamS.UO), TestParamS.GI);
                        //Events.LocEnd_mm[EN-1] = SamplesToRangemm(indx - (tp->c->FPO + tp->UO)*10000/tp->c->DS, tp->c->DS, tp->GI);
                        if (fod_ver < 21){
                            switch(ev_code){
                                case EV_CODE_MB:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_MACROBEND;
                                    break;
                                case EV_CODE_SP:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_SPLITTER;
                                    break;
                                case EV_CODE_GS:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_GR_START;
                                    break;
                                case EV_CODE_GM:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_GR_MIDDLE;
                                    break;
                                case EV_CODE_GE:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_GR_END;
                                    break;
                                case EV_CODE_ST:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_LINK_START;
                                    break;
                                case EV_CODE_SG:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_LINK_START|EV_CTYPEN_MSK_GR_START;
                                    break;
                                case EV_CODE_EG:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_GR_END;
                                    break;
                                case EV_CODE_EN:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_LINK_END;
                                    break;
                                case EV_CODE_MS:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_MACROBEND|EV_CTYPEN_MSK_SPLITTER;
                                    break;
                                default:
                                    Events.CustomType[EN-1] = EV_CTYPEN_MSK_SIMPLE;
                                    break;
                            }
                        }else{
                            Events.CustomType[EN-1] = ev_code;
                        }
                    }
                }
            }
    /*FOD02Params read finish*/

            if ((flg_Fod03Params == 1) && (Events != null))
            {
      /* Fod Parameters Block */
                fis.getChannel().position(fis.getChannel().position() + size_Fod03Params);
                if (rev_Fod03Params >= 0xC8){
                    fis.getChannel().position(fis.getChannel().position() + 12);
                }
                w = f_read_unt16();           /* MB Fixed Part */
                w = f_read_unt16();            /* MB GIR Error */
                w = f_read_unt16();            /* MB Pulse Rounding*/
                w = f_read_unt16();            /* MB Digitizing Error */
                w = f_read_unt16();            /* MB Variable Part */
                w = f_read_unt16();            /* Splitter Detection Threshold*/
                w = f_read_unt16();            /* MB Setting */
                TestParamS.c.MacrobendDetection = w;
            }

            if ((flg_Fod05Params == 1) && (Events != null))
            {
                int n_ev; //uint16_t
                int ev_ratio; //uint16_t

                /* Fod Parameters Block */
                fis.getChannel().position(size_Fod05Params);

                if (rev_Fod05Params >= 0xC8) {
                    fis.getChannel().position(fis.getChannel().position() + 12);
                }

                w = f_read_unt16();            /* Magic number */
                if(w != BELL_FODPARAMS_MAGIC)
                {
                    return 0;                              /* return */
                }
                w = f_read_unt16();           /* Block Version */
                if(w >= 0x0001)
                {
                    fod_ver = w;
                }
                else{
                    return 1;                           /* return ERROR */
                }

                for (int i = 0 ; i < 12; i++){
                    TestParamS.EventPassThrSplitterLossMin[i] = f_read_unt16();
                    TestParamS.EventPassThrSplitterLossMax[i] = f_read_unt16();
                }
                TestParamS.c.EventPassThrSplitterRefl = f_read_unt16();

      /* PON config */
                for(int i = 0; i < 3; i++){
                    TestParamS.c.ev_spl_cfg.r1[i] = f_read_unt16();
                    TestParamS.c.ev_spl_cfg.r2[i] = f_read_unt16();
                }

                w = f_read_unt16();            /* Number of events */
                n_ev = w;
                for(int i = 0; i < n_ev; i++)
                {
                    w = f_read_unt16();          /* Event number  */
                    EN = w;
                    w = f_read_unt16();         /* Event Pass/Fail Status*/
                    ev_ratio = w;
                    if (EN-1 < TestParamS.TNKE){
                        Events.SplitterRatio[EN-1] = ev_ratio & 0x7ff;
                    }
                }
            }else{
                //pf_assign_splitter_ratios(tp, Events);
                //pftFillThrToTestParams(tp);
            }

            if ((flg_Fod04Params == 1) && (Events != null))
            {
                int n_ev; //uint16_t
                int ev_passfail; //uint16_t
                /* Fod Parameters Block */
                fis.getChannel().position(size_Fod04Params);
                if (rev_Fod04Params >= 0xC8){
                    fis.getChannel().position(fis.getChannel().position() + 12);
                }

                w = f_read_unt16();           /* Magic number */
                if(w != BELL_FODPARAMS_MAGIC)
                {
                    return 0;                              /* return */
                }
                w = f_read_unt16();           /* Block Version */
                if(w >= 0x0001)
                {
                    fod_ver = w;
                    //BellFodParamsVerOK = 1;
                }else{
                    return 1;                            /* return ERROR */
                }

                w = f_read_unt16();           /* Link Length P/F */
                TestParamS.LinkPassFailResult = w & 3;
                w = f_read_unt16();           /* Link Loss P/F  */
                TestParamS.LinkPassFailResult |= (w << 2);
                w = f_read_unt16();            /* Link Loss/Dist. P/F  */
                TestParamS.LinkPassFailResult |= (w << 4);
                w = f_read_unt16();            /* Link ORL P/F  */
                TestParamS.LinkPassFailResult |= (w << 6);

                if (fod_ver >= 0x0002)
                {
                    w = f_read_unt16();           /* Link Pass Thresholds On/off */
                    TestParamS.c.LinkPassFailThresholdsOnOff[0] = w;
                    w = f_read_unt16();           /* Event Pass Fail Thresholds On/Off */
                    TestParamS.c.EventPassFailThresholdsOnOff[0] = w;
                    dw = f_read_int32();          /* Link Length Threshold */
                    TestParamS.c.LinkPassThrLength[0] = dw;
                    w = f_read_unt16();            /* Link Length Tolerance Threshold */
                    TestParamS.c.LinkPassThrLengthTolerance[0] = w;
                    w = f_read_unt16();            /* Link Loss Threshold */
                    TestParamS.c.LinkPassThrLoss[0] = w;
                    w = f_read_unt16();            /* Link Loss/Distance Threshold */
                    TestParamS.c.LinkPassThrACI[0] = w;
                    w = f_read_unt16();            /* Link ORL Threshold */
                    TestParamS.c.LinkPassThrORL[0] = w;
                    w = f_read_unt16();            /* Event Conn. Loss Threshold */
                    TestParamS.c.EventPassThrConnLoss[0] = w;
                    w = f_read_unt16();            /* Event Conn. Refl. Threshold */
                    TestParamS.c.EventPassThrConnRefl[0] = w;
                    w = f_read_unt16();            /* Event Splice Loss Threshold */
                    TestParamS.c.EventPassThrSpliceLoss[0] = w;
                    w = f_read_unt16();            /* Event Splitter 1 Ratio */
                    TestParamS.c.EventPassThrSplitter1Ratio[0] = w;
                    w = f_read_unt16();            /* Event Splitter 2 Ratio */
                    TestParamS.c.EventPassThrSplitter2Ratio[0] = w;
                    w = f_read_unt16();           /* Event Splitter 1 Loss Tolerance */
                    TestParamS.c.EventPassThrSplitter1LossTolerance[0] = w;
                    w = f_read_unt16();            /* Event Splitter 2 Loss Tolerance */
                    TestParamS.c.EventPassThrSplitter2LossTolerance[0] = w;
                    w = f_read_unt16();            /* Event End Refl. Threshold */
                    TestParamS.c.EventPassThrEndRefl[0] = w;
                    w = f_read_unt16();           /* Event Fiber Section Loss/Dist. (ACI) Threshold */
                    TestParamS.c.EventPassThrACI[0] = w;

                    if (fod_ver >= 0x0003){
                        w = f_read_unt16();            /* Link Fault Thresholds On/off */
                        TestParamS.c.LinkPassFailThresholdsOnOff[1] = w;
                        w = f_read_unt16();            /* Event Fault Thresholds On/Off */
                        TestParamS.c.EventPassFailThresholdsOnOff[1] = w;
                        dw = f_read_int32();          /* Link Length Fault Threshold */
                        TestParamS.c.LinkPassThrLength[1] = dw;
                        w = f_read_unt16();            /* Link Length Tolerance Fault Threshold */
                        TestParamS.c.LinkPassThrLengthTolerance[1] = w;
                        w = f_read_unt16();            /* Link Loss Fault Threshold */
                        TestParamS.c.LinkPassThrLoss[1] = w;
                        w = f_read_unt16();            /* Link Loss/Distance Fault Threshold */
                        TestParamS.c.LinkPassThrACI[1] = w;
                        w = f_read_unt16();            /* Link ORL Fault Threshold */
                        TestParamS.c.LinkPassThrORL[1] = w;
                        w = f_read_unt16();            /* Event Conn. Loss Fault Threshold */
                        TestParamS.c.EventPassThrConnLoss[1] = w;
                        w = f_read_unt16();          /* Event Conn. Refl. Fault Threshold */
                        TestParamS.c.EventPassThrConnRefl[1] = w;
                        w = f_read_unt16();          /* Event Splice Loss Fault Threshold */
                        TestParamS.c.EventPassThrSpliceLoss[1] = w;
                        w = f_read_unt16();            /* Event Splitter 1 Ratio */
                        TestParamS.c.EventPassThrSplitter1Ratio[1] = w;
                        w = f_read_unt16();           /* Event Splitter 2 Ratio */
                        TestParamS.c.EventPassThrSplitter2Ratio[1] = w;
                        w = f_read_unt16();            /* Event Splitter 1 Loss Fault Tolerance */
                        TestParamS.c.EventPassThrSplitter1LossTolerance[1] = w;
                        w = f_read_unt16();            /* Event Splitter 2 Loss Fault Tolerance */
                        TestParamS.c.EventPassThrSplitter2LossTolerance[1] = w;
                        w = f_read_unt16();            /* Event End Refl. Fault Threshold */
                        TestParamS.c.EventPassThrEndRefl[1] = w;
                        w = f_read_unt16();            /* Event Fiber Section Loss/Dist. (ACI) Fault Threshold */
                        TestParamS.c.EventPassThrACI[1] = w;

                        fis.getChannel().position(fis.getChannel().position() + 60);
                    }else{
                        fis.getChannel().position(fis.getChannel().position() + 94);
                    }
                }else{
                    fis.getChannel().position(fis.getChannel().position() + 128);
                }
                w = f_read_unt16();            /* Number of events */
                n_ev = w;
                for(int i = 0; i < n_ev; i++){
                    w = f_read_unt16();          /* Event number  */
                    EN = w;
                    w = f_read_unt16();          /* Event Pass/Fail Status*/
                    ev_passfail = w;
                    if (EN-1 < TestParamS.TNKE){
                        Events.PassFail[EN-1] = ev_passfail;
                    }
                }
            }
        }

        TestParamS.TraceValid = 1;

        return 0;
    }

    static int f_read_unt16() throws IOException {
        byte[] byteData= new byte[2];
        fis.read(byteData,0, byteData.length);
        return bytesToShort(byteData);
    }
    
    static int f_read_int32() throws IOException {
        byte[] byteData= new byte[4];
        fis.read(byteData,0, byteData.length);
        return bytesToInt(byteData);
    }

    static void f_gets(byte str[], int len) throws IOException {
        int ch = 0;
        while (len != 0){
            byte[] b = new byte[1];
            fis.read(b);
            str[ch++] = b[0];
            len--;
            if(b[0] == '\0'){
                str[ch++] = '\n';
                return;
            }
        }
    }


    int hostifPwCheck(int pw)
    {
        int i;
        int hostif_pws[] = {3, 5, 10, 20, 25, 30, 50, 100, 200, 300, 500, 1000, 2000, 3000, 5000, 10000, 20000};

        for(i = 0; i < 17; i++){
            if(hostif_pws[i] == pw){
                return i+1;
            }
        }

        return 0;
    }

    static String f_gets() throws IOException {
        String str = "";
        byte[] b = new byte[1];
        for(;;)  {
            fis.read(b);
            if(b[0] == '\0'){
                return str;
            }
            str += (char) b[0];
        }
    }

    static ldiv_t ldiv(long numer, long denom){
        ldiv_t ret_val = new ldiv_t();
        ret_val.quot = numer/denom;
        ret_val.rem = numer % denom;
        return ret_val;
    }


    /* two way time -> mm */
    static int Time100psToRangemm(int t, int gir)
    {
        double mm;
        mm = ((LightVelocity * 100.0)/gir) * t;
        return (int) Math.round(mm);
    }


    static String getEventName(int ev_nr){
        String str = "";
        Boolean have_type = false;
        if(Events.CustomType[ev_nr] == 0){
            if((Events.Type[ev_nr] & 0x00FF)== 0x30){
                str = "SPLICE";
            }else{
                str = "CONNECTOR";
            }
        }else{
            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_MACROBEND) != 0){
                str += "MACROBEND";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_SPLITTER) != 0){
                if(have_type){ str += ", "; }
                str += "SPLITTER";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_GR_START) != 0){
                if(have_type){ str += ", "; }
                str += "MACROBEND";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_GR_MIDDLE) != 0){
                if(have_type){ str += ", "; }
                str += "GR_MIDDLE";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_GR_END) != 0){
                if(have_type){ str += ", "; }
                str += "GR_END";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_LINK_START) != 0){
                if(have_type){ str += ", "; }
                str += "LINK_START";
                have_type = true;
            }

            if((Events.CustomType[ev_nr] & EV_CTYPEN_MSK_LINK_END) != 0){
                if(have_type){ str += ", "; }
                str += "LINK_END";
            }
        }


        return str;
    }

    static String getLen_KM(int ev_nr){
        String str = "";
        double l = Events.LocStart_mm[ev_nr]*0.000001;
        str = String.format("%.05f", l);
        return str;
    }

    static String getSectionLen_KM(int ev_nr){
        String str = "";
        double l =  (Events.LocStart_mm[ev_nr+1] - Events.LocStart_mm[ev_nr]) * 0.000001;
        str = String.format("%.05f", l);
        return str;
    }

    static String getLost(int ev_nr){
        String str = " - ";
        if(Events.InsertionLoss[ev_nr] != 0) {
            double l = Events.InsertionLoss[ev_nr] * 0.001 ;
            str = String.format("%.03f", l);
        }
        return str;
    }

    static String getSectiontLost(int ev_nr){
        String str = " - ";
        if(Events.ACI[ev_nr+1] != 0) {
            double len =  (Events.LocStart_mm[ev_nr+1] - Events.LocStart_mm[ev_nr]) * 0.000001;
            double l = (len*Events.ACI[ev_nr+1]) * 0.001;
            str = String.format("%.03f", l);
        }
        return str;
    }

    static String getRef(int ev_nr){
        String str = " - ";
        if(Events.ReflectionLevel[ev_nr] != 0) {
            double l = Events.ReflectionLevel[ev_nr] * 0.001;
            str = String.format("%.03f", l);
        }
        return str;
    }

    static String getACI(int ev_nr){
        String str = " - ";
        if(Events.ACI[ev_nr] != 0) {
            double l = Events.ACI[ev_nr] * 0.001;
            str = String.format("%.03f", l);
        }
        return str;
    }

    static String getDeviceId(){
        String str = " - ";
        //if(TestParamS. != 0) {
        //    double l = Events.ACI[ev_nr] * 0.001;
        //    str = String.format("%.03f", l);
        //}
        return str;
    }

    public static byte[] toBytes(short s) {
        return new byte[]{0, 0, (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF)};
    }

    public static int toInt(short short_arr) {
        byte[] arr = toBytes(short_arr);
        //ByteBuffer buf = ByteBuffer.wrap(arr); // big-endian by default
        ByteBuffer buf = ByteBuffer.wrap(arr);
        return buf.getInt();
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        String date = "";
        try{
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(timestamp * 1000L);
            date = DateFormat.format("yyyy-MM-dd hh:mm:ss", cal).toString();
        }catch (Exception e) {
        }
        return date;
    }
}
