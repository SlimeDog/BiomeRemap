package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class ListSub extends BRSubCommand {
	private static final String NAME = "list";
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap list [world]";
	private final SlimeDogPlugin plugin;
	private final Settings settings;
	private final Messages messages;
	private final List<String> worldNames = new ArrayList<>();

	public ListSub(SlimeDogPlugin plugin, Settings settings, Messages messages) {
		super(NAME, PERMS, USAGE);
		this.plugin = plugin;
		this.settings = settings;
		this.messages = messages;
		for (World world : plugin.getWorldProvider().getAllWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], worldNames, list);
		}
		return list;
	}

	@Override
	public boolean onCommand(SDCRecipient sender, String[] args, List<String> opts) {
		List<BiomeMap> list = new ArrayList<>();
		String worldName = null;
		if (args.length == 0) {
			for (String name : settings.getBiomeMapNames()) {
				BiomeMap map = settings.getBiomeMap(name);
				if (map == null) {
					SDCSingleContextMessageFactory<String> msg = messages.errorBiomeMapNotFound();
					plugin.getLogger().warning(msg.getMessage(msg.getContextFactory().getContext(name)).getFilled());
					continue;
				}
				list.add(map);
			}
		} else {
			worldName = args[0];
			if (!worldNames.contains(worldName)) {
				SDCSingleContextMessageFactory<String> msg = messages.errorWorldNotFound();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(worldName)));
				return true;
			}
			BiomeMap map = settings.getApplicableBiomeMap(worldName);
			if (map != null)
				list.add(map);
		}
		if (list.isEmpty() && worldName != null) {
			SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapNoMap();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(worldName)));
		} else {
			sender.sendMessage(messages.getBiomeRemapListHeaders().getMessage());
			SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapListItem();
			for (BiomeMap map : list) {
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(map.getName())));
			}
		}
		return true;
	}

}
