package net.simplyvanilla.simplytab.rank;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.simplyvanilla.simplytab.SimplyTabPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class TablistRankHandler implements Listener {
    private final SimplyTabPlugin plugin;
    private final TeamManager teamManager;
    private final String groupPlaceholder;
    private final String groupPlayerColor;
    private final String tabDisplayName;
    private final List<String> groupSorting;

    public TablistRankHandler(SimplyTabPlugin plugin) {
        this.plugin = plugin;
        this.teamManager = this.plugin.getScoreboardLibrary().createTeamManager();
        this.groupPlaceholder = plugin.getConfig().getString("group-placeholder");
        this.groupPlayerColor = plugin.getConfig().getString("group-player-color");
        this.tabDisplayName = plugin.getConfig().getString("tab-displayname");
        this.groupSorting = plugin.getConfig().getStringList("group-sorting");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        this.createTab(event.getPlayer());
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        String teamName = this.getTeamName(event.getPlayer());
        teamManager.removePlayer(event.getPlayer());
        if (!teamManager.teamExists(teamName)) {
            return;
        }
        teamManager.removeTeam(teamName);

    }

    public void createTab(Player player) {
        teamManager.addPlayer(player);

        String teamName = this.getTeamName(player);
        ScoreboardTeam scoreboardTeam = this.teamManager.createIfAbsent(teamName);
        TeamDisplay display = scoreboardTeam.defaultDisplay();

        display.prefix(MiniMessage.miniMessage().deserialize(
            this.plugin.getConfig().getString("group-prefix", ""),
            MiniPlaceholders.getAudienceGlobalPlaceholders(player)
        ));

        display.suffix(MiniMessage.miniMessage().deserialize(
            this.plugin.getConfig().getString("group-suffix", ""),
            MiniPlaceholders.getAudienceGlobalPlaceholders(player)
        ));

        Component component = MiniMessage.miniMessage().deserialize(this.groupPlayerColor, MiniPlaceholders.getAudienceGlobalPlaceholders(player));

        display.playerColor(NamedTextColor.nearestTo(component.style().color() == null ? NamedTextColor.WHITE : component.style().color()));
        display.displayName(MiniMessage.miniMessage().deserialize(this.tabDisplayName, MiniPlaceholders.getAudienceGlobalPlaceholders(player), Placeholder.component("player_displayname", player.displayName())));
        display.addEntry(player.getName());
    }

    private String getTeamName(Player onlinePlayer) {
        String group = PlainTextComponentSerializer.plainText().serialize(
            MiniMessage.miniMessage().deserialize(this.groupPlaceholder, MiniPlaceholders.getAudiencePlaceholders(onlinePlayer))
        );

        int index = this.groupSorting.indexOf(group);
        if (index == -1) {
            index = this.groupSorting.size() + 1;
        }
        // create team name like "0001_name"
        String formattedName = String.format("%04d_%s", index, onlinePlayer.getName());
        // team name can only be 16 characters long
        return formattedName.substring(0, Math.min(formattedName.length(), 16));
    }
}
