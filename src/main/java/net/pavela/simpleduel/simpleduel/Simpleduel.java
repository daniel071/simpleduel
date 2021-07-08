package net.pavela.simpleduel.simpleduel;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

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
    String version = "0.0.3";
    public ArrayList<ArrayList<Player>> duelRequests = new ArrayList<ArrayList<Player>>();
    World arenaWorld = Bukkit.getWorld("arena");

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

                player.sendMessage(ChatColor.GREEN + "Duel request accepted! Teleporting in 5 seconds.");

                runFunction(player);

            }

            return true;
        }
    }


    public class MyListener implements Listener {
    //        @EventHandler
    //        public void onDeath(PlayerDeathEvent event) {
    //            if (event.getEntity().getWorld().equals(arenaWorld)) {
    //                Player player = event.getPlayer();
    //                player.teleport(new Location(arenaWorld, 1701, 12, -1027));
    //            }
    //
    //        }
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
            for (ArrayList<Player> selectedPlayer : duelRequests) {
                if (selectedPlayer.get(1) == player) {
                    selectedPlayer.get(0).teleport(loc1);
                    player.teleport(loc2);
                    arenaOccupied = true;
                    break;
                }
            }

        }, 100L); // amount to wait in ticks , 20 ticks = 1 second
    }
}
