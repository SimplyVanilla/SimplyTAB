package net.simplyvanilla.simplytab.tab;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.simplyvanilla.simplytab.SimplyTabPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

import static io.github.miniplaceholders.api.MiniPlaceholders.getAudienceGlobalPlaceholders;
import static java.lang.String.valueOf;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.Tag.inserting;
import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;

public class TablistManager {
    private final JavaPlugin javaPlugin;
    private final TpsProvider tpsProvider;

    public TablistManager(JavaPlugin plugin) {
        this.javaPlugin = plugin;
        this.tpsProvider = SimplyTabPlugin.isFolia() ? new FoliaTpsProvider() : TpsProvider.DEFAULT;

        this.javaPlugin.getServer().getAsyncScheduler().runAtFixedRate(this.javaPlugin, this::updateTablist, 0, 1, TimeUnit.SECONDS);
    }

    private void updateTablist(ScheduledTask task) {
        for (Player onlinePlayer : this.javaPlugin.getServer().getOnlinePlayers()) {
            if (SimplyTabPlugin.isFolia()) {
                onlinePlayer.getScheduler().run(this.javaPlugin, scheduledTask -> {
                    this.updateTablistForPlayer(onlinePlayer);
                }, () -> {
                });
            } else {
                this.updateTablistForPlayer(onlinePlayer);
            }
        }
    }

    private void updateTablistForPlayer(Player player) {

        player.playerListName(player.displayName());
        TagResolver tpsResolver = resolver("tps", inserting(miniMessage().deserialize(this.getTps())));

        TagResolver resolvers = resolver(
            getAudienceGlobalPlaceholders(player),
            tpsResolver,
            resolver("mc-version", inserting(text(this.javaPlugin.getServer().getMinecraftVersion()))),
            resolver("online-count", inserting(text(valueOf(this.javaPlugin.getServer().getOnlinePlayers().size()))))
        );

        player.sendPlayerListHeaderAndFooter(
            miniMessage().deserialize(String.join("<newline>", this.javaPlugin.getConfig().getStringList("tab.header")), resolvers),
            miniMessage().deserialize(String.join("<newline>", this.javaPlugin.getConfig().getStringList("tab.footer")), resolvers)
        );
    }

    private String getTps() {
        double tps = this.tpsProvider.getTps();

        String absoluteTps = valueOf((int) tps);

        var placeholder = this.javaPlugin.getConfig().getString("tab.tps.*", "%value%");
        if (this.javaPlugin.getConfig().contains("tab.tps.%s".formatted(absoluteTps))) {
            placeholder = this.javaPlugin.getConfig().getString("tab.tps.%s".formatted(absoluteTps), "%value%");
        }

        String formattedTps = String.format("%.2f", tps);
        return placeholder.replace("%value%", formattedTps);
    }
}
