package com.autcraft.mobsizerandomizer.external;

import com.autcraft.mobsizerandomizer.MobSizeRandomizer;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.type.NaturalFlag;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/lands-%E2%AD%95-land-claim-plugin-%E2%9C%85-grief-prevention-protection-gui-management-nations-wars-1-21-support.53313/">Lands</a>
 * that McMobSizeRandomizerRPG needs in order to support it.
 */
public class LandsHook {

    private static final ItemStack flagIcon;

    static {
        flagIcon = new ItemStack(Material.ZOMBIE_HORSE_SPAWN_EGG);
    }

    private final MobSizeRandomizer plugin;
    private final LandsIntegration landsIntegration;
    private NaturalFlag randomizeFlag;

    public LandsHook(@NotNull MobSizeRandomizer plugin) {
        this.plugin = plugin;
        this.landsIntegration = LandsIntegration.of(plugin);
        landsIntegration.onLoad(this::setupFlag);
    }

    private void setupFlag() {
        randomizeFlag = NaturalFlag.of(landsIntegration, FlagTarget.PLAYER, "random_mob_size");
        randomizeFlag.setDefaultState(true)
                .setApplyInSubareas(true)
                .setActiveInWar(true)
                .setDisplay(true)
                .setDisplayName("Enable Randomizing Mob Size")
                .setDescription(List.of("Will allow mobs that spawn to have their size", "randomized while within your land."))
                .setIcon(flagIcon);
    }

    /**
     * Checks to see if the provided {@link Player} is standing in is an {@link Area}
     * that they own.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that
     * they own.
     */
    public boolean isPlayerStandingInOwnedLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null) {
            return area.getOwnerUID().equals(player.getUniqueId());
        }
        return false;
    }

    /**
     * Checks to see if the provided {@link Player} is standing in an {@link Area} that
     * they are trusted in.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that they are
     * trusted in.
     */
    public boolean isPlayerStandingInTrustedLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null) {
            return area.isTrusted(player.getUniqueId());
        }
        return false;
    }

    /**
     * Checks to see if the provided {@link Player} is standing in an {@link Area} that
     * they are a tenant of.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that they
     * are a tenant of.
     */
    public boolean isPlayerStandingInTenantLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null && area.getTenant() != null) {
            return area.getTenant() == player.getUniqueId();
        }
        return false;
    }

    /**
     * Checks to see if the {@link Player} is currently standing in an {@link Area}.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided {@link Player} is standing in an {@link Area}.
     */
    public boolean isPlayerStandingInLand(@NotNull Player player) {
        return isLocationInLand(player.getLocation());
    }

    /**
     * Checks to see if the {@link Location} is currently in an {@link Area}.
     *
     * @param location The {@link Location} to check.
     * @return {@code true} if the provided {@link Location} is in an {@link Area}.
     */
    public boolean isLocationInLand(@NotNull Location location) {
        return landsIntegration.getArea(location) != null;
    }

    /**
     * Checks to see if the land belonging to the provided {@link Location} allows mob size randomization.
     *
     * @param location The {@link Location} to check.
     * @return {@code true} if the land belonging to the provided {@link Location} allows mob size randomization.
     */
    public boolean doesLandAllowSizeRandomization(@NotNull Location location) {
        Area area = landsIntegration.getArea(location);
        return area == null || area.hasNaturalFlag(randomizeFlag);
    }
}
