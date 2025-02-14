package org.sdi.chatmanager.dtos;

import java.util.Date;

public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String text;
    private Date timestamp;
    private boolean edited;
    private byte[] audioData;

    public MessageResponse(Long id, Long senderId, Long recipientId, String text, Date timestamp, boolean edited, byte[] audioData) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.text = text;
        this.timestamp = timestamp;
        this.edited = edited;
        this.audioData = audioData;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
