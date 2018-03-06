package com.thirdarm.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thirdarm.chat.sms.SmsHelper;
import com.thirdarm.chat.sms.SmsObject;
import com.thirdarm.chat.ui.MessageListAdapter;
import com.thirdarm.chat.utils.Utils;

public class MessageListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, MessageListAdapter.MessageListAdapterOnClickHandler {

    private static final String LOG_TAG = MessageListFragment.class.getSimpleName();

    RecyclerView rv_messageList;
    MessageListAdapter messageListAdapter;

    private static final int ID_SMS_HISTORY_LOADER = 101;

    // base columns
    public static final String[] MAIN_MESSAGES_PROJECTION = {
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

    public static final int INDEX_MESSAGES_ADDRESS = 0;
    public static final int INDEX_MESSAGES_BODY = 1;
    public static final int INDEX_MESSAGES_CREATOR = 2;
    public static final int INDEX_MESSAGES_DATE_RECEIVED = 3;
    public static final int INDEX_MESSAGES_DATE_SENT = 4;
    public static final int INDEX_MESSAGES_ERROR_CODE = 5;
    public static final int INDEX_MESSAGES_LOCKED = 6;
    public static final int INDEX_MESSAGES_PERSON_SENDER_ID = 7;
    public static final int INDEX_MESSAGES_PROTOCOL_ID = 8;
    public static final int INDEX_MESSAGES_READ = 9;
    public static final int INDEX_MESSAGES_SEEN = 10;
    public static final int INDEX_MESSAGES_SERVICE_CENTER = 11;
    public static final int INDEX_MESSAGES_SUBJECT = 12;
    public static final int INDEX_MESSAGES_THREAD_ID = 13;
    public static final int INDEX_MESSAGES_TYPE = 14;

    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(SmsObject smsObject) {
        SmsHelper.logSmsObjectFields(smsObject, LOG_TAG + ": Sms log");

        //Intent intent = new Intent(getActivity(), ComposeMessageActivity.class);
        //startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        rv_messageList = (RecyclerView) view.findViewById(R.id.message_list_recyclerview);

        messageListAdapter = new MessageListAdapter(getContext(), this);
        rv_messageList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv_messageList.setAdapter(messageListAdapter);

        initializeLoading();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SmsHelper.READ_CONTACTS_PERMISSIONS_REQUEST:
                // upon first granting of read permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSMSMessages();
                }
                break;
            case SmsHelper.READ_SMS_PERMISSIONS_REQUEST:
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
                        SmsHelper.READ_SMS_PERMISSIONS_REQUEST
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
                        SmsHelper.READ_CONTACTS_PERMISSIONS_REQUEST
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
                Uri messageListUri = Telephony.Sms.CONTENT_URI;
                String sortOrder = Telephony.TextBasedSmsColumns.ADDRESS + ", " + Telephony.TextBasedSmsColumns.DATE + " DESC";

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