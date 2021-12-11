package me.ford.biomeremap.commands.sub;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.mapping.BiomeMap;

public class CBCommand extends SubCommand {
    private static final String PERMS = "biomeremap.cb";
    private static final String USAGE = "/biomeremap cb";
    private static final String NAME = "cb";
    private final BiomeRemap br;

    public CBCommand(BiomeRemap br) {
        super(NAME);
        this.br = br;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Need a palyer");
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        BiomeMap map = br.getSettings().getApplicableBiomeMap(loc.getWorld().getName());
        if (map == null) {
            player.sendMessage(br.getMessages().getBiomeRemapNoMap(loc.getWorld().getName()));
            return true;
        }
        Block block = loc.getBlock();
        Biome before = block.getBiome();
        Biome biome = map.getBiomeFor(before);
        if (biome == null) {
            sender.sendMessage("Current biome not mapped to anything");
            return true;
        }
        block.setBiome(biome);
        sender.sendMessage("Remapped from " + before.name() + " to " + biome.name());
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender instanceof Player && sender.hasPermission(PERMS);
    }

    @Override
    public String getUsage(CommandSender sender) {
        return USAGE;
    }

}
