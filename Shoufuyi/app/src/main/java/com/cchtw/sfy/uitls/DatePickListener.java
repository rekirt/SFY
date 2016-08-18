package com.cchtw.sfy.uitls;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.cchtw.sfy.R;
import com.cchtw.sfy.activity.QuerySignActivity;
import com.cchtw.sfy.uitls.view.datepicker.HLDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Shoufuyi
 * Description:
 * Created by fuhongliang on
 * Date:16/5/27
 * Time:上午10:39
 * Copyright © 2016-05-16/5/27 Jason. All rights reserved.
 * blog:http://fuhongliang.com/
 */
public class DatePickListener  implements OnClickListener {
    private EditText editText = null;
    private Context context;
    private Date date;
    private long time;
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

    public DatePickListener(Context c, EditText edit) {
        this.context = c;
        this.editText = edit;
        editText.setFocusable(false);
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.activity_date_time_dialog, null);
        final HLDatePicker beginTimePicker = (HLDatePicker) view
                .findViewById(R.id.date_picker);
        initDatePicker(beginTimePicker);
        final HLDatePicker endTimePicker = (HLDatePicker) view
                .findViewById(R.id.time_picker);
        initDatePicker(endTimePicker);
        builder.setView(view);
        builder.setTitle("请选取提交日期范围");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {

                        int beginDayOfMonth=beginTimePicker.getCurrentDay();
                        int beginMonth=beginTimePicker.getCurrentMonth();
                        int beginYear=beginTimePicker.getCurrentYear();

                        int endDayOfMonth=endTimePicker.getCurrentDay();
                        int endMonth=endTimePicker.getCurrentMonth();
                        int endYear=endTimePicker.getCurrentYear();

                        int begin = beginDayOfMonth+beginMonth*100+beginYear*10000;
                        int end = endDayOfMonth+endMonth*100+endYear*10000;

                        if (begin>end){
                            ToastHelper.ShowToast("开始时间不能大于结束时间");
                            return;
                        }else {
                            editText.setText(beginYear+"."+changeNumber(beginMonth)+"."+changeNumber(beginDayOfMonth) + "至\n" + endYear+"."+changeNumber(endMonth)+"."+changeNumber(endDayOfMonth));
                            dialog.cancel();
                            ((QuerySignActivity)context).refresh();
                        }
                    }
                }).show();
    }

    private void initDatePicker(HLDatePicker mTimePicker){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 1899;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mTimePicker.setCurrentDay(day);
        mTimePicker.setCurrentMonth(month);
        mTimePicker.setCurrentYear(year);
    }

    private String changeNumber(int number){
        if (number<10){
            return ("0"+number);
        }
        return number+"";
    }

    private void resizeTimerPicker(TimePicker tp)
    {
        List<NumberPicker> npList = findNumberPicker(tp);

        for (NumberPicker np : npList)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 0, 10, 0);
            np.setLayoutParams(params);
        }
    }




    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup)
    {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;

        if (null != viewGroup)
        {
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker)
                {
                    npList.add((NumberPicker)child);
                }
                else if (child instanceof LinearLayout)
                {
                    List<NumberPicker> result = findNumberPicker((ViewGroup)child);
                    if (result.size() > 0)
                    {
                        return result;
                    }
                }
            }
        }

        return npList;
    }
}