package com.ronda.serialportassist;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.ronda.serialportassist.serialport.ChatService;
import com.ronda.serialportassist.serialport.SerialPortFinder;
import com.ronda.serialportassist.view.LSpinner;

public class MainActivity extends AppCompatActivity implements LSpinner.OnSelectedListener,
        AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private LSpinner<String> mSpinnerDevice, mSpinnerBaudrate;
    private Switch mSwichView;
    private EditText mEtSend, mEtReceive;
    private Button mBtnSend, mBtnClear;
    private ChatService mChatService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initEvent();
    }

    private void initView() {

        mSpinnerDevice = (LSpinner<String>) findViewById(R.id.spinner_device);
        mSpinnerBaudrate = (LSpinner<String>) findViewById(R.id.spinner_baudrate);
        mSwichView = (Switch) findViewById(R.id.switch_view);

        mEtSend = (EditText) findViewById(R.id.et_send);
        mEtReceive = (EditText) findViewById(R.id.et_receive);

        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnClear = (Button) findViewById(R.id.btn_clear);

        mSpinnerDevice.setData(new SerialPortFinder().getAllDevicesPath());
        mSpinnerBaudrate.setData(getResources().getStringArray(R.array.baudrates));

        setViewState(false);
    }

    private void initEvent() {

        mSpinnerDevice.setOnItemSelectedListener(this);
        mSpinnerBaudrate.setOnItemSelectedListener(this);

        mSwichView.setOnCheckedChangeListener(this);

        mBtnSend.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);

    }

    //==============OnItemSelectedListener===================
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        closeSerial();
    }

    //============OnCheckedChangeListener======================
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            openSerial(mSpinnerDevice.getSelectedItem(), Integer.parseInt(mSpinnerBaudrate.getSelectedItem()));
        } else {
            closeSerial();
        }
    }

    //===============OnClickListener================
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_send:
                if (mChatService != null) {
                    String msg = mEtSend.getText().toString();
                    mChatService.write(msg.getBytes());
                }
                break;
            case R.id.btn_clear:
                mEtReceive.setText("");
                break;
        }
    }

    // 打开串口通信
    private void openSerial(String devicePath, int baudrate) {
        mChatService = new ChatService(devicePath, baudrate, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                mEtReceive.append((String) msg.obj);
            }
        });

        if (mChatService.isActive()) {
            setViewState(true);
        } else {
            setViewState(false);
            Toast.makeText(MainActivity.this, "重量串口不能使用", Toast.LENGTH_SHORT).show();
        }
    }


    // 关闭串口通信
    private void closeSerial() {
        if (mChatService != null) {
            mChatService.closeSerial();
            mChatService = null;
        }

        //设置View不可用
        setViewState(false);
    }

    // 设置相关View状态
    private void setViewState(boolean enable) {
        mSwichView.setChecked(enable);
        mEtSend.setEnabled(enable);
        mBtnSend.setEnabled(enable);
    }
}
