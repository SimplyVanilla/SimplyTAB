package net.simplyvanilla.simplytab.rank;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.simplyvanilla.simplytab.SimplyTabPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TablistRankHandler implements Listener {
    private final SimplyTabPlugin plugin;
    private final TeamManager teamManager;

    public TablistRankHandler(SimplyTabPlugin plugin) {
        this.plugin = plugin;
        this.teamManager = this.plugin.getScoreboardLibrary().createTeamManager();
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
        display.addEntry(player.getName());
    }

    private String getTeamName(Player onlinePlayer) {
        List<Map<String, Object>> tmp = (List<Map<String, Object>>) this.plugin.getConfig().getList("sorting");

        int sortingId = 0;
        if (tmp != null)
            for (var section : tmp) {
                var type = (String) section.get("type");

                switch (type.toLowerCase()) {
                    case "index":
                        var placeholder = (String) section.getOrDefault("placeholder", "");
                        var resolvedValue = PlainTextComponentSerializer.plainText().serialize(
                            MiniMessage.miniMessage().deserialize(placeholder, MiniPlaceholders.getAudiencePlaceholders(onlinePlayer))
                        );
                        var values = (List<String>) section.get("values");
                        int index = values.indexOf(resolvedValue);
                        if (index == -1) {
                            index = values.size() + 1;
                        }
                        sortingId += index * 100;
                        break;
                    case "alphabetical":
                        placeholder = (String) section.getOrDefault("placeholder", "");
                        resolvedValue = PlainTextComponentSerializer.plainText().serialize(
                            MiniMessage.miniMessage().deserialize(placeholder, MiniPlaceholders.getAudiencePlaceholders(onlinePlayer))
                        );

                        List<String> playerValues = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                            .map(player -> PlainTextComponentSerializer.plainText().serialize(
                                MiniMessage.miniMessage().deserialize(placeholder, MiniPlaceholders.getAudiencePlaceholders(player))
                            )).toList());

                        // sort player values alphabetically
                        playerValues.sort(String::compareToIgnoreCase);
                        index = playerValues.indexOf(resolvedValue);
                        if (index == -1) {
                            index = playerValues.size() + 1;
                        }
                        sortingId += index;
                        break;
                    default:
                        break;
                }
            }
        // create team name like "0001_name"
        String formattedName = String.format("%05d_%s", sortingId, onlinePlayer.getName());
        // team name can only be 16 characters long
        return formattedName.substring(0, Math.min(formattedName.length(), 16));
    }
}
