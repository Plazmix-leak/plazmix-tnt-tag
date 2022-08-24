package net.plazmix.tnttag.mysql;

import lombok.NonNull;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.mysql.GameMysqlDatabase;
import net.plazmix.game.mysql.RemoteDatabaseRowType;
import net.plazmix.game.user.GameUser;
import net.plazmix.tnttag.util.GameConstants;

public class TntTagStatsMysqlDatabase extends GameMysqlDatabase {

    public TntTagStatsMysqlDatabase() {
        super("TntTag", true);
    }

    @Override
    public void initialize() {
        addColumn(GameConstants.DATABASE_PLAYER_WINS, RemoteDatabaseRowType.INT, user -> user.getCache().getInt(GameConstants.DATABASE_PLAYER_WINS));
    }

    @Override
    public void onJoinLoad(@NonNull GamePlugin gamePlugin, @NonNull GameUser gameUser) {
        loadPrimary(true, gameUser, gameUser.getCache()::set);
    }
}
