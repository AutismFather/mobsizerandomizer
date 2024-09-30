package com.autcraft.mobsizerandomizer;

import com.autcraft.mobsizerandomizer.commands.MainCommands;
import com.autcraft.mobsizerandomizer.events.SpawnEvent;
import com.google.common.collect.ImmutableSet;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class MobSizeRandomizer extends JavaPlugin {
    private boolean debug;
    private Double defaultMaxSize = 1d;
    private Double defaultMinSize = 0.8d;
    private Map<String, MobConfig> mobConfigs;
    private List<String> excludedWorlds = new ArrayList<>();
    private boolean chunkloadeffected;
    // Spawn reason blocklist
    private boolean spawnReasonBlocklistEnabled = false;
    private Set<CreatureSpawnEvent.SpawnReason> blockedSpawnReasons = new HashSet<>();
    public static MobSizeRandomizer instance;

    private final Random random = new Random();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        loadConfig();

        getCommand("mobsizerandomizer").setExecutor(new MainCommands(this));
        getServer().getPluginManager().registerEvents(new SpawnEvent(this), this);
        getLogger().info("Mob Size Randomizer Loaded.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }

    /**
     * Load values from config.yml
     */
    public void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();

        // Set values from config.yml
        setDebug(config.getBoolean("debug", false));
        setDefaultMaxSize(config.getDouble("defaultmax", defaultMaxSize));
        setDefaultMinSize(config.getDouble("defaultmin", defaultMinSize));
        setSpawnReasonBlocklistEnabled(config.getBoolean("enable-spawn-reason-blocklist", spawnReasonBlocklistEnabled));

        // Blocked spawn reasons
        setBlockedSpawnReasons(config.getStringList("spawn-reasons-blocklist").stream()
                .map(CreatureSpawnEvent.SpawnReason::valueOf)
                .collect(Collectors.toSet()));

        // Chunkload effected
        setChunkloadeffected(config.getBoolean("enable-chunkloadeffected", chunkloadeffected));

        // Load excluded worlds
        setExcludedWorlds(config.getStringList("excluded-worlds"));

        // Load mob configurations
        setMobScaleMap(config);
    }

    /**
     * Load the mob scale configurations from the provided FileConfiguration.
     * @param config The configuration to read from.
     */
    private void setMobScaleMap(FileConfiguration config) {
        Map<String, MobConfig> mobConfigs = new HashMap<>();

        // Adds safety avoiding a nullpointer exception if mobs[] is empty in the config.yml file
        ConfigurationSection mobsSection = getConfig().getConfigurationSection("mobs");
        if (mobsSection != null) {
            for (String mobName : config.getConfigurationSection("mobs").getKeys(false)) {
                double min = config.getDouble("mobs." + mobName + ".min", 1.0); // Default to 1.0 if not found
                double max = config.getDouble("mobs." + mobName + ".max", 1.0); // Default to 1.0 if not found
                String distribution = config.getString("mobs." + mobName + ".distribution", "uniform"); // Default to "uniform"

                if(isDebug()){
                    debug("min = " + min + ", max = " + max + ", distribution = " + distribution);
                }

                mobConfigs.put(mobName, new MobConfig(min, max, distribution));

            }
            System.out.print("MOBCONFIGS" + mobConfigs.toString());
        } else {
            getLogger().warning("Mobs section missing or empty in config.yml.");
        }

        // Optionally store mobConfigs in a field if you need to access it later
        this.mobConfigs = mobConfigs;
    }

    /**
     * Getting the random size depending on the Mob, Mob-Config values
     * @param min   minimum size
     * @param max   maximum size
     * @param distributionType Type pf distribution, See DistributionType enum
     * @return  Return Scale value
     */
    public double getRandomSize(double min, double max, String distributionType) {
        DistributionType dist = DistributionType.fromString(distributionType);
        switch (dist) {
            case NORMAL:
                return getNormalRandom(min, max);
            case LEFT_EXPONENTIAL:
                return getLeftExponentialRandom(min, max);
            case RIGHT_EXPONENTIAL:
                return getRightExponentialRandom(min, max);
            default:
                return getUniformRandom(min, max);
        }
    }

    // Uniform distribution between min and max
    private double getUniformRandom(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    //
    /** Normal distribution with mean = (max + min) / 2, and standard deviation
     * TODO Suggest making this more customizable,
     * by editing stdDev/spread and mean/midpoint.
     * @param min minimum value
     * @param max maximum value
     * @return
     */
    private double getNormalRandom(double min, double max) {
        double mean = (max + min) / 2;
        double stdDev = (max - min) / 6; // 99.7% of values within min-max
        double value;
        do {
            value = mean + random.nextGaussian() * stdDev;
        } while (value < min || value > max); // Clamp within range
        return value;
    }

    /** Left-skewed exponential distribution
     * TODO Suggest making this more customizable,
     * By editing lambda
     * @param min, minimum value
     * @param max maximum value
     * @return
     */
    private double getLeftExponentialRandom(double min, double max) {
        double lambda = 1.0; // Adjust lambda for steeper curves
        double value;
        do {
            value = max - Math.log(1 - random.nextDouble()) / lambda;  // Reflect the distribution
        } while (value < min || value > max); // Clamp within range
        return value;
    }

    /** Right-skewed exponential distribution
     * TODO Suggest making this more customizable,
     * By editing lambda
     * @param min, minimum value
     * @param max maximum value
     * @return
     */
    // Right-skewed exponential distribution
    private double getRightExponentialRandom(double min, double max) {
        double lambda = 1.0; // Adjust lambda for steeper curves
        double value;
        do {
            value = min + Math.log(1 - random.nextDouble()) / (-lambda);
        } while (value < min || value > max); // Clamp within range
        return value;
    }

    /**
     * Set the list of excluded worlds
     * Worlds in this will not alter mob scale sizes.
     */
    private void setExcludedWorlds(@NotNull List<String> stringList) {
        this.excludedWorlds = getConfig().getStringList("excluded-worlds");
    }

    /**
     * Check to see if given world is in excluded world list.\
     *
     * @param world String
     * @return Boolean
     */
    public Boolean isExcludedWorld(String world) {
        for (String w : this.excludedWorlds) {
            if (w.equalsIgnoreCase(world)) {
                return true;
            }
        }
        return false;
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

    public static MobSizeRandomizer getInstance() {
        if (instance == null) {
            instance = new MobSizeRandomizer();
        }
        return instance;
    }

    /**
     * Checks to see if certain {@link CreatureSpawnEvent.SpawnReason}s should be excluded from
     * having size randomized.
     *
     * @return {@code true} if certain {@link CreatureSpawnEvent.SpawnReason}s should be excluded from
     * having size randomized.
     */
    public boolean isSpawnReasonBlocklistEnabled() {
        return spawnReasonBlocklistEnabled;
    }

    /**
     * Sets if the {@link CreatureSpawnEvent.SpawnReason} block list should be used or not.
     *
     * @param spawnReasonBlocklistEnabled If the {@link CreatureSpawnEvent.SpawnReason} block list should be used or not.
     */
    public void setSpawnReasonBlocklistEnabled(boolean spawnReasonBlocklistEnabled) {
        this.spawnReasonBlocklistEnabled = spawnReasonBlocklistEnabled;
    }

    public void setChunkloadeffected(boolean chunkloadeffected) {
        this.chunkloadeffected = chunkloadeffected;
    }

    public boolean isChunkloadeffected() {
        return chunkloadeffected;
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link CreatureSpawnEvent.SpawnReason}s that are excluded from
     * having their size randomized.
     *
     * @return An {@link ImmutableSet} of all {@link CreatureSpawnEvent.SpawnReason}s that are excluded from
     * having their size randomized.
     */
    @NotNull
    public Set<CreatureSpawnEvent.SpawnReason> getBlockedSpawnReasons() {
        return ImmutableSet.copyOf(blockedSpawnReasons);
    }

    /**
     * Sets the {@link CreatureSpawnEvent.SpawnReason}s that are excluded from having their size randomized.
     *
     * @param blockedSpawnReasons The {@link CreatureSpawnEvent.SpawnReason}s that are excluded from having their size randomized.
     */
    public void setBlockedSpawnReasons(@NotNull Set<CreatureSpawnEvent.SpawnReason> blockedSpawnReasons) {
        this.blockedSpawnReasons = blockedSpawnReasons;
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
        String distribution = "uniform";
        Double max = getDefaultMaxSize();
        Double min = getDefaultMinSize();
        String entityName = entity.getName().replaceAll(" ", "_").toUpperCase();


        // Get mob scale info from the list/map
        if (mobConfigs.get(entityName) != null) {
            debug("Mob found! " + entityName);
            MobConfig mobConfig = mobConfigs.get(entityName);
            if (mobConfig != null) {
                // Proceed with calling getDistribution()
                distribution = mobConfig.getDistribution();
                min = mobConfig.getMin();
                max = mobConfig.getMax();
                debug("Scale of " + entityName + " spawned in world '" + entity.getLocation().getWorld().getName() + "' set to " + scale + " Distribution-type set to " + mobConfigs.get(entityName).getDistribution());
            } else {
                // Handle the case where mobConfig is null (e.g., log a warning or use a default distribution)
                debug("No mobconfig found for " + entityName);
            }

            debug("Min set to " + min + " and max set to " + max + " Distribution-type set to " + distribution);

            // Get random value to scale
            // If the % is found to be 0, then keep scale at 1.
            if (max > min) {
                scale = getRandomSize(min, max, distribution);
                debug( entityName + " scale set to " + scale);
            }
        }else {
            scale = 1.0;
            debug("No mobconfig found for " + entityName + " scale set to " + scale);
        }

        // Set the scale attribute
        entity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(scale);
    }
}
