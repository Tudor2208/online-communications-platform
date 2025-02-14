package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PatchGroupRequest {

    @NotNull(message="Group name cannot be null")
    @NotBlank(message="Group name cannot be blank")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(@NotNull(message = "Group name cannot be null") @NotBlank(message = "Group name cannot be blank") String name) {
        this.name = name;
    }
}
