package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import dev.ratas.slimedogcore.api.commands.SDCCommandOptionSet;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import dev.ratas.slimedogcore.impl.commands.AbstractSubCommand;
import me.ford.biomeremap.commands.BiomeRemapCommand;

public class HelpSub extends AbstractSubCommand {
	private static final String NAME = "help";
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap help";
	private final BiomeRemapCommand base;

	public HelpSub(BiomeRemapCommand base) {
		super(NAME, PERMS, USAGE);
		this.base = base;
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		return new ArrayList<>();
	}

	@Override
    public boolean onOptionedCommand(SDCRecipient sender, String[] args, SDCCommandOptionSet opts) {
		base.getUsage(sender);
		return true;
	}

}
