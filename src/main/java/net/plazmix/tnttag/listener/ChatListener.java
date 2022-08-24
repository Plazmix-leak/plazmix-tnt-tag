package net.plazmix.tnttag.listener;

import com.google.common.base.Joiner;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.utility.ChatUtil;
import net.plazmix.utility.NumberUtil;
import net.plazmix.utility.player.LocalizationPlayer;
import net.plazmix.utility.player.PlazmixUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

public final class ChatListener implements Listener {

    public static final Pattern DETECT_PLAYER_MESSAGE_PATTERN
            = Pattern.compile("@[A-zА-я0-9_]{4,16}");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlazmixUser plazmixUser = PlazmixUser.of(event.getPlayer());
        String message = event.getMessage();

        event.setCancelled(true);

        PlazmixCoreApi.GroupApi groupApi = PlazmixCoreApi.GROUP_API;
        if (DETECT_PLAYER_MESSAGE_PATTERN.matcher(message).find()) {
            String announcedPlayerName = message;

            for (String replacement : DETECT_PLAYER_MESSAGE_PATTERN.split(announcedPlayerName)) {
                announcedPlayerName = announcedPlayerName.replace(replacement, "");
            }

            announcedPlayerName = announcedPlayerName.substring(1);


            // Говорим игроку об упоминании
            Player announcedPlayer = Bukkit.getPlayer(announcedPlayerName);
            if (announcedPlayer != null) {

                announcedPlayer.playSound(announcedPlayer.getLocation(), Sound.LEVEL_UP, 1, 1);
                announcedPlayer.sendTitle("", "§7Вы были упомянуты " + plazmixUser.getDisplayName() + " §7в чате");

                // Заменяем цвет ника в сообщении
                event.setMessage( message.replace(("@").concat(announcedPlayerName), ChatColor.GREEN + announcedPlayerName + groupApi.getGroupSuffix(plazmixUser.getName())) );

            } else {

                // Если не в сети, то на красный заменяем
                event.setMessage( message.replace(("@").concat(announcedPlayerName), ChatColor.RED + announcedPlayerName + " [Не в сети]" + groupApi.getGroupSuffix(plazmixUser.getName())) );
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(createHoverChatMessage(PlazmixUser.of(player).localization(), plazmixUser, event.getMessage()));
        }
    }

    private BaseComponent[] createHoverChatMessage(@NonNull LocalizationPlayer localizationPlayer, @NonNull PlazmixUser plazmixUser, @NonNull String message) {
        String hoverText = Joiner.on("\n").join(localizationPlayer.getMessageList("CHAT_HOVER"))
                .replace("%player%", plazmixUser.getDisplayName())
                .replace("%level%", NumberUtil.spaced(plazmixUser.getLevel()))
                .replace("%exp%", NumberUtil.spaced(plazmixUser.getExperience()))
                .replace("%max_exp%", NumberUtil.spaced(plazmixUser.getMaxExperience()));

        PlazmixCoreApi.GroupApi groupApi = PlazmixCoreApi.GROUP_API;
        return ChatUtil.newBuilder(plazmixUser.getDisplayName() + " §8➥ " + groupApi.getGroupSuffix(plazmixUser.getName()) + message)

                .setClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + plazmixUser.getName())
                .setHoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.translateAlternateColorCodes('&', hoverText))

                .build();
    }

}
