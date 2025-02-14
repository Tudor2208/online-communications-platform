package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateGroupMessageRequest {

    @NotNull(message="Text cannot be null")
    @NotBlank(message="Text cannot be blank")
    private String text;
    @NotNull(message="Group ID cannot be null")
    @Positive(message="Group ID should be a positive number")
    private Long groupId;
    @NotNull(message="Sender ID cannot be null")
    @Positive(message="Sender ID should be a positive number")
    private Long senderId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
}
