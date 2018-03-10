package com.thirdarm.chat.MmsSms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for MmsSms handling
 */
public final class MmsSmsHelper {

    // request codes
    public static final int READ_SMS_PERMISSIONS_REQUEST = 1001;
    public static final int SEND_SMS_PERMISSIONS_REQUEST = 1002;
    public static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1011;

    // helper constants
    public static final String COLUMN_NORMALIZED_DATE = "normalized_date";
    public static final int DATE_NORMALIZER_CONSTANT = 1000;


    private MmsSmsHelper() {
    }

    /**
     * Sends a text message while displaying a status toast
     *
     * @param context
     * @param phoneNo
     * @param msg
     */
    public static void sendTextMessage(Context context, String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(context, "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(context, ex.toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    /**
     * TODO: Stores a text message to the device's MmsSms Content provider
     *
     * @param context
     * @param originatingAddress
     * @param messageBody
     * @param timestamp
     */
    public static void storeTextMessage(Context context, String originatingAddress, String messageBody, long timestamp) {
        ContentResolver contentResolver = context.getContentResolver();
    }

    /**
     * Converts an array of phone number address strings into the associated contact names, if available in Contacts. If a contact does not exist, the phone number is provided instead
     * <p>
     * Note this runs on a separate thread, and your class must implement the ReadableAddressCallback for results to be properly returned
     *
     * @param context
     * @param addresses
     * @param callback
     * @return
     */
    public static String getReadableAddressString(@NonNull Context context, @NonNull String[] addresses, @NonNull ReadableAddressCallback callback, boolean runOnMainThread) {
        ReadableAddressCursor cursor = new ReadableAddressCursor(addresses, callback);
        if (runOnMainThread) {
            return cursor.doInBackground(context);
        } else {
            cursor.execute(context);
            return null;
        }
    }

    /**
     * Callback for returning the readable address to the caller
     */
    public interface ReadableAddressCallback {
        void returnReadableAddress(String result);
    }

    /**
     * Separate thread to convert addresses (phone numbers) into contact names, if available.
     * Callback returns results
     */
    private static class ReadableAddressCursor extends AsyncTask<Context, Void, String> {

        String[] addresses;
        ReadableAddressCallback callback;

        ReadableAddressCursor(@NonNull String[] addresses, @NonNull ReadableAddressCallback callback) {
            this.addresses = addresses;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Context... contexts) {
            List<String> contactNames = new ArrayList<>();

            for (String address : addresses) {
                Uri lookupUri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(address)
                );
                String[] columns = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                Cursor cursor = contexts[0].getContentResolver().query(lookupUri, columns, null, null, null);

                if (cursor != null && cursor.moveToNext()) {
                    // store the contact name if available
                    address = cursor.getString(0);
                    contactNames.add(address);
                    cursor.close();
                } else {
                    // otherwise, store the number, in a consistent format
                    if (address != null) {
                        String formatted = PhoneNumberUtils.formatNumber(address, "us");
                        if (formatted == null) {
                            formatted = address;
                        }
                        contactNames.add(formatted);
                    }
                }
            }

            return TextUtils.join(", ", contactNames);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.returnReadableAddress(s);
        }
    }

    /**
     * Grabs the phone number addresses associated with the Mms message id and calls getReadableAddressString() to convert the phone numbers into their associated contact name, if available in Contacts. Otherwise, the phone number is returned
     * <p>
     * Note this runs on a separate thread, and your class must implement the ReadableAddressCallback for results to be properly returned
     *
     * @param context
     * @param id
     * @param callback
     * @param convertToReadable If false, returns the pre-formatted addresses to the callback
     */
    public static void getAddressFromMms(@NonNull Context context, @NonNull String id, @NonNull ReadableAddressCallback callback, boolean convertToReadable) {
        MmsAddressCursor cursor = new MmsAddressCursor(id, callback, convertToReadable);
        cursor.execute(context);
    }

    /**
     * Separate thread to collect phone number addresses from the associated Mms message id. Callback returns results
     */
    private static class MmsAddressCursor extends AsyncTask<Context, Void, String> {

        String id;
        ReadableAddressCallback callback;
        boolean convertToReadable;

        List<String> numbers = new ArrayList<>();

        MmsAddressCursor(String id, ReadableAddressCallback callback, boolean convertToReadable) {
            this.id = id;
            this.callback = callback;
            this.convertToReadable = convertToReadable;
        }

        @Override
        protected String doInBackground(Context... contexts) {
            // use the msg_id to grab address info
            Uri lookupUri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", id));
            String selection = "msg_id = " + id;
            String[] columns = new String[]{"address"};

            Cursor cursor = contexts[0].getContentResolver().query(lookupUri, columns, selection, null, null);

            if (cursor != null) {
                // store all available addresses
                while (cursor.moveToNext()) {
                    String t = cursor.getString(0);
                    if (!t.contains("insert")) {
                        numbers.add(t);
                    }
                }
                cursor.close();
            }

            if (convertToReadable) {
                getReadableAddressString(contexts[0], numbers.toArray(new String[]{}), callback, false);
                return null;
            } else {
                return TextUtils.join(", ", numbers);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            // only called if convertToReadable is false
            super.onPostExecute(s);
            callback.returnReadableAddress(s);
        }
    }

    /**
     * Debug method for viewing object field values
     *
     * @param smsObject
     * @param tag
     */
    public static void logSmsObjectFields(SmsObject smsObject, String tag) {
        Log.v(tag, "--------------------");
        Log.v(tag, "Address: " + smsObject.getAddress());
        Log.v(tag, "Body: " + smsObject.getBody());
        Log.v(tag, "Creator: " + smsObject.getCreator());
        Log.v(tag, "Date Received: " + smsObject.getDateReceived());
        Log.v(tag, "Date Sent: " + smsObject.getDateSent());
        Log.v(tag, "Error code: " + smsObject.getErrorCode());
        Log.v(tag, "Locked: " + smsObject.isLocked());
        Log.v(tag, "Person Sender Id: " + smsObject.getPersonSenderId());
        Log.v(tag, "Protocol Id: " + smsObject.getProtocolId());
        Log.v(tag, "Read: " + smsObject.isRead());
        Log.v(tag, "Seen: " + smsObject.isSeen());
        Log.v(tag, "Service Center: " + smsObject.getServiceCenter());
        Log.v(tag, "Subject: " + smsObject.getSubject());
        Log.v(tag, "Thread Id: " + smsObject.getThreadId());
        Log.v(tag, "Type: " + smsObject.getType());
    }

    /**
     * Debug method for viewing object field values
     *
     * @param mmsObject
     * @param tag
     */
    public static void logMmsObjectFields(MmsObject mmsObject, String tag) {
        Log.v(tag, "--------------------");
        Log.v(tag, "Content class: " + mmsObject.getContentClass());
        Log.v(tag, "Content location: " + mmsObject.getContentLocation());
        Log.v(tag, "Content type: " + mmsObject.getContentType());
        Log.v(tag, "Creator: " + mmsObject.getCreator());
        Log.v(tag, "Date Received: " + mmsObject.getDateReceived());
        Log.v(tag, "Date Sent: " + mmsObject.getDateSent());
        Log.v(tag, "Epiry time: " + mmsObject.getExpiryTime());
        Log.v(tag, "Locked: " + mmsObject.isLocked());
        Log.v(tag, "Message box: " + mmsObject.getMessageBox());
        Log.v(tag, "Message class: " + mmsObject.getMessageClass());
        Log.v(tag, "Message id: " + mmsObject.getMessageId());
        Log.v(tag, "Message size: " + mmsObject.getMessageSize());
        Log.v(tag, "Message type: " + mmsObject.getMessageType());
        Log.v(tag, "MMS version: " + mmsObject.getMmsVersion());
        Log.v(tag, "Priority: " + mmsObject.getPriority());
        Log.v(tag, "Read: " + mmsObject.isRead());
        Log.v(tag, "Seen: " + mmsObject.isSeen());
        Log.v(tag, "Status: " + mmsObject.getStatus());
        Log.v(tag, "Subject: " + mmsObject.getSubject());
        Log.v(tag, "Subject charset: " + mmsObject.getSubjectCharset());
        Log.v(tag, "Text only: " + mmsObject.isTextOnly());
        Log.v(tag, "Thread Id: " + mmsObject.getThreadId());
    }
}
