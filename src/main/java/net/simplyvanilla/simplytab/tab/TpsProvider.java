package net.simplyvanilla.simplytab.tab;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public interface TpsProvider {
    TpsProvider DEFAULT = () -> {
        @NotNull double[] tps = Bukkit.getTPS();

        return tps[0];
    };

    double getTps();
}
