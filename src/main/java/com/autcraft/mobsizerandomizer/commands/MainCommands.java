package com.autcraft.mobsizerandomizer.commands;

import com.autcraft.mobsizerandomizer.MobSizeRandomizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommands implements CommandExecutor, TabCompleter {
    private final MobSizeRandomizer plugin;

    public MainCommands(MobSizeRandomizer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Mob Size Randomizer"));
            return true;
        }

        // RELOAD
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("mobsizerandomizer.reload")) {
            plugin.loadConfig();
            sender.sendMessage(Component.text("Mob Size Randomizer Configuration Reloaded", NamedTextColor.AQUA));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> commands = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("mobsizerandomizer.reload")) {
                commands.add("reload");
            }

            StringUtil.copyPartialMatches(args[0], commands, completions);
            Collections.sort(completions);
        }

        return completions;
    }
}
