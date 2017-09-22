package com.example.zlt.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.zlt.adapter.PwdAdapter;
import com.example.zlt.entity.PwdBean;
import com.example.zlt.library.ColorPickerDialog;
import com.example.zlt.service.DetectionService;
import com.example.zlt.utils.SPHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ColorPickerDialog.OnColorPickedListener {
    private Switch showOrDismiss;
    private Switch showPackage;
    private LinearLayout modifyTextColor;
    private TextView colorModule;
    private ColorPickerDialog pickerDialog = null;

    private Switch autoCompeleteSwith = null;
    private EditText autoCompleteEditText = null;
    private RelativeLayout autoCompleteLayout = null;
    private RelativeLayout autoCompleteLayout0 = null;

    private LinearLayout outLayout = null;
    private RecyclerView codeList = null;
    private List<PwdBean> codes = null;
    private PwdAdapter mAdapter = null;
    private ImageView ivAdd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //总辅助服务开启模块
        showOrDismiss = (Switch) findViewById(R.id.show_dismiss_dialog);
        showOrDismiss.setOnCheckedChangeListener(this);
        outLayout = (LinearLayout) findViewById(R.id.out_layout);

        //包名显示模块
        showPackage = (Switch) findViewById(R.id.show_packagename);
        showPackage.setOnCheckedChangeListener(this);

        //字体颜色调整模块
        modifyTextColor = (LinearLayout) findViewById(R.id.modify_text_color);
        modifyTextColor.setOnClickListener(this);
        colorModule = (TextView) findViewById(R.id.color_module);
        pickerDialog = ColorPickerDialog.createColorPickerDialog(this);
        pickerDialog.setOnColorPickedListener(this);

        //自动补全功能模块
        autoCompeleteSwith = (Switch) findViewById(R.id.auto_compelete_switch);
        autoCompleteEditText = (EditText) findViewById(R.id.auto_compelete_text);
        autoCompleteLayout = (RelativeLayout) findViewById(R.id.auto_compelete_layout);
        autoCompleteLayout0 = (RelativeLayout) findViewById(R.id.auto_compelete_layout0);
        autoCompleteEditText.setText(SPHelper.getAutoCompeleteText(this));
        autoCompeleteSwith.setOnCheckedChangeListener(this);
        ivAdd = (ImageView) findViewById(R.id.auto_compelete_add);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codes.add(new PwdBean("", ""));
                mAdapter.notifyDataSetChanged();
            }
        });
        autoCompleteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    SPHelper.setAutoCompeleteText(MainActivity.this, charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        codeList = (RecyclerView) findViewById(R.id.code_list);
        String pwdAndAccounts = SPHelper.getAutoCompeleteText(this);
        String[] array = pwdAndAccounts.split("::");
        codes = new ArrayList<>();
        if (array.length >= 2) {
            for (int i = 0; i < array.length; i += 2) {
                codes.add(new PwdBean(array[i], array[i + 1]));
            }
        }
        codeList.setAdapter(mAdapter = new PwdAdapter(this, codes));
        codeList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //如果版本太低,不支持这个服务
            autoCompleteLayout0.setVisibility(View.GONE);
            autoCompleteLayout.setVisibility(View.GONE);
        }

        if (!isAccessibilityEnabled() || !SPHelper.isShowWindow(MainActivity.this)) {
            showOrDismiss.setChecked(false);
            FloatWidnow.dismiss();
        } else {
            showOrDismiss.setChecked(true);
            String color = SPHelper.getTextColor(this);
            colorModule.setBackgroundColor(Color.parseColor(color));
            SPHelper.setIsShowWindow(this, true);
            setVisibileViews(View.VISIBLE);
            autoCompeleteSwith.setChecked(SPHelper.isEnableAutoCompelete(this));
            autoCompleteLayout.setVisibility(SPHelper.isEnableAutoCompelete(this) ? View.VISIBLE :
                    View.GONE);
            Log.d("zlt", "onResume : autocCompeleteSwitch set Checked " + SPHelper.isEnableAutoCompelete(this));
        }

        if (!showOrDismiss.isChecked()) {
            setVisibileViews(View.GONE);
        } else {
            showPackage.setChecked(SPHelper.isShowPackageName(this));
        }

    }

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {

        if (!isAccessibilityEnabled() && b) {
            showEnableServiceDialog(compoundButton);
            return;
        }

        switch (compoundButton.getId()) {
            case R.id.show_dismiss_dialog:
                SPHelper.setIsShowWindow(MainActivity.this, b);
                setVisibileViews(b ? View.VISIBLE : View.GONE);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DetectionService.ACTION_UPDATE_UI));
                break;
            case R.id.show_packagename:
                SPHelper.setIsShowPackageName(MainActivity.this, b);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DetectionService.ACTION_UPDATE_UI));
                break;
            case R.id.auto_compelete_switch:
                SPHelper.setIsEnableAutoCompelete(MainActivity.this, b);
                autoCompleteLayout.setVisibility(b ? View.VISIBLE : View.GONE);
                Log.i("zlt", "autoCompleteLayout setVisibility " + b);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.modify_text_color) {
            if (!pickerDialog.isShowing()) {
                pickerDialog.show();
            }
        }
    }

    @Override
    public void onColorPicked(int color, String hexVal) {
        SPHelper.setTextColor(this, hexVal);
        colorModule.setBackgroundColor(Color.parseColor(hexVal));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DetectionService.ACTION_UPDATE_UI));
    }

    /**
     * 判断辅助服务是否打开
     *
     * @return
     */
    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String ACCESSIBILITY_SERVICE_NAME = "com.example.zlt.topactivity/com.example.zlt.service.DetectionService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {

            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)) {
                        return true;
                    }
                }
            }
        }
        return accessibilityFound;
    }

    private void setVisibileViews(int visible) {
        outLayout.setVisibility(visible);
        if (visible == View.VISIBLE) {
            String color = SPHelper.getTextColor(this);
            colorModule.setBackgroundColor(Color.parseColor(color));
        }
    }

    private void showEnableServiceDialog(final CompoundButton compoundButton) {
        new AlertDialog.Builder(this)
                .setMessage("需要你在辅助功能里开启权限,才能正常工作.")
                .setPositiveButton(android.R.string.ok
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
                                startActivityForResult(intent, 0);
                            }
                        })
                .setNegativeButton(android.R.string.cancel
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                compoundButton.setChecked(false);
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        compoundButton.setChecked(false);
                    }
                })
                .create()
                .show();
    }

}
