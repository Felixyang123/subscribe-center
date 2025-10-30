package com.wly.center.core.exception;

public class NodeExistException extends BusinessException {

    public NodeExistException(String message) {
        super(BusinessExceptions.NODE_EXIST.name(), message);
    }
}
