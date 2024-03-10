package net.simplyvanilla.simplytab.tab;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.simplyvanilla.simplytab.SimplyTabPlugin;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

import static io.github.miniplaceholders.api.MiniPlaceholders.getAudienceGlobalPlaceholders;
import static java.lang.String.valueOf;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.Tag.inserting;
import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;

public class TablistManager {
    private final SimplyTabPlugin plugin;
    private final TpsProvider tpsProvider;

    public TablistManager(SimplyTabPlugin plugin) {
        this.plugin = plugin;
        this.tpsProvider = SimplyTabPlugin.isFolia() ? new FoliaTpsProvider() : TpsProvider.DEFAULT;

        this.plugin.getServer().getAsyncScheduler().runAtFixedRate(this.plugin, this::updateTablist, 0, 1, TimeUnit.SECONDS);
    }

    private void updateTablist(ScheduledTask task) {
        for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (SimplyTabPlugin.isFolia()) {
                onlinePlayer.getScheduler().run(this.plugin, scheduledTask -> {
                    this.updateTablistForPlayer(onlinePlayer);
                }, () -> {
                });
            } else {
                this.updateTablistForPlayer(onlinePlayer);
            }
        }
    }

    private void updateTablistForPlayer(Player player) {
        Component component = this.plugin.getPrefix(player)
            .append(player.displayName())
            .append(this.plugin.getSuffix(player));
        player.playerListName(component);

        TagResolver tpsResolver = resolver("tps", inserting(miniMessage().deserialize(this.getTps())));

        TagResolver resolvers = resolver(
            getAudienceGlobalPlaceholders(player),
            tpsResolver,
            resolver("mc-version", inserting(text(this.plugin.getServer().getMinecraftVersion()))),
            resolver("online-count", inserting(text(valueOf(this.plugin.getServer().getOnlinePlayers().size()))))
        );

        player.sendPlayerListHeaderAndFooter(
            miniMessage().deserialize(String.join("<newline>", this.plugin.getConfig().getStringList("tab.header")), resolvers),
            miniMessage().deserialize(String.join("<newline>", this.plugin.getConfig().getStringList("tab.footer")), resolvers)
        );
    }

    private String getTps() {
        double tps = this.tpsProvider.getTps();

        String absoluteTps = valueOf((int) tps);

        var placeholder = this.plugin.getConfig().getString("tab.tps.*", "%value%");
        if (this.plugin.getConfig().contains("tab.tps.%s".formatted(absoluteTps))) {
            placeholder = this.plugin.getConfig().getString("tab.tps.%s".formatted(absoluteTps), "%value%");
        }

        String formattedTps = String.format("%.2f", tps);
        return placeholder.replace("%value%", formattedTps);
    }
}
