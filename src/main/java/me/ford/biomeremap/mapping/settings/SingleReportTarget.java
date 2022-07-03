package me.ford.biomeremap.mapping.settings;

import dev.ratas.slimedogcore.api.messaging.SDCMessage;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;

public class SingleReportTarget implements ReportTarget {
    private final SDCRecipient target;

    public SingleReportTarget(SDCRecipient target) {
        this.target = target;
    }

    @Override
    public void sendMessage(SDCMessage<?> msg) {
        target.sendMessage(msg);
    }

}
