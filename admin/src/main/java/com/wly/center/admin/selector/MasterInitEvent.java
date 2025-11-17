package com.wly.center.admin.selector;

import org.springframework.context.ApplicationEvent;

public class MasterInitEvent extends ApplicationEvent {

    public MasterInitEvent(Object source) {
        super(source);
    }
}
