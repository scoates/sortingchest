package com.seancoates.minecraft.sortingchest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ChestHandler {

    public static Boolean debug = true;

    protected SortingChestPlugin plugin;
    protected InventoryCloseEvent event;
    protected Player player;
    protected String player_name;

    protected String source_name;
    protected String destination_name;

    public ChestHandler(SortingChestPlugin plugin, InventoryCloseEvent event) {
        this.event = event;
        this.plugin = plugin;

        if (event.getPlayer() instanceof Player){
            Player player = (Player) event.getPlayer();
            this.player = player;
            this.player_name = player.getName();
        }

        this.source_name = plugin.getConfig().getString("source_chest_name", "source").toLowerCase();
        this.destination_name = plugin.getConfig().getString("destination_chest_name", "destination").toLowerCase();
    }

    protected void debug(String message) {
        this.plugin.debug(message, this.player);
    }

    public void doInvClose() {

        // not a player
        if (this.player == null) {
            return;
        }

        // no permission
        if (!this.player.hasPermission(this.plugin.sort_allowed)) {
            this.player.sendMessage("You do not have permission to sort items from this sorting chest.");
            return;
        }

        InventoryHolder holder = this.event.getInventory().getHolder();

        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            if (chest.getCustomName().toLowerCase().equals(this.source_name)) {
                Location loc = chest.getLocation();
                this.debug(this.player_name + " closed the source chest at " + loc);
                this.sortItemsFromChest(chest);
            }
        } else if (holder instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) holder;
            if (((Chest)chest.getLeftSide()).getCustomName().toLowerCase().equals(this.source_name) ||
                    ((Chest)chest.getRightSide()).getCustomName().toLowerCase().equals(this.source_name)) {
                this.player.sendMessage("Chest sorting doesn't work on double chests.");
            }
        }

    }

    protected void sortItemsFromChest(Chest chest) {

        List<Chest> foundChests = this.findDestinationChestsNear(chest.getBlock());
        Inventory inv = chest.getInventory();
        Integer placedItems = 0;

        for (Chest foundChest : foundChests) {
            for (ItemStack is : inv) {
                if (is != null && is.getAmount() > 0) {
                    this.debug("Trying to place: " + is + " : " + is.getType());
                    placedItems += this.sortItemsIntoChest(is, foundChest);
                }
            }
        }

        String itm = "item";
        if (placedItems > 1 || placedItems == 0) {
            itm = "items";
        }
        this.player.sendMessage("Placed " + placedItems + " " + itm + " in Destination chests.");


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
            this.player.sendMessage("Could not place " + unplacedItems + " " + unpl + ".");
        }
    }

    protected List<Chest> findDestinationChestsNear(Block block) {

        // this method finds chest *blocks* not whole chests. this is fine for our purposes.

        this.debug("Searching for chests at (" + block.getLocation() + ") for " + this.player_name + ".");
        List<Chest> foundChests = new ArrayList<>();
        Integer distance = this.plugin.getConfig().getInt("sort_distance", 20);

        for (int x = -(distance); x <= distance; x++) {
            for (int y = -(distance); y <= distance; y++) {
                for (int z = -(distance); z <= distance; z++) {
                    Block foundBlock = block.getRelative(x,y,z);
                    Material material = foundBlock.getType();
                    if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
                        Chest foundChest = (Chest) foundBlock.getState();
                        if (foundChest.getCustomName().toLowerCase().equals(this.destination_name)) {
                            foundChests.add(foundChest);
                            this.debug("Found a destination chest at: " + foundChest.getLocation() + " for " + this.player_name);
                        }
                    }
               }
           }
        }

        this.debug("Done searching for chests for " + this.player_name + ". Found: " + foundChests.size());
        return foundChests;
    }

    protected Integer sortItemsIntoChest(ItemStack sourceItems, Chest chest) {
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
            Integer destSpaceFree;
            if (this.plugin.getConfig().getBoolean("allow_full_stacks", false)) {
                // full stacks of anything
                destSpaceFree = 64;
            } else {
                destSpaceFree = destItems.getMaxStackSize() - destPreAmount;
            }
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
