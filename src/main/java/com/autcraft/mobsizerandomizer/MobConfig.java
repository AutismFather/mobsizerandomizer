package com.autcraft.mobsizerandomizer;

/**
 * MOB-Config for each mob
 * with values as min and max size
 * and its distribution type
 */
public class MobConfig {
    private double min;
    private double max;
    private String distribution;

    public MobConfig(double min, double max, String distribution) {
        this.min = min;
        this.max = max;
        this.distribution = distribution;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getDistribution() {
        return distribution;
    }
}
