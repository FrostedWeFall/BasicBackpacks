package io.github.frostedwefall.basicbackpacks;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BasicBackpacks extends JavaPlugin implements Listener {
    private static final int BACKPACK_SIZE = 27;
    private static final String BACKPACK_TITLE = "Your Backpack";
    private final Map<UUID, Inventory> openBackpacks = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // Save periodically as a safety net for players who remain online for a long time.
        getServer().getScheduler().runTaskTimer(
                this, this::saveAllBackpacks, 20L * 60 * 5, 20L * 60 * 5
        );

        getLogger().info("BasicBackpacks has been enabled.");
    }

    @Override
    public void onDisable() {
        saveAllBackpacks();
        getLogger().info("BasicBackpacks has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("bbp")) {
            sender.sendMessage(ChatColor.AQUA + "BasicBackpacks " + ChatColor.GRAY + "v1.0.0");
            sender.sendMessage(ChatColor.GRAY + "Created originally by FrostedWeFall; modernised project.");
            return true;
        }

        if (name.equals("bbhelp")) {
            sender.sendMessage(ChatColor.GRAY + "-----+----------------------+-----");
            sender.sendMessage(ChatColor.AQUA + "BasicBackpacks v1.0.0");
            sender.sendMessage(ChatColor.GRAY + "/backpack " + ChatColor.AQUA + "- Opens your personal backpack");
            sender.sendMessage(ChatColor.GRAY + "/bp " + ChatColor.AQUA + "- Alias for /backpack");
            sender.sendMessage(ChatColor.GRAY + "/bbp " + ChatColor.AQUA + "- Plugin information");
            sender.sendMessage(ChatColor.GRAY + "-----+----------------------+-----");
            return true;
        }

        if (name.equals("backpack") || name.equals("bp")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can open a backpack.");
                return true;
            }
            if (!player.hasPermission("basicbackpacks.use")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use backpacks.");
                return true;
            }

            Inventory backpack = openBackpacks.computeIfAbsent(
                    player.getUniqueId(), ignored -> loadBackpack(player.getUniqueId())
            );
            player.openInventory(backpack);
            player.sendMessage(ChatColor.GRAY + "Opening your personal backpack.");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Inventory backpack = openBackpacks.remove(playerId);
        if (backpack != null) {
            saveBackpack(playerId, backpack);
            saveConfig();
        }
    }

    private Inventory loadBackpack(UUID playerId) {
        Inventory inventory = Bukkit.createInventory(null, BACKPACK_SIZE, Component.text(BACKPACK_TITLE));
        String basePath = "BasicBackpacks." + playerId;
        ConfigurationSection slots = getConfig().getConfigurationSection(basePath + ".slots");

        if (slots != null) {
            for (String key : slots.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot < 0 || slot >= inventory.getSize()) {
                        continue;
                    }
                    Object value = slots.get(key);
                    if (value instanceof ItemStack item && !item.getType().isAir()) {
                        inventory.setItem(slot, item.clone());
                    }
                } catch (NumberFormatException ignored) {
                    getLogger().warning("Ignoring invalid backpack slot '" + key + "' for " + playerId);
                }
            }
            return inventory;
        }

        // Best-effort migration from the old format, which stored entries as a, b, c...
        ConfigurationSection legacy = getConfig().getConfigurationSection(basePath);
        if (legacy != null) {
            int slot = 0;
            for (String key : legacy.getKeys(false)) {
                if (key.equals("slots") || slot >= inventory.getSize()) {
                    continue;
                }
                ConfigurationSection oldItem = legacy.getConfigurationSection(key);
                if (oldItem == null) {
                    continue;
                }
                Material material = Material.matchMaterial(oldItem.getString("type", ""));
                int amount = oldItem.getInt("amount", 1);
                if (material != null && !material.isAir() && amount > 0) {
                    inventory.setItem(slot++, new ItemStack(material, Math.min(amount, material.getMaxStackSize())));
                }
            }
            if (slot > 0) {
                getLogger().info("Migrated legacy backpack data for " + playerId
                        + ". Please check item order and durability in-game.");
            }
        }
        return inventory;
    }

    private void saveBackpack(UUID playerId, Inventory inventory) {
        String basePath = "BasicBackpacks." + playerId;
        getConfig().set(basePath, null);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                getConfig().set(basePath + ".slots." + slot, item.clone());
            }
        }
    }

    private void saveAllBackpacks() {
        for (Map.Entry<UUID, Inventory> entry : openBackpacks.entrySet()) {
            saveBackpack(entry.getKey(), entry.getValue());
        }
        saveConfig();
    }
}
