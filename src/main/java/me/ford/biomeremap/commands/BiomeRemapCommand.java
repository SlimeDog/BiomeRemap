package me.ford.biomeremap.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.sub.ChunkSub;
import me.ford.biomeremap.commands.sub.HelpSub;
import me.ford.biomeremap.commands.sub.InfoSub;
import me.ford.biomeremap.commands.sub.ListSub;
import me.ford.biomeremap.commands.sub.RegionSub;
import me.ford.biomeremap.commands.sub.ReloadSub;
import me.ford.biomeremap.commands.sub.ScanSub;

public class BiomeRemapCommand extends ArgSplittingCommand {
	private static final String PERMS = "biomeremap.use";
	private final List<SubCommand> subCommands = new ArrayList<>();
	private final List<String> subCommandNames = new ArrayList<>();
	private final BiomeRemap br;

	public BiomeRemapCommand(BiomeRemap plugin) {
		br = plugin;// help|info|list|chunk|region|scan|reload
		subCommands.add(new HelpSub(this));
		subCommands.add(new InfoSub(br.getSettings(), br.getMessages()));
		subCommands.add(new ListSub(br));
		subCommands.add(new ChunkSub(br));
		subCommands.add(new RegionSub(br));
		subCommands.add(new ScanSub(br));
		subCommands.add(new ReloadSub(br));
		for (SubCommand cmd : subCommands) {
			subCommandNames.add(cmd.getName());
		}
	}

	public List<SubCommand> getSubCommands() {
		return new ArrayList<>(subCommands);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		List<String> list = new ArrayList<>();
		if (!hasPermission(sender))
			return list;
		if (args.length == 1) {
			List<String> cmds = new ArrayList<>();
			for (SubCommand cmd : subCommands) {
				if (cmd.hasPermission(sender))
					cmds.add(cmd.getName());
			}
			return StringUtil.copyPartialMatches(args[0], cmds, list);
		} else if (args.length > 1) {
			SubCommand cmd = getSuitableSubCommand(args[0]);
			if (cmd == null) {
				return list;
			} else {
				if (cmd.hasPermission(sender)) {
					return cmd.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length), opts);
				} else {
					return list;
				}
			}
		} else {
			return null; // shouldn't really happen
		}
	}

	private SubCommand getSuitableSubCommand(String name) {
		SubCommand cmd = null;
		for (SubCommand sub : subCommands) {
			if (sub.getName().equalsIgnoreCase(name)) {
				cmd = sub;
				break;
			}
		}
		return cmd;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		if (!hasPermission(sender)) {
			sender.sendMessage(br.getMessages().errorNoPermissions());
			return true;
		}
		if (args.length == 0) {
			String usage = getUsage(sender);
			if (usage.isEmpty()) {
				sender.sendMessage(br.getMessages().errorNoPermissions());
			} else {
				sender.sendMessage(usage);
			}
			return true;
		} else {
			SubCommand cmd = getSuitableSubCommand(args[0]);
			if (cmd == null || !cmd.hasPermission(sender)) {
				if (cmd != null) {
					sender.sendMessage(br.getMessages().errorNoPermissions());
					return true;
				}
				sender.sendMessage(getUsage(sender));
				return true;
			}
			if (!cmd.onCommand(sender, Arrays.copyOfRange(args, 1, args.length), opts)) {
				sender.sendMessage(cmd.getUsage(sender));
			}
			return true;
		}
	}

	private boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(PERMS) || sender instanceof ConsoleCommandSender;
	}

	private String getUsage(CommandSender sender) {
		StringBuilder msg = new StringBuilder();
		for (SubCommand cmd : subCommands) {
			if (cmd.hasPermission(sender)) {
				if (msg.length() > 0) {
					msg.append("\n");
				}
				msg.append(cmd.getUsage(sender));
			}
		}
		return msg.toString();
	}

}
