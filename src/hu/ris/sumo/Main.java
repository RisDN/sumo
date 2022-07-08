package hu.ris.sumo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Main extends JavaPlugin {
	
	FileManager location_fs;
	Freeze freeze;
	PlayerMoveEventHandler move;
	ConsoleCommandSender console;
	@Override
	public void onEnable() {
		console = Bukkit.getServer().getConsoleSender();
		Collections.sort(argsList);
		Collections.sort(locationList);
		
		getCommand("sumo").setTabCompleter(new TabCompletion(this));
		location_fs = new FileManager(this, "locations.yml");
		freeze = new Freeze(this);
		move = new PlayerMoveEventHandler(this);
		
		saveDefaultConfig();
		reloadConfig();
		getLogger().info("Sumo plugin indítása v" + getDescription().getVersion());
		getServer().getPluginManager().registerEvents(freeze, this);
		getServer().getPluginManager().registerEvents(move, this);
	}
	
	public void onDisable() {
		getLogger().info("Sumo plugin leállítása v" + getDescription().getVersion());
		stop();
	}

	
	public String messageFormatter(String message) {
		return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", getConfig().getString("prefix")));
	}
	

	private List<String> argsList = Arrays.asList("start", "stop", "join", "set", "info", "reload");
	private List<String> locationList = Arrays.asList("spawn", "lobby", "arena1", "arena2");
	private ArrayList<Player> joinedPlayers = new ArrayList<>();
	private int maxPlayers = getConfig().getInt("maximum");
	private boolean ingame;
	private int alertSched;
	private int startSched;
	private int countDown;
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		
		if(!cmd.getLabel().equalsIgnoreCase("sumo")) {
			return true;
		}
		
		
		if(args.length == 0) {
			player.sendMessage(messageFormatter("&5&l-------- &dCica Sumo&5&l --------"));
			player.sendMessage(messageFormatter("&e Parancsok: "));
			player.sendMessage(messageFormatter("&f  - /sumo"));
			for(int i = 0; i < argsList.size(); i++) {
				if(player.hasPermission("sumo.admin")) {
					player.sendMessage(messageFormatter("&f  - /sumo " + argsList.get(i)));
				}
			}
			player.sendMessage(messageFormatter("&5&l---------------------"));
			return true;
		}
		
		
		if(args[0].equalsIgnoreCase("join")) {
			
			if(!ingame) {
				player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nemmegy")));
				return true;
			}
		
			if(joinedPlayers.contains(player)) {
				player.sendMessage(messageFormatter(getConfig().getString("uzenetek.marcsatlakoztal")));
				return true;
			}
			for (Player p : joinedPlayers) {
				p.sendMessage(messageFormatter(getConfig().getString("uzenetek.xycsatlakozott")
				.replace("%player%", player.getName())
				.replace("%joinedplayers%", String.valueOf(joinedPlayers.size()+1))
				.replace("%maxplayers%", String.valueOf(maxPlayers))));
			}
			joinedPlayers.add(player);
			player.teleport(location_fs.getConfig("locations.yml").getLocation("lobby"));
			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.csatlakoztal")
			.replace("%joinedplayers%", String.valueOf(joinedPlayers.size()))
			.replace("%maxplayers%", String.valueOf(maxPlayers))));
			
			
			if(joinedPlayers.size() == getConfig().getInt("minimum")) {
				countDown = getConfig().getInt("varakozas");
				List<Integer> alertTimes = getConfig().getIntegerList("ertesitesek");
				startSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					@Override
					public void run() {
						if(alertTimes.contains(countDown)) {
							for (Player p : joinedPlayers) {
								p.sendMessage(messageFormatter(getConfig().getString("uzenetek.hamarosanindul")
								.replace("%mp%", String.valueOf(countDown))));
							}
						}
						countDown--;
						if(countDown == 0) {
							Bukkit.getScheduler().cancelTask(startSched);
							Bukkit.getScheduler().cancelTask(alertSched);
							Collections.shuffle(joinedPlayers);
							nextRound();
						}
					}
					
				}, 0, 20);
				
			} 

			return true;
		}
		
		if(player.hasPermission("sumo.admin")) {
			if(args[0].equalsIgnoreCase("start")) {
				
				if(ingame) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.marmegy")));
					return true;
				}
				
				ingame = true;
				player.sendMessage(messageFormatter(getConfig().getString("uzenetek.elinditottad")));
				
				int kesleltetes = getConfig().getInt("kesleltetes") * 20;
				alertSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					@Override
					public void run() {
						for (Player p : getServer().getOnlinePlayers()) {
							if(!joinedPlayers.contains(p)) {
						        TextComponent component = new TextComponent(TextComponent.fromLegacyText(messageFormatter(getConfig().getString("uzenetek.hirdeto"))));
						        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sumo join"));
						        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messageFormatter("&dKatt a csatlakozáshoz"))));
						        p.spigot().sendMessage(component);
							}
						}
					}
				}, 0, kesleltetes);
				
				return true;
			}
			
			
			if(args[0].equalsIgnoreCase("stop")) {
				if(!ingame) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nemmegy")));
				}
				stop();
				return true;
			}
			
		
			
			
			if(args[0].equalsIgnoreCase("set")) {
				if(args.length > 1) {
					if(locationList.contains(args[1])) {
						Location currentLocation = player.getLocation();
						location_fs.getConfig("locations.yml").set(args[1], currentLocation);
						location_fs.saveConfig("locations.yml");
						location_fs.reloadConfig("locations.yml");
						player.sendMessage(messageFormatter(getConfig().getString("uzenetek.helybeallitva").replace("%hely%", args[1])));
					} else {
						player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsilyenhelyszin")));
					}
				} else {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsilyenhelyszin")));
				}
				return true;
			}
			
			
			if(args[0].equalsIgnoreCase("reload")) {
				try {
					saveConfig();
					reloadConfig();
					location_fs.saveConfig("locations.yml");
					location_fs.reloadConfig("locations.yml");
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.reloadjo")));
				} catch (Exception e) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.reloadrossz")));
				}
				return true;
			}
		} else {
			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsjogod")));
		}
		return true;
	}
	private ArrayList<Player> inArenaPlayers = new ArrayList<>();
	private int pos = 0;
	private int arenaStartSched;
	private int arenaStartTimer;
	public void nextRound() {
		inArenaPlayers.clear();
		inArenaPlayers.add(joinedPlayers.get(pos));
		if(pos+1 > joinedPlayers.size()) { pos = 0; }
		inArenaPlayers.add(joinedPlayers.get(pos+1));
		
		inArenaPlayers.get(0).teleport(location_fs.getConfig("locations.yml").getLocation("arena1"));
		inArenaPlayers.get(1).teleport(location_fs.getConfig("locations.yml").getLocation("arena2"));
		
		for(Player p : inArenaPlayers) {
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setFlying(false);
			p.setGameMode(GameMode.ADVENTURE);
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 9999, true));
			freeze.freeze(p, true);
		}
		
		arenaStartTimer = getConfig().getInt("visszaszamlalas");
		arenaStartSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for(Player p : inArenaPlayers) {
					p.sendTitle(messageFormatter("&a" + arenaStartTimer), "", 0, 20, 0);
				}

				if(arenaStartTimer == 0) {
					for(Player pl : inArenaPlayers) {
						freeze.freeze(pl, false);
					}
					Bukkit.getScheduler().cancelTask(arenaStartSched);
				}
				arenaStartTimer--;
			}
		}, 0, 20);
		
	}
	
	public void stop() {
		for(Player p : joinedPlayers) {
			p.teleport(location_fs.getConfig("locations.yml").getLocation("spawn"));
			p.sendMessage(messageFormatter(getConfig().getString("uzenetek.eventvege")));
		}
		joinedPlayers.clear();
		inArenaPlayers.clear();
		ingame = false;
	}
	
	public void lose(Player p) {
		if(!inArenaPlayers.contains(p)) { return; }
		
		getLogger().info("cica");
		
		joinedPlayers.remove(p);
		inArenaPlayers.remove(p);
		p.teleport(location_fs.getConfig("locations.yml").getLocation("spawn"));
		p.sendMessage(messageFormatter(getConfig().getString("uzenetek.vesztettel")));
		p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		
		Player winner = inArenaPlayers.get(0);
		winner.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		if(joinedPlayers.size() == 1) {
			arenaWin(winner);
			return;
		}
		winner.sendMessage(messageFormatter(getConfig().getString("uzenetek.kortnyertel")));
		winner.teleport(location_fs.getConfig("locations.yml").getLocation("lobby"));
		nextRound();
	}
	

	public void arenaWin(Player p) {
		for(Player allp : getServer().getOnlinePlayers()) {
			allp.sendMessage(messageFormatter(getConfig().getString("uzenetek.nyertelbroadcast")
			.replace("%nyertes%", p.getName())));
		}
		List<String> winCommands = getConfig().getStringList("nyertesparancsok");
		for(String cmd : winCommands) {
			Bukkit.dispatchCommand(console, cmd.replace("%nyertes%", p.getName()));
		}
		stop();
	}
	public ArrayList<Player> getInArenaPlayers() {
		return inArenaPlayers;
	}
	
}
