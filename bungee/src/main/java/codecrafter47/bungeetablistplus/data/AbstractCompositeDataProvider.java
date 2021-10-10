package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.config.player.Player;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCompositeDataProvider<T> {

    @Getter
    private final DataKey<T> compositeDataKey;
    private final Map<CompositeKey, PlayerDataListener> playerDataListenerMap = new HashMap<>();

    protected AbstractCompositeDataProvider(DataKey<T> compositeDataKey) {
        this.compositeDataKey = compositeDataKey;
    }

    public final void onPlayerAdded(Player player, DataKey<T> key) {
        if (player instanceof BungeePlayer) {
            PlayerDataListener playerDataListener = new PlayerDataListener((BungeePlayer) player, key);
            playerDataListenerMap.put(new CompositeKey((BungeePlayer) player, key), playerDataListener);
            registerListener(player, key, playerDataListener);
            playerDataListener.run();
        }
    }

    protected abstract void registerListener(Player player, DataKey<T> key, Runnable playerDataListener);

    public final void onPlayerRemoved(Player player, DataKey<T> key) {
        if (player instanceof BungeePlayer) {
            PlayerDataListener playerDataListener = playerDataListenerMap.remove(new CompositeKey((BungeePlayer) player, key));
            if (playerDataListener != null) {
                unregisterListener(player, key, playerDataListener);
            }
        }
    }

    protected abstract void unregisterListener(Player player, DataKey<T> key, Runnable listener);

    protected abstract T computeCompositeData(BungeePlayer player, DataKey<T> key);

    @Value
    private static class CompositeKey {
        BungeePlayer player;
        DataKey<?> dataKey;
    }

    protected class PlayerDataListener implements Runnable {
        private final BungeePlayer player;
        private final DataKey<T> dataKey;

        private PlayerDataListener(BungeePlayer player, DataKey<T> dataKey) {
            this.player = player;
            this.dataKey = dataKey;
        }

        @Override
        public void run() {
            player.getLocalDataCache().updateValue(dataKey, computeCompositeData(player, dataKey));
        }
    }
}
