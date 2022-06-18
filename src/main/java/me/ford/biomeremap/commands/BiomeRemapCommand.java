package me.ford.biomeremap.commands;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.impl.commands.BukkitFacingParentCommand;
import me.ford.biomeremap.commands.sub.ChunkSub;
import me.ford.biomeremap.commands.sub.HelpSub;
import me.ford.biomeremap.commands.sub.InfoSub;
import me.ford.biomeremap.commands.sub.ListSub;
import me.ford.biomeremap.commands.sub.RegionSub;
import me.ford.biomeremap.commands.sub.ReloadSub;
import me.ford.biomeremap.commands.sub.ScanSub;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemapCommand extends BukkitFacingParentCommand {
	private final SlimeDogPlugin br;

	public BiomeRemapCommand(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper,
			BiomeScanner scanner) {
		br = plugin;// help|info|list|chunk|region|scan|reload
		addSubCommand(new HelpSub(this));
		addSubCommand(new InfoSub(settings, messages));
		addSubCommand(new ListSub(br, settings, messages));
		addSubCommand(new ChunkSub(br, settings, messages, remapper));
		addSubCommand(new RegionSub(br, settings, messages, remapper));
		addSubCommand(new ScanSub(br, settings, messages, remapper, scanner));
		addSubCommand(new ReloadSub(br, messages));
	}

}
