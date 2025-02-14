package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateGroupInviteRequest {

    @NotNull(message="User ID cannot be null")
    @Positive(message="User ID should be a positive number")
    private Long userId;
    @NotNull(message="Group ID cannot be null")
    @Positive(message="Group ID should be a positive number")
    private Long groupId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
