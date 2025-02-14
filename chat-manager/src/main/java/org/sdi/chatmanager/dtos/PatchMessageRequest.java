package org.sdi.chatmanager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PatchMessageRequest {

    @NotBlank(message = "Text cannot be blank")
    @NotNull(message = "Text cannot be null")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
