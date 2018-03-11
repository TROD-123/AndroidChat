package com.thirdarm.chat.MmsSms;

import android.provider.BaseColumns;
import android.provider.Telephony;

/**
 * Created by TROD on 20180310.
 */

public final class MmsSmsColumns {

    private MmsSmsColumns() {}

    // MmsSms base columns - for listing conversation threads (rolled up)
    // TODO: Filter columns to what you actually need
    public static final String[] MAIN_MESSAGES_PROJECTION = {
            // COMMON MMS AND SMS COLUMNS
            BaseColumns._ID,
            BaseColumns._ID, // for categorizing mms vs sms messages
            MmsSmsHelper.COLUMN_NORMALIZED_DATE, // for fixing MMS dates
            Telephony.TextBasedSmsColumns.DATE, // incorrect for MMS
            Telephony.TextBasedSmsColumns.DATE_SENT, // incorrect for MMS
            Telephony.TextBasedSmsColumns.LOCKED, // MMS OK
            Telephony.TextBasedSmsColumns.READ, // MMS OK
            Telephony.TextBasedSmsColumns.SUBJECT, // MMS OK
            Telephony.TextBasedSmsColumns.THREAD_ID, // MMS OK

            // SMS COLUMNS ONLY
            Telephony.TextBasedSmsColumns.ADDRESS, // null for MMS
            Telephony.TextBasedSmsColumns.BODY, // null for MMS
            Telephony.TextBasedSmsColumns.ERROR_CODE, // null for MMS
            Telephony.TextBasedSmsColumns.PERSON, // null for MMS
            Telephony.TextBasedSmsColumns.SERVICE_CENTER, // null for MMS
            Telephony.TextBasedSmsColumns.TYPE, // null for MMS

            // MMS COLUMNS ONLY
            Telephony.BaseMmsColumns.CONTENT_CLASS,
            Telephony.BaseMmsColumns.CONTENT_LOCATION,
            Telephony.BaseMmsColumns.CONTENT_TYPE,
            Telephony.BaseMmsColumns.EXPIRY,
            Telephony.BaseMmsColumns.MESSAGE_BOX,
            Telephony.BaseMmsColumns.MESSAGE_CLASS,
            Telephony.BaseMmsColumns.MESSAGE_ID,
            Telephony.BaseMmsColumns.MESSAGE_SIZE,
            Telephony.BaseMmsColumns.MESSAGE_TYPE,
            Telephony.BaseMmsColumns.MMS_VERSION,
            Telephony.BaseMmsColumns.PRIORITY,
            Telephony.BaseMmsColumns.STATUS,
            Telephony.BaseMmsColumns.SUBJECT_CHARSET,
            Telephony.BaseMmsColumns.TEXT_ONLY,
            BaseColumns._ID
    };

    public static final int INDEX_MESSAGES_ID = 0;
    public static final int INDEX_MESSAGES_TYPE_DISCRIMINATOR = 1;
    public static final int INDEX_MESSAGES_DATE_NORMALIZED = 2;
    public static final int INDEX_MESSAGES_DATE_RECEIVED = 3;
    public static final int INDEX_MESSAGES_DATE_SENT = 4;
    public static final int INDEX_MESSAGES_LOCKED = 5;
    public static final int INDEX_MESSAGES_READ = 6;
    public static final int INDEX_MESSAGES_SUBJECT = 7;
    public static final int INDEX_MESSAGES_THREAD_ID = 8;

    public static final int INDEX_MESSAGES_ADDRESS = 9;
    public static final int INDEX_MESSAGES_BODY = 10;
    public static final int INDEX_MESSAGES_ERROR_CODE = 11;
    public static final int INDEX_MESSAGES_PERSON_SENDER_ID = 12;
    public static final int INDEX_MESSAGES_SERVICE_CENTER = 13;
    public static final int INDEX_MESSAGES_TYPE = 14;

    public static final int INDEX_MESSAGES_CONTENT_CLASS = 15;
    public static final int INDEX_MESSAGES_CONTENT_LOCATION = 16;
    public static final int INDEX_MESSAGES_CONTENT_TYPE = 17;
    public static final int INDEX_MESSAGES_EXPIRY = 18;
    public static final int INDEX_MESSAGES_MESSAGE_BOX = 19;
    public static final int INDEX_MESSAGES_MESSAGE_CLASS = 20;
    public static final int INDEX_MESSAGES_MESSAGE_ID = 21;
    public static final int INDEX_MESSAGES_MESSAGE_SIZE = 22;
    public static final int INDEX_MESSAGES_MESSAGE_TYPE = 23;
    public static final int INDEX_MESSAGES_MMS_VERSION = 24;
    public static final int INDEX_MESSAGES_PRIORITY = 25;
    public static final int INDEX_MESSAGES_STATUS = 26;
    public static final int INDEX_MESSAGES_SUBJECT_CHARSET = 27;
    public static final int INDEX_MESSAGES_TEXT_ONLY = 28;

    // MmsSms base columns - for individual conversation thread use
    // TODO: Filter columns to what you actually need
    public static final String[] MAIN_MESSAGING_PROJECTION = {
            // COMMON MMS AND SMS COLUMNS
            BaseColumns._ID,
            BaseColumns._ID, // for categorizing mms vs sms messages
            MmsSmsHelper.COLUMN_NORMALIZED_DATE, // for fixing MMS dates
            Telephony.TextBasedSmsColumns.DATE, // incorrect for MMS
            Telephony.TextBasedSmsColumns.DATE_SENT, // incorrect for MMS
            Telephony.TextBasedSmsColumns.LOCKED, // MMS OK
            Telephony.TextBasedSmsColumns.READ, // MMS OK
            Telephony.TextBasedSmsColumns.SUBJECT, // MMS OK
            Telephony.TextBasedSmsColumns.THREAD_ID, // MMS OK

            // SMS COLUMNS ONLY
            Telephony.TextBasedSmsColumns.ADDRESS, // null for MMS
            Telephony.TextBasedSmsColumns.BODY, // null for MMS
            Telephony.TextBasedSmsColumns.ERROR_CODE, // null for MMS
            Telephony.TextBasedSmsColumns.PERSON, // null for MMS
            Telephony.TextBasedSmsColumns.SERVICE_CENTER, // null for MMS
            Telephony.TextBasedSmsColumns.TYPE, // null for MMS

            // MMS COLUMNS ONLY
            Telephony.BaseMmsColumns.CONTENT_CLASS,
            Telephony.BaseMmsColumns.CONTENT_LOCATION,
            Telephony.BaseMmsColumns.CONTENT_TYPE,
            Telephony.BaseMmsColumns.EXPIRY,
            Telephony.BaseMmsColumns.MESSAGE_BOX,
            Telephony.BaseMmsColumns.MESSAGE_CLASS,
            Telephony.BaseMmsColumns.MESSAGE_ID,
            Telephony.BaseMmsColumns.MESSAGE_SIZE,
            Telephony.BaseMmsColumns.MESSAGE_TYPE,
            Telephony.BaseMmsColumns.MMS_VERSION,
            Telephony.BaseMmsColumns.PRIORITY,
            Telephony.BaseMmsColumns.STATUS,
            Telephony.BaseMmsColumns.SUBJECT_CHARSET,
            Telephony.BaseMmsColumns.TEXT_ONLY,
            BaseColumns._ID
    };
}
