package net.simplyvanilla.simplytab;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.simplyvanilla.simplytab.packets.PacketListener;
import net.simplyvanilla.simplytab.rank.TablistRankHandler;
import net.simplyvanilla.simplytab.tab.TablistManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SimplyTabPlugin extends JavaPlugin {

    private ScoreboardLibrary scoreboardLibrary;
    private PacketListener packetListener;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(this);
        } catch (NoPacketAdapterAvailableException e) {
            // If no packet adapter was found, you can fallback to the no-op implementation:
            scoreboardLibrary = new NoopScoreboardLibrary();
            this.getLogger().warning("No scoreboard packet adapter available!");
        }

        new TablistManager(this);
        new TablistRankHandler(this);

        this.packetListener = new PacketListener();
        this.packetListener.loadAll();
        Bukkit.getPluginManager().registerEvents(this.packetListener, this);
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) this.packetListener.unloadAll();
        this.scoreboardLibrary.close();
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public @NotNull Component getPrefix(Player player) {
        return MiniMessage.miniMessage().deserialize(
            getConfig().getString("group-prefix", ""),
            MiniPlaceholders.getAudienceGlobalPlaceholders(player)
        );
    }

    public @NotNull Component getSuffix(Player player) {
        return MiniMessage.miniMessage().deserialize(
            getConfig().getString("group-suffix", ""),
            MiniPlaceholders.getAudienceGlobalPlaceholders(player)
        );
    }

    public ScoreboardLibrary getScoreboardLibrary() {
        return this.scoreboardLibrary;
    }
}
