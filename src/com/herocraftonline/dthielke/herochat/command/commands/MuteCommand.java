package com.herocraftonline.dthielke.herochat.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herochat.HeroChat;
import com.herocraftonline.dthielke.herochat.channels.Channel;
import com.herocraftonline.dthielke.herochat.channels.ChannelManager;
import com.herocraftonline.dthielke.herochat.command.BaseCommand;

public class MuteCommand extends BaseCommand {

    public MuteCommand(HeroChat plugin) {
        super(plugin);
        name = "Mute";
        description = "Prevents a player from speaking in a channel";
        usage = "/ch mute §9<channel> §8[player]";
        minArgs = 1;
        maxArgs = 2;
        identifiers.add("ch mute");
        identifiers.add("mute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ChannelManager cm = plugin.getChannelManager();
        Channel channel = cm.getChannel(args[0]);
        if (channel != null) {
            if (args.length == 1) {
                displayMuteList(sender, channel);
            } else {
                if (sender instanceof Player) {
                    Player muter = (Player) sender;
                    if (plugin.getPermissions().isAdmin(muter) || channel.getModerators().contains(muter.getName())) {
                        Player mutee = plugin.getServer().getPlayer(args[1]);
                        if (mutee != null) {
                            String name = mutee.getName();
                            if (!(plugin.getPermissions().isAdmin(mutee) || channel.getModerators().contains(name))) {
                                if (channel.getMutelist().contains(name)) {
                                    channel.getMutelist().remove(name);
                                    muter.sendMessage(plugin.getTag() + name + " has been unmuted in " + channel.getCName());
                                    mutee.sendMessage(plugin.getTag() + "You have been unmuted in " + channel.getCName());
                                } else {
                                    channel.getMutelist().add(name);
                                    muter.sendMessage(plugin.getTag() + name + " has been muted in " + channel.getCName());
                                    mutee.sendMessage(plugin.getTag() + "You have been muted in " + channel.getCName());
                                }
                            } else {
                                muter.sendMessage(plugin.getTag() + "You cannot mute " + name + " in " + channel.getCName());
                            }
                        } else {
                            muter.sendMessage(plugin.getTag() + "Player not found");
                        }
                    } else {
                        muter.sendMessage(plugin.getTag() + "You do not have sufficient permission");
                    }
                } else {
                    sender.sendMessage(plugin.getTag() + "You must be a player to use this command");
                }
            }
        } else {
            sender.sendMessage(plugin.getTag() + "Channel not found");
        }
    }
    
    private void displayMuteList(CommandSender sender, Channel channel) {
        String muteListMsg;
        List<String> mutes = channel.getMutelist();
        if (mutes.isEmpty()) {
            muteListMsg = plugin.getTag() + "No one is currently muted in " + channel.getCName();
        } else {
            muteListMsg = "Currently muted in " + channel.getCName() + "§f: ";
            for (String s : mutes) {
                muteListMsg += s + ",";
            }
            muteListMsg = muteListMsg.substring(0, muteListMsg.length() - 1);
        }
        sender.sendMessage(muteListMsg);
    }
}
