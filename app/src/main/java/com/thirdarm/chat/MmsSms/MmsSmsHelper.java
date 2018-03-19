package com.thirdarm.chat.MmsSms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Telephone;

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

    // mimetype targets
    public static final int MIME_TYPE_UNDEFINED = -1;
    public static final int MIME_TYPE_TEXT_PLAIN = 0;
    public static final int MIME_TYPE_TEXT_VCARD = 1;
    public static final int MIME_TYPE_IMAGE = 2;
    public static final int MIME_TYPE_GIF = 3;
    public static final int MIME_TYPE_VIDEO = 4;
    public static final int MIME_TYPE_AUDIO = 5;
    public static final int MIME_TYPE_SMIL = 6;


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
     * Returns the given array with only unique address values
     *
     * @param addresses
     * @return
     */
    public static String[] getUniqueAddressesFromArray(String[] addresses) {
        List<String> uniqueAddresses = new ArrayList<>();
        for (String address : addresses) {
            if (!uniqueAddresses.contains(address)) {
                uniqueAddresses.add(address);
            }
        }
        return uniqueAddresses.toArray(new String[]{});
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
        addresses = getUniqueAddressesFromArray(addresses);
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

    /**
     * Returns a cursor for the associated mms message id
     *
     * @param context
     * @param msg_id  The message id, equivalent to _id from mms-sms/conversations
     * @return
     */
    public static Cursor getMmsMessageCursor(Context context, String msg_id) {
        Uri lookupUri = Uri.parse("content://mms/part");
        String selection = Telephony.Mms.Part.MSG_ID + " = ? ";
        String[] selectionArgs = new String[]{msg_id};
        return context.getContentResolver().query(
                lookupUri,
                null,
                selection,
                selectionArgs,
                null
        );
    }

    /**
     * Retrieves the mimetype of a mms cursor at a specific position. Throws an exception if the cursor is empty
     *
     * @param mmsMessageCursor
     * @return
     */
    public static String getMimeTypeFromMmsCursorAtPosition(@NonNull Cursor mmsMessageCursor, int position) {
        if (mmsMessageCursor.moveToPosition(position)) {
            return mmsMessageCursor.getString(mmsMessageCursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
        } else {
            throw new UnsupportedOperationException("The passed cursor is empty.");
        }
    }

    /**
     * Mms cursors may contain multiple mimeType components. Extracts all of them
     *
     * @param mmsMessageCursor
     * @return
     */
    public static String[] getAllMimeTypesFromMmsCursor(@NonNull Cursor mmsMessageCursor) {
        List<String> mimeTypes = new ArrayList<>();
        while (mmsMessageCursor.moveToNext()) {
            String mimeType = mmsMessageCursor.getString(mmsMessageCursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
            mimeTypes.add(mimeType);
        }
        if (mimeTypes.size() > 0) {
            return mimeTypes.toArray(new String[]{});
        } else {
            throw new UnsupportedOperationException("The passed cursor is empty.");
        }
    }

    /**
     * Matches string mimetype to category constants
     *
     * @param mimeType
     * @return
     */
    public static int matchMimeType(@NonNull String mimeType) {
        String[] plaintextTypes = new String[]{"text/plain"};
        String[] vCardTypes = new String[]{"text/vCard", "text/x-vCard"};
        String[] imageTypes = new String[]{"image/jpeg", "image/bmp", "image/jpg", "image/png"};
        String[] gifTypes = new String[]{"image/gif"};
        String[] videoTypes = new String[]{"video/*", "video/mp4"};
        String[] audioTypes = new String[]{"audio"};
        String[] smil = new String[]{"application/smil"};

        if (Arrays.asList(plaintextTypes).contains(mimeType)) {
            return MIME_TYPE_TEXT_PLAIN;
        } else if (Arrays.asList(vCardTypes).contains(mimeType)) {
            return MIME_TYPE_TEXT_VCARD;
        } else if (Arrays.asList(imageTypes).contains(mimeType)) {
            return MIME_TYPE_IMAGE;
        } else if (Arrays.asList(gifTypes).contains(mimeType)) {
            return MIME_TYPE_GIF;
        } else if (Arrays.asList(videoTypes).contains(mimeType)) {
            return MIME_TYPE_VIDEO;
        } else if (Arrays.asList(audioTypes).contains(mimeType)) {
            return MIME_TYPE_AUDIO;
        } else if (Arrays.asList(smil).contains(mimeType)) {
            return MIME_TYPE_SMIL;
        } else {
            return MIME_TYPE_UNDEFINED;
        }
    }

    public static String getMmsText(@NonNull Context context, @NonNull String body,
                                    String data, String partId) {
        if (data != null) {
            // TODO: Figure out what this is for
            // for vCards...
            return getMmsTextFromPartId(context, partId);
        } else {
            // body already has text. return it
            return body;
        }
    }

    public static VCard getVCardObject(@NonNull Context context, String data, String partId) {
        if (data != null) {
            return getVCardFromPartId(context, partId);
        } else {
            throw new UnsupportedOperationException("The vCard passed does not have data.");
        }
    }

    public static String getVCardRawData(@NonNull Context context, String data, String partId) {
        if (data != null) {
            return getVCardStringFromPartId(context, partId);
        } else {
            throw new UnsupportedOperationException("The vCard passed does not have data.");
        }
    }

    public static Uri getMmsImageVideoUri(String partId) {
        return Uri.parse("content://mms/part/" + partId);
    }

    /**
     * Returns true if mms is sent by the user
     *
     * @param messageBox
     * @return
     */
    public static boolean checkIfMmsIsFromUser(int messageBox) {
        return messageBox == Telephony.BaseMmsColumns.MESSAGE_BOX_OUTBOX ||
                messageBox == Telephony.BaseMmsColumns.MESSAGE_BOX_SENT;
    }

    /**
     * Gets the name of the sender of mms. Returns "You" if user is the sender
     *
     * @param context
     * @param msg_id
     * @param messageBox
     * @return
     */
    public static String getSenderAddressFromMms(Context context, String msg_id, int messageBox) {
        if (checkIfMmsIsFromUser(messageBox)) {
            return "You";
        } else {
            String[] addresses = getAddressFromMms(context, msg_id, null, false, true);
            String address = "";
            if (addresses != null && addresses.length > 0) {
                address = getReadableAddressString(context, new String[]{addresses[0]}, null, true);
            }
            return address;
        }
    }

    // TODO: Don't know if this is necessary, but this is for if there is no mms text in cursor
    private static String getMmsTextFromPartId(Context context, String _id) {
        Uri partUri = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partUri);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String temp = br.readLine();
                while (temp != null) {
                    sb.append(temp + "\r\n");
                    temp = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static VCard getVCardFromPartId(Context context, String _id) {
        String vCardText = getMmsTextFromPartId(context, _id);
        return Ezvcard.parse(vCardText).first();
    }

    /**
     * Creates a new file for the vCardRawData
     * @param context
     * @param vCardRawData
     * @return The path and filename of file output
     */
    public static String writeVCardDataToFile(Context context, String vCardRawData) {
        String fileName = "temp_vcard.vcf";
        // save to external files directory: /storage/emulated/0/Android/data/com.thirdarm.chat/files/
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream outputStream;
//        VCardWriter vcw = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(vCardRawData.getBytes());
            outputStream.close();
//            vcw = new VCardWriter(file, VCardVersion.V4_0);
//            vcw.write(vCard);
            Log.d(LOG_TAG, "vCard path: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVCardStringFromPartId(Context context, String _id) {
        return getMmsTextFromPartId(context, _id);
    }

    public static String getReadableVCardString(@NonNull VCard vCard) {
        String fullName = vCard.getFormattedName().getValue();
        List<Telephone> phoneNumbers = vCard.getTelephoneNumbers();
        String phoneNumberMain = "";
        if (phoneNumbers != null && phoneNumbers.size() > 0) {
            phoneNumberMain = phoneNumbers.get(0).getText();
        }
        return String.format("%s: %s", fullName, phoneNumberMain);
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
