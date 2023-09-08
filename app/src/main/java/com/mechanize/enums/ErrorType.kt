package com.mechanize.enums

enum class ErrorType(val value: String) {
    INVALID_CURRENT_PASSWORD("invalid_current_password"),
    INVALID_NEW_PASSWORD("invalid_new_password"),
    INVALID_NEW_PASSWORD_CONFIRMATION("invalid_new_password_confirmation"),
}
