package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateGroupRequest {

    @NotNull(message="Group name cannot be null")
    @NotBlank(message="Group name cannot be blank")
    private String name;
    @NotNull(message="Owner ID cannot be null")
    @Positive(message="Owner ID should be a positive number")
    private Long ownerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
