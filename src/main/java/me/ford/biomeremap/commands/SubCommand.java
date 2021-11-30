package me.ford.biomeremap.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

public abstract class SubCommand implements CommandWithOptions {
	private static final Pattern MAX_Y_PATTERN = Pattern.compile("\\-\\-maxy=(\\d*)");
	public static final int MAX_Y = 255 + 128;
	private final String name;

	public SubCommand(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean hasPermission(CommandSender sender);

	public abstract String getUsage(CommandSender sender);

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
