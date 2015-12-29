package net.madmanmarkau.MultiHome.Data;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zane on 12/29/15.
 *
 * This class provides an in-memory cache for data written to the disk.
 * Greatly improves performance of many repeatedly used commands by cutting down on disk access.
 */
public class DataCache {
    private HashMap<UUID, HashMap<String, HomeEntry>> homeCache = new HashMap<>();
    private HashMap<UUID, HashMap<String, CoolDownEntry>> cooldownCache = new HashMap<>();

    public boolean homeContainsPlayer(UUID player) {
        return homeCache.containsKey(player);
    }
    public void addHome(UUID player, String homeName, HomeEntry homeEntry) {
        HashMap<String, HomeEntry> playerEntry = homeCache.get(player);
        if(playerEntry!=null) {
            playerEntry.put(homeName, homeEntry);
        } else {
            playerEntry = new HashMap<>();
            playerEntry.put(homeName, homeEntry);
            homeCache.put(player, playerEntry);
        }
    }
    public void addHome(HomeEntry homeEntry) {
        addHome(homeEntry.getOwnerUUID(), homeEntry.getHomeName(), homeEntry);
    }
    public HomeEntry getHome(UUID player, String homeName) {
        HashMap<String, HomeEntry> playerEntry = homeCache.get(player);

        HomeEntry home = null;
        if(playerEntry!=null) {
            home = playerEntry.get(homeName);
        }
        return home;
    }

    public void removeHome(UUID player, String homeName) {
        if(homeCache.containsKey(player)) {
            HashMap<String, HomeEntry> playerEntry = homeCache.get(player);
            playerEntry.remove(homeName);
        }
    }

    public void clearHomeCache() {
        homeCache = new HashMap<>();
    }

}
