package com.wly.center.core.exception;

public class NodeNotExistException extends BusinessException {

    public NodeNotExistException(String message) {
        super(BusinessExceptions.NODE_NOT_EXIST.name(), message);
    }
}
