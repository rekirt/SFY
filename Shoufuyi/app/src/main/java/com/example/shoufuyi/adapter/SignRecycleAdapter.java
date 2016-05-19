package com.example.shoufuyi.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shoufuyi.R;
import com.example.shoufuyi.uitls.ImageHelper;
import com.example.shoufuyi.uitls.SharedPreferencesHelper;
import com.itech.message.Result_120023;


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
//                showAvatar(businessBean.getGallery());
            }
        });
        new_holder.tv_give_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//放弃签约
//                showAvatar(businessBean.getGallery());
            }
        });
//      签约类型判断
        switch (signBean.getState()){
            case "":
                break;
            default:
                ImageHelper.displayImage("drawable://" + R.drawable.ic_unfinish, new_holder.iv_sign_stye);
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
