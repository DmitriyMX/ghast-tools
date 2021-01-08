package ghast.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import ru.dmitriymx.reflection.ReflectionObject;

@UtilityClass
@SuppressWarnings("unused")
public class CommandManager {

    public Builder create(String name) {
        return new Builder(name);
    }

    public void register(String name, CommandExecuter executer) {
        create(name).executer(executer).register();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final String name;
        private CommandExecuter executer;
        private ErrorConsumer errorConsumer;
        private Boolean onlyPlayer;
        private String deniedMessage;

        public Builder executer(CommandExecuter executer) {
            this.executer = executer;
            return this;
        }

        public Builder onError(ErrorConsumer errorConsumer) {
            this.errorConsumer = errorConsumer;
            return this;
        }

        public Builder useOnlyPlayer(String deniedMessage) {
            this.onlyPlayer = true;
            this.deniedMessage = deniedMessage;
            return this;
        }

        public Builder useOnlyPlayer() {
            return useOnlyPlayer(null);
        }

        public Builder useOnlyConsole(String deniedMessage) {
            this.onlyPlayer = false;
            this.deniedMessage = deniedMessage;
            return this;
        }

        public Builder useOnlyConsole() {
            return useOnlyConsole(null);
        }

        public void register() {
            //TODO для Paper такие "извращения" не требуются. Нужно продумать.
            new ReflectionObject(Bukkit.getServer())
                    .method("getCommandMap").invoke()
                    .method("register", String.class, Command.class).invoke(
                        name, new CommandWrapper(name, this.onlyPlayer, this.deniedMessage,
                            this.executer, this.errorConsumer)
                    );
        }
    }
}
