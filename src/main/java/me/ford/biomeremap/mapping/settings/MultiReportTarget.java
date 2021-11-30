package me.ford.biomeremap.mapping.settings;

import org.bukkit.command.CommandSender;

public class MultiReportTarget implements ReportTarget {
    private final CommandSender[] targets;

    public MultiReportTarget(CommandSender... targets) {
        this.targets = targets;
    }

    @Override
    public void sendMessage(String msg) {
        for (CommandSender target : targets) {
            target.sendMessage(msg);
        }
    }

}
