package net.plazmix.tnttag.state;

import lombok.NonNull;
import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.mysql.GameMysqlDatabase;
import net.plazmix.game.setting.GameSetting;
import net.plazmix.game.state.type.StandardEndingState;
import net.plazmix.game.user.GameUser;
import net.plazmix.game.utility.GameSchedulers;
import net.plazmix.game.utility.hotbar.GameHotbar;
import net.plazmix.game.utility.hotbar.GameHotbarBuilder;
import net.plazmix.game.utility.worldreset.GameWorldReset;
import net.plazmix.tnttag.mysql.TntTagStatsMysqlDatabase;
import net.plazmix.tnttag.scoreboard.EndingScoreboard;
import net.plazmix.tnttag.util.GameConstants;
import net.plazmix.utility.ItemUtil;
import net.plazmix.utility.location.LocationUtil;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public final class EndingState extends StandardEndingState {

    private final GameHotbar gameHotbar = GameHotbarBuilder.newBuilder()
            .setMoveItems(true)
            .setAllowInteraction(true)

            .addItem(5, ItemUtil.newBuilder(Material.PAPER)
                            .setName("§aСыграть еще раз")
                            .build(),

                    player -> GamePlugin.getInstance().getService().playAgain(player))

            .addItem(9, ItemUtil.newBuilder(Material.SKULL_ITEM)
                            .setDurability(3)
                            .setTextureValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNmFkNjkyN2NkYWE5ZDgxNzkzMzdjYWRmOTY0NDYwNjcyMTE3YjMyNmU2MGY1YjFkMTlhNGI1NGYyYTMyMyJ9fX0=")
                            .setName("§aПокинуть арену §7(ПКМ)")
                            .build(),

                    player -> PlazmixCoreApi.redirect(player, "arcadelobby-1"))
            .build();


    public EndingState(GamePlugin plugin) {
        super(plugin, "Перезагрузка");

        GameSetting.INTERACT_BLOCK.set(plugin.getService(), false);
    }

    @Override
    protected String getWinnerPlayerName() {
        return plugin.getCache().get(GameConstants.INGAME_WINNER_PLAYER_NAME, String.class);
    }

    @Override
    protected void handleStart() {
        GameWorldReset.resetAllWorlds();
        GameUser winnerUser = GameUser.from(getWinnerPlayerName());

        if (winnerUser == null) {
            plugin.broadcastMessage(ChatColor.RED + "Произошли техничекие неполадки, из-за чего игра была принудительно остановлена!");

            forceShutdown();
            return;
        }

        // Add player win.
        winnerUser.getCache().increment(GameConstants.DATABASE_PLAYER_WINS);

        // Run fireworks spam.
        GameSchedulers.runTimer(0, 20, () -> {

            if (winnerUser.getBukkitHandle() == null) {
                return;
            }

            Firework firework = winnerUser.getBukkitHandle().getWorld().spawn(winnerUser.getBukkitHandle().getLocation(), Firework.class);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();

            fireworkMeta.setPower(1);
            fireworkMeta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.STAR)
                    .withColor(Color.RED)
                    .withColor(Color.GREEN)
                    .withColor(Color.WHITE)
                    .build());

            firework.setFireworkMeta(fireworkMeta);
        });

        GameMysqlDatabase statsMysqlDatabase = plugin.getService().getGameDatabase(TntTagStatsMysqlDatabase.class);

        for (Player player : Bukkit.getOnlinePlayers()) {
            GameUser gameUser = GameUser.from(player);
            gameUser.setGhost(false);

            // Announcements.
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST2, 1, 0);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);

            player.sendMessage(GameConstants.PREFIX + "§aИгра окончена!");

            if (winnerUser.getName().equalsIgnoreCase(player.getName())) {
                player.sendTitle("§6§lПОБЕДА", "§fВы одержали победу в этой битве!");

                player.sendMessage("§e+250 монет (победа)");
                gameUser.getPlazmixHandle().addCoins(250);

            } else {

                player.sendTitle("§c§lПОРАЖЕНИЕ", "§fЭто не повод уходить в тильт, просто иди задонать)))");
            }

            // Set hotbar items.
            gameHotbar.setHotbarTo(player);

            // Update player data in database.
            statsMysqlDatabase.insert(false, GameUser.from(player));
        }
    }

    @Override
    protected void handleScoreboardSet(@NonNull Player player) {
        new EndingScoreboard(GameUser.from(getWinnerPlayerName()), player);
    }

    @Override
    protected Location getTeleportLocation() {
        return LocationUtil.stringToLocation(plugin.getConfig().getString("wait-lobby-spawn"));
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

}
