package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.MultiverseCore.commandTools.ColourAlternator;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCommand extends InventoriesCommand {

    public RootCommand(MultiverseInventories plugin) {
        super(plugin);
    }

    @CommandAlias("mvinv")
    @Description("Multiverse-Inventories")
    public void onRootCommand(@NotNull CommandSender sender) {
        this.plugin.getCore().getMVCommandManager().showPluginInfo(
                sender,
                this.plugin.getDescription(),
                new ColourAlternator(ChatColor.DARK_AQUA, ChatColor.AQUA),
                "mvinv"
        );
    }
}
