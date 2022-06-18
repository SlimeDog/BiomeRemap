package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.block.Biome;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.mapping.settings.RemapArea;
import me.ford.biomeremap.mapping.settings.RemapOptions;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class LargeAreaMappingTaskStarter extends LargeTaskStarter {
    private final RemapOptions options;
    private final RemapArea area;
    private final Runnable runnable;
    private final Settings settings;
    private final Messages messages;
    private final BiomeRemapper remapper;
    private final BiomeScanner scanner;

    public LargeAreaMappingTaskStarter(SlimeDogPlugin plugin, Settings settings, Messages messages,
            BiomeRemapper remapper, BiomeScanner scanner, RemapOptions options, Runnable runnable) {
        super(plugin, options.getRemapArea().getWorld(), options.getReportTarget(), options.getRemapArea().getAreaX(),
                options.getRemapArea().getAreaZ(), options.getRemapArea().describesRegion(), options.isDebug());
        this.options = options;
        this.area = options.getRemapArea();
        this.runnable = runnable;
        this.settings = settings;
        this.messages = messages;
        this.remapper = remapper;
        this.scanner = scanner;
    }

    @Override
    protected void startTask() {
        if (!options.doScanAfter()) {
            new LargeMappingTask(br(), settings, remapper, world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
                    settings.getRegionRemapProgressStep(), (progress) -> reportProgress(progress),
                    (report) -> remappingEnded(report), options.getBiomeMap(), options.getMaxY());
        } else {
            new LargeMappingWithScanTask(br(), settings, remapper, scanner, world(), chunkX(), stopX(), chunkZ(),
                    stopZ(), debug(), settings.getRegionRemapProgressStep(), (progress) -> reportProgress(progress),
                    (report) -> remappingEnded(report), options.getBiomeMap(), (report) -> showMap(report),
                    options.getMaxY());
        }
    }

    private void showMap(BiomeReport report) {
        String header;
        if (area.describesRegion()) {
            header = messages.getScanRegionHeader(area.getWorld().getName(), area.getAreaX(),
                    area.getAreaZ());
        } else {
            header = messages.getScanChunkHeader(area.getWorld().getName(), area.getAreaX(), area.getAreaZ());
        }
        options.getReportTarget().sendMessage(header);
        Map<Biome, Integer> sortedMap = report.getBiomes().entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().name().compareTo(e2.getKey().name()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        double total = 0;
        for (int val : sortedMap.values()) {
            total += val;
        }
        for (Map.Entry<Biome, Integer> entry : sortedMap.entrySet()) {
            String percentage = String.format("%3.0f%%", 100 * ((double) entry.getValue()) / total);
            String msg = messages.getScanListItem(percentage, entry.getKey().name(), entry.getValue());
            options.getReportTarget().sendMessage(msg);
        }
        String msg = messages.getScanListItem("100%", "TOTAL", (int) total);
        options.getReportTarget().sendMessage(msg);
    }

    private void reportProgress(String progress) {
        String msg = messages.getBiomeRemapProgress(progress);
        options.getReportTarget().sendMessage(msg);
    }

    private void remappingEnded(TaskReport report) {
        if (options.isDebug())
            options.getReportTarget().sendMessage(messages.getBiomeRemapSummary(report.getChunksDone(),
                    report.getCompTime(), report.getTicksUsed()));
        if (runnable != null)
            runnable.run();
    }

}
