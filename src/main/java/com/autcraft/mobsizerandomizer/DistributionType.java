package com.autcraft.mobsizerandomizer;

public enum DistributionType {
    UNIFORM,
    NORMAL,
    LEFT_EXPONENTIAL;

    public static DistributionType fromString(String str) {
        switch (str.toLowerCase()) {
            case "normal":
                return NORMAL;
            case "leftexponential":
                return LEFT_EXPONENTIAL;
            default:
                return UNIFORM;
        }
    }
}
