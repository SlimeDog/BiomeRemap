package me.ford.biomeremap.commands.sub;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ratas.slimedogcore.impl.commands.AbstractSubCommand;

public abstract class BRSubCommand extends AbstractSubCommand {
    private static final Pattern MAX_Y_PATTERN = Pattern.compile("\\-\\-maxy=(\\d*)");
    public static final int MAX_Y = 320;

    protected BRSubCommand(String name, String perms, String usage) {
        super(name, perms, usage);
    }

    protected int getMaxY(List<String> opts) {
        for (String opt : opts) {
            Matcher matcher = MAX_Y_PATTERN.matcher(opt);
            if (matcher.matches()) {
                return Math.min(Integer.parseInt(matcher.group(1)), MAX_Y);
            }
        }
        return MAX_Y;
    }

}
