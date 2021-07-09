package net.pavela.simpleduel.simpleduel;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;


/*
arena 1 player 1: 8 1 35
arena 1 player 2: -10 1 35
arena 2 player 1: 8 1 1
arena 2 player 2: -10 1 1
arena 3 player 1: 8 1 -34
arena 3 player 2: -10 1 -34
*/

// some stuff xd

public final class Simpleduel extends JavaPlugin {

    Server server = getServer();
    Boolean arenaOccupied = false;
    ConsoleCommandSender console = server.getConsoleSender();
    public ArrayList<ArrayList<Player>> duelRequests = new ArrayList<>();
    public ArrayList<ArrayList<Player>> currentDuels = new ArrayList<>();
    public ArrayList<ArrayList<Object>> playerData = new ArrayList<>();
    public ArrayList<Player> playerTPQueue = new ArrayList<>();
    HashMap<Player, ItemStack[]> inventories = new HashMap<Player, ItemStack[]>();
    World arenaWorld = Bukkit.getWorld("arena");
    PluginDescriptionFile pdf = this.getDescription();
    String version = pdf.getVersion();

    public void savePlayer(Player p){
        inventories.put(p, p.getInventory().getContents());
    }

    public ItemStack[] getSavedPlayerInventory(Player p){

        if(inventories.containsKey(p)){
            return inventories.get(p);
        }

        return new ItemStack[0];
    }

    public class teleportWorld implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                World selectedWorld = Bukkit.getWorld(args[0]);

                Location loc = new Location(selectedWorld, 0, 10 ,0);

                player.teleport(loc);

            }
            return true;
        }
    }

    public class CommandDuel implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                Player target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player is not online.");
                } else {
                    ArrayList<Player> duelList = new ArrayList<>();
                    duelList.add(player);
                    duelList.add(target);
                    duelRequests.add(duelList);

                    player.sendMessage(ChatColor.GREEN + "Duel request sent.");
                    target.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.GREEN + " has sent you a duel request!");
                    target.sendMessage(ChatColor.GREEN + "Use /duelaccept to accept the duel!");
                }
            }
            return true;
        }
    }

    public class CommandDuelAccept implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (duelRequests.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "There are no duel requests.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Duel request accepted! Teleporting in 5 seconds.");

                    for (ListUtils.EnumeratedItem<ArrayList<Player>> selectedPlayer : ListUtils.enumerate(duelRequests)) {
                        if (selectedPlayer.item.get(1) == player) {
                            selectedPlayer.item.get(0).sendMessage(ChatColor.GREEN + "The player has accepted the duel request! Teleporting in 5 seconds.");
                        }
                    }

                    runFunction(player);
                }
            }

            return true;
        }
    }


    public class MyListener implements Listener {
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            if (event.getEntity().getWorld().equals(arenaWorld)) {
                Player playerLost = event.getEntity();
                Player playerWon = null;

                ArrayList<ArrayList<Player>> currentDuelsTemp = new ArrayList<>(currentDuels);
                for (ListUtils.EnumeratedItem<ArrayList<Player>> selectedPlayer :  ListUtils.enumerate(currentDuelsTemp)) {
                    if (selectedPlayer.item.get(0) == playerLost) {
                        playerWon = selectedPlayer.item.get(1);
                        currentDuels.remove(selectedPlayer.index);
                    } else {
                        playerWon = selectedPlayer.item.get(0);
                    }
                }

                ArrayList<ArrayList<Player>> playerDataTemp = new ArrayList<>(currentDuels);
                for (ListUtils.EnumeratedItem<ArrayList<Player>> selectedPlayer : ListUtils.enumerate(playerDataTemp)) {
                    if (selectedPlayer.item.get(0) == playerWon) {
                        playerWon.teleport((Location) selectedPlayer.item.get(1));
                        getSavedPlayerInventory(playerWon);
                    } else if (selectedPlayer.item.get(0) == playerLost) {
                        playerTPQueue.add(playerLost);
                    }
                    playerData.remove(selectedPlayer.index);
                }
                Bukkit.broadcastMessage(ChatColor.BLUE + playerWon.getName() + ChatColor.GREEN + " has won the duel!");

                arenaOccupied = false;

            }

        }

        public void onPlayerRespawn(PlayerRespawnEvent event) {
            if (!playerTPQueue.isEmpty()) {
                for (ArrayList<Object> selectedPlayer : playerData) {
                    if (selectedPlayer.get(0) == playerTPQueue.get(playerTPQueue.size() - 1)) {
                        playerTPQueue.get(playerTPQueue.size() - 1).teleport((Location) selectedPlayer.get(1));
                        getSavedPlayerInventory(playerTPQueue.get(playerTPQueue.size() - 1));
                        playerTPQueue.remove(playerTPQueue.size() - 1);
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().createWorld(WorldCreator.name("arena"));
        this.getCommand("duel").setExecutor(new CommandDuel());
        this.getCommand("duelaccept").setExecutor(new CommandDuelAccept());
        this.getCommand("worldtp").setExecutor(new teleportWorld());
        getServer().getPluginManager().registerEvents(new MyListener(), this);

        console.sendMessage(ChatColor.GREEN+"Simpleduel " + version + " loaded!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void runFunction(Player player) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Location loc1 = new Location(arenaWorld, 8, 1 ,35);
            Location loc2 = new Location(arenaWorld, -10, 1 ,35);
            // oldInvs.put(player, player.getInventory());

            for (ListUtils.EnumeratedItem<ArrayList<Player>> selectedPlayer : ListUtils.enumerate(duelRequests)) {
                if (selectedPlayer.item.get(1) == player) {
                    Location playerLoc1 = (Location) selectedPlayer.item.get(0).getLocation().getBlock().getLocation();
                    Location playerLoc2 = (Location) selectedPlayer.item.get(1).getLocation().getBlock().getLocation();

                    ArrayList<Object> playerData1 = new ArrayList<>();
                    playerData1.add(selectedPlayer.item.get(0));
                    playerData1.add(playerLoc1);
                    playerData.add(playerData1);

                    ArrayList<Object> playerData2 = new ArrayList<>();
                    playerData1.add(selectedPlayer.item.get(1));
                    playerData1.add(playerLoc2);
                    playerData.add(playerData1);

                    savePlayer(selectedPlayer.item.get(0));
                    savePlayer(selectedPlayer.item.get(1));

                    selectedPlayer.item.get(0).getInventory().clear();
                    selectedPlayer.item.get(1).getInventory().clear();

                    selectedPlayer.item.get(0).teleport(loc1);
                    player.teleport(loc2);
                    arenaOccupied = true;

                    ArrayList<Player> tempList = new ArrayList<>();
                    tempList.add(selectedPlayer.item.get(0));
                    tempList.add(selectedPlayer.item.get(1));
                    currentDuels.add(tempList);

                    duelRequests.remove(selectedPlayer.index);

                    break;
                }
            }

        }, 100L); // amount to wait in ticks , 20 ticks = 1 second
    }
}
