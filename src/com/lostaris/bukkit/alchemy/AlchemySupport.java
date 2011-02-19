package com.lostaris.bukkit.alchemy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
//import com.nijikokun.bukkit.iConomy.iConomy;

/**
 * Handle events for all Player related events
 * @author Lostaris
 */
public class AlchemySupport extends PlayerListener {
	private final Alchemy plugin;

	public AlchemySupport(Alchemy instance) {
		plugin = instance;
	}

	@SuppressWarnings("unchecked")
	public static int getTotalItems(ItemStack item, Player player) {
		int total = 0;
		PlayerInventory inven = player.getInventory();
		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
		//iterator for the hashmap
		Set<?> set = items.entrySet();
		Iterator<?> i = set.iterator();
		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			ItemStack item1 = (ItemStack) me.getValue();
			//if the player has doesn't not have enough of the item used to repair
			total += item1.getAmount();					
		}
		return total;
	}

	@SuppressWarnings("unchecked")
	public int findLargest(ItemStack item, Player player) {
		PlayerInventory inven = player.getInventory();
		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
		int slot = -1;
		int highest = 0;
		//iterator for the hashmap
		Set<?> set = items.entrySet();
		Iterator<?> i = set.iterator();
		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			ItemStack item1 = (ItemStack) me.getValue();
			//if the player has doesn't not have enough of the item used to repair
			if (item1.getAmount() > highest) {
				highest = item1.getAmount();
				slot = (Integer)me.getKey();
			}						
		}		
		return slot;
	}

	@SuppressWarnings("unchecked")
	public static int findSmallest(ItemStack item, Player player) {
		PlayerInventory inven = player.getInventory();
		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
		int slot = -1;
		int smallest = 64;
		//iterator for the hashmap
		Set<?> set = items.entrySet();
		Iterator<?> i = set.iterator();
		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			ItemStack item1 = (ItemStack) me.getValue();
			//if the player has doesn't not have enough of the item used to repair
			if (item1.getAmount() <= smallest) {
				smallest = item1.getAmount();
				slot = (Integer)me.getKey();
			}
		}		
		return slot;
	}
	
	public static String printFormatReqs(ArrayList<ItemStack> items) {
		StringBuffer string = new StringBuffer();
		string.append(" ");
		for (int i = 1; i < items.size(); i++) {
			string.append(items.get(i).getAmount() + " " + items.get(i).getType() + " ");
		}
		return string.toString();
	}

	// checks to see if the player has enough of a list of items
	public static boolean isEnough(String itemName, Player player) {
		ArrayList<ItemStack> reqItems = Alchemy.getRecipies().get(itemName);
		boolean enoughItemFlag = true;
		int currTotal = 0;
		for (int i =1; i < reqItems.size(); i++) {
			ItemStack currItem = new ItemStack(reqItems.get(i).getTypeId(), reqItems.get(i).getAmount());
			
			int neededAmount = reqItems.get(i).getAmount();
			currTotal = getTotalItems(currItem, player);
			if (neededAmount > currTotal) {
				enoughItemFlag = false;
			}
		}
		return enoughItemFlag;
	}
	
	public static void deduct(ArrayList<ItemStack> reqItems, Player player) {
		PlayerInventory inven = player.getInventory();
		for (int i =1; i < reqItems.size(); i++) {
			ItemStack currItem = new ItemStack(reqItems.get(i).getTypeId(), reqItems.get(i).getAmount());
			int neededAmount = reqItems.get(i).getAmount();
			int smallestSlot = AlchemySupport.findSmallest(currItem, player);
			while (neededAmount > 0) {									
				smallestSlot = AlchemySupport.findSmallest(currItem, player);
				ItemStack smallestItem = inven.getItem(smallestSlot);
				if (neededAmount < smallestItem.getAmount()) {
					// got enough in smallest stack deal and done
					ItemStack newSize = new ItemStack(currItem.getType(), smallestItem.getAmount() - neededAmount);
					inven.setItem(smallestSlot, newSize);
					neededAmount = 0;										
				} else {
					// need to remove from more than one stack, deal and continue
					neededAmount -= smallestItem.getAmount();
					inven.clear(smallestSlot);
				}
			}
		}
		ItemStack reward = new ItemStack(reqItems.get(0).getTypeId(), reqItems.get(0).getAmount());
		player.sendMessage("§3You have made " + reward.getAmount() + " " + reward.getType());
		inven.addItem(reward);
	}
	
	public Alchemy getPlugin() {
		return plugin;
	}
}

