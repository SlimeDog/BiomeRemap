package me.ford.biomeremap.mapping.settings;

import dev.ratas.slimedogcore.api.messaging.SDCMessage;

public interface ReportTarget {

    void sendMessage(SDCMessage<?> msg);

}
