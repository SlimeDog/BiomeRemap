package me.ford.biomeremap.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public abstract class ArgSplittingCommand implements TabExecutor, CommandWithOptions {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		Pair<String, List<String>> pair = splitArgsToArgsAndOptions(args);
		return onTabComplete(sender, pair.getFirst(), pair.getSecond());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Pair<String, List<String>> pair = splitArgsToArgsAndOptions(args);
		return onCommand(sender, pair.getFirst(), pair.getSecond());
	}

	protected Pair<String, List<String>> splitArgsToArgsAndOptions(String[] args) {
		List<String> arguments = new ArrayList<>();
		List<String> options = new ArrayList<>();
		for (String arg : args) {
			if (arg.startsWith("--")) {
				options.add(arg);
			} else {
				arguments.add(arg);
			}
		}
		return new Pair<String, List<String>>(arguments.toArray(new String[0]), options);
	}

	private static class Pair<T, U> {
		private final T[] t;
		private final U u;

		private Pair(T[] t, U u) {
			this.t = t;
			this.u = u;
		}

		private T[] getFirst() {
			return t;
		}

		private U getSecond() {
			return u;
		}
	}

}
