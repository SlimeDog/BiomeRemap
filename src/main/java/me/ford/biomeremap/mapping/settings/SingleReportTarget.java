package me.ford.biomeremap.mapping.settings;

import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;

public class SingleReportTarget implements ReportTarget {
    private final SDCRecipient target;

    public SingleReportTarget(SDCRecipient target) {
        this.target = target;
    }

    @Override
    public void sendMessage(String msg) {
        target.sendRawMessage(msg);
    }

}
