package net.plazmix.tnttag.state;

import lombok.NonNull;
import net.md_5.bungee.api.ChatMessageType;
import net.plazmix.event.EntityDamageByPlayerEvent;
import net.plazmix.event.PlayerDamageByEntityEvent;
import net.plazmix.event.PlayerDamageEvent;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.setting.GameSetting;
import net.plazmix.game.state.GameState;
import net.plazmix.game.team.GameTeam;
import net.plazmix.game.user.GameUser;
import net.plazmix.game.utility.GameWorldCache;
import net.plazmix.tnttag.scoreboard.IngameScoreboard;
import net.plazmix.tnttag.util.GameConstants;
import net.plazmix.utility.PlayerUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class IngameState extends GameState {

    public static int explosionCountdown = 30;

    public IngameState(@NonNull GamePlugin plugin) {
        super(plugin, "Идет игра", false);
    }

    private GameWorldCache getMapWorldCache() {
        return GameWorldCache.fromWorld(plugin.getService().getMapWorld());
    }

    @Override
    protected void onStart() {
        GameSetting.setAll(plugin.getService(), true);

        GameSetting.FOOD_CHANGE.set(plugin.getService(), false);
        GameSetting.WEATHER_CHANGE.set(plugin.getService(), false);
        GameSetting.BLOCK_BREAK.set(plugin.getService(), false);
        GameSetting.BLOCK_PLACE.set(plugin.getService(), false);
        GameSetting.PLAYER_DROP_ITEM.set(plugin.getService(), false);

        tntPlacer();
        explosionCountdown();

        Location spawnLocation = getMapWorldCache().get("spawn", Location.class);
        for (Player player : Bukkit.getOnlinePlayers()) {

            // Send Titles.
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 2);
            player.teleport(spawnLocation);

            player.setFlying(false);
            player.setAllowFlight(false);

            if (isTnt(GameUser.from(player))) {
                player.getInventory().addItem(new ItemStack(Material.TNT, 1));
            }

            player.setGameMode(GameMode.SURVIVAL);

            player.setLevel(0);
            player.setExp(0);

            player.getInventory().setHeldItemSlot(0);

            new IngameScoreboard(player);
        }
    }

    public void explosionCountdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (explosionCountdown > 0) {
                    explosionCountdown--;

                    setLevel(this);

                } else {
                    tntCheck(this);
                }
            }

        }.runTaskTimer(plugin, 0, 20);

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(false);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByPlayerEvent event) {
        event.setCancelled(false);
    }

    @EventHandler
    public void onPlayerDamage(PlayerDamageByEntityEvent event) {
        event.setCancelled(false);
    }

    @EventHandler
    public void playerTagPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(false);
            event.setDamage(0);

            Player tagger = (Player) event.getDamager();
            Player tagged = (Player) event.getEntity();

            GameUser gameTagger = GameUser.from(tagger);
            GameUser gameTagged = GameUser.from(tagged);

            if (isTnt(gameTagger) && isTnt(gameTagged)) {
                tagger.sendMessage(GameConstants.PREFIX + "Игрок уже тнт!");
                return;
            }

            if (isTnt(gameTagger)) {
                tagger.getInventory().setHelmet(null);
                tagger.getInventory().clear();
                tagger.sendMessage(GameConstants.PREFIX + "Ты передал §cTNT §fигроку " + PlayerUtil.getDisplayName(tagged));
                setTnt(gameTagger);

                plugin.broadcastMessage(ChatMessageType.ACTION_BAR, "§fИгрок " + PlayerUtil.getDisplayName(tagged) + " §fстал §cTNT§f!");

                tagged.sendMessage(GameConstants.PREFIX + "Игрок " + PlayerUtil.getDisplayName(tagger) + " §fпередал Вам §cTNT§f!");
                setTnt(gameTagged);
                tagged.playSound(tagger.getLocation(), Sound.EXPLODE, 1, 1);
            }
        }
    }

    @EventHandler
    public void editInventory(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) {
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void onShutdown() {

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GameUser gameUser = GameUser.from(event.getPlayer());

        if (gameUser.isAlive()) {
            gameUser.setGhost(true);
        }
    }

    public void tntPlacer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (int i = 0; i < 1; i++) {
            Player player = players.get(random.nextInt(players.size()));

            setTnt(GameUser.from(player));
            plugin.broadcastMessage(GameConstants.PREFIX + "§fИгрок " + PlayerUtil.getDisplayName(player) + " §fстал §cTNT§f!");

            players.remove(player);
        }

        players.forEach(player -> {
            GameTeam.DEFAULT_GREEN_TEAM.addPlayer(player);
            GameUser.from(player).getBukkitHandle().setPlayerListName(ChatColor.GREEN + GameUser.from(player).getPlazmixHandle().getName());
        });
    }

    public boolean isTnt(@NonNull GameUser gameUser) {
        return GameTeam.DEFAULT_RED_TEAM.hasPlayer(gameUser);
    }

    public void setTnt(@NonNull GameUser gameUser) {
        if (isTnt(gameUser)) {
            GameTeam.DEFAULT_RED_TEAM.removePlayer(gameUser);
            GameTeam.DEFAULT_GREEN_TEAM.addPlayer(gameUser);
            gameUser.getBukkitHandle().setPlayerListName(ChatColor.GREEN + gameUser.getPlazmixHandle().getName());
            return;
        }

        GameTeam.DEFAULT_RED_TEAM.addPlayer(gameUser);
        gameUser.getBukkitHandle().setPlayerListName(ChatColor.RED + gameUser.getPlazmixHandle().getName());

        gameUser.getBukkitHandle().getInventory().setHelmet(new ItemStack(Material.TNT));
        gameUser.getBukkitHandle().getInventory().addItem(new ItemStack(Material.TNT, 1));
    }

    public void setLevel(BukkitRunnable runnable) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setLevel(explosionCountdown);
        });

        if (plugin.getService().getAlivePlayers().size() <= 1) {
            runnable.cancel();
        }
    }

    public void tntCheck(BukkitRunnable runnable) {
        Bukkit.getOnlinePlayers().forEach(player -> {

            GameUser gameUser = GameUser.from(player);
            if (isTnt(GameUser.from(player))) {

                player.getInventory().setHelmet(null);
                player.getInventory().clear();

                Location playerLocation = gameUser.getBukkitHandle().getLocation();
                playerLocation.getWorld().createExplosion(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ(), 1, false, false);

                gameUser.getBukkitHandle().sendMessage(GameConstants.PREFIX + "§cBOOM§f! §cТнт §fбыли взорваны.");
                gameUser.setGhost(true);
                player.getInventory().clear();
            }
        });

        if (plugin.getService().getAlivePlayers().size() == 1) {
            runnable.cancel();

            plugin.getCache().set(GameConstants.INGAME_WINNER_PLAYER_NAME,
                    plugin.getService().getAlivePlayers().stream().findFirst().orElse(null).getName());

            nextStage();

        } else {
            explosionCountdown = 30;
            tntPlacer();
        }
    }

}
