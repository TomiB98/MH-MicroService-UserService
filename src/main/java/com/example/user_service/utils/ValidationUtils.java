package com.example.user_service.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static final Pattern EMAIL_PATTERN = Pattern.compile(".{3,}@.*");

    public static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_.])(?=\\S+$).{8,}");

}
