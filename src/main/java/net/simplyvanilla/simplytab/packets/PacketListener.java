package net.simplyvanilla.simplytab.packets;

import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

public class PacketListener implements Listener {
    private static final String UPDATE_INFO_PACKET = "ClientboundPlayerInfoUpdatePacket";
    private static final String UPDATE_GAME_MODE_PACKET = "UPDATE_GAME_MODE";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void loadAll() {
        Bukkit.getOnlinePlayers().forEach(this::injectPlayer);
    }

    public void unloadAll() {
        Bukkit.getOnlinePlayers().forEach(this::removePlayer);
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {
                try {
                    if (!packet.toString().contains(UPDATE_INFO_PACKET)) return;

                    EnumSet<?> actions = getFieldObject(packet, EnumSet.class.getSimpleName());
                    List<?> entries = getFieldObject(packet, List.class.getSimpleName());
                    if (actions.stream().noneMatch(p -> p.name().equals(UPDATE_GAME_MODE_PACKET))) return;

                } finally {
                    super.write(channelHandlerContext, packet, promise);
                }
            }

        };

        ChannelPipeline pipeline = getPlayerChannel(player).pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    private void removePlayer(Player player) {
        Channel channel = getPlayerChannel(player);
        channel.eventLoop().submit(() -> channel.pipeline().remove(player.getName()));
    }

    private Channel getPlayerChannel(Player player) {
        try {
            if (player == null) throw new NullPointerException("Player cannot be null");

            // ((CraftPlayer) player).getHandle()
            Method getHandle = Arrays.stream(player.getClass().getMethods())
                    .filter(m -> m.getParameterCount() == 0)
                    .filter(m -> m.getReturnType().getSimpleName().equals("EntityPlayer"))
                    .findFirst().orElse(null);
            if (getHandle == null)
                throw new NoSuchMethodException(String.format("Could not find method EntityPlayer() in %s", player.getClass().getCanonicalName()));
            Object handle = getHandle.invoke(player);

            // handle.playerConnection
            Object playerConnection = getFieldObject(handle, "PlayerConnection");

            // playerConnection.networkManager
            Object networkManager = getFieldObject(playerConnection, "NetworkManager");

            // networkManager.channel
            return getFieldObject(networkManager, "Channel");
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            else throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldObject(Object object, String typeName) {
        try {
            Field field = getField(object, typeName);
            return (T) field.get(object);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }

    private Field getField(Object object, String typeName) {
        try {
            Field field = Stream.concat(Arrays.stream(object.getClass().getFields()),
                    Arrays.stream(object.getClass().getDeclaredFields()))
                .filter(f -> f.getType().getSimpleName().equals(typeName))
                .findFirst().orElse(null);
            if (field == null)
                throw new NoSuchFieldException(String.format("Could not find field %s in %s", typeName, object.getClass().getCanonicalName()));
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }
}
