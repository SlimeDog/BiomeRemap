package me.ford.biomeremap.mapping.settings;

import org.bukkit.command.CommandSender;

import me.ford.biomeremap.mapping.BiomeMap;

public class RemapOptions {
    private final boolean debug;
    private final boolean scanAfter;
    private final RemapArea area;
    private final ReportTarget reportTarget;
    private final BiomeMap map;
    private final Runnable endRunnable;
    private final int maxY;

    public RemapOptions(boolean debug, boolean scanAfter, RemapArea area, ReportTarget reportTarget, BiomeMap map,
            Runnable endRunnable, int maxY) {
        this.debug = debug;
        this.scanAfter = scanAfter;
        this.area = area;
        this.reportTarget = reportTarget;
        this.map = map;
        this.endRunnable = endRunnable;
        this.maxY = maxY;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean doScanAfter() {
        return scanAfter;
    }

    public RemapArea getRemapArea() {
        return area;
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

    public int getMaxY() {
        return maxY;
    }

    public static class Builder {
        private RemapArea area = null;
        private ReportTarget reportTarget = null;
        private BiomeMap map = null;
        // above shouldn't stay null
        private boolean debug = false;
        private boolean scanAfter = false;
        private Runnable endRunnable = null;
        private int maxY = 255 + 128;

        public Builder() {
        }

        public Builder withArea(RemapArea area) {
            this.area = area;
            return this;
        }

        public Builder withTargets(CommandSender... senders) {
            this.reportTarget = new MultiReportTarget(senders);
            return this;
        }

        public Builder withTarget(CommandSender sender) {
            this.reportTarget = new SingleReportTarget(sender);
            return this;
        }

        public Builder withTarget(ReportTarget reportTarget) {
            this.reportTarget = reportTarget;
            return this;
        }

        public Builder withMap(BiomeMap map) {
            this.map = map;
            return this;
        }

        public Builder isDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder scanAfter(boolean scanAfter) {
            this.scanAfter = scanAfter;
            return this;
        }

        public Builder endRunnable(Runnable run) {
            this.endRunnable = run;
            return this;
        }

        public Builder maxY(int maxY) {
            this.maxY = maxY;
            return this;
        }

        public RemapOptions build() {
            if (area == null || reportTarget == null || map == null) {
                throw new IllegalStateException("Need to specify area, report target and map");
            }
            return new RemapOptions(debug, scanAfter, area, reportTarget, map, endRunnable, maxY);
        }
    }

}
