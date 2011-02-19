package com.lostaris.bukkit.alchemy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Reads in config file 
 * @author lostaris
 */

public class ReadAlchemyReq {
	public static final Logger log = Logger.getLogger("Minecraft");

	public static HashMap<String, ArrayList<ItemStack> > readProperties() {
		HashMap<String, ArrayList<ItemStack> > map = new HashMap<String, ArrayList<ItemStack> >();

		try {
			String fileName = "plugins/Alchemy/AlchemyRecipes.properties";
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				if ((line.trim().length() == 0) || 
						(line.charAt(0) == '#')) {
					continue;
				}
				int keyPosition = line.indexOf('=');
				String[] reqs;
				ArrayList<ItemStack> itemReqs = new ArrayList<ItemStack>();
				String item = line.substring(0, keyPosition).trim();
				String[] itemSplit = item.split("\\(");
				itemReqs.add(0, new ItemStack(Material.getMaterial(itemSplit[0]), Integer.parseInt(itemSplit[1].substring(0, itemSplit[1].length() -1))));

				String recipiesString = line.substring(keyPosition+1, line.length()).trim();
				String[] allReqs = recipiesString.split(":");		
				for (int i =0; i < allReqs.length; i++) {
					reqs = allReqs[i].split(",");
					ItemStack currItem = new ItemStack(Integer.parseInt(reqs[0]), Integer.parseInt(reqs[1]));
					itemReqs.add(currItem);
				}
				map.put(itemSplit[0], itemReqs);
			}
			reader.close();
		} catch (Exception e) {
			log.info("Error reading AlchemyRecipies.properties." +
					"Either file is not located in the Alchemy folder in the plugins directory" +
					"or the incorrect syntax is used");
		}

		return map;
	}
}
