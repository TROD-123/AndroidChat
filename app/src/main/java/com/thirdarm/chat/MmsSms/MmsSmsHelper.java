package com.thirdarm.chat.MmsSms;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for MmsSms handling
 */
public final class MmsSmsHelper {

    private static final String LOG_TAG = MmsSmsHelper.class.getSimpleName();

    // request codes
    public static final int READ_SMS_PERMISSIONS_REQUEST = 1001;
    public static final int SEND_SMS_PERMISSIONS_REQUEST = 1002;
    public static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1011;

    // helper constants
    public static final String COLUMN_NORMALIZED_DATE = "normalized_date";
    public static final int DATE_NORMALIZER_CONSTANT = 1000;

    // keys
    public static final String MESSAGING_THREAD_KEY = "messaging-thread-id";
    public static final String MESSAGING_THREAD_ADDRESS_NUMBERS_OUTGOING_KEY =
            "messaging-thread-address-numbers-outgoing-key";

    // status codes
    public static final int MESSAGE_DELIVERY_STATUS_SUCCESS = 0;
    public static final int MESSAGE_DELIVERY_STATUS_FAILURE = 1;

    // default values
    public static final long DEFAULT_MESSAGING_THREAD_ID = -1L;


    private MmsSmsHelper() {
    }

    /**
     * Sends a text message while displaying a status toast
     * TODO: Allow sending to multiple addresses
     * TODO: Allow MMS functionality
     * TODO: Need to store sent messages to internal content provider (is it outbox?)
     *
     * @param context
     * @param phoneNo
     * @param msg
     */
    public static void sendTextMessage(Context context, String phoneNo, String msg,
                                       MessageDeliveryCallbacks callbacks) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            callbacks.onMessageSent(MESSAGE_DELIVERY_STATUS_SUCCESS);
        } catch (Exception ex) {
            ex.printStackTrace();
            callbacks.onMessageSent(MESSAGE_DELIVERY_STATUS_FAILURE);
        }
    }

    public interface MessageDeliveryCallbacks {
        void onMessageSent(int statusCode);
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
    public static String getReadableAddressString(@NonNull Context context, @NonNull String[] addresses, @Nullable ReadableAddressCallback callback, boolean runOnMainThread) {
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
     * TODO: Clean this up. Use a loader?
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
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

            for (String address : addresses) {
                Uri lookupUri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(address)
                );
                String[] columns = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                Cursor cursor = contexts[0].getContentResolver().query(lookupUri, columns,
                        null, null, null);

                if (cursor != null && cursor.moveToNext()) {
                    // store the contact name if available
                    address = cursor.getString(0);
                    contactNames.add(address);
                    cursor.close();
                } else {
                    // otherwise, store the number, in a consistent format
                    if (address != null) {
                        String formatted = address;
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                            // handle e-mail addresses here
                        } else {
                            // otherwise assume address is a phone number
                            Phonenumber.PhoneNumber number;
                            try {
                                number = phoneUtil.parse(address, Locale.getDefault().getCountry());
                                formatted = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                            } catch (NumberParseException e) {
                                Log.e(LOG_TAG, "NumberParseException was thrown: " + address);
                                e.printStackTrace();
                            }
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
    public static String[] getAddressFromMms(@NonNull Context context, @NonNull String id,
                                             @Nullable ReadableAddressCallback callback,
                                             boolean convertToReadable, boolean runOnMainThread) {
        MmsAddressCursor cursor = new MmsAddressCursor(id, callback, convertToReadable, runOnMainThread);
        if (runOnMainThread) {
            return cursor.doInBackground(context);
        } else {
            cursor.execute(context);
            return null;
        }
    }

    /**
     * Separate thread to collect phone number addresses from the associated Mms message id. Callback returns results
     * TODO: Clean this up. Use a loader?
     */
    private static class MmsAddressCursor extends AsyncTask<Context, Void, String[]> {

        String id;
        ReadableAddressCallback callback;
        boolean convertToReadable, runOnMainThread;

        List<String> numbers = new ArrayList<>();

        MmsAddressCursor(String id, ReadableAddressCallback callback, boolean convertToReadable,
                         boolean runOnMainThread) {
            this.id = id;
            this.callback = callback;
            this.convertToReadable = convertToReadable;
            this.runOnMainThread = runOnMainThread;
        }

        @Override
        protected String[] doInBackground(Context... contexts) {
            // use the msg_id to grab address info
            Uri lookupUri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", id));
            String selection = "msg_id = " + id;
            String[] columns = new String[]{"address"};

            Cursor cursor = contexts[0].getContentResolver().query(lookupUri, columns, selection, null, null);

            if (cursor != null) {
                Log.d(LOG_TAG, "Cursor size for the id " + id + ": " + cursor.getCount());
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
                getReadableAddressString(contexts[0], numbers.toArray(new String[]{}), callback,
                        runOnMainThread);
                return null;
            } else {
                return numbers.toArray(new String[]{});
            }
        }
    }

    // TODO: Need to split into separate methods based on MIMEtype
    public static String getMmsData(Context context, String id, int messagebox) {
        String[] addresses = getAddressFromMms(context, id, null, false, true);
        String address = "";
        if (addresses != null && addresses.length > 0) {
            address = getReadableAddressString(context, new String[]{addresses[0]}, null, true);
        }
        Uri lookupUri = Uri.parse("content://mms/part");
        String selection = Telephony.Mms.Part.MSG_ID + " = ? ";
        String[] selectionArgs = new String[]{id};
        Cursor cursor = context.getContentResolver().query(
                lookupUri,
                null,
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            String partId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._ID));
            String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));

            switch (type) {
                // use MIME type to check for desired format
                case "text/plain":
                    // type is text
                    String data = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
                    String body;
                    if (data != null) {
                        // TODO: don't know what this is for
                        body = "There is text"; //getMmsText(context, partId);
                    } else {
                        // text is stored in cursor. access it directly
                        if (messagebox == Telephony.BaseMmsColumns.MESSAGE_BOX_OUTBOX ||
                                messagebox == Telephony.BaseMmsColumns.MESSAGE_BOX_SENT) {
                            // Append "You" if user is the sender. Otherwise, append the address (sender address is the first address in array)
                            body = "You: ";
                        } else {
                            body = address + ": ";
                        }
                        body += cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
                    }
                    return body;
                case "image/jpeg":
                case "image/bmp":
                case "image.gif":
                case "image/jpg":
                case "image/png":
                    // type is image
                    //Bitmap bitmap = getMmsImage(partId);
                    return "Image";
                case "audio":
                    // TODO: Set up for other media types, including audio, video, gifs? Look up their MIME types and figure out how to render them
                    break;
                default:

            }
            cursor.close();
        }
        return "Uhm.";
    }


    // TODO: Don't know if this is necessary, but this is for if there is no mms text in cursor
    public static String getMmsText(Context context, String _id) {
        Uri partUri = Uri.parse("content://mms/part" + _id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partUri);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String temp = br.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = br.readLine();
                }
            }
        } catch (IOException e) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        return sb.toString();
    }

    // TODO: Implement
    public static Bitmap getMmsImage(Context context, String _id) {
        return null;
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
