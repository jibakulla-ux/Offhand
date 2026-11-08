package dev.dco;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

public final class DamageBoost implements Listener {

    private final JavaPlugin plugin;

    public DamageBoost(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getConfig().addDefault("damage-multiplier", 2.0);
        plugin.getConfig().addDefault("hostile-mobs", true);
        plugin.getConfig().addDefault("passive-mobs", true);
        plugin.getConfig().addDefault("include-projectiles", true);
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity) || victim instanceof Player || victim instanceof ArmorStand) {
            return;
        }
        if (!isPlayerAttack(event.getDamager())) {
            return;
        }

        boolean hostile = victim instanceof Enemy;
        if (hostile && !plugin.getConfig().getBoolean("hostile-mobs", true)) {
            return;
        }
        if (!hostile && !plugin.getConfig().getBoolean("passive-mobs", true)) {
            return;
        }

        double multiplier = plugin.getConfig().getDouble("damage-multiplier", 2.0);
        if (multiplier < 0 || multiplier == 1.0) {
            return;
        }
        event.setDamage(event.getDamage() * multiplier);
    }

    private boolean isPlayerAttack(Entity damager) {
        if (damager instanceof Player) {
            return true;
        }
        if (damager instanceof Projectile projectile
                && plugin.getConfig().getBoolean("include-projectiles", true)) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof Player;
        }
        return false;
    }
      }
