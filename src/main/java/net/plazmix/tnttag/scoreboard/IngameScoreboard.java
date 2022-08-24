package net.plazmix.tnttag.scoreboard;

import lombok.NonNull;
import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.state.type.StandardWaitingState;
import net.plazmix.game.team.GameTeam;
import net.plazmix.game.user.GameUser;
import net.plazmix.scoreboard.BaseScoreboardBuilder;
import net.plazmix.scoreboard.BaseScoreboardScope;
import net.plazmix.scoreboard.animation.ScoreboardDisplayFlickAnimation;
import net.plazmix.tnttag.state.IngameState;
import net.plazmix.utility.DateUtil;
import net.plazmix.utility.NumberUtil;
import net.plazmix.utility.player.PlazmixUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class IngameScoreboard {

    public IngameScoreboard(@NonNull Player player) {
        PlazmixUser plazmixUser = PlazmixUser.of(player);

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

            List<String> scoreboardLineList = getScoreboardLines(plazmixUser);

            for (String scoreboardLine : scoreboardLineList) {
                baseScoreboard.setScoreboardLine(scoreboardLineList.size() - scoreboardLineList.indexOf(scoreboardLine), player, scoreboardLine);
            }
        }, 20);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }

    private List<String> getScoreboardLines(@NonNull PlazmixUser plazmixUser) {

        List<String> scoreboardLineList = new LinkedList<>();

        for (String scoreboardLine : plazmixUser.localization().getMessageList("TNT_BOARD_GAME_LINES")) {
            scoreboardLine = scoreboardLine
                    .replace("%date%", "§7TNTTAG " + DateUtil.formatPattern(DateUtil.DEFAULT_DATE_PATTERN))

                    .replace("%status%", (GameTeam.DEFAULT_RED_TEAM.hasPlayer(plazmixUser.getName()) ? "§cТнт" : "§7Игрок"))
                    .replace("%countdown%", NumberUtil.spaced(IngameState.explosionCountdown))
                    .replace("%map%", GamePlugin.getInstance().getService().getMapName())

                    .replace("%server%", PlazmixCoreApi.getCurrentServerName());

            scoreboardLineList.add(scoreboardLine);
        }

        return scoreboardLineList;
    }
}
