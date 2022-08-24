package net.plazmix.tnttag.scoreboard;

import lombok.NonNull;
import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.state.type.StandardWaitingState;
import net.plazmix.game.user.GameUser;
import net.plazmix.scoreboard.BaseScoreboardBuilder;
import net.plazmix.scoreboard.BaseScoreboardScope;
import net.plazmix.scoreboard.animation.ScoreboardDisplayFlickAnimation;
import net.plazmix.utility.DateUtil;
import net.plazmix.utility.NumberUtil;
import net.plazmix.utility.player.PlazmixUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class WaitingScoreboard {

    public WaitingScoreboard(@NonNull StandardWaitingState.TimerStatus timerStatus, @NonNull Player player) {
        PlazmixUser plazmixUser = PlazmixUser.of(player);

        GamePlugin gamePlugin = GamePlugin.getInstance();
        BaseScoreboardBuilder scoreboardBuilder = BaseScoreboardBuilder.newScoreboardBuilder();

        scoreboardBuilder.scoreboardScope(BaseScoreboardScope.PROTOTYPE);

        ScoreboardDisplayFlickAnimation displayFlickAnimation = new ScoreboardDisplayFlickAnimation();

        displayFlickAnimation.addColor(ChatColor.LIGHT_PURPLE);
        displayFlickAnimation.addColor(ChatColor.DARK_PURPLE);
        displayFlickAnimation.addColor(ChatColor.WHITE);
        displayFlickAnimation.addColor(ChatColor.DARK_PURPLE);

        displayFlickAnimation.addTextToAnimation(plazmixUser.localization().getMessageText("TNT_BOARD_TITLE"));
        scoreboardBuilder.scoreboardDisplay(displayFlickAnimation);

        scoreboardBuilder.scoreboardUpdater((baseScoreboard, player1) -> {
            baseScoreboard.setScoreboardDisplay(displayFlickAnimation);

            List<String> scoreboardLineList = getScoreboardLines(plazmixUser, gamePlugin, timerStatus);

            for (String scoreboardLine : scoreboardLineList) {
                baseScoreboard.setScoreboardLine(scoreboardLineList.size() - scoreboardLineList.indexOf(scoreboardLine), player, scoreboardLine);
            }
        }, 20);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }

    private List<String> getScoreboardLines(@NonNull PlazmixUser plazmixUser, GamePlugin gamePlugin, StandardWaitingState.TimerStatus timerStatus) {

        List<String> scoreboardLineList = new LinkedList<>();

        for (String scoreboardLine : plazmixUser.localization().getMessageList("TNT_BOARD_WAITING_LINES")) {
            scoreboardLine = scoreboardLine
                    .replace("%date%", "§7TntTag " + DateUtil.formatPattern(DateUtil.DEFAULT_DATE_PATTERN))

                    .replace("%map%", gamePlugin.getService().getMapName())

                    .replace("%online%", NumberUtil.spaced(Bukkit.getOnlinePlayers().size()))
                    .replace("%max%", NumberUtil.spaced(gamePlugin.getService().getMaxPlayers()))

                    .replace("%status%", (!timerStatus.isLived() ? "§cОжидание игроков..." : "§fИгра начнется через §e" + NumberUtil.formattingSpaced(timerStatus.getLeftSeconds(), "§fсекунду", "§fсекунды", "§fсекунд")))

                    .replace("%server%", PlazmixCoreApi.getCurrentServerName());

            scoreboardLineList.add(ChatColor.translateAlternateColorCodes('&', scoreboardLine));
        }

        return scoreboardLineList;
    }

}
