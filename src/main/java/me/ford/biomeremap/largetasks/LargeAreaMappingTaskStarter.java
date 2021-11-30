package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.mapping.settings.RemapArea;
import me.ford.biomeremap.mapping.settings.RemapOptions;

public class LargeAreaMappingTaskStarter extends LargeTaskStarter {
    private final RemapOptions options;
    private final RemapArea area;
    private final Runnable runnable;

    public LargeAreaMappingTaskStarter(BiomeRemap plugin, RemapOptions options, Runnable runnable) {
        super(plugin, options.getRemapArea().getWorld(), options.getReportTarget(), options.getRemapArea().getAreaX(),
                options.getRemapArea().getAreaZ(), options.getRemapArea().describesRegion(), options.isDebug());
        this.options = options;
        this.area = options.getRemapArea();
        this.runnable = runnable;
    }

    @Override
    protected void startTask() {
        if (!options.doScanAfter()) {
            new LargeMappingTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
                    br().getSettings().getRegionRemapProgressStep(), (progress) -> reportProgress(progress),
                    (report) -> remappingEnded(report), options.getBiomeMap());
        } else {
            new LargeMappingWithScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
                    br().getSettings().getRegionRemapProgressStep(), (progress) -> reportProgress(progress),
                    (report) -> remappingEnded(report), options.getBiomeMap(), (report) -> showMap(report));
        }
    }

    private void showMap(BiomeReport report) {
        String header;
        if (area.describesRegion()) {
            header = br().getMessages().getScanRegionHeader(area.getWorld().getName(), area.getAreaX(),
                    area.getAreaZ());
        } else {
            header = br().getMessages().getScanChunkHeader(area.getWorld().getName(), area.getAreaX(), area.getAreaZ());
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
            String msg = br().getMessages().getScanListItem(percentage, entry.getKey().name(), entry.getValue());
            options.getReportTarget().sendMessage(msg);
        }
        String msg = br().getMessages().getScanListItem("100%", "TOTAL", (int) total);
        options.getReportTarget().sendMessage(msg);
    }

    private void reportProgress(String progress) {
        String msg = br().getMessages().getBiomeRemapProgress(progress);
        options.getReportTarget().sendMessage(msg);
    }

    private void remappingEnded(TaskReport report) {
        if (options.isDebug())
            options.getReportTarget().sendMessage(br().getMessages().getBiomeRemapSummary(report.getChunksDone(),
                    report.getCompTime(), report.getTicksUsed()));
        if (runnable != null)
            runnable.run();
    }

}
