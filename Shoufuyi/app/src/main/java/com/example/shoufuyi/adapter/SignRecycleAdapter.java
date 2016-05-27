package com.example.shoufuyi.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.shoufuyi.R;
import com.example.shoufuyi.activity.SignDetailActivity;
import com.example.shoufuyi.api.ApiRequest;
import com.example.shoufuyi.api.JsonHttpHandler;
import com.example.shoufuyi.uitls.Constant;
import com.example.shoufuyi.uitls.ImageHelper;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.example.shoufuyi.uitls.ToastHelper;
import com.example.shoufuyi.uitls.dialog.DialogHelper;
import com.itech.message.APP_120009;
import com.itech.message.Result_120023;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 */
public class SignRecycleAdapter extends RecycleBaseAdapter {


    private Context context;
    public SharedPreferencesHelper mSharedPreferences;

    public SignRecycleAdapter(Context context) {
        super(context);
        this.context = context;
        mSharedPreferences =  SharedPreferencesHelper.getInstance(context);
    }

    public SignRecycleAdapter(View headerView, Context context) {
        super(context);
        mHeaderView = headerView;
    }


    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.item_sign,null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        SignRecycleAdapter.ViewHolder new_holder = (ViewHolder) holder;
        final Result_120023 signBean = (Result_120023) _data.get(position);
        new_holder.tv_sign_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//跳转到详情
                goToSignDetail(signBean);
            }
        });
        new_holder.tv_give_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//放弃签约
                giveUpSign(signBean);
            }
        });
//      签约类型判断
        switch (signBean.getState()){
            case "1":
//                ImageHelper.displayImage("drawable://" + R.drawable.ic_unsign, new_holder.iv_sign_stye);
                new_holder.iv_sign_stye.setImageResource(R.drawable.ic_unsign);
                break;
            case "2":
//                ImageHelper.displayImage("drawable://" + R.drawable.ic_sign_finish, new_holder.iv_sign_stye);
                new_holder.iv_sign_stye.setImageResource(R.drawable.ic_sign_finish);
                break;
            case "3":
//                ImageHelper.displayImage("drawable://" + R.drawable.ic_unfinish, new_holder.iv_sign_stye);
                new_holder.iv_sign_stye.setImageResource(R.drawable.ic_unfinish);
                break;
            default:
                ImageHelper.displayImage("drawable://" + R.drawable.ic_unfinish, new_holder.iv_sign_stye);
                new_holder.iv_sign_stye.setImageResource(R.drawable.ic_unfinish);
                break;
        }

//      姓名
        new_holder.tv_sign_name.setText(signBean.getAccountName());
//      手机号码
        new_holder.tv_phone_number.setText(signBean.getMobile());
//      身份证号
        new_holder.tv_id_number.setText(signBean.getIdCard());
//      银行卡号
        new_holder.tv_card_number.setText(signBean.getAccountNo());

    }

    private void goToSignDetail(Result_120023 signBean){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", signBean);
        intent.putExtras(bundle);
        intent.setClass(context, SignDetailActivity.class);
        context.startActivity(intent);
    }

    private void giveUpSign(final Result_120023 signBean){
        DialogHelper.showProgressDialog(context, "正在处理，请稍候...", true, false);
        APP_120009 app = new APP_120009();
        app.setMerchantId(signBean.getMerchantId());
        app.setIdCard(signBean.getIdCard());
        app.setAccountNo(signBean.getAccountNo());
        app.setUserName(signBean.getAccountName());
        ApiRequest.requestData(app, SharedPreferencesHelper.getString(Constant.PHONE, ""), new JsonHttpHandler() {
            @Override
            public void onDo(JSONObject responseJsonObject) {
                APP_120009 app120009 = JSON.parseObject(responseJsonObject.toString(), APP_120009.class);
                    if (app120009.getDetailCode().equals("0000")) {
                        removeItem(signBean);
                    }
                ToastHelper.ShowToast(app120009.getDetailInfo());
            }

            @Override
            public void onDo(JSONArray responseJsonArray) {

            }

            @Override
            public void onDo(String responseString) {

            }

            @Override
            public void onFinish() {
                DialogHelper.dismissProgressDialog();
            }
        });

    }
    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        public TextView tv_sign_name;
        public TextView tv_phone_number;
        public TextView tv_id_number;
        public TextView tv_card_number;
        public ImageView iv_sign_stye;
        public TextView tv_sign_detail;
        public TextView tv_give_up;
        public ViewHolder(int viewType, View view) {
            super(viewType, view);
            tv_sign_name = (TextView) view.findViewById(R.id.tv_sign_name);
            tv_phone_number  = (TextView) view.findViewById(R.id.tv_phone_number);
            tv_id_number = (TextView) view.findViewById(R.id.tv_id_number);
            tv_card_number = (TextView) view.findViewById(R.id.tv_card_number);
            iv_sign_stye = (ImageView) view.findViewById(R.id.iv_sign_stye);
            tv_sign_detail = (TextView) view.findViewById(R.id.tv_sign_detail);
            tv_give_up  = (TextView) view.findViewById(R.id.tv_give_up);
        }
    }
}
