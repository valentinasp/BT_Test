package com.fods.psp_bt;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Locale;

import static com.fods.psp_bt.ClassPdfCreater.fvalue_t.value_less;
import static com.fods.psp_bt.ClassPdfCreater.fvalue_t.value_more;
import static com.fods.psp_bt.SOR.TestParamS;
import static com.fods.psp_bt.SOR.getACI;
import static com.fods.psp_bt.SOR.getEventName;
import static com.fods.psp_bt.SOR.getLen_KM;
import static com.fods.psp_bt.SOR.getLost;
import static com.fods.psp_bt.SOR.getRef;
import static com.fods.psp_bt.SOR.getSectionLen_KM;
import static com.fods.psp_bt.SOR.getSectiontLost;

/**
 * Created by Valentinas on 2017-12-07.
 */

public final class ClassPdfCreater {
    private float left, right, top, bottom;
    private float width, height;
    private float padding;
    private float x,y;
    private float top_rect;

    private PDDocument document;
    private PDPage page;
    private PDAcroForm acroForm;
    private PDPageContentStream contentStream;
    //private MainActivity mActivity;

    private PDType0Font font_roboto_regular;
    private PDType0Font font_roboto_bold;
    private PDType0Font font_fontello;
    //static int array_size = 100000;
    //public static int[] sk = Generator.generateRandom(array_size);


    public void InitFonts() throws IOException{

        AssetManager assetManager = MainActivity.getInstance().getAssets();
        String[] file_list = assetManager.list("Fonts");

        System.out.println("list:" + Arrays.toString(file_list));
        font_roboto_regular  = PDType0Font.load(document, assetManager.open("Fonts/Roboto-Regular.ttf"));
        font_roboto_bold     = PDType0Font.load(document, assetManager.open("Fonts/Roboto-Bold.ttf"));
        font_fontello        = PDType0Font.load(document, assetManager.open("Fonts/fontello.ttf"));

    }

    private void DoHeader0() throws IOException{

        //Block 1

        //Bitmap fodIcon = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.fod_icon);
        Bitmap fodIcon = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.logo_afl_90_45);


        {
            float block_height = fodIcon.getHeight();
            y = top - block_height;
            float block_bottom_padding = 11;

            PDImageXObject fodIconPdf = LosslessFactory.createFromImage(document, fodIcon);
            contentStream.drawImage(fodIconPdf, left, y);

            {
                int font_size = 28;
                contentStream.setFont(font_roboto_regular, font_size);

                String label, icon;

                {
                    label = "N/A";
                    icon = "";
                    contentStream.setNonStrokingColor(0, 0, 0);

                }

                float label_width = font_roboto_regular.getStringWidth(label)/1000f*font_size;

                contentStream.beginText();
                contentStream.newLineAtOffset(right - label_width, y+block_bottom_padding);
                contentStream.showText(label);
                contentStream.endText();

                float icon_width = font_fontello.getStringWidth(icon)/1000f*font_size;

                contentStream.beginText();
                contentStream.setFont(font_fontello, font_size);
                contentStream.newLineAtOffset(right - label_width - icon_width, y+block_bottom_padding);
                contentStream.showText(icon);
                contentStream.endText();
            }

        }

        //Block 2
        {
            float font_size = 16;
            float block_height = font_size;
            y -= block_height;

            {
                String label = "Report";

                contentStream.beginText();
                contentStream.setNonStrokingColor(0, 0, 0);
                contentStream.setFont(font_roboto_bold, font_size);
                contentStream.newLineAtOffset(left, y);
                contentStream.showText(label);
                contentStream.endText();
            }

            {
                String label = "Verdict";
                float label_width = font_roboto_bold.getStringWidth(label)/1000f*font_size;

                contentStream.beginText();
                contentStream.setNonStrokingColor(0, 0, 0);
                contentStream.setFont(font_roboto_bold, font_size);
                contentStream.newLineAtOffset(right - label_width, y);
                contentStream.showText(label);
                contentStream.endText();
            }

        }
    }

    private void DoHeader1(String header_id) throws IOException{

        float font_size = 12;
        float block_height = font_size;
        float block_margin_top = font_size;
        y -= (block_height+block_margin_top);
        x = left + font_size;

        {
            //String label = mActivity.getString(header_id);

            contentStream.beginText();
            //contentStream.setNonStrokingColor(0, 0, 0);
            contentStream.setNonStrokingColor(15, 38, 192);
            contentStream.setFont(font_roboto_bold, font_size);
            contentStream.newLineAtOffset(x, y);
            //contentStream.showText(label);
            contentStream.showText(header_id);
            contentStream.endText();
        }
    }

    private void DoGeneralInformation() throws IOException{

        float font_size = 10;
        float block_margin_top = 8;
        float block_line1_height = 16;
        float block_line3_height = 48;

        y -= block_margin_top;

        float x1 = left;
        float x2 = x1 + (float)(width * 0.20);
        float x3 = right;
        //float x3 = x2 + (float)(width * 0.30);
        //float x4 = x3 + (float)(width * 0.20);
        //float x5 = right;

        //float h1 = 5 * block_line1_height + block_line3_height;
        //float h3 = 5 * block_line1_height + block_line3_height;

        //Customer
        float y1 = y - block_line1_height;
        //Operator
        float y2 = y1 - block_line1_height;
        //Device ID
        float y3 = y2 - block_line1_height;
        //Device ser.
        float y4 = y3 - block_line1_height;
        //Software version
        float y5 = y4 - block_line1_height;

        //float y6 = y5 - block_line3_height;

        float m = 2;
        float m2 = 6;

        contentStream.addRect(x1, y5, x3-x1, y-y5);
        //contentStream.setStrokingColor(0, 0, 0);
        contentStream.setStrokingColor(255, 0, 0);
        contentStream.setLineWidth(1.0f);
        contentStream.stroke();


        contentStream.setLineWidth(0.5f);

        contentStream.moveTo(x2, y);
        contentStream.lineTo(x2, y5);
        contentStream.stroke();

        //contentStream.moveTo(x3, y);
        //contentStream.lineTo(x3, y5);
        //contentStream.stroke();

        //contentStream.moveTo(x4, y);
        //contentStream.lineTo(x4, y5);
        //contentStream.stroke();

        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x3, y1);
        contentStream.stroke();

        contentStream.moveTo(x1, y2);
        contentStream.lineTo(x3, y2);
        contentStream.stroke();

        contentStream.moveTo(x1, y3);
        contentStream.lineTo(x3, y3);
        contentStream.stroke();

        contentStream.moveTo(x1, y4);
        contentStream.lineTo(x3, y4);
        contentStream.stroke();

        //contentStream.moveTo(x1, y5);
        //contentStream.lineTo(x5, y5);
        //contentStream.stroke();


        //contentStream.setNonStrokingColor(0, 0, 0);
        contentStream.setNonStrokingColor(15, 38, 192);
        contentStream.setFont(font_roboto_regular, font_size);

        //System.out.println("customer:" + MainActivity.getInstance().getString(R.string.pdf_customer));

        DoText(x1+m2, y -m-font_size, MainActivity.getInstance().getString(R.string.pdf_customer));
        DoText(x1+m2, y1-m-font_size, MainActivity.getInstance().getString(R.string.pdf_operator));
        DoText(x1+m2, y2-m-font_size, MainActivity.getInstance().getString(R.string.pdf_device_id));
        DoText(x1+m2, y3-m-font_size, MainActivity.getInstance().getString(R.string.pdf_device_sn));
        DoText(x1+m2, y4-m-font_size, MainActivity.getInstance().getString(R.string.pdf_sw_version));

        //DoText(x3+m2, y -m-font_size, MainActivity.getInstance().getString(R.string.pdf_job_id));
        //DoText(x3+m2, y1-m-font_size, MainActivity.getInstance().getString(R.string.pdf_cable_id));
        //DoText(x3+m2, y2-m-font_size, MainActivity.getInstance().getString(R.string.pdf_cable_end));
        //DoText(x3+m2, y3-m-font_size, MainActivity.getInstance().getString(R.string.pdf_fiber_id));
        //DoText(x1+m2, y5-m-font_size, MainActivity.getInstance().getString(R.string.pdf_datetime));

        //DoText(x1+m2, y5-m-font_size, MainActivity.getInstance().getString(R.string.pdf_comment));


        //Create editable fields
        acroForm = new PDAcroForm(document);
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), PDType1Font.HELVETICA);

        acroForm.setDefaultResources(resources);
        acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");
        acroForm.setNeedAppearances(true);
        document.getDocumentCatalog().setAcroForm(acroForm);

        DoTextField("Customer", false, x2+m, y1+m, x3-x2-m-m, y -y1-m-m, "");
        DoTextField("Operator" , false, x2+m, y2+m, x3-x2-m-m, y1-y2-m-m, "");
        //DoTextField("Location", false, x2+m, y3+m, x3-x2-m-m, y2-y3-m-m, "");
        //DoTextField("Operator", false, x2+m, y4+m, x3-x2-m-m, y3-y4-m-m, "");
        //==============================

        //DoText(x2+m, y4-m-font_size, "test");

        DoText(x2+m2, y2-m-font_size, TestParamS.MFID);
        DoText(x2+m2, y3-m-font_size, TestParamS.OTDR);
        DoText(x2+m2, y4-m-font_size, TestParamS.SR);

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = MainActivity.getInstance().getResources().getConfiguration().getLocales().get(0);
        }
        else{
            //noinspection deprecation
            locale = MainActivity.getInstance().getResources().getConfiguration().locale;
        }

        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,locale);
        //DoText(x4+m2, y4-m-font_size, "20171201 15:35");

        //DoTextField("Comment", true, x2+m, y6+m, x5-x2-m-m, y5-y6-m-m, "my comment");

        y=y5;
    }

    private void DoLocations() throws IOException{

        float font_size = 10;
        float block_margin_top = 8;
        float block_line1_height = 16;

        y -= block_margin_top;

        float x1 = left;
        float x2 = x1 + (float)(width * 0.20);
        float x3 = right;

        //OTDR End
        float y1 = y - block_line1_height;
        //FAR End
        float y2 = y1 - block_line1_height;
        //Cable
        float y3 = y2 - block_line1_height;
        //Link
        float y4 = y3 - block_line1_height;
        //Time Stamp
        float y5 = y4 - block_line1_height;

        float m = 2;
        float m2 = 6;

        contentStream.addRect(x1, y5, x3-x1, y-y5);
        contentStream.setStrokingColor(255, 0, 0);
        contentStream.setLineWidth(1.0f);
        contentStream.stroke();

        contentStream.setLineWidth(0.5f);

        contentStream.moveTo(x2, y);
        contentStream.lineTo(x2, y5);
        contentStream.stroke();

        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x3, y1);
        contentStream.stroke();

        contentStream.moveTo(x1, y2);
        contentStream.lineTo(x3, y2);
        contentStream.stroke();

        contentStream.moveTo(x1, y3);
        contentStream.lineTo(x3, y3);
        contentStream.stroke();

        contentStream.moveTo(x1, y4);
        contentStream.lineTo(x3, y4);
        contentStream.stroke();

        contentStream.setNonStrokingColor(15, 38, 192);
        contentStream.setFont(font_roboto_regular, font_size);

        DoText(x1+m2, y -m-font_size, MainActivity.getInstance().getString(R.string.pdf_otdr_end));
        DoText(x1+m2, y1-m-font_size, MainActivity.getInstance().getString(R.string.pdf_far_end));
        DoText(x1+m2, y2-m-font_size, MainActivity.getInstance().getString(R.string.pdf_cable));
        DoText(x1+m2, y3-m-font_size, MainActivity.getInstance().getString(R.string.pdf_link));
        DoText(x1+m2, y4-m-font_size, MainActivity.getInstance().getString(R.string.pdf_time_stamp));

        DoText(x2+m2, y-m-font_size, TestParamS.OL);
        DoText(x2+m2, y1-m-font_size,TestParamS.TL);
        DoText(x2+m2, y2-m-font_size,TestParamS.CID);
        DoText(x2+m2, y3-m-font_size,TestParamS.FID);
        DoText(x2+m2, y4-m-font_size, SOR.getDateCurrentTimeZone(TestParamS.TimeStamp));//

        y=y5;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void DoEventsInformation() throws IOException{

        float font_size = 10;
        float block_margin_top = 8;
        float block_line1_height = 16;
        int line_n = (SOR.Events.N * 2)-1;
        int CurrEvntNr = 0;
        String EventNR;
        String EventTypeStr;

        float m = 2;
        float m2 = 6;

        y -= block_margin_top;
        float x1 = left;
        float x2 = x1 + (float)(width * 0.05);
        float x3 = x2 + (float)(width * 0.20);
        float x4 = x3 + (float)(width * 0.20);
        float x5 = x4 + (float)(width * 0.20);
        float x6 = x5 + (float)(width * 0.20);
        float x7 = right;



        float y1 = y - block_line1_height;

        //add heder rect
        contentStream.addRect(x1, y1, x7-x1, y-y1);
        contentStream.setStrokingColor(255, 0, 0);
        contentStream.setLineWidth(1.0f);
        contentStream.stroke();

        contentStream.setFont(font_roboto_regular, font_size);
        DoText(x1+m2, y-m-font_size, "Nr.");
        DoText(x2+m2, y-m-font_size, "TYPE");
        DoText(x3+m2, y-m-font_size, "L,km");
        DoText(x4+m2, y-m-font_size, "Loss,db");
        DoText(x5+m2, y-m-font_size, "Ref.,db");
        DoText(x6+m2, y-m-font_size, "ACI");

        while(line_n != 0) {
            float event_size = block_line1_height;
            if(line_n>1){
                event_size += block_line1_height;
            }
            if((y - block_line1_height)-event_size < 0){

                y = y - block_line1_height;
                contentStream.setStrokingColor(255, 0, 0);
                contentStream.setLineWidth(1.0f);
                contentStream.addRect(x1, y1, x7-x1, y-y1);
                y1 += block_line1_height;
                DoLine(x2 ,y, x2, y1);
                contentStream.addRect(x3, y1, x4-x3, y-y1);
                contentStream.addRect(x5, y1, x6-x5, y-y1);
                contentStream.stroke();

                newPage();
                DoHeader1("Events");
                y -= block_margin_top;
                y1 = y - block_line1_height;
                //add heder rect
                contentStream.addRect(x1, y1, x7-x1, y-y1);
                contentStream.setStrokingColor(255, 0, 0);
                contentStream.setLineWidth(1.0f);
                contentStream.stroke();

                contentStream.setFont(font_roboto_regular, font_size);
                DoText(x1+m2, y-m-font_size, "Nr.");
                DoText(x2+m2, y-m-font_size, "TYPE");
                DoText(x3+m2, y-m-font_size, "L,km");
                DoText(x4+m2, y-m-font_size, "Loss,db");
                DoText(x5+m2, y-m-font_size, "Ref.,db");
                DoText(x6+m2, y-m-font_size, "ACI");
            }

            y = y - block_line1_height;
            EventNR = "Nr." + (CurrEvntNr + 1);
            DoText(x1 + m2, y - m - font_size, EventNR);
            DoText(x2 + m2, y - m - font_size, getEventName(CurrEvntNr));
            DoText(x3+m2, y-m-font_size, getLen_KM(CurrEvntNr));
            DoText(x4+m2, y-m-font_size, getLost(CurrEvntNr));
            DoText(x5+m2, y-m-font_size, getRef(CurrEvntNr));
            DoText(x6+m2, y-m-font_size, " - ");
            line_n--;
            if(line_n != 0) {
                contentStream.setLineWidth(0.5f);
                DoLine(x3 ,y - block_line1_height, x7, y - block_line1_height);

                y = y - block_line1_height;

                //DoText(x1+m2, y-m-font_size, EventNR);
                //DoText(x2+m2, y-m-font_size, getEventName(CurrEvntNr));
                DoText(x3+m2, y-m-font_size, getSectionLen_KM(CurrEvntNr));
                DoText(x4+m2, y-m-font_size, getSectiontLost(CurrEvntNr));
                DoText(x5+m2, y-m-font_size, " - ");
                if(CurrEvntNr+1 > SOR.Events.N){
                    DoText(x6+m2, y-m-font_size, " - ");
                }else {
                    DoText(x6 + m2, y - m - font_size, getACI(CurrEvntNr + 1));
                }
                contentStream.setLineWidth(1.0f);
                DoLine(x1 ,y - block_line1_height, x7, y - block_line1_height);
                line_n--;
            }
            CurrEvntNr++;
        }

        y = y - block_line1_height;
        contentStream.setStrokingColor(255, 0, 0);
        contentStream.setLineWidth(1.0f);
        contentStream.addRect(x1, y1, x7-x1, y-y1);
        y1 += block_line1_height;
        DoLine(x2 ,y, x2, y1);
        contentStream.addRect(x3, y1, x4-x3, y-y1);
        contentStream.addRect(x5, y1, x6-x5, y-y1);

        contentStream.stroke();
    }


    private void DoLine (float x1, float y1, float x2, float y2) throws IOException{
        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x2, y2);
        contentStream.stroke();
    }
    private void DoText (float x, float y, String value) throws IOException{
        if (null == value) return;
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(value);
        contentStream.endText();
    }
    private void DoTextCentered (float x1, float y1, float x2, float y2, PDType0Font font, float size,  String value) throws IOException{

        float tw = font.getStringWidth(value)/1000f*size;
        float x0 = (x2+x1)/2 - tw/2;
        float y0 = Math.min(y1,y2) + (Math.abs(y1-y2)-size)/2 + 1;

        contentStream.setFont(font, size);

        contentStream.beginText();
        contentStream.newLineAtOffset(x0, y0);
        contentStream.showText(value);
        contentStream.endText();
    }

    private void DoTextField (String name, boolean isMultiline, float x, float y, float w, float h, String value) throws IOException{

        PDTextField textField = new PDTextField(acroForm);
        textField.setMultiline(isMultiline);
        textField.setPartialName(name + "_textfield");
        textField.setDefaultAppearance("/Helv 10 Tf 0 g");
        acroForm.getFields().add(textField);

        PDAnnotationWidget widget = textField.getWidget();
        widget.setPrinted(true);
        widget.setAnnotationName(name + "_widget");
        //the position/size of the text field
        PDRectangle rect = new PDRectangle(x, y, w, h);
        widget.setRectangle(rect);
        widget.setPage(page);
        page.getAnnotations().add(widget);
        textField.setValue(value);
    }

    private void DoDeviceInfo() throws IOException{

        float font_size = 10;
        float block_margin_top = 8;
        float block_line1_height = 16;

        y -= block_margin_top;

        float x1 = left;
        float x2  = x1 + 160;
        float x3  = x2 + 130;

        float y1 = y;
        float y2 = y1 - block_line1_height;
        float y3 = y2 - block_line1_height;
        float y4 = y3 - block_line1_height;
        float y5 = y4 - block_line1_height;

        contentStream.setLineWidth(1.0f);
        DoLine(x1 ,y1, x1, y5);
        DoLine(x3 ,y1, x3, y5);
        DoLine(x1 ,y1, x3, y1);
        DoLine(x1 ,y5, x3, y5);

        contentStream.setLineWidth(0.5f);
        DoLine(x2 ,y1, x2, y5);
        DoLine(x1 ,y2, x3, y2);
        DoLine(x1 ,y3, x3, y3);
        DoLine(x1 ,y4, x3, y4);

        float m2 = 6;
        float m = 2;

        contentStream.setFont(font_roboto_regular, font_size);

        DoText(x1+m2,y1-m-font_size, "Dev. model");
        DoText(x1+m2,y2-m-font_size, "Device SN");
        DoText(x1+m2,y3-m-font_size, "Device FW");
        DoText(x1+m2,y4-m-font_size, "App Version");

        DoText(x2+m2,y1-m-font_size, "name");
        DoText(x2+m2,y2-m-font_size, "sn");
        DoText(x2+m2,y3-m-font_size, "fw");
        DoText(x2+m2,y4-m-font_size, "av");

        //contentStream.setLineWidth(0.5f);
        //contentStream.setStrokingColor(0, 0, 255);
        //DoLine(0 ,0, 450, 450);

        //y = y2;
        y = y5;

    }

    private void DoGraph() throws IOException {
        float rect_height = 200;
        float block_margin_top = 8;
        float graph_ponts = (right - left) * 10;//*10;
        float point_width;
        float point_height = (float) (rect_height / 65535.0);
        float LineXPoints;
        float LineYPoints;

        y -= block_margin_top;

/*
        //draw trace zones
        contentStream.setNonStrokingColor(173,216,230);
        int ZoneWidth = 20;
        contentStream.fillRect(left, y-rect_height, ZoneWidth, rect_height);
*/
        //draw sale linens
        contentStream.setNonStrokingColor(128, 128, 128);
        //contentStream.fillRect(left, y-rect_height, right-left, rect_height);
        LineYPoints = rect_height;
        contentStream.setLineWidth(0.5f);
        //contentStream.setStrokingColor(58,58,58);
        contentStream.setStrokingColor(0, 0, 0);
        boolean draw_point = true;
        float LineWidth = 1;
        float y1 = y - rect_height;
        float y2 = y1 + LineWidth;

        while (draw_point) {
            float offset = rect_height / 5;
            float x_cnt = 0;
            LineXPoints = right;
            float curr_x = left + offset;
            do {
                DoLine(curr_x, y1, curr_x, y2);
                x_cnt++;
                curr_x = left + offset * x_cnt;
            } while (LineXPoints > curr_x);
            if (y2 == y) {
                draw_point = false;
                //break;
            }
            y1 = y1 + (LineWidth * 2) + 1;
            if (y1 > y) {
                draw_point = false;
                //break;
            }
            y2 = y1 + LineWidth;
            if (y2 > y) {
                y2 = y;
            }
        }
        draw_point = true;
        float x1 = left;
        float x2 = x1 + LineWidth;

        while (draw_point) {
            float offset = rect_height / 5;
            float y_cnt = 0;
            LineYPoints = y - rect_height;

            float curr_y = y - rect_height;
            do {
                DoLine(x1, curr_y, x2, curr_y);
                y_cnt++;
                curr_y = LineYPoints + offset * y_cnt;
            } while (y > curr_y);

            if (x2 == right) {
                draw_point = false;
                //break;
            }
            x1 = x1 + (LineWidth * 2) + 1;
            if (x1 > right) {
                draw_point = false;
                //break;
            }
            x2 = x1 + LineWidth;
            if (x2 > right) {
                x2 = right;
            }
        }

/*
        while (LineYPoints-- > 0) {
            float offset = rect_height/5;
            float x_cnt = 0;
            LineXPoints = right;
            if(draw_point) {
                float curr_x = left + offset;
                do{
                    DoLine(curr_x, y - LineYPoints, curr_x, y - LineYPoints);
                    x_cnt++;
                    curr_x = left + offset * x_cnt;
                }while (LineXPoints > curr_x);
                draw_point = false;
                continue;
            }
            draw_point = true;
        }

        draw_point = false;
        LineXPoints = right-left;
        while (LineXPoints-- > 0) {
            float offset = rect_height/5;
            float y_cnt = 0;
            LineYPoints = y-rect_height;
            if(draw_point) {
                float curr_y = y-rect_height;
                do{
                    DoLine(LineXPoints+left, curr_y, LineXPoints+left, curr_y);
                    y_cnt++;
                    curr_y = LineYPoints + offset * y_cnt;
                }while (y > curr_y);
                draw_point = false;
                continue;
            }
            draw_point = true;
        }
*/
        //DoLine(right ,y-rect_height, right, y);
        float Coefficient = (float) ((SOR.sk.length) / graph_ponts);
        if (SOR.sk.length < graph_ponts) {
            point_width = (float) (0.1 + ((graph_ponts-SOR.sk.length)/SOR.sk.length * 0.1));
            graph_ponts = SOR.sk.length;
        }else{
            point_width = (float) 0.1;
        }
        draw_dec(Coefficient, graph_ponts,y-rect_height,point_height,point_width);

/*
        //draw markers
        contentStream.setLineWidth(1.0f);
        contentStream.setStrokingColor(0,0,0);
        int MarkerAPoz = 45;
        int MarkerBPoz = 145;
        DoLine(left+MarkerAPoz ,y-rect_height, left+MarkerAPoz, y);
        contentStream.setStrokingColor(0,128,0);
        DoLine(left+MarkerBPoz ,y-rect_height, left+MarkerBPoz, y);
        //draw events
        //draw scale
        float font_size = 12;
        contentStream.setNonStrokingColor(192,192,192);
        String valueTxt = "13.1 dB, 1.03 km/div";
        contentStream.fillRect(left, y-rect_height , valueTxt.length()*(font_size/2), font_size+5);
        contentStream.setNonStrokingColor(0,0,0);
        contentStream.setFont(font_roboto_regular, font_size);
        DoText(left,y-rect_height+5, valueTxt);
*/
        //draw rectangle
        contentStream.setStrokingColor(0, 0, 0);
        contentStream.setLineWidth(1.0f);
        DoLine(left ,y-rect_height, right, y-rect_height);
        DoLine(left ,y, right, y);
        DoLine(left ,y-rect_height, left, y);
        DoLine(right ,y-rect_height, right, y);

        y = y-rect_height;
    }

    /**
     * Creates a new PDF from scratch and saves it to a file
     */
    @SuppressLint("NewApi")
    public boolean createPdf(String path) {
        document = new PDDocument();
        page = new PDPage();
        page.setMediaBox(PDRectangle.A4);
        document.addPage(page);

        padding = 40;
        left = padding;
        top = PDRectangle.A4.getHeight() - padding;
        right = PDRectangle.A4.getWidth() - padding;
        bottom = padding;
        width = PDRectangle.A4.getWidth() - 2*padding;
        height = PDRectangle.A4.getHeight() - 2*padding;

        try {
            // Define a content stream for adding to the PDF
            contentStream = new PDPageContentStream(document, page);

            InitFonts();
            //int font_size = 28;
            //contentStream.setFont(font_roboto_regular, font_size);
            DoHeader0();
            DoHeader1("General Information");
            DoGeneralInformation();
            DoHeader1("Locations");
            DoLocations();
            // Write Hello World in blue text
            //contentStream.beginText();
            //contentStream.setNonStrokingColor(15, 38, 192);
            //contentStream.setFont(font_roboto_regular, 12);
            //contentStream.newLineAtOffset(100, 700);
            //contentStream.showText("Hello World");
            //contentStream.endText();
            //DoDeviceInfo();
            DoHeader1("OTDR Trace");
            DoGraph();
            DoHeader1("Events");
            DoEventsInformation();
            // Make sure that the content stream is closed:
            contentStream.close();
            // Save the final pdf document to a file
            //String path = root.getAbsolutePath() + "/Download/Created.pdf";
// New page

// New page

            document.save(path);
            document.close();
            System.out.println("!PDF Created!");
            //Toast.makeText(MainActivity.getInstance(), "!PDF Created!", Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //helpers
    public void newPage() throws IOException {
        contentStream.close();
        PDPage page = new PDPage(PDRectangle.A4);
        contentStream = new PDPageContentStream(document, page);
        //InitFonts();
        y = top;
        document.addPage(page);
    }

    // Method for getting the maximum value
    public static float getMax(float[] inputArray){
        float maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }

    public static float getMax(int[] inputArray){
        int maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }

    // Method for getting the minimum value
    public static int getMin(int[] inputArray){
        int minValue = inputArray[0];
        for(int i=1;i<inputArray.length;i++){
            if(inputArray[i] < minValue){
                minValue = inputArray[i];
            }
        }
        return minValue;
    }

    // Method for getting the minimum value
    public static float getMin(float[] inputArray){
        float minValue = inputArray[0];
        for(int i=1;i<inputArray.length;i++){
            if(inputArray[i] < minValue){
                minValue = inputArray[i];
            }
        }
        return minValue;
    }

    enum fvalue_t {
        value_equal,
        value_more,
        value_less
    }
    // Method for find the minimum value by procents
    public static boolean findValue(float[] inputArray,int value,int procents,fvalue_t fvalue){
        int ValueCnt = (int) (((float)inputArray.length/100.0)*(float)procents);

        for(int i=1;i < inputArray.length;i++){
            switch (fvalue) {
                case value_equal:
                    if (inputArray[i] == value) {
                        ValueCnt--;
                        if (ValueCnt <= 0) {
                            return true;
                        }
                    }
                 break;
                case value_less:
                    if (value >= inputArray[i]) {
                        ValueCnt--;
                        if (ValueCnt <= 0) {
                            return true;
                        }
                    }
                    break;
                case value_more:
                    if (value <= inputArray[i]) {
                        ValueCnt--;
                        if (ValueCnt <= 0) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }




    // Method for getting the minimum value
    public static int getIncFp(float Scale,float PCnt){
        float inc_coef = (Scale-PCnt)/PCnt;
        if((1 % inc_coef) == 0){
            return (int)(1 / inc_coef);
        }else{
            return (int)(1 / inc_coef)+1;
        }
    }

    public static int getIncSp(float fp, float Scale,float PCnt){
        float inc_coef = ((Scale-PCnt)-(PCnt/fp))/PCnt;
        if((1 % inc_coef) == 0){
            return (int)(1 / inc_coef);
        }else{
            return (int)(1 / inc_coef)+1;
        }
    }



    // Method for getting the draw coefficent
    public static float getCoefficient(float MaxGraphPonts,float SORMaxPoints){
        if(MaxGraphPonts>SORMaxPoints){
            return (MaxGraphPonts-SORMaxPoints)/SORMaxPoints;
        }else if (MaxGraphPonts<SORMaxPoints){
            return (SORMaxPoints-MaxGraphPonts)/MaxGraphPonts;
        }else{
            return 1;
        }
    }

    public static int draw_inc(float Coefficient, float MaxGraphPonts,float SORMaxPoints){
        float temp_Coefficient = Coefficient;
        int MaxPoints = (int)SORMaxPoints;
        int drow_point = 0;
        while (MaxPoints-- > 0){
            if(temp_Coefficient < 1) {
                while (temp_Coefficient < 1) {
                    temp_Coefficient += Coefficient;
                    drow_point++;
                }
            }
            if(temp_Coefficient >= 1) {
                while (temp_Coefficient > 1) {
                    temp_Coefficient -= 1;
                    drow_point++;
                }
            }
        }
        return drow_point;
    }

    private int draw_dec(float Coefficient,float MaxGraphPonts,float graph_y, float point_height,float point_width)throws IOException{
        float temp_Coefficient = Coefficient;
        int MaxPoints = (int)MaxGraphPonts;
        int drow_point = 0;
        int sk_idx =0;
        float x1 = 0;
        float x2 = 0;
        float y1 = 0;
        float y2 = 0;
        boolean have_less = false;
        float stored_y[] = new float[2];

        contentStream.setLineWidth(0.8f);
        contentStream.setStrokingColor(0, 0, 160);

        y1 = (float) (getMax(SOR.sk)*0.001)*point_height;
        int y_min = 65535;


        while (MaxPoints-- > 0) {
            if (temp_Coefficient < 1) {
                while (temp_Coefficient < 1) {
                    temp_Coefficient += Coefficient;
                }
            }
            if (temp_Coefficient >= 1) {
                float[] point_arr = new float[(int) temp_Coefficient];
                int curr_indx = 0;
                while (temp_Coefficient >= 1) {
                    temp_Coefficient -= 1;
                    point_arr[curr_indx++] = SOR.sk[sk_idx++];//get
                }
                //get max value and draw
                if(findValue(point_arr, 60000,30,value_more)){
                    if(have_less) {
                        //draw 2 points
                        stored_y[1] = (y_min - getMin(point_arr))*point_height;
                        if(stored_y[0] > stored_y[1]){
                            //draw first
                            x2 += point_width;
                            y2 = stored_y[0];
                            DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                            y1 = y2;
                            x1 = x2;
                            //draw second
                            x2 += point_width;
                            y2 = 0;
                            DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                            y1 = y2;
                            x1 = x2;
                        }else{
                            //draw first
                            x2 += 0.1;
                            y2 = 0;
                            DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                            y1 = y2;
                            x1 = x2;
                            //draw second
                            x2 += point_width;
                            y2 = stored_y[1];
                            DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                            y1 = y2;
                            x1 = x2;
                        }
                        have_less = false;
                    }else {
                        stored_y[0] = (y_min - getMin(point_arr)) * point_height;
                        have_less = true;
                    }
                }else{
                    if(have_less) {
                        //draw 2 points
                        //draw first
                        x2 += point_width;
                        y2 = Coefficient;
                        DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                        y1 = y2;
                        x1 = x2;
                        //draw second
                        x2 += point_width;
                        y2 = (float) ((y_min - getMin(point_arr))*point_height);
                        DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                        y1 = y2;
                        x1 = x2;
                        have_less = false;
                    }else{
                        //x2 += 0.1;
                        x2 += point_width;
                        //draw 1 points
                        y2 = (float) ((y_min - getMin(point_arr))*point_height);
                        DoLine(x1+left ,graph_y+y1, x2+left, graph_y+y2);
                        y1 = y2;
                        x1 = x2;
                    }
                }
                drow_point++;
            }
        }
        return drow_point;
    }

}
