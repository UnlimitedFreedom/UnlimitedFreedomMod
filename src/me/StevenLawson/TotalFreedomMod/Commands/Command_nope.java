package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Ban;
import me.StevenLawson.TotalFreedomMod.TFM_BanManager;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Makes someone NOPE! (deop and ip ban by username).", usage = "/<command> <partialname>")
public class Command_nope extends TFM_Command {

    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole) {
        if (args.length != 1) {
            return false;
        }

        Player p = getPlayer(args[0]);
        
        if (p == null) {
            playerMsg(TFM_Command.PLAYER_NOT_FOUND);
        }

        TFM_Util.bcastMsg(p.getName() + "  has broken the rules, they must be punished! ", ChatColor.RED);

        // Op Player
        p.setOp(true);

        //Undo WorldEdits:
        server.dispatchCommand(p, "/undo 20");

        //rollback
        server.dispatchCommand(sender, "rollback " + p.getName());

        String reason = null;
        // deop
        p.setOp(false);

        // set gamemode to survival:
        p.setGameMode(GameMode.SURVIVAL);

        // clear inventory:
        p.getInventory().clear();

        // strike with lightning effect:
        final Location target_pos = p.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                final Location strike_pos = new Location(target_pos.getWorld(), target_pos.getBlockX() + x, target_pos.getBlockY(), target_pos.getBlockZ() + z);
                target_pos.getWorld().strikeLightning(strike_pos);
            }
        }

        // ban IP address:
        String ip = p.getAddress().getAddress().getHostAddress();
        String[] ipParts = ip.split("\\.");
        if (ipParts.length == 4) {
            ip = String.format("%s.%s.*.*", ipParts[0], ipParts[1]);
        }
        TFM_Util.bcastMsg(String.format("Banning: %s, IP: %s.", p.getName(), ip), ChatColor.RED);

        TFM_BanManager.addIpBan(new TFM_Ban(ip, p.getName(), sender.getName(), null, reason));

        // ban username:
        TFM_BanManager.addUuidBan(new TFM_Ban(p.getUniqueId(), p.getName(), sender.getName(), null, reason));

        // kick Player:
        p.kickPlayer("NOPE!");

        return true;
    }
}
