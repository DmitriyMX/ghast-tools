package ghast.command;

import org.bukkit.command.CommandSender;

public interface ErrorConsumer {

	void accept(CommandSender sender, String commandName, String[] args, Exception exception);
}
