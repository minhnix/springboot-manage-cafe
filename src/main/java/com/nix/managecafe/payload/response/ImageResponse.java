package com.nix.managecafe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageResponse {
    private String filename;
    private boolean success;
}
