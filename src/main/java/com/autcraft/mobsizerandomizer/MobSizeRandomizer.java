package com.autcraft.mobsizerandomizer;

import com.autcraft.mobsizerandomizer.commands.MainCommands;
import com.autcraft.mobsizerandomizer.events.SpawnEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class MobSizeRandomizer extends JavaPlugin {

    private Random rand = new Random();
    private boolean debug;
    private Double defaultMaxSize = 1d;
    private Double defaultMinSize = 0.8d;
    private Map<String, MobScale> mobScaleMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        loadConfig();

        getCommand("mobsizerandomizer").setExecutor(new MainCommands(this));
        getServer().getPluginManager().registerEvents(new SpawnEvent(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Load values from config.yml
     */
    public void loadConfig() {
        reloadConfig();

        // Set values from config.yml
        setDebug(getConfig().getBoolean("debug", false));
        setDefaultMaxSize(getConfig().getDouble("defaultmax", defaultMaxSize));
        setDefaultMinSize(getConfig().getDouble("defaultmin", defaultMinSize));
        setMobScaleMap();
    }

    /**
     * Create the mob scale map to use from values in config.yml
     */
    private void setMobScaleMap() {
        this.mobScaleMap.clear();
        for (String key : getConfig().getConfigurationSection("mobs").getKeys(false)) {
            key = key.toUpperCase();
            this.mobScaleMap.put(key, new MobScale(this, key));
        }
    }

    /**
     * Display debug message to console if debug: true
     *
     * @param message
     */
    public void debug(String message) {
        if (isDebug()) {
            getLogger().info(message);
        }
    }

    public Double getDefaultMaxSize() {
        return defaultMaxSize;
    }

    public void setDefaultMaxSize(Double defaultMaxSize) {
        this.defaultMaxSize = defaultMaxSize;
    }

    public Double getDefaultMinSize() {
        return defaultMinSize;
    }

    public void setDefaultMinSize(Double defaultMinSize) {
        this.defaultMinSize = defaultMinSize;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Scale the mob according to data from the config.yml
     *
     * @param entity LivingEntity
     */
    public void scaleMob(LivingEntity entity) {
        if (entity instanceof Player) {
            return;
        }

        // Set the default sizes
        Double scale = 1d;
        Double max = getDefaultMaxSize();
        Double min = getDefaultMinSize();
        String entityName = entity.getName().toUpperCase();

        // Get mob scale info from the list/map
        if (mobScaleMap.get(entityName) != null) {
            debug("Mob found! " + entityName);

            MobScale mobScale = mobScaleMap.get(entityName);
            max = mobScale.getMaxSize();
            min = mobScale.getMinSize();
            debug("Min set to " + min + " and max set to " + max);
        }

        // Get random value to scale
        // If the % is found to be 0, then keep scale at 1.
        if (max > min ) {
            scale = rand.nextDouble(min, max);
        }

        // Set the scale attribute
        entity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(scale);
        debug("Scale of " + entityName + " set to " + scale);
    }
}
