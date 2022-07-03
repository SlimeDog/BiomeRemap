package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings.ReloadIssues;

public class ReloadSub extends BRSubCommand {
	private static final String NAME = "reload";
	private static final String PERMS = "biomeremap.reload";
	private static final String USAGE = "/biomeremap reload";
	private final SlimeDogPlugin br;
	private final Messages messages;

	public ReloadSub(SlimeDogPlugin plugin, Messages messages) {
		super(NAME, PERMS, USAGE);
		br = plugin;
		this.messages = messages;
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(SDCRecipient sender, String[] args, List<String> opts) {
		ReloadIssues issues = ((BiomeRemap) br).reload();
		if (issues != null && !issues.hasIssues()) {
			sender.sendMessage(messages.getBiomeRemapReload().getMessage());
		} else if (issues != null) {
			sender.sendRawMessage(String.join("\n", issues.getIssues()));
		} else { // issues == null
			sender.sendMessage(messages.errorConfigUnreadable().getMessage());
		}
		return true;
	}

}
