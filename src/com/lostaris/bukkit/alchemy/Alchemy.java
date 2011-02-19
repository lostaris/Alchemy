package com.lostaris.bukkit.alchemy;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
//import com.nijikokun.bukkit.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
 * Plugin to make allow players to make items by combining one or more items
 * by a player typing /alchemy
 *
 * @author Lostaris
 */
public class Alchemy extends JavaPlugin {
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private static HashMap<String, ArrayList<ItemStack> > recipies;
	public PermissionHandler Permissions = null;
	public static final Logger log = Logger.getLogger("Minecraft");
	public boolean isPermissions = false;

	public Alchemy(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		// TODO: Place any custom initialisation code here
		loadConfig();

		// NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
	}

	public void onDisable() {
		// TODO: Place any custom disable code here

		// NOTE: All registered events are automatically unregistered when a plugin is disabled

		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		System.out.println("Goodbye world!");
	}

	public void onEnable() {
		// TODO: Place any custom enable code here including the registration of any events

		// Register our events
		@SuppressWarnings("unused")
		PluginManager pm = getServer().getPluginManager();

		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		//System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		setupPermissions();
	}

	@Override
	public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String commandLabel, String[] args) {
		Player player = null;

		if(sender instanceof Player) {
			player = (Player) sender;
		}
		String[] split = args;
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("alchemy")) {
			if (split.length == 1 ) {
				try {
					if (split[0].equalsIgnoreCase("reload")) {
						if (!isPermissions || this.Permissions.has(player, "Alchemy.reload")) {
							loadConfig();
							player.sendMessage("§3Re-loaded Alchemy config file");
						} else {
							player.sendMessage("§cYou dont have permission to do the reload command.");
						}
					}else if(split[0].equalsIgnoreCase("all"))	{
						if (!isPermissions || this.Permissions.has(player, "Alchemy.all")) {
							HashMap<String, ArrayList<ItemStack> > recipies = getRecipies();
							if (!recipies.isEmpty()) {
								player.sendMessage("§6List of all items avaliable to make with Alchemy");						
								for (String itemName : recipies.keySet()) {
									int id = Material.getMaterial(itemName).getId();
									player.sendMessage("§6"+itemName+" - /alchemy "+id);
								}
							}
						} else {
							player.sendMessage("§cYou dont have permission to do the all command.");
						}
					} else {
						if (!isPermissions || this.Permissions.has(player, "Alchemy.alchemist")) {
							int itemId = Integer.parseInt(split[0]);
							HashMap<String, ArrayList<ItemStack> > recipies = getRecipies();
							String itemName = Material.getMaterial(itemId).toString();					
							if (recipies.containsKey(itemName)) {
								ArrayList<ItemStack> reqItems = recipies.get(itemName);
								if (AlchemySupport.isEnough(itemName, player)) {
									AlchemySupport.deduct(reqItems, player);
								} else {
									player.sendMessage("§cYou are missing one or more items to make " + reqItems.get(0).getType());
									player.sendMessage("§cNeed: " + AlchemySupport.printFormatReqs(reqItems));
								}
							} else {
								player.sendMessage("§c" +itemName + " not found in recipes file");
							}
						} else {
							player.sendMessage("§cYou dont have permission to do the alchemy command.");
						}
					}
				} catch (Exception e) {
					player.sendMessage("§cInvalid item id");
				}
			} else if (split.length == 2) {								
				try {
					if (split[1].charAt(0) == '?') {
						int itemId = Integer.parseInt(split[0]);
						String itemName = "" +Material.getMaterial(itemId);
						if (recipies.containsKey(itemName)) {							
							player.sendMessage("§6To make " + recipies.get(itemName).get(0).getAmount() + " " +
									Material.getMaterial(itemId) + " you need:");
							player.sendMessage("§6" + AlchemySupport.printFormatReqs(recipies.get(itemName)));
						} else {
							player.sendMessage("§c" + itemName + " not found in recipies file");
						}
					}
				} catch (Exception e){
					player.sendMessage("§cInvalid item id");					
				}
			} else {
				return false;
			}
			//commands are fine
			return true;
		}
		return false;
	}

	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

		if(this.Permissions == null) {
			if(test != null) {
				this.Permissions = ((Permissions)test).getHandler();
				this.isPermissions = true;
			} else {
				log.info("Permission system not enabled. Alchemy plugin defaulting to everybody can use all commands");
			}
		}
	}

	public void loadConfig() {
		try {
			recipies = ReadAlchemyReq.readProperties();
		} catch (Exception e) {
		}
	}

	public static HashMap<String, ArrayList<ItemStack>> getRecipies() {
		return recipies;
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}
}