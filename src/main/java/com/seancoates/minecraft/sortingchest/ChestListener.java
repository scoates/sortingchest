package com.seancoates.minecraft.sortingchest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ChestListener implements Listener {
	
	public static Boolean debug = false;
	public static Integer radius = 20; // cube, though  TODO: config
	
	protected static void debugMessage(Player player, String message) {
		if (debug) {
			player.sendMessage(message);
		}
	}

	@EventHandler
	public void onInv(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player){
        	InventoryHolder holder = event.getInventory().getHolder();
            Player player = (Player) event.getPlayer();

        	if (holder instanceof Chest) {
                Chest chest = (Chest) holder;
                if (chest.getCustomName().toLowerCase().equals("source")) {
                	Location loc = chest.getLocation();
                	debugMessage(player, "You closed the source chest at " + loc);
                	this.sortItemsFromChest(chest, player);
                }
            } else if (holder instanceof DoubleChest) {
            	DoubleChest chest = (DoubleChest) holder;
            	if (((Chest)chest.getLeftSide()).getCustomName().toLowerCase().equals("source") ||
            			((Chest)chest.getRightSide()).getCustomName().toLowerCase().equals("source")) {
            		player.sendMessage("Chest sorting doesn't work on double chests.");
            	}
            }
        }
	}
	
	protected void sortItemsFromChest(Chest chest, Player player) {
		
		List<Chest> foundChests = this.findDestinationChestsNear(chest.getBlock(), player);
		Inventory inv = chest.getInventory();
		Integer placedItems = 0;
		
		for (Chest foundChest : foundChests) {
			for (ItemStack is : inv) {
				if (is != null && is.getAmount() > 0) {
					debugMessage(player, "Trying to place: " + is + " : " + is.getType());
					placedItems += this.sortItemsIntoChest(is, foundChest, player);
				}
			}
		}
		
		String itm = "item";
		if (placedItems > 1 || placedItems == 0) {
			itm = "items";
		}
		player.sendMessage("Placed " + placedItems + " " + itm + " in Destination chests.");
		
		
		Integer unplacedItems = 0;
		for (ItemStack is : inv) {
			if (is != null && is.getAmount() > 0) {
				unplacedItems += is.getAmount();
			}
		}

		if (unplacedItems > 0) {
			String unpl = "item";
			if (unplacedItems > 1) {
				unpl = "items";
			}
			player.sendMessage("Could not place " + unplacedItems + " " + unpl + ".");
		}
	}
	
	protected List<Chest> findDestinationChestsNear(Block block, Player player) {
		// this method finds chest *blocks* not whole chests. this is fine for our purposes.
		debugMessage(player, "Searching for chests.");
		List<Chest> foundChests = new ArrayList<>();
		for (int x = -(radius); x <= radius; x ++) {
			for (int y = -(radius); y <= radius; y ++) {
				for (int z = -(radius); z <= radius; z ++) {
					Block foundBlock = block.getRelative(x,y,z);
					Material material = foundBlock.getType();
					if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
						Chest foundChest = (Chest) foundBlock.getState();
						if (foundChest.getCustomName().toLowerCase().equals("destination")) {
							foundChests.add(foundChest);
							debugMessage(player, "Found a destination chest at: " + foundChest.getLocation());
						}
					}
		       }
		   }
		}
		debugMessage(player, "Done searching for chests. Found: " + foundChests.size());
		return foundChests;
	}
	
	protected Integer sortItemsIntoChest(ItemStack sourceItems, Chest chest, Player player) {
		Integer placedItems = 0;
		Material sourceItemsType = sourceItems.getType();
		Inventory inv = chest.getInventory();
		for (ItemStack destItems : inv) {
			if (destItems == null) {
				// empty slot
				continue;
			}
			
			if (destItems.getType() != sourceItemsType) {
				// items don't match
				continue;
			}
			
			Integer destPreAmount = destItems.getAmount();
			Integer destSpaceFree = destItems.getMaxStackSize() - destPreAmount; // TODO: config for stacks of 64
			if (destSpaceFree < 1) {
				// no space left
				continue;
			}
			
			Integer sourceAmount = sourceItems.getAmount();
			Integer itemsToPlace = Math.min(destSpaceFree, sourceAmount);
			
			destItems.setAmount(destItems.getAmount() + itemsToPlace);
			sourceItems.setAmount(sourceItems.getAmount() - itemsToPlace);
			
			placedItems += itemsToPlace;
		}
		return placedItems;
	}

	
}
