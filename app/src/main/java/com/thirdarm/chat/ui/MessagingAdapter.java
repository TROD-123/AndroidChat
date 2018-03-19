package com.thirdarm.chat.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.thirdarm.chat.MmsSms.MmsSmsHelper;
import com.thirdarm.chat.R;
import com.thirdarm.chat.utils.Utils;

import java.util.Arrays;

import ezvcard.VCard;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by TROD on 20180310.
 */

public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.MessagingVH> {

    private Context mContext;
    private Cursor mCursor;

    public static final String SMS_TAG = "sms";
    public static final String MMS_TAG = "mms";

    final private MessagingAdapterMmsVCardOnClickHandler mMmsVCardClickHandler;

    public interface MessagingAdapterMmsVCardOnClickHandler {
        void onClick(String vCardFilePath);
    }


    public MessagingAdapter(Context context, MessagingAdapterMmsVCardOnClickHandler mmsVCardClickHandler) {
        mContext = context;
        mMmsVCardClickHandler = mmsVCardClickHandler;
    }

    @Override
    public MessagingVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.message_item, parent, false);
        return new MessagingVH(view);
    }

    @Override
    public void onBindViewHolder(MessagingVH holder, int position) {
        if (mCursor.moveToPosition(position)) {
            String[] mmsTypes = new String[]{
                    "application/vnd.wap.multipart.mixed",
                    "application/vnd.wap.multipart.related"
            };
            String messageType =
                    mCursor.getString(mCursor.getColumnIndex("ct_t"));
            if (Arrays.asList(mmsTypes).contains(messageType)) {
                // message is MMS
                // TODO: Figure out why null messageType can ALSO be MMS (from old MMS sent pre 2018)
                bindMMSView(holder, position);
            } else {
                // message is SMS. Also, messageType is null by default for SMS
                bindSMSView(holder, position);
            }
        }
    }

    // TODO: Set MMS content
    private void bindMMSView(MessagingVH holder, int position) {
        String time =
                Utils.convertMillisToReadableDateTime(mCursor.getLong(mCursor.getColumnIndex("normalized_date")));

        String baseColumnId = mCursor.getString(mCursor.getColumnIndex("_id"));
        int messageBox = mCursor.getInt(mCursor.getColumnIndex("msg_box"));

        // getting mms content
        Cursor mmsCursor = MmsSmsHelper.getMmsMessageCursor(mContext, baseColumnId);
        String[] mimeTypes = MmsSmsHelper.getAllMimeTypesFromMmsCursor(mmsCursor);

        String body = null;
        Uri imageUri = null;
        Uri gifUri = null;
        Uri videoUri = null;
        String vCardRawData = null;

        for (int i = 0; i < mimeTypes.length; i++) {
            // populate all content types if available
            String mimeType = MmsSmsHelper.getMimeTypeFromMmsCursorAtPosition(mmsCursor, i);
            int mimeTypeCategory = MmsSmsHelper.matchMimeType(mimeType);
            String partId = mmsCursor.getString(mmsCursor.getColumnIndex(Telephony.Mms.Part._ID));
            String data = mmsCursor.getString(mmsCursor.getColumnIndex(Telephony.Mms.Part._DATA));
            String text = mmsCursor.getString(mmsCursor.getColumnIndex(Telephony.Mms.Part.TEXT));

            switch (mimeTypeCategory) {
                case MmsSmsHelper.MIME_TYPE_TEXT_PLAIN:
                    body = MmsSmsHelper.getMmsText(mContext, text, data, partId);
                    break;
                case MmsSmsHelper.MIME_TYPE_TEXT_VCARD:
                    VCard vCard = MmsSmsHelper.getVCardObject(mContext, data, partId);
                    vCardRawData = MmsSmsHelper.getVCardRawData(mContext, data, partId);
                    body = MmsSmsHelper.getReadableVCardString(vCard);
                    break;
                case MmsSmsHelper.MIME_TYPE_IMAGE:
                    // TODO: This is leaking to other views. Why?
                    // TODO: Set an onClickListener to expand image preview when user clicks on image
                    imageUri = MmsSmsHelper.getMmsImageVideoUri(partId);
                    break;
                case MmsSmsHelper.MIME_TYPE_GIF:
                    gifUri = MmsSmsHelper.getMmsImageVideoUri(partId);
                    break;
                case MmsSmsHelper.MIME_TYPE_VIDEO:
                    videoUri = MmsSmsHelper.getMmsImageVideoUri(partId);
                    break;
                case MmsSmsHelper.MIME_TYPE_SMIL:
                    // TODO: Deal with this somehow. Create an SMIL container?
                default:
                    body = "Unhandled mimetype: " + mimeType;
            }
        }
        mmsCursor.close();

        // Prepending the sender address
        String senderAddress = MmsSmsHelper.getSenderAddressFromMms(mContext, baseColumnId, messageBox);

        holder.bindMms(senderAddress, time, body, imageUri, gifUri, videoUri, vCardRawData);
    }

    // TODO: Set HTML compat for clicking hyperlinks
    private void bindSMSView(MessagingVH holder, int position) {
        String senderName =
                MmsSmsHelper.getReadableAddressString(mContext,
                        new String[]{mCursor.getString(mCursor.getColumnIndex("address"))},
                        holder, true);
        String time =
                Utils.convertMillisToReadableDateTime(
                        mCursor.getLong(mCursor.getColumnIndex("normalized_date")));
        String body =
                mCursor.getString(mCursor.getColumnIndex("body"));

        int type =
                mCursor.getInt(mCursor.getColumnIndex("type"));

        // if we erroneously came here, go to bindMMSView()
        if (senderName == null || senderName.length() == 0) {
            bindMMSView(holder, position);
        } else {
            // if the message was in the outbox, then the user is the sender
            if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ||
                    type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
                senderName = "You";
            }
            holder.bindSms(senderName, time, body);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MessagingVH extends RecyclerView.ViewHolder
            implements MmsSmsHelper.ReadableAddressCallback, View.OnClickListener {

        private TextView tv_name;
        private TextView tv_time;
        private TextView tv_body;
        private ImageView iv_image;
        private GifImageView giv_gif;
        private VideoView vv_video;
        private ImageView iv_video;

        public String vCardRawData;

        public MessagingVH(View itemView) {
            super(itemView);

            tv_name = (TextView) itemView.findViewById(R.id.message_item_sender_name_textview);
            tv_time = (TextView) itemView.findViewById(R.id.message_item_time_textview);
            tv_body = (TextView) itemView.findViewById(R.id.message_item_body_textview);
            iv_image = (ImageView) itemView.findViewById(R.id.message_item_image);
            giv_gif = (GifImageView) itemView.findViewById(R.id.message_item_gif);
            vv_video = (VideoView) itemView.findViewById(R.id.message_item_vid);
            iv_video = (ImageView) itemView.findViewById(R.id.message_item_vid_img);

            itemView.setOnClickListener(this);
        }

        public void bindSms(String senderName, String time, String body) {
            clearAllFields();

            tv_name.setText(senderName);
            tv_time.setText(time);
            tv_body.setText(body);

            itemView.setTag(SMS_TAG);
        }

        public void bindMms(String senderName, String time, String body,
                            Uri imageUri, Uri gifUri, Uri videoUri, String vCardRawData) {
            clearAllFields();

            tv_name.setText(senderName);
            tv_time.setText(time);
            tv_body.setText(body);

            this.vCardRawData = vCardRawData;

            if (imageUri != null) {
                iv_image.setVisibility(View.VISIBLE);
                iv_image.setImageURI(imageUri);
            }
            if (gifUri != null) {
                giv_gif.setVisibility(View.VISIBLE);
                giv_gif.setImageURI(gifUri);
            }
            if (videoUri != null) {
                vv_video.setVisibility(View.VISIBLE);
                iv_video.setVisibility(View.VISIBLE);
                vv_video.setVideoURI(videoUri);
                // TODO: Play video
            }

            itemView.setTag(MMS_TAG);
        }

        @Override
        public void returnReadableAddress(String result) {
            tv_name.setText(result);
        }

        private void clearAllFields() {
            tv_name.setText(null);
            tv_time.setText(null);
            tv_body.setText(null);
            iv_image.setImageDrawable(null);
            iv_image.setVisibility(View.GONE);
            giv_gif.setBackground(null);
            giv_gif.setVisibility(View.GONE);
            vv_video.setVideoURI(null);
            vv_video.setVisibility(View.GONE);
            iv_video.setImageDrawable(null);
            iv_video.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            if (vCardRawData != null) {
                mMmsVCardClickHandler.onClick((vCardRawData));
            }
        }
    }
}
