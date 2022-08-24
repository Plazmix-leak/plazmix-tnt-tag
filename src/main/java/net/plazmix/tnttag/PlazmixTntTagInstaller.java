package net.plazmix.tnttag;

import lombok.NonNull;
import net.plazmix.game.GamePlugin;
import net.plazmix.game.installer.GameInstallerTask;
import net.plazmix.game.utility.GameWorldCache;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public final class PlazmixTntTagInstaller extends GameInstallerTask {

    public PlazmixTntTagInstaller(@NonNull GamePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void handleExecute(@NonNull Actions actions, @NonNull Settings settings) {
        settings.setCenter(plugin.getService().getMapWorld().getSpawnLocation());

        settings.setRadius(50);

        actions.addEntity(EntityType.ARMOR_STAND, (entity) -> {

            GameWorldCache worldCache = GameWorldCache.fromEntity(entity);

            ((ArmorStand) entity).setCanPickupItems(false);
            ((ArmorStand) entity).setMarker(false);
            ((ArmorStand) entity).setVisible(false);

            worldCache.set("spawn", entity.getLocation());
        });
    }
    
}
