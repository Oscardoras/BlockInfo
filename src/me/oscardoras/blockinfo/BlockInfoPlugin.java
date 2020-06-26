package me.oscardoras.blockinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import me.oscardoras.spigotutils.BukkitPlugin;
import me.oscardoras.spigotutils.command.v1_16_1_V1.Argument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister;
import me.oscardoras.spigotutils.command.v1_16_1_V1.LiteralArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister.CommandExecutorType;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister.PerformedCommand;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.IntegerArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.ItemStackArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.LocationArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.OfflinePlayerArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.LocationArgument.LocationType;
import me.oscardoras.spigotutils.io.ConfigurationFile;
import me.oscardoras.spigotutils.io.TranslatableMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class BlockInfoPlugin extends BukkitPlugin implements Listener {
	
	protected final Map<Chunk, ConfigurationFile> configs = new HashMap<Chunk, ConfigurationFile>();
	
	@Override
	public void onLoad() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("here", new LiteralArgument("here"));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.ENTITY, (cmd) -> {
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(cmd.getLocation().getBlock());
			return exec(cmd, blocks, null, null, 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("look", new LiteralArgument("look"));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(((Player) cmd.getExecutor()).getTargetBlock(null, 512).getLocation().getBlock());
			return exec(cmd, blocks, null, null, 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("at", new LiteralArgument("at"));
		arguments.put("command.getLocation()", new LocationArgument(LocationType.BLOCK_LOCATION));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.ALL, (cmd) -> {
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(((Location) cmd.getArg(0)).getBlock());
			return exec(cmd, blocks, null, null, 0);
		});
		
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), null, null, 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(1), null, 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), null, (OfflinePlayer) cmd.getArg(1), 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), null, null, (int) cmd.getArg(1));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(1), (OfflinePlayer) cmd.getArg(2), 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(1), null, (int) cmd.getArg(2));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(2), (OfflinePlayer) cmd.getArg(1), 0);
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), null, (OfflinePlayer) cmd.getArg(1), (int) cmd.getArg(2));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(2), null, (int) cmd.getArg(1));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), null, (OfflinePlayer) cmd.getArg(2), (int) cmd.getArg(1));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(1), (OfflinePlayer) cmd.getArg(2), (int) cmd.getArg(3));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(1), (OfflinePlayer) cmd.getArg(3), (int) cmd.getArg(2));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(2), (OfflinePlayer) cmd.getArg(1), (int) cmd.getArg(3));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(3), (OfflinePlayer) cmd.getArg(1), (int) cmd.getArg(2));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(2), (OfflinePlayer) cmd.getArg(3), (int) cmd.getArg(1));
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("search", new LiteralArgument("search"));
		arguments.put("distance", new IntegerArgument(0, 10));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("since"));
		arguments.put("days", new IntegerArgument(1));
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("player"));
		arguments.put("targets", new OfflinePlayerArgument());
		arguments.put(UUID.randomUUID().toString(), new LiteralArgument("block"));
		arguments.put("block", new ItemStackArgument());
		CommandRegister.register("blockinfo", arguments, new Permission("blockinfo.command.blockinfo"), CommandExecutorType.PLAYER, (cmd) -> {
			return search(cmd, cmd.getLocation(), (int) cmd.getArg(0), (ItemStack) cmd.getArg(3), (OfflinePlayer) cmd.getArg(2), (int) cmd.getArg(1));
		});
	}
	
	private int search(PerformedCommand cmd, Location location, int distance, ItemStack itemStack, OfflinePlayer offlinePlayer, int since) {
		List<Block> blocks = new ArrayList<Block>();
		
		World world = location.getWorld();
		int minX = location.getBlockX() - distance;
		int maxX = location.getBlockX() + distance;
		int minY = location.getBlockY() - distance;
		int maxY = location.getBlockY() + distance;
		int minZ = location.getBlockZ() - distance;
		int maxZ = location.getBlockZ() + distance;
		for (int x = minX; x <= maxX; x++)
			for (int y = minY; y <= maxY; y++)
				for (int z = minZ; z <= maxZ; z++) {
					Location l = new Location(world, x, y, z);
					if (location.distance(l) <= distance) blocks.add(l.getBlock());
				}
		
		return exec(cmd, blocks, itemStack, offlinePlayer, since);
	}
	
	private int exec(PerformedCommand cmd, List<Block> blocks, ItemStack itemStack, OfflinePlayer offlinePlayer, int since) {
		int entries = 0;
		
		for (Block block : blocks) {
			Chunk chunk = block.getChunk();
			
			if (!configs.containsKey(chunk)) configs.put(chunk, new ConfigurationFile(chunk.getWorld().getWorldFolder() + "/blockinfo/" + chunk.getX() + "_" + chunk.getZ() + ".yml"));
			
			FileConfiguration config = configs.get(chunk);
			if (config.contains(block.getX() + "." + block.getY() + "." + block.getZ())) {
				ConfigurationSection section = config.getConfigurationSection(block.getX() + "." + block.getY() + "." + block.getZ());
				if (section.contains("time") && section.contains("player") && section.contains("old_type"))
					try {
						long time = section.getLong("time");
							if (since == 0 || time >= new Date().getTime() - 1000*60*60*24*since) {
							OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(section.getString("player")));
							if (offlinePlayer == null || offlinePlayer.equals(player)) {
								String type;
								if (itemStack != null) type = itemStack.getType().name().toLowerCase();
								else type = null;
								if (type == null || type.equals(section.getString("old_type")) || (section.contains("new_type") && type.equals(section.getString("new_type")))) {
									if (section.contains("new_type")) cmd.sendMessage(
											new TranslatableMessage(this, "place", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(time), player.getName()),
											new ComponentBuilder(section.getString("old_type")).color(ChatColor.GREEN).create(),
											new TranslatableMessage(this, "by"),
											new ComponentBuilder(section.getString("new_type")).color(ChatColor.GREEN).create(),
											new TranslatableMessage(this, "at"),
											new ComponentBuilder(block.getX() + " " + block.getY() + " " + block.getZ()).color(ChatColor.GREEN).create()
									);
									else cmd.sendMessage(
											new TranslatableMessage(this, "break", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(time), player.getName()),
											new ComponentBuilder(section.getString("old_type")).color(ChatColor.GREEN).create(),
											new TranslatableMessage(this, "at"),
											new ComponentBuilder(block.getX() + " " + block.getY() + " " + block.getZ()).color(ChatColor.GREEN).create()
									);
									entries++;
								}
							}
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
			}
		}
		
		cmd.sendMessage(new TranslatableMessage(this, "entries", ""+entries));
		return entries;
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		for (ConfigurationFile fileValue : configs.values()) fileValue.save();
		configs.clear();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			Block block = e.getBlock();
			Chunk chunk = block.getChunk();
			
			if (!configs.containsKey(chunk)) configs.put(chunk, new ConfigurationFile(chunk.getWorld().getWorldFolder() + "/blockinfo/" + chunk.getX() + "_" + chunk.getZ() + ".yml"));
			
			ConfigurationSection section = configs.get(chunk).createSection(block.getX() + "." + block.getY() + "." + block.getZ());
			section.set("time", new Date().getTime());
			section.set("player", e.getPlayer().getUniqueId().toString().toLowerCase());
			section.set("old_type", block.getType().name().toLowerCase());
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(BlockPlaceEvent e) {
		if (!e.isCancelled()) {
			Block block = e.getBlock();
			Chunk chunk = block.getChunk();
			
			if (!configs.containsKey(chunk)) configs.put(chunk, new ConfigurationFile(chunk.getWorld().getWorldFolder() + "/blockinfo/" + chunk.getX() + "_" + chunk.getZ() + ".yml"));
			
			ConfigurationSection section = configs.get(chunk).createSection(block.getX() + "." + block.getY() + "." + block.getZ());
			section.set("time", new Date().getTime());
			section.set("player", e.getPlayer().getUniqueId().toString());
			section.set("old_type", e.getBlockReplacedState().getType().name().toLowerCase());
			section.set("new_type", e.getBlockPlaced().getType().name().toLowerCase());
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(ChunkUnloadEvent e) {
		if (e.isSaveChunk()) {
			Chunk chunk = e.getChunk();
			if (configs.containsKey(chunk)) {
				configs.get(chunk).save();
				configs.remove(chunk);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(WorldSaveEvent e) {
		World world = e.getWorld();
		List<Chunk> toRemove = new ArrayList<Chunk>();
		for (Entry<Chunk, ConfigurationFile> entry : configs.entrySet()) {
			Chunk chunk = entry.getKey();
			if (world.equals(chunk.getWorld())) {
				entry.getValue().save();
				toRemove.add(chunk);
			}
		}
		for (Chunk chunk : toRemove) configs.remove(chunk);
	}
	
}