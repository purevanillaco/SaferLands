package co.purevanilla.mcplugins.saferlands;

import me.angeschossen.lands.api.events.ChunkDeleteEvent;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import me.angeschossen.lands.api.events.land.claiming.LandUnclaimAllEvent;
import me.angeschossen.lands.api.events.land.claiming.selection.LandUnclaimSelectionEvent;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Container;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SaferHandler implements Listener {

    Plugin plugin;

    SaferHandler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUnclaim(LandUnclaimAllEvent event) {
        for (Container container : event.getLand().getContainers()) {
            if (blockUnclaim(container.getChunks(), container.getWorld().getWorld(), event.getPlayerUUID())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLandUnclaim(LandDeleteEvent event) {
        for (Container container : event.getLand().getContainers()) {
            if (blockUnclaim(container.getChunks(), container.getWorld().getWorld(), event.getPlayerUUID())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUnclaimSelection(LandUnclaimSelectionEvent event) {
        if (blockUnclaim(event.getSelection().getChunks(), event.getSelection().getPos1().getWorld(), event.getPlayerUUID())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkDelete(ChunkDeleteEvent event) {
        if (blockUnclaim(List.of(event.getWorld().getChunkAt(event.getX(), event.getZ())), event.getPlayerUUID())){
            event.setCancelled(true);
        }
    }

    boolean blockUnclaim(Collection<Chunk> chunks, UUID unclaimerId) {
        if (unclaimerId == null) {
            return false;
        }
        Player unclaimer = Bukkit.getPlayer(unclaimerId);
        if (unclaimer == null) {
            return false;
        }
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (player.getUniqueId().equals(unclaimerId)) {
                continue;
            }
            for (Chunk chunk : chunks) {
                if(checkChunk(player, chunk)) {
                    unclaimer.sendMessage(
                            Objects.requireNonNull(
                                    MiniMessage.miniMessage().deserializeOrNull(
                                            this.plugin.getConfig().getString("player_present")
                                    )
                            )
                    );
                    plugin.getLogger().info("Player " + player.getName() + " prevented from unclaiming.");
                    return true;
                }
            }
        }
        return false;
    }

    boolean blockUnclaim(Collection<? extends ChunkCoordinate> chunks, World world, UUID unclaimerId) {
        List<Chunk> chunkList = new ArrayList<>();
        for (ChunkCoordinate chunkCoordinate : chunks) {
            Chunk chunk = world.getChunkAt(
                    chunkCoordinate.getX(),
                    chunkCoordinate.getZ()
            );
            chunkList.add(chunk);
        }
        return blockUnclaim(chunkList, unclaimerId);
    }

    boolean checkChunk(Player player, Chunk chunk) {
        return player.getLocation().getChunk().equals(chunk);
    }

}
