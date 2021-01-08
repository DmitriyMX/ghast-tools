package ghast.command;

import ghast.XLog;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

class CommandWrapper extends BukkitCommand {

    private static final String DEFAULT_DENIED_MESSAGE_PLAYERS = ChatColor.RED + "This command use only players";
    private static final String DEFAULT_DENIED_MESSAGE_CONSOLE = ChatColor.RED + "This command use only in console";

    private static final ErrorConsumer DEFAULT_ERROR_CONSUMER =
            (sender, commandName, args, exception) -> {
                sender.sendMessage(String.format("%sError execute command '%s'!", ChatColor.RED, commandName));
                XLog.error("Error execute command ''{0}'' with args ''{1}''",
                        commandName, String.join(" ", args), exception);
            };

    private final CommandExecuter executer;
    private final ErrorConsumer errorConsumer;
    private final Boolean onlyPlayer;
    private String deniedMessage;

    protected CommandWrapper(String name, Boolean onlyPlayer, String deniedMessage,
            CommandExecuter executer, ErrorConsumer errorConsumer) {
        super(name);
        this.onlyPlayer = onlyPlayer;
        this.executer = executer;

        if (onlyPlayer != null) {
            if (deniedMessage == null) {
                this.deniedMessage = Boolean.TRUE.equals(onlyPlayer) ? DEFAULT_DENIED_MESSAGE_PLAYERS
                        : DEFAULT_DENIED_MESSAGE_CONSOLE;
            } else {
                this.deniedMessage = deniedMessage;
            }
        }

        if(errorConsumer == null) {
            this.errorConsumer = DEFAULT_ERROR_CONSUMER;
        } else {
            this.errorConsumer = errorConsumer;
        }
    }

    @Override
    @SuppressWarnings("java:S1066")
    public boolean execute(CommandSender commandSender, String commandName, String[] args) {
        if (Boolean.TRUE.equals(onlyPlayer)) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(deniedMessage);
                return true;
            }
        } else if (Boolean.FALSE.equals(onlyPlayer)) { // use console only
            if (commandSender instanceof Player) {
                commandSender.sendMessage(deniedMessage);
                return true;
            }
        }

        try {
            executer.execute(commandSender, args);
            return true;
        } catch (Exception e) {
            errorConsumer.accept(commandSender, commandName, args, e);
            return false;
        }
    }
}
