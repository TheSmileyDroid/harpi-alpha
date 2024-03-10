package harpi.alpha.commands;

import javax.annotation.Nonnull;

public interface CommandGroup {

  void registerCommands(@Nonnull CommandHandler handler);

}