package com.thirdarm.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.thirdarm.chat.MmsSms.MmsObject;
import com.thirdarm.chat.MmsSms.MmsSmsColumns;
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

    private static final int ID_CONVERSATIONS_LIST_LOADER = 101;

    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(SmsObject smsObject) {
        MmsSmsHelper.logSmsObjectFields(smsObject, LOG_TAG + ": Sms log");

        long threadId = smsObject.getThreadId();
        String[] addressNumbersOutgoing = new String[]{smsObject.getAddress()};
        startMessagingActivity(threadId, addressNumbersOutgoing);
    }

    @Override
    public void onClick(MmsObject mmsObject) {
        MmsSmsHelper.logMmsObjectFields(mmsObject, LOG_TAG + ": Mms log");

        long threadId = mmsObject.getThreadId();
        String[] addressNumbersOutgoing = MmsSmsHelper.getAddressFromMms(getContext(),
                mmsObject.getBaseColumnId(), null, false, true);
        startMessagingActivity(threadId, addressNumbersOutgoing);
    }

    private void startMessagingActivity(long threadId, String[] addressNumbersOutgoing) {
        Intent intent = new Intent(getActivity(), ComposeMessageActivity.class);
        intent.putExtra(MmsSmsHelper.MESSAGING_THREAD_KEY, threadId);
        intent.putExtra(MmsSmsHelper.MESSAGING_THREAD_ADDRESS_NUMBERS_OUTGOING_KEY, addressNumbersOutgoing);
        startActivity(intent);
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
                    getActivity().getSupportLoaderManager().initLoader(ID_CONVERSATIONS_LIST_LOADER, null, this);
                }
                break;
        }
    }

    // helper method to get text messages
    public void loadSMSMessages() {
        if (getPermissionToReadSMS()) {
            getActivity().getSupportLoaderManager().initLoader(ID_CONVERSATIONS_LIST_LOADER, null, this);
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
            case ID_CONVERSATIONS_LIST_LOADER:
                Uri messageListUri = Uri.parse("content://mms-sms/conversations"); // this is the same as Telephony.MmsSms.CONTENT_CONVERSATIONS_URI

                String sortOrder = MmsSmsHelper.COLUMN_NORMALIZED_DATE + " desc";

                return new CursorLoader(
                        getContext(),
                        messageListUri,
                        MmsSmsColumns.MAIN_MESSAGES_PROJECTION,
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
        if (data != null && data.getCount() > 0) {
            messageListAdapter.swapCursor(data);
        } else {
            Log.e(LOG_TAG, "The cursor is null!");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        messageListAdapter.swapCursor(null);
    }

}