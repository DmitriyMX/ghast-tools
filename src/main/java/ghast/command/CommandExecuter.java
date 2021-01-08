package ghast.command;

import org.bukkit.command.CommandSender;

public interface CommandExecuter {

    void execute(CommandSender sender, String[] args);
}
