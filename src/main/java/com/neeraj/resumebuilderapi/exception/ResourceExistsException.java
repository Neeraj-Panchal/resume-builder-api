package com.neeraj.resumebuilderapi.exception;


public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException(String message){
        super(message);
    }
}
