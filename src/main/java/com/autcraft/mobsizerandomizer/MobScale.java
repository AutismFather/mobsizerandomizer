package com.autcraft.mobsizerandomizer;

public class MobScale {
    private final MobSizeRandomizer plugin;
    private String name;
    private Double maxSize = 1d;
    private Double minSize = 0.8d;
    private boolean exists = false;

    /**
     * MobScale Construction - Create MobScale object
     *
     * @param plugin MobSizeRandomizer
     * @param name String
     */
    public MobScale(MobSizeRandomizer plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        this.setName(name);
        this.setMaxSize(plugin.getConfig().getDouble("mobs." + name.toLowerCase() + ".max"));
        this.setMinSize(plugin.getConfig().getDouble("mobs." + name.toLowerCase() + ".min"));
        plugin.debug("Min for "  +name + " set to " + plugin.getConfig().getDouble("mobs." + name.toLowerCase() + ".min"));
        this.setExists(true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Double maxSize) {
        this.maxSize = maxSize;
    }

    public Double getMinSize() {
        return minSize;
    }

    public void setMinSize(Double minSize) {
        this.minSize = minSize;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
