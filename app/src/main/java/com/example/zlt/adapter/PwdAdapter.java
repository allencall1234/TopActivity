package com.example.zlt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.zlt.activity.R;
import com.example.zlt.entity.PwdBean;
import com.example.zlt.utils.SPHelper;

import java.util.List;

/**
 * Created by 524202 on 2017/9/19.
 */

public class PwdAdapter extends RecyclerView.Adapter<PwdAdapter.PwdHolder> {

    private List<PwdBean> list;
    private Context mContext;

    public PwdAdapter(Context context, List<PwdBean> list) {
        mContext = context;
        this.list = list;
    }

    @Override
    public PwdHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_pwd_layout, parent, false);
        return new PwdHolder(view);
    }

    @Override
    public void onBindViewHolder(PwdHolder holder, final int position) {
        holder.setIsRecyclable(false);
        holder.etAccount.setText(list.get(position).account);
        holder.etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                list.get(position).account = s.toString();
                saveResult();
            }
        });

        holder.etPassword.setText(list.get(position).pwd);
        holder.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                list.get(position).pwd = s.toString();
                saveResult();
            }
        });
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                notifyDataSetChanged();
                saveResult();
            }
        });
    }

    public void saveResult() {
        StringBuilder result = new StringBuilder();
        for (PwdBean bean : list) {
            result.append(bean.toString());
            result.append("::");
        }
        SPHelper.setAutoCompeleteText(mContext, result.toString());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PwdHolder extends RecyclerView.ViewHolder {
        EditText etAccount;
        EditText etPassword;
        ImageView ivDelete;

        public PwdHolder(View itemView) {
            super(itemView);
            etAccount = (EditText) itemView.findViewById(R.id.pwd_account_content);
            etPassword = (EditText) itemView.findViewById(R.id.pwd_password_content);
            ivDelete = (ImageView) itemView.findViewById(R.id.pwd_image_delete);
        }
    }
}
