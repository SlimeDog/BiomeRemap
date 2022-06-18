package me.ford.biomeremap.mapping.settings;

import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;

public class MultiReportTarget implements ReportTarget {
    private final SDCRecipient[] targets;

    public MultiReportTarget(SDCRecipient... targets) {
        this.targets = targets;
    }

    @Override
    public void sendMessage(String msg) {
        for (SDCRecipient target : targets) {
            target.sendRawMessage(msg);
        }
    }

}
