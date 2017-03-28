package org.geowebcache.monitoring;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.StepCounter;
import com.netflix.servo.stats.StatsConfig;

public class ServoMonitor {

    private final StatsTimer fileReadTimer;
    private final StatsTimer fileWriteTimer;
    private final StatsTimer fileDeleteTimer;
    private final StatsTimer folderDeleteTimer;
    private final StatsTimer folderScanningTimer;

    private final Counter readCountTotal;
    private final Counter readCount;
    private final Counter cacheHit;
    private final Counter cacheMiss;
    private final BasicCounter writeCountTotal;
    private final StepCounter writeCount;


    private static StatsTimer newStatsTimer(String name) {
        final double [] percentiles = {5.0, 50.0, 95.0, 99.5};
        final StatsConfig statsConfig = new StatsConfig.Builder()
                .withSampleSize(1000)
                .withPercentiles(percentiles)
                .build();
        final MonitorConfig config = MonitorConfig.builder(name).build();
        return new StatsTimer(config, statsConfig);
    }

    private static ServoMonitor INSTANCE = new ServoMonitor();

    private ServoMonitor() {
        fileReadTimer = newStatsTimer("fileReadTimer");
        fileWriteTimer = newStatsTimer("fileWriteTimer");
        folderScanningTimer = newStatsTimer("folderScanningTimer");
        fileDeleteTimer = newStatsTimer("fileDeleteTimer");
        folderDeleteTimer = newStatsTimer("folderDeleteTimer");
        readCountTotal = new BasicCounter(MonitorConfig.builder("readCountTotal").build());
        readCount = new StepCounter(MonitorConfig.builder("readCount").build());
        cacheHit = new StepCounter(MonitorConfig.builder("cacheHit").build());
        cacheMiss = new StepCounter(MonitorConfig.builder("cacheMiss").build());
        writeCountTotal = new BasicCounter(MonitorConfig.builder("writeCountTotal").build());
        writeCount = new StepCounter(MonitorConfig.builder("writeCount").build());
        Monitors.registerObject("MetricsMonitor", this);
    }

    public static ServoMonitor getInstance() {
        return INSTANCE;
    }

    public StatsTimer getFileReadTimer() {
        return fileReadTimer;
    }

    public StatsTimer getFileWriteTimer() {
        return fileWriteTimer;
    }

    public Counter getReadCountTotal() {
        return readCountTotal;
    }

    public Counter getReadCount() {
        return readCount;
    }

    public Counter getCacheHit() {
        return cacheHit;
    }

    public Counter getCacheMiss() {
        return cacheMiss;
    }

    public BasicCounter getWriteCountTotal() {
        return writeCountTotal;
    }

    public StepCounter getWriteCount() {
        return writeCount;
    }

    public StatsTimer getFolderScanningTimer() {
        return folderScanningTimer;
    }

    public StatsTimer getFileDeleteTimer() {
        return fileDeleteTimer;
    }

    public StatsTimer getFolderDeleteTimer() {
        return folderDeleteTimer;
    }
}
