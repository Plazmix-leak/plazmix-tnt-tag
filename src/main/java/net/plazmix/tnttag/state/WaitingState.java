package net.plazmix.tnttag.state;

import lombok.NonNull;
import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.state.type.StandardWaitingState;
import net.plazmix.game.utility.GameSchedulers;
import net.plazmix.game.utility.hotbar.GameHotbar;
import net.plazmix.game.utility.hotbar.GameHotbarBuilder;
import net.plazmix.tnttag.scoreboard.WaitingScoreboard;
import net.plazmix.tnttag.util.GameConstants;
import net.plazmix.utility.ItemUtil;
import net.plazmix.utility.location.LocationUtil;
import net.plazmix.utility.player.PlazmixUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class WaitingState extends StandardWaitingState {

    private final GameHotbar gameHotbar = GameHotbarBuilder.newBuilder()
            .setMoveItems(false)

            .addItem(9, ItemUtil.newBuilder(Material.SKULL_ITEM)
                            .setDurability(3)
                            .setTextureValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNmFkNjkyN2NkYWE5ZDgxNzkzMzdjYWRmOTY0NDYwNjcyMTE3YjMyNmU2MGY1YjFkMTlhNGI1NGYyYTMyMyJ9fX0=")
                            .setName("§aПокинуть арену §7(ПКМ)")
                            .build(),

                    player -> PlazmixCoreApi.redirect(player, "arcadelobby-1"))
            .build();


    public WaitingState(@NonNull GamePlugin plugin) {
        super(plugin, "Ожидание игроков");
    }

    @Override
    protected Location getTeleportLocation() {
        return LocationUtil.stringToLocation(plugin.getConfig().getString("wait-lobby-spawn"));
    }

    @Override
    protected void handleEvent(@NonNull PlayerJoinEvent event) {
        int online = Bukkit.getOnlinePlayers().size();
        int maxOnline = getPlugin().getService().getMaxPlayers();

        GameSchedulers.runLater(10, () -> {

            new WaitingScoreboard(getTimerStatus(), event.getPlayer());

            gameHotbar.setHotbarTo(event.getPlayer());
        });

        event.setJoinMessage(GameConstants.PREFIX + PlazmixUser.of(event.getPlayer()).getDisplayName() + " §fподключился к игре! §7(" + online + "/" + maxOnline + ")");

        // Если сервер полный, то запускаем таймер
        if (online >= 2 && !timerStatus.isLived()) {
            timerStatus.runTask(10);
        }
    }

    @Override
    protected void handleEvent(@NonNull PlayerQuitEvent event) {
        int online = Bukkit.getOnlinePlayers().size() - 1;
        int maxOnline = getPlugin().getService().getMaxPlayers();

        event.setQuitMessage(GameConstants.PREFIX + PlazmixUser.of(event.getPlayer()).getDisplayName() + " §fпокинул игру! §7(" + online + "/" + maxOnline + ")");

        // Если кто-то вышел, то надо вырубать таймер
        if (online < 2 && timerStatus.isLived()) {
            timerStatus.cancelTask();
        }
    }

    @Override
    protected void handleTimerUpdate(@NonNull TimerStatus timerStatus) {
    }
}
