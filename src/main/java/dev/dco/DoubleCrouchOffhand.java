package dev.dco;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DoubleCrouchOffhand extends JavaPlugin implements Listener {

    private final Map<UUID, Long> lastSneak = new HashMap<>();
    private final Set<UUID> disabled = new HashSet<>();
    private long windowMs = 400L;
    private boolean requirePermission = false;

    @Override
    public void onEnable() {
        getConfig().addDefault("window-ms", 400);
        getConfig().addDefault("require-permission", false);
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new DamageBoost(this), this);
    }

    private void loadSettings() {
        reloadConfig();
        windowMs = Math.max(100L, getConfig().getLong("window-ms", 400L));
        requirePermission = getConfig().getBoolean("require-permission", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return; // only count the moment the player starts crouching
        }
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        if (disabled.contains(id)) {
            return;
        }
        if (requirePermission && !player.hasPermission("doublecrouchoffhand.use")) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = lastSneak.get(id);

        if (last != null && now - last <= windowMs) {
            swapHands(player);
            lastSneak.remove(id);
        } else {
            lastSneak.put(id, now);
        }
    }

    private void swapHands(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();
        ItemStack off = inv.getItemInOffHand();
        inv.setItemInMainHand(off);
        inv.setItemInOffHand(main);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastSneak.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("doublecrouchoffhand.admin")) {
                sender.sendMessage(Component.text("You don't have permission."));
                return true;
            }
            loadSettings();
            sender.sendMessage(Component.text("DoubleCrouchOffhand reloaded."));
            return true;
        }

        if (sender instanceof Player player && (args.length == 0 || args[0].equalsIgnoreCase("toggle"))) {
            UUID id = player.getUniqueId();
            if (disabled.remove(id)) {
                player.sendMessage(Component.text("Double-crouch offhand swap: ON"));
            } else {
                disabled.add(id);
                player.sendMessage(Component.text("Double-crouch offhand swap: OFF"));
            }
            return true;
        }

        sender.sendMessage(Component.text("Usage: /" + label + " [toggle|reload]"));
        return true;
    }
  }
