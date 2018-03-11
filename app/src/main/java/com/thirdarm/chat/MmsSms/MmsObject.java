package com.thirdarm.chat.MmsSms;

/**
 * Created by TROD on 20180310.
 */

public class MmsObject {

    private int contentClass;
    private String contentLocation;
    private String contentType;
    private String creator;
    private long dateReceived;
    private long dateSent;
    private long expiryTime;
    private boolean locked;
    private int messageBox;
    private String messageClass;
    private String messageId;
    private int messageSize;
    private int messageType;
    private int mmsVersion;
    private int priority;
    private boolean read;
    private boolean seen;
    private int status;
    private String subject;
    private int subjectCharset;
    private boolean textOnly;
    private long threadId;
    private String baseColumnId;

    public MmsObject() {
    }

    public MmsObject(int contentClass, String contentLocation, String contentType, String creator, long dateReceived, long dateSent, long expiryTime, boolean locked, int messageBox, String messageClass, String messageId, int messageSize, int messageType, int mmsVersion, int priority, boolean read, boolean seen, int status, String subject, int subjectCharset, boolean textOnly, long threadId, String baseColumnId) {
        this.contentClass = contentClass;
        this.contentLocation = contentLocation;
        this.contentType = contentType;
        this.creator = creator;
        this.dateReceived = dateReceived;
        this.dateSent = dateSent;
        this.expiryTime = expiryTime;
        this.locked = locked;
        this.messageBox = messageBox;
        this.messageClass = messageClass;
        this.messageId = messageId;
        this.messageSize = messageSize;
        this.messageType = messageType;
        this.mmsVersion = mmsVersion;
        this.priority = priority;
        this.read = read;
        this.seen = seen;
        this.status = status;
        this.subject = subject;
        this.subjectCharset = subjectCharset;
        this.textOnly = textOnly;
        this.threadId = threadId;
        this.baseColumnId = baseColumnId;
    }

    public int getContentClass() {
        return contentClass;
    }

    public void setContentClass(int contentClass) {
        this.contentClass = contentClass;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getMessageBox() {
        return messageBox;
    }

    public void setMessageBox(int messageBox) {
        this.messageBox = messageBox;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMmsVersion() {
        return mmsVersion;
    }

    public void setMmsVersion(int mmsVersion) {
        this.mmsVersion = mmsVersion;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getSubjectCharset() {
        return subjectCharset;
    }

    public void setSubjectCharset(int subjectCharset) {
        this.subjectCharset = subjectCharset;
    }

    public boolean isTextOnly() {
        return textOnly;
    }

    public void setTextOnly(boolean textOnly) {
        this.textOnly = textOnly;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getBaseColumnId() {
        return baseColumnId;
    }

    public void setBaseColumnId(String baseColumnId) {
        this.baseColumnId = baseColumnId;
    }
}
