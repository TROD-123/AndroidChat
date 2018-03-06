package com.thirdarm.chat.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.thirdarm.chat.ui.NotificationUtils;
import com.thirdarm.chat.utils.Utils;

/**
 * Created by TROD on 20180304.
 */

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Telephony.Sms.Intents.SMS_DELIVER_ACTION:
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                Utils.showToast(context, "Number of SmsMessage[]: " + messages.length);

                if (messages.length > 0) {
                    SmsMessage message = messages[0];

                    String messageBody = message.getMessageBody();
                    String originatingAddress = message.getOriginatingAddress();
                    long timestamp = message.getTimestampMillis();

                    // TODO: Need to save down the message to local SMS content provider. This isn't done automatically upon receiving messages
                    SmsHelper.storeTextMessage(context, messageBody, originatingAddress, timestamp);

                    // TODO: Need to show notification of incoming message
                    NotificationUtils.createNoficiationReceivedMessage(context, messageBody, originatingAddress, timestamp);
                }
        }
    }
}
