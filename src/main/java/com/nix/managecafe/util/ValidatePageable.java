package com.nix.managecafe.util;

import com.nix.managecafe.exception.BadRequestException;

public class ValidatePageable {
    public static void invoke(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}
