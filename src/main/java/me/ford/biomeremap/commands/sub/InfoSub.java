package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.commands.SDCCommandOptionSet;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
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
    public boolean onOptionedCommand(SDCRecipient sender, String[] args, SDCCommandOptionSet opts) {
		if (args.length < 1) {
			return false;
		}
		BiomeMap map = settings.getBiomeMap(args[0]);
		if (map == null) {
			SDCSingleContextMessageFactory<String> msg = messages.errorBiomeMapNotFound();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[0])));
			return true;
		}
		SDCDoubleContextMessageFactory<String, List<String>> msg = messages.getBiomeRemapInfo();
		sender.sendMessage(msg
				.getMessage(msg.getContextFactory().getContext(map.getDescription(), map.getApplicableWorldNames())));
		int floor = map.getFloor();
		if (floor != BiomeMap.DEFAULT_FLOOR && !map.getApplicableWorldNames().isEmpty()) {
			SDCTripleContextMessageFactory<Integer, Integer, String> floorMsg = messages.getInfoFloorWithDefault();
			for (String world : map.getApplicableWorldNames()) {
				sender.sendMessage(floorMsg.getMessage(
						floorMsg.getContextFactory().getContext(map.getFloor(), BiomeMap.DEFAULT_FLOOR, world)));
			}
		}
		return true;

	}

}
