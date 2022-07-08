package hu.ris.sumo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TabCompletion implements TabCompleter {
	
	public TabCompletion(Main main) {	}
	
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> argsList = Arrays.asList("start", "stop", "join", "set", "info", "reload");
		List<String> setArgsList = Arrays.asList("spawn", "lobby", "arena1", "arena2");
		if(args.length == 1) {
			Collections.sort(argsList);
			return argsList;
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("set")) {
			Collections.sort(setArgsList);
			return setArgsList;
		}
		return null;
	}

}
