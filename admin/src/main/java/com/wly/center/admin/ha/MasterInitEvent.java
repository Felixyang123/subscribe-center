package com.wly.center.admin.ha;

import org.springframework.context.ApplicationEvent;

public class MasterInitEvent extends ApplicationEvent {

    public MasterInitEvent(Object source) {
        super(source);
    }
}
