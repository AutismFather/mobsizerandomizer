package com.autcraft.mobsizerandomizer;

public enum DistributionType {
    UNIFORM,
    NORMAL,
    RIGHT_EXPONENTIAL,
    LEFT_EXPONENTIAL;

    /**
     * Distribution style
     * @param str Checks for distribution style and converts it to a standard enum
     * for MobConfig class to use.
     * @return
     */
    public static DistributionType fromString(String str) {
        switch (str.toLowerCase()) {
            case "normal":
                return NORMAL;
            case "leftexponential":
                return LEFT_EXPONENTIAL;
            case "rightexponential":
                return RIGHT_EXPONENTIAL;
            default:
                return UNIFORM;
        }
    }
}
