package com.thirdarm.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thirdarm.chat.MmsSms.MmsObject;
import com.thirdarm.chat.MmsSms.MmsSmsHelper;
import com.thirdarm.chat.MmsSms.SmsObject;
import com.thirdarm.chat.ui.MessageListAdapter;
import com.thirdarm.chat.utils.Utils;

public class MessageListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MessageListAdapter.MessageListAdapterSmsOnClickHandler,
        MessageListAdapter.MessageListAdapterMmsOnClickHandler {

    private static final String LOG_TAG = MessageListFragment.class.getSimpleName();

    RecyclerView rv_messageList;
    MessageListAdapter messageListAdapter;

    private static final int ID_SMS_HISTORY_LOADER = 101;

    // MmsSms base columns - for conversation use (rolled up)
    // TODO: Does not contain group convos. Does not contain MMS
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

    // sms base columns
    public static final String[] SMS_MESSAGES_PROJECTION = {
            Telephony.TextBasedSmsColumns.ADDRESS,
            Telephony.TextBasedSmsColumns.BODY,
            Telephony.TextBasedSmsColumns.CREATOR,
            Telephony.TextBasedSmsColumns.DATE,
            Telephony.TextBasedSmsColumns.DATE_SENT,
            Telephony.TextBasedSmsColumns.ERROR_CODE,
            Telephony.TextBasedSmsColumns.LOCKED,
            Telephony.TextBasedSmsColumns.PERSON,
            Telephony.TextBasedSmsColumns.PROTOCOL,
            Telephony.TextBasedSmsColumns.READ,
            Telephony.TextBasedSmsColumns.SEEN,
            Telephony.TextBasedSmsColumns.SERVICE_CENTER,
            Telephony.TextBasedSmsColumns.SUBJECT,
            Telephony.TextBasedSmsColumns.THREAD_ID,
            Telephony.TextBasedSmsColumns.TYPE
    };

    public static final int INDEX_SMS_MESSAGES_ADDRESS = 0;
    public static final int INDEX_SMS_MESSAGES_BODY = 1;
    public static final int INDEX_SMS_MESSAGES_CREATOR = 2;
    public static final int INDEX_SMS_MESSAGES_DATE_RECEIVED = 3;
    public static final int INDEX_SMS_MESSAGES_DATE_SENT = 4;
    public static final int INDEX_SMS_MESSAGES_ERROR_CODE = 5;
    public static final int INDEX_SMS_MESSAGES_LOCKED = 6;
    public static final int INDEX_SMS_MESSAGES_PERSON_SENDER_ID = 7;
    public static final int INDEX_SMS_MESSAGES_PROTOCOL_ID = 8;
    public static final int INDEX_SMS_MESSAGES_READ = 9;
    public static final int INDEX_SMS_MESSAGES_SEEN = 10;
    public static final int INDEX_SMS_MESSAGES_SERVICE_CENTER = 11;
    public static final int INDEX_SMS_MESSAGES_SUBJECT = 12;
    public static final int INDEX_SMS_MESSAGES_THREAD_ID = 13;
    public static final int INDEX_SMS_MESSAGES_TYPE = 14;

    // mms base columns
    public static final String[] MMS_MESSAGES_PROJECTION = {
            Telephony.BaseMmsColumns.DATE
    };

    public static final int INDEX_MMS_MESSAGES_ADDRESS = 0;
    public static final int INDEX_MMS_MESSAGES_BODY = 1;
    public static final int INDEX_MMS_MESSAGES_CREATOR = 2;
    public static final int INDEX_MMS_MESSAGES_DATE_RECEIVED = 3;
    public static final int INDEX_MMS_MESSAGES_DATE_SENT = 4;
    public static final int INDEX_MMS_MESSAGES_ERROR_CODE = 5;
    public static final int INDEX_MMS_MESSAGES_LOCKED = 6;
    public static final int INDEX_MMS_MESSAGES_PERSON_SENDER_ID = 7;
    public static final int INDEX_MMS_MESSAGES_PROTOCOL_ID = 8;
    public static final int INDEX_MMS_MESSAGES_READ = 9;
    public static final int INDEX_MMS_MESSAGES_SEEN = 10;
    public static final int INDEX_MMS_MESSAGES_SERVICE_CENTER = 11;
    public static final int INDEX_MMS_MESSAGES_SUBJECT = 12;
    public static final int INDEX_MMS_MESSAGES_THREAD_ID = 13;
    public static final int INDEX_MMS_MESSAGES_TYPE = 14;


    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(SmsObject smsObject) {
        MmsSmsHelper.logSmsObjectFields(smsObject, LOG_TAG + ": Sms log");

        //Intent intent = new Intent(getActivity(), ComposeMessageActivity.class);
        //startActivity(intent);
    }

    @Override
    public void onClick(MmsObject mmsObject) {
        MmsSmsHelper.logMmsObjectFields(mmsObject, LOG_TAG + ": Mms log");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        rv_messageList = (RecyclerView) view.findViewById(R.id.message_list_recyclerview);

        messageListAdapter = new MessageListAdapter(getContext(), this, this);
        rv_messageList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv_messageList.setAdapter(messageListAdapter);

        initializeLoading();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MmsSmsHelper.READ_CONTACTS_PERMISSIONS_REQUEST:
                // upon first granting of read permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSMSMessages();
                }
                break;
            case MmsSmsHelper.READ_SMS_PERMISSIONS_REQUEST:
                // upon first granting of send permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getActivity().getSupportLoaderManager().initLoader(ID_SMS_HISTORY_LOADER, null, this);
                }
                break;
        }
    }

    // helper method to get text messages
    public void loadSMSMessages() {
        if (getPermissionToReadSMS()) {
            getActivity().getSupportLoaderManager().initLoader(ID_SMS_HISTORY_LOADER, null, this);
        } else {
            // code is run in onRequestPermissionsResult
        }
    }

    // 2 step process:
    // - permission to read contacts
    // - permission to read SMS
    public void initializeLoading() {
        if (getPermissionToReadContacts()) {
            loadSMSMessages();
        } else {
            // code is run in onRequestPermissionsResult
        }
    }

    // Checks for permission to read SMS. returns true if permission is already granted
    private boolean getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            // if permission not granted...
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {

            } else {
                requestPermissions(
                        new String[]{Manifest.permission.READ_SMS},
                        MmsSmsHelper.READ_SMS_PERMISSIONS_REQUEST
                );
            }
            return false;
        } else {
            return true;
        }
    }

    // Checks for permissions to read contacts. returns true if permission is already granted
    private boolean getPermissionToReadContacts() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {

            } else {
                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MmsSmsHelper.READ_CONTACTS_PERMISSIONS_REQUEST
                );
            }
            return false;
        } else {
            return true;
        }
    }

    ///
    /// Loader stuff
    ///

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_SMS_HISTORY_LOADER:
                // Looks at only all text-based SMS messages (TODO: join it with MMS messages as well)
                Uri messageListUri = Uri.parse("content://mms-sms/conversations"); // We can use Telephony.MmsSms.CONTENT_CONVERSATIONS_URI for the message list. (TODO: still need to figure out how to show both MMS and SMS together for the compose message screen)

                String sortOrder = MmsSmsHelper.COLUMN_NORMALIZED_DATE + " desc";

                return new CursorLoader(
                        getContext(),
                        messageListUri,
                        MAIN_MESSAGES_PROJECTION,
                        null,
                        null,
                        sortOrder
                );

            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            Utils.showToast(getContext(), "Length: " + data.getCount());
        } else {
            Utils.showToast(getContext(), "Cursor is null");
        }
        messageListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        messageListAdapter.swapCursor(null);
    }

}