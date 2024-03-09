package net.simplyvanilla.simplytab.tab;

import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import org.bukkit.Bukkit;

public class FoliaTpsProvider implements TpsProvider {
    @Override
    public double getTps() {

        TickData.TickReportData report = TickRegionScheduler.getCurrentRegion()
            .getData()
            .getRegionSchedulingHandle()
            .getTickReport15s(System.nanoTime());

        return report.tpsData().segmentAll().average();
    }
}
