package com.wly.center.admin.ha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class MasterInitListener implements ApplicationListener<MasterInitEvent> {

    private final LinkedBlockingQueue<Object> masterInitQueue = new LinkedBlockingQueue<>();

    @Override
    public void onApplicationEvent(MasterInitEvent event) {
        masterInitQueue.add(event.getSource());
    }

    public Object subscribe() {
        Object source = null;
        try {
            source = masterInitQueue.take();

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            } else {
                log.error("MasterInitListener subscribe error", e);
            }
        }
        return source;
    }
}
