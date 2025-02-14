package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateMessageRequest {

    @NotNull(message = "Sender ID cannot be null")
    @Positive(message = "Sender ID  must be positive")
    private Long senderId;
    @NotNull(message = "Recipient ID cannot be null")
    @Positive(message = "Recipient ID  must be positive")
    private Long recipientId;
    @NotNull(message = "Text cannot be null")
    @NotBlank(message = "Text cannot be blank")
    private String text;

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
}
