package com.thirdarm.chat.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thirdarm.chat.MessageListFragment;
import com.thirdarm.chat.MmsSms.MmsObject;
import com.thirdarm.chat.MmsSms.MmsSmsColumns;
import com.thirdarm.chat.R;
import com.thirdarm.chat.MmsSms.MmsSmsHelper;
import com.thirdarm.chat.MmsSms.SmsObject;
import com.thirdarm.chat.utils.Utils;

/**
 * Created by TROD on 20180303.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListVH> {

    private Context mContext;
    private Cursor mCursor;

    public static final String SMS_TAG = "sms";
    public static final String MMS_TAG = "mms";

    final private MessageListAdapterSmsOnClickHandler mSmsClickHandler;
    final private MessageListAdapterMmsOnClickHandler mMmsClickHandler;

    public interface MessageListAdapterSmsOnClickHandler {
        void onClick(SmsObject smsObject);
    }

    public interface MessageListAdapterMmsOnClickHandler {
        void onClick(MmsObject mmsObject);
    }

    public MessageListAdapter(Context context, MessageListAdapterSmsOnClickHandler smsClickHandler,
                              MessageListAdapterMmsOnClickHandler mmsClickHandler) {
        mContext = context;
        mSmsClickHandler = smsClickHandler;
        mMmsClickHandler = mmsClickHandler;
    }

    @Override
    public MessageListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.message_list_item, parent, false);
        return new MessageListVH(view);
    }

    // TODO: Filter fields to what you actually need
    private void bindSMSView(MessageListVH holder, int position) {
        String initials = String.valueOf(position);
        String address =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_ADDRESS);
        String body =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_BODY);
        String creator = null;
        long dateReceived =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_RECEIVED);
        long dateSent =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_SENT);
        int errorCode =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_ERROR_CODE);
        boolean locked =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_LOCKED) == 1;
        int personSenderId =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_PERSON_SENDER_ID);
        int protocolId = -1;
        boolean read =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_READ) == 1;
        boolean seen = false;
        String serviceCenter =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_SERVICE_CENTER);
        String subject =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_SUBJECT);
        int threadId =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_THREAD_ID);
        int type =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_TYPE);

        // This will be updated on item itself once data is available
        MmsSmsHelper.getReadableAddressString(mContext, new String[]{address},
                holder, false);

        SmsObject smsObject = new SmsObject(address, body, creator, dateReceived, dateSent,
                errorCode, locked, personSenderId, protocolId, read, seen, serviceCenter,
                subject, threadId, type);

        // if the message was in the outbox, then the user is the sender
        if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ||
                type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
            body = "You: " + body;
        }

        holder.bindSms(
                smsObject,
                initials,
                address,
                body,
                Utils.convertMillisToReadableDateTime(
                        mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_NORMALIZED))
        );
    }

    // TODO: Filter fields to what you actually need
    // TODO: Show MMS
    private void bindMMSView(MessageListVH holder, int position) {
        String initials = String.valueOf(position);
        int contentClass =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_CONTENT_CLASS);
        String contentLocation =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_CONTENT_LOCATION);
        String contentType =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_CONTENT_TYPE);
        String creator = null;
        long dateReceived =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_RECEIVED) *
                MmsSmsHelper.DATE_NORMALIZER_CONSTANT;
        long dateSent =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_SENT) *
                MmsSmsHelper.DATE_NORMALIZER_CONSTANT;
        long expiryTime =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_EXPIRY);
        boolean locked =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_LOCKED) == 1;
        int messageBox =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_MESSAGE_BOX);
        String messageClass =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_MESSAGE_CLASS);
        String messageId =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_MESSAGE_ID);
        int messageSize =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_MESSAGE_SIZE);
        int typeMessage =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_MESSAGE_TYPE);
        int mmsVersion =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_MMS_VERSION);
        int priority =
                mCursor
                        .getInt(MmsSmsColumns.INDEX_MESSAGES_PRIORITY);
        boolean read = mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_READ) == 1;
        boolean seen = false;
        int status =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_STATUS);
        String subject =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_SUBJECT);
        int subjectCharset =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_SUBJECT_CHARSET);
        boolean textOnly =
                mCursor.getInt(MmsSmsColumns.INDEX_MESSAGES_TEXT_ONLY) == 1;
        long threadId =
                mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_THREAD_ID);
        String baseColumnId =
                mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_ID);

        // This will be updated on item itself once data is available. INDEX_MESSAGES_ID is the base column _id
        MmsSmsHelper.getAddressFromMms(mContext, baseColumnId, holder,
                true, false);

        String body = MmsSmsHelper.getMmsData(mContext, baseColumnId, messageBox);

        MmsObject mmsObject = new MmsObject(contentClass, contentLocation, contentType, creator,
                dateReceived, dateSent, expiryTime, locked, messageBox, messageClass, messageId,
                messageSize, typeMessage, mmsVersion, priority, read, seen, status, subject,
                subjectCharset, textOnly, threadId, baseColumnId);

        holder.bindMms(
                mmsObject,
                initials,
                "",
                body,
                Utils.convertMillisToReadableDateTime(
                        mCursor.getLong(MmsSmsColumns.INDEX_MESSAGES_DATE_NORMALIZED))
        );
    }

    @Override
    public void onBindViewHolder(MessageListVH holder, int position) {

        if (mCursor.moveToPosition(position)) {
            String messageType =
                    mCursor.getString(MmsSmsColumns.INDEX_MESSAGES_CONTENT_TYPE);
            if ("application/vnd.wap.multipart.related".equals(messageType)) {
                // message is MMS
                bindMMSView(holder, position);
            } else {
                // message is SMS
                bindSMSView(holder, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MessageListVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, MmsSmsHelper.ReadableAddressCallback {

        private TextView tv_initials;
        private TextView tv_name;
        private TextView tv_message;
        private TextView tv_time;

        SmsObject mSmsObject;
        MmsObject mMmsObject;


        public MessageListVH(View itemView) {
            super(itemView);

            tv_initials = (TextView) itemView.findViewById(R.id.message_list_item_initials_textview);
            tv_name = (TextView) itemView.findViewById(R.id.message_list_item_name_textview);
            tv_message = (TextView) itemView.findViewById(R.id.message_list_item_message_textview);
            tv_time = (TextView) itemView.findViewById(R.id.message_list_item_time_textview);

            itemView.setOnClickListener(this);
        }

        public void bindSms(SmsObject smsObject, String initials, String name,
                            String message, String time) {
            tv_initials.setText(initials);
            tv_name.setText(name);
            tv_message.setText(message);
            tv_time.setText(time);

            itemView.setTag(SMS_TAG);

            mSmsObject = smsObject;
        }

        public void bindMms(MmsObject mmsObject, String initials, String name,
                            String message, String time) {
            tv_initials.setText(initials);
            tv_name.setText(name);
            tv_message.setText(message);
            tv_time.setText(time);

            itemView.setTag(MMS_TAG);

            mMmsObject = mmsObject;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            if (itemView.getTag().equals(SMS_TAG)) {
                mSmsClickHandler.onClick(mSmsObject);
            } else if (itemView.getTag().equals(MMS_TAG)) {
                mMmsClickHandler.onClick(mMmsObject);
            } else {
                throw new UnsupportedOperationException(
                        "Item view tag not implemented! " + itemView.getTag().toString());
            }
        }

        @Override
        public void returnReadableAddress(String result) {
            tv_name.setText(result);
        }
    }
}
