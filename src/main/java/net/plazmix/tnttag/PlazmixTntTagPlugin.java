package net.plazmix.tnttag;

import net.plazmix.core.PlazmixCoreApi;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.installer.GameInstaller;
import net.plazmix.game.installer.GameInstallerTask;
import net.plazmix.game.team.GameTeam;
import net.plazmix.tnttag.listener.ChatListener;
import net.plazmix.tnttag.mysql.TntTagStatsMysqlDatabase;
import net.plazmix.tnttag.state.EndingState;
import net.plazmix.tnttag.state.IngameState;
import net.plazmix.tnttag.state.WaitingState;
import net.plazmix.tnttag.util.GameConstants;

/*  Leaked by https://t.me/leak_mine
    - Все слитые материалы вы используете на свой страх и риск.

    - Мы настоятельно рекомендуем проверять код плагинов на хаки!
    - Список софта для декопиляции плагинов:
    1. Luyten (последнюю версию можно скачать можно тут https://github.com/deathmarine/Luyten/releases);
    2. Bytecode-Viewer (последнюю версию можно скачать можно тут https://github.com/Konloch/bytecode-viewer/releases);
    3. Онлайн декомпиляторы https://jdec.app или http://www.javadecompilers.com/

    - Предложить свой слив вы можете по ссылке @leakmine_send_bot или https://t.me/leakmine_send_bot
*/

public class PlazmixTntTagPlugin extends GamePlugin {
    @Override
    public GameInstallerTask getInstallerTask() {
        return new PlazmixTntTagInstaller(this);
    }

    @Override
    protected void handleEnable() {
        saveDefaultConfig();

        // Setup game info.
        service.setMapName(getConfig().getString("map", getServer().getWorlds().get(0).getName()));
        service.setGameName("TntTag");
        service.setServerMode("TntTag");
        service.setMaxPlayers(16);

        // Register game states.
        service.registerState(new WaitingState(this));
        service.registerState(new IngameState(this));
        service.registerState(new EndingState(this));

        service.addGameDatabase(new TntTagStatsMysqlDatabase());

        service.registerTeam(GameTeam.DEFAULT_RED_TEAM);
        service.registerTeam(GameTeam.DEFAULT_GREEN_TEAM);

        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        GameInstaller.create().executeInstall(getInstallerTask());
    }

    @Override
    protected void handleDisable() {
        broadcastMessage(GameConstants.PREFIX + "Арена " + PlazmixCoreApi.getCurrentServerName() + " перезапускается!");
    }
}
