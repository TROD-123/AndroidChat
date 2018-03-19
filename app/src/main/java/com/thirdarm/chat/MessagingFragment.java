package com.thirdarm.chat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.thirdarm.chat.MmsSms.MmsSmsHelper;
import com.thirdarm.chat.ui.MessagingAdapter;
import com.thirdarm.chat.utils.Utils;

import java.io.File;

public class MessagingFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        MmsSmsHelper.MessageDeliveryCallbacks,
        MessagingAdapter.MessagingAdapterMmsVCardOnClickHandler {

    private static final String LOG_TAG = MessagingFragment.class.getSimpleName();

    String mMessageOutgoing, mAddressNamesOutgoing;
    String[] mAddressNumbersOutgoing;
    long mThreadId;

    RecyclerView mRv_messages;
    MessagingAdapter mMessagingAdapter;

    EditText mEt_messageOutgoing;
    Button mButton_send;

    private static final int ID_CONVERSATIONS_LOADER = 102;

    public MessagingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messaging, container, false);

        // set up recyclerview

        mRv_messages = (RecyclerView) view.findViewById(R.id.messaging_conversations_recyclerview);
        mMessagingAdapter = new MessagingAdapter(getContext(), this);

        mRv_messages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRv_messages.setAdapter(mMessagingAdapter);


        // load up initial data from intent

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            Bundle args = new Bundle();
            if (intent.hasExtra(MmsSmsHelper.MESSAGING_THREAD_KEY)) {
                mThreadId = intent.getLongExtra(
                        MmsSmsHelper.MESSAGING_THREAD_KEY, MmsSmsHelper.DEFAULT_MESSAGING_THREAD_ID);
                if (mThreadId != MmsSmsHelper.DEFAULT_MESSAGING_THREAD_ID) {
                    args.putLong(MmsSmsHelper.MESSAGING_THREAD_KEY, mThreadId);
                }
            } else {
                Log.e(LOG_TAG, "The thread id is null!");
            }

            if (intent.hasExtra(MmsSmsHelper.MESSAGING_THREAD_ADDRESS_NUMBERS_OUTGOING_KEY)) {
                mAddressNumbersOutgoing = intent.getStringArrayExtra(
                        MmsSmsHelper.MESSAGING_THREAD_ADDRESS_NUMBERS_OUTGOING_KEY);
            } else {
                Log.e(LOG_TAG, "There are no addresses!");
            }
            initializeLoading(args);
        }


        // set-up outgoing message container
        // TODO: Implement MMS outgoing message functionality, and for multiple outgoing addresses

        mEt_messageOutgoing = (EditText) view.findViewById(R.id.messaging_message_edittext);

        mButton_send = (Button) view.findViewById(R.id.messaging_button_send);
        mButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageOutgoing = mEt_messageOutgoing.getText().toString();

                if (checkIfNumberMessageEmpty(mAddressNumbersOutgoing[0], mMessageOutgoing)) {
                    sendSMSTextMessage();
                }
            }
        });

        // disable the send button if message field is empty
        mEt_messageOutgoing.addTextChangedListener(new TextWatcher() {
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
        enableDisableSendButton(mEt_messageOutgoing.getText());

        // TODO: slide the recycler view upwards by keyboard height when keyboard pops up

        return view;
    }

    @Override
    public void onClick(String vCardRawData) {
        // create temp file for vCardData
        String path = MmsSmsHelper.writeVCardDataToFile(getContext(), vCardRawData);
        if (path != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(path);
            intent.setDataAndType(FileProvider.getUriForFile(getContext(), "com.thirdarm.chat.filesProvider", file), "text/vcard");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Utils.showToast(getContext(), "There was an error saving the temp vCard data");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MmsSmsHelper.SEND_SMS_PERMISSIONS_REQUEST:
                // upon first granting of send permission
                // TODO: Implement sendTextMessage()
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MmsSmsHelper.sendTextMessage(getActivity(), mAddressNumbersOutgoing[0],
                            mMessageOutgoing, this);
                }
        }
    }

    /**
     * Helper method to start the loader and set the action bar title
     *
     * @param bundle
     */
    private void initializeLoading(Bundle bundle) {
        if (bundle != null && bundle.containsKey(MmsSmsHelper.MESSAGING_THREAD_KEY)) {
            // start the loader if thread id is available
            getActivity().getSupportLoaderManager().initLoader(ID_CONVERSATIONS_LOADER, bundle, this);
        }
        if (mAddressNumbersOutgoing != null && mAddressNumbersOutgoing.length > 0) {
            // set the action bar title if addresses are available
            mAddressNamesOutgoing = MmsSmsHelper.getReadableAddressString(getContext(),
                    mAddressNumbersOutgoing, null, true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mAddressNamesOutgoing);
        } else {
            Log.e(LOG_TAG, "There are no outgoing addresses!");
        }
    }

    // helper method to send text message
    // TODO: Implement to support multiple outgoing addresses
    public void sendSMSTextMessage() {
        if (getPermissionToSendSMS()) {
            MmsSmsHelper.sendTextMessage(getActivity(), mAddressNumbersOutgoing[0],
                    mMessageOutgoing, this);
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
                        MmsSmsHelper.SEND_SMS_PERMISSIONS_REQUEST
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

    public void onMessageSent(int statusCode) {
        switch (statusCode) {
            case MmsSmsHelper.MESSAGE_DELIVERY_STATUS_SUCCESS:
                Utils.showToast(getContext(), "Message Sent");
                mEt_messageOutgoing.setText("");
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case MmsSmsHelper.MESSAGE_DELIVERY_STATUS_FAILURE:
                Utils.showToast(getContext(), "There was an error sending your message");
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_CONVERSATIONS_LOADER:
                long threadId = args.getLong(MmsSmsHelper.MESSAGING_THREAD_KEY);

                Uri baseMessageListUri = Uri.parse("content://mms-sms/conversations");
                Uri messageThreadUri = ContentUris.withAppendedId(baseMessageListUri, threadId);

                String sortOrder = MmsSmsHelper.COLUMN_NORMALIZED_DATE + " asc";

                // TODO: Pass in proper projection array, containing columns you actually need
                return new CursorLoader(
                        getContext(),
                        messageThreadUri,
                        new String[]{"normalized_date", "body", "address", "m_type", "person", "type", "msg_box", "_id", "ct_t"},
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
            mMessagingAdapter.swapCursor(data);
            mRv_messages.scrollToPosition(data.getCount() - 1);
        } else {
            Log.e(LOG_TAG, "The cursor is null!");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMessagingAdapter.swapCursor(null);
    }
}
