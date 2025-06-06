package com.example.tvshows_auth.exceptions;

public class UnsupportedVersionException extends Throwable {
    public UnsupportedVersionException(String apiVersion) {
        super("Unsupported API version: " + apiVersion);
    }
}
