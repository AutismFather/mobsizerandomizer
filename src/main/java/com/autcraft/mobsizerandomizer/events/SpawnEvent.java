package com.autcraft.mobsizerandomizer.events;


import com.autcraft.mobsizerandomizer.MobSizeRandomizer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;


public class SpawnEvent implements Listener {
    private final MobSizeRandomizer plugin;

    public SpawnEvent(MobSizeRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawnEvent(@NotNull CreatureSpawnEvent event) {
        // Adjust scale of mob if not in an excluded world
        if (!plugin.isExcludedWorld(event.getLocation().getWorld().getName()) && canSpawnReasonBeScaled(event)) {
            plugin.scaleMob(event.getEntity());
        }
    }

    /**
     * Handles the chunk load event to modify mob sizes for entities in the loaded chunk
     * if scaling is enabled in the plugin configuration.
     *
     * This method is triggered whenever a chunk is loaded. It retrieves all the entities
     * in the loaded chunk and checks whether the plugin is configured to affect mob sizes
     * upon chunk load. If enabled, it scales the size of any living entity present in the chunk.
     *
     * @param event The ChunkLoadEvent triggered when a chunk is loaded.
     */
    @EventHandler
    public void onChunkLoadEvent(@NotNull ChunkLoadEvent event) {
        Entity[] entitiesarray = event.getChunk().getEntities();

        if (!plugin.isChunkloadeffected()) {
            MobSizeRandomizer.getInstance().debug("Chunk load event ignored");
            return;
        }

        for (Entity entity : entitiesarray) {
            if(entity instanceof LivingEntity) {
                plugin.scaleMob((LivingEntity) entity);
            }
        }
    }

    /**
     * Checks to see if the provided {@link CreatureSpawnEvent} can have the mob scale adjusted based
     * on the {@link CreatureSpawnEvent.SpawnReason} block list.
     *
     * @param event The {@link CreatureSpawnEvent} to check.
     * @return {@code true} if the provided {@link CreatureSpawnEvent} can have the mob scale adjusted.
     */
    private boolean canSpawnReasonBeScaled(@NotNull CreatureSpawnEvent event) {
        if (plugin.isSpawnReasonBlocklistEnabled()) {
            return !plugin.getBlockedSpawnReasons().contains(event.getSpawnReason());
        }
        return true;
    }
}
