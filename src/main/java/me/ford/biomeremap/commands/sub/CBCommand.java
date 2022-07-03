package me.ford.biomeremap.commands.sub;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class CBCommand extends BRSubCommand {
    private static final String PERMS = "biomeremap.cb";
    private static final String USAGE = "/biomeremap cb";
    private static final String NAME = "cb";
    private final Settings settings;
    private final Messages messages;

    public CBCommand(Settings settings, Messages messages) {
        super(NAME, PERMS, USAGE);
        this.settings = settings;
        this.messages = messages;
    }

    @Override
    public List<String> onTabComplete(SDCRecipient sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(SDCRecipient sender, String[] args, List<String> opts) {
        if (!sender.isPlayer()) {
            sender.sendRawMessage("Need a palyer");
            return true;
        }
        Location loc = ((SDCPlayerRecipient) sender).getLocation();
        BiomeMap map = settings.getApplicableBiomeMap(loc.getWorld().getName());
        if (map == null) {
            SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapNoMap();
            sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(loc.getWorld().getName())));
            return true;
        }
        Block block = loc.getBlock();
        Biome before = block.getBiome();
        Biome biome = map.getBiomeFor(before);
        if (biome == null) {
            sender.sendRawMessage("Current biome not mapped to anything");
            return true;
        }
        block.setBiome(biome);
        sender.sendRawMessage("Remapped from " + before.name() + " to " + biome.name());
        return true;
    }

}
