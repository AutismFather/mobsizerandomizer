package com.autcraft.mobsizerandomizer.events;

import com.autcraft.mobsizerandomizer.MobSizeRandomizer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SpawnEvent implements Listener {
    private final MobSizeRandomizer plugin;

    public SpawnEvent(MobSizeRandomizer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnEvent(CreatureSpawnEvent event) {
        plugin.scaleMob(event.getEntity());
    }
}
