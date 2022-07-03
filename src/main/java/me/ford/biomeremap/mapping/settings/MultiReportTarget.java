package me.ford.biomeremap.mapping.settings;

import dev.ratas.slimedogcore.api.messaging.SDCMessage;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;

public class MultiReportTarget implements ReportTarget {
    private final SDCRecipient[] targets;

    public MultiReportTarget(SDCRecipient... targets) {
        this.targets = targets;
    }

    @Override
    public void sendMessage(SDCMessage<?> msg) {
        for (SDCRecipient target : targets) {
            target.sendMessage(msg);
        }
    }

}
