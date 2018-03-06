package com.thirdarm.chat.sms;

import android.provider.Telephony;

/**
 * Created by TROD on 20180304.
 */

public class SmsObject {

    /**
     * The address of the other party
     */
    private String address;

    /**
     * The body of the message
     */
    private String body;

    /**
     * Package name of the app which sent the message
     */
    private String creator;

    /**
     * The date the message was received
     */
    private long dateReceived;

    /**
     * The date the message was sent
     */
    private long dateSent;

    /**
     * ?
     */
    private int errorCode;

    /**
     * ?
     */
    private boolean locked;

    /**
     * references person in content://contacts/people
     */
    private int personSenderId;

    /**
     * ?
     */
    private int protocolId;

    /**
     * has the message been read, in the actual SMS app?
     */
    private boolean read;

    /**
     * ?
     */
    private boolean seen;

    /**
     * a phone number
     */
    private String serviceCenter;

    /**
     * null for Sms messages
     */
    private String subject;

    /**
     * probably unique by thread?
     */
    private int threadId;

    /**
     * The type of message
     * ALL = 0
     * INBOX = 1
     * SENT = 2
     * DRAFT = 3
     * OUTBOX = 4
     * FAILED = 5
     * QUEUED = 6
     */
    private int type;

    public SmsObject() {}

    public SmsObject(String address, String body, String creator, long dateReceived, long dateSent, int errorCode, boolean locked, int personSenderId, int protocolId, boolean read, boolean seen, String serviceCenter, String subject, int threadId, int type) {
        this.address = address;
        this.body = body;
        this.creator = creator;
        this.dateReceived = dateReceived;
        this.dateSent = dateSent;
        this.errorCode = errorCode;
        this.locked = locked;
        this.personSenderId = personSenderId;
        this.protocolId = protocolId;
        this.read = read;
        this.seen = seen;
        this.serviceCenter = serviceCenter;
        this.subject = subject;
        this.threadId = threadId;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(long dateReceived) {
        this.dateReceived = dateReceived;
    }

    public long getDateSent() {
        return dateSent;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getPersonSenderId() {
        return personSenderId;
    }

    public void setPersonSenderId(int personSenderId) {
        this.personSenderId = personSenderId;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
