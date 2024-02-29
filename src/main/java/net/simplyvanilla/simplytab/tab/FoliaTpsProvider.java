package net.simplyvanilla.simplytab.tab;

import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegionScheduler;

public class FoliaTpsProvider implements TpsProvider {
    @Override
    public double getTps() {
        TickData.TickReportData report = TickRegionScheduler.getCurrentRegion()
            .getData()
            .getRegionSchedulingHandle()
            .getTickReport5s(System.nanoTime());

        double average = report.tpsData().segmentAll().average();

        return report == null ? 0D : average;
    }
}
