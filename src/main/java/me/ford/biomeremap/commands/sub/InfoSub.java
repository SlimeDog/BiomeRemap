package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class InfoSub extends BRSubCommand {
	private static final String NAME = "info";
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap info <biomemap-id>";
	private final Settings settings;
	private final Messages messages;

	public InfoSub(Settings settings, Messages messages) {
		super(NAME, PERMS, USAGE);
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], settings.getBiomeMapNames(), list);
		} else {
			return list;
		}
	}

	@Override
	public boolean onCommand(SDCRecipient sender, String[] args, List<String> opts) {
		if (args.length < 1) {
			return false;
		}
		BiomeMap map = settings.getBiomeMap(args[0]);
		if (map == null) {
			sender.sendRawMessage(messages.errorBiomeMapNotFound(args[0]));
			return true;
		}
		sender.sendRawMessage(messages.getBiomeRemapInfo(map.getDescription(), map.getApplicableWorldNames()));
		int floor = map.getFloor();
		if (floor != BiomeMap.DEFAULT_FLOOR && !map.getApplicableWorldNames().isEmpty()) {
			for (String world : map.getApplicableWorldNames()) {
				sender.sendRawMessage(messages.getInfoFloorWithDefault(map.getFloor(), BiomeMap.DEFAULT_FLOOR,
						world));
			}
		}
		return true;

	}

}
