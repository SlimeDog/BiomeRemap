package me.ford.biomeremap.mapping.settings;

import me.ford.biomeremap.mapping.BiomeMap;

public class RemapOptions {
    private final boolean debug;
    private final boolean scanAfter;
    private final RemapArea region;
    private final ReportTarget reportTarget;
    private final BiomeMap map;
    private final Runnable endRunnable;

    public RemapOptions(boolean debug, boolean scanAfter, RemapArea region, ReportTarget reportTarget, BiomeMap map,
            Runnable endRunnable) {
        this.debug = debug;
        this.scanAfter = scanAfter;
        this.region = region;
        this.reportTarget = reportTarget;
        this.map = map;
        this.endRunnable = endRunnable;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean doScanAfter() {
        return scanAfter;
    }

    public RemapArea getRemapArea() {
        return region;
    }

    public ReportTarget getReportTarget() {
        return reportTarget;
    }

    public BiomeMap getBiomeMap() {
        return map;
    }

    public Runnable getEndRunnable() {
        return endRunnable;
    }

}
