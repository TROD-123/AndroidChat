package com.thirdarm.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thirdarm.chat.sms.SmsHelper;
import com.thirdarm.chat.utils.Utils;

public class MessagingFragment extends Fragment {

    String mPhoneNo, mMessage;

    EditText mEt_Message;
    Button mButton_send;

    public MessagingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messaging, container, false);

        mPhoneNo = "8182820973";

        mEt_Message = (EditText) view.findViewById(R.id.messaging_message_edittext);

        mButton_send = (Button) view.findViewById(R.id.messaging_button_send);
        mButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessage = mEt_Message.getText().toString();

                if (checkIfNumberMessageEmpty(mPhoneNo, mMessage)) {
                    sendSMSTextMessage();
                }
            }
        });

        // disable the send button if message field is empty
        mEt_Message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                enableDisableSendButton(editable);
            }
        });
        enableDisableSendButton(mEt_Message.getText());

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SmsHelper.SEND_SMS_PERMISSIONS_REQUEST:
                // upon first granting of send permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsHelper.sendTextMessage(getActivity(), mPhoneNo, mMessage);
                }
        }
    }

    // helper method to send text message
    public void sendSMSTextMessage() {
        if (getPermissionToSendSMS()) {
            SmsHelper.sendTextMessage(getActivity(), mPhoneNo, mMessage);
        } else {
            // code is run in onRequestPermissionsResult
        }
    }

    // Checks for permission to read SMS. returns true if permission is already granted
    private boolean getPermissionToSendSMS() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // if permission not granted...
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {

            } else {
                requestPermissions(
                        new String[]{Manifest.permission.SEND_SMS},
                        SmsHelper.SEND_SMS_PERMISSIONS_REQUEST
                );
            }
            return false;
        } else {
            return true;
        }
    }

    // final validation check (send button should be disabled if no phoneNo or message is provided)
    private boolean checkIfNumberMessageEmpty(String phoneNo, String message) {
        if (phoneNo == null || phoneNo.isEmpty()) {
            Utils.showToast(getContext(), "There is no phone number provided!");
            return false;
        } else if (message == null || message.isEmpty()) {
            Utils.showToast(getContext(), "There is no message provided!");
            return false;
        }
        return true;
    }

    // disable button if message box is empty. otherwise enable
    private void enableDisableSendButton(Editable editable) {
        if (editable.toString().isEmpty()) {
            mButton_send.setEnabled(false);
        } else {
            mButton_send.setEnabled(true);
        }
    }
}
