package com.autcraft.mobsizerandomizer.events;

import com.autcraft.mobsizerandomizer.MobSizeRandomizer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

public class SpawnEvent implements Listener {
    private final MobSizeRandomizer plugin;

    public SpawnEvent(MobSizeRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnEvent(CreatureSpawnEvent event) {
        // Adjust scale of mob if not in an excluded world
        if (!plugin.isExcludedWorld(event.getLocation().getWorld().getName()) && canSpawnReasonBeScaled(event) && canSpawnBeScaledInLands(event)) {
            plugin.scaleMob(event.getEntity());
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

    /**
     * Checks to see if the provided {@link CreatureSpawnEvent} can have the mob scale adjusted
     * based on if the spawn happened in a {@link me.angeschossen.lands.api.land.Land} or not.
     *
     * @param event The {@link CreatureSpawnEvent} to check.
     * @return {@code true} if the provided {@link CreatureSpawnEvent} can have the mob scale adjusted.
     */
    private boolean canSpawnBeScaledInLands(@NotNull CreatureSpawnEvent event) {
        return plugin.getLandsHook().map(landsHook -> landsHook.doesLandAllowSizeRandomization(event.getLocation())).orElse(true);
    }
}
