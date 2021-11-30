package me.ford.biomeremap.mapping.settings;

import org.bukkit.command.CommandSender;

public class SingleReportTarget implements ReportTarget {
    private final CommandSender target;

    public SingleReportTarget(CommandSender target) {
        this.target = target;
    }

    @Override
    public void sendMessage(String msg) {
        target.sendMessage(msg);
    }

}
