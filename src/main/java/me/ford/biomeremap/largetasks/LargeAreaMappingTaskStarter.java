package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.block.Biome;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.SDCMessage;
import dev.ratas.slimedogcore.api.messaging.context.SDCTripleContext;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
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
        SDCMessage<SDCTripleContext<String, Integer, Integer>> header;
        if (area.describesRegion()) {
            SDCTripleContextMessageFactory<String, Integer, Integer> msg = messages.getScanRegionHeader();
            header = msg.getMessage(msg.getContextFactory().getContext(area.getWorld().getName(), area.getAreaX(),
                    area.getAreaZ()));
        } else {
            SDCTripleContextMessageFactory<String, Integer, Integer> msg = messages.getScanChunkHeader();
            header = msg.getMessage(msg.getContextFactory().getContext(area.getWorld().getName(), area.getAreaX(),
                    area.getAreaZ()));
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
            SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
            options.getReportTarget().sendMessage(msg.getMessage(
                    msg.getContextFactory().getContext(percentage, entry.getKey().name(), entry.getValue())));
        }

        SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
        options.getReportTarget().sendMessage(msg.getMessage(
                msg.getContextFactory().getContext("100%", "TOTAL", (int) total)));
    }

    private void reportProgress(String progress) {
        SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapProgress();
        options.getReportTarget().sendMessage(msg.getMessage(msg.getContextFactory().getContext(progress)));
    }

    private void remappingEnded(TaskReport report) {
        if (options.isDebug()) {
            SDCTripleContextMessageFactory<Integer, Long, Integer> msg = messages.getBiomeRemapSummary();
            options.getReportTarget().sendMessage(msg.getMessage(msg.getContextFactory()
                    .getContext(report.getChunksDone(), report.getCompTime(), report.getTicksUsed())));
        }
        if (runnable != null)
            runnable.run();
    }

}
