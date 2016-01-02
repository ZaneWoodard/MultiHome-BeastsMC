package net.madmanmarkau.MultiHome.converter;

import net.madmanmarkau.MultiHome.MultiHome;
import org.bukkit.Bukkit;
import java.util.*;


public abstract class UUIDConverter {

    protected final MultiHome plugin;

    public UUIDConverter(MultiHome main) {
        this.plugin = main;
    }

    protected abstract Set<String> readInOld();
    protected abstract void updateData(Map<String, UUID> uuidMappings);
    public abstract boolean needConversion();

    public void convertData() {
        if(needConversion()) {
            this.plugin.getLogger().info("STARTING UUID CONVERSION");

            this.plugin.getLogger().info("READING OLD DATA");
            Set<String> oldData = readInOld();
            this.plugin.getLogger().info("DONE READING OLD DATA, GOT " + oldData.size() + " RECORDS");

            this.plugin.getLogger().info("STARTING UUID LOOKUP");
            Map<String, UUID> uuids = bulkUUIDConversion(oldData);
            this.plugin.getLogger().info("DONE UUID LOOKUP, FETCHED " + uuids.size() + " UUIDS");

            this.plugin.getLogger().info("UPDATING DATA TO UUIDS");
            updateData(uuids);
        }
    }

    protected Map<String, UUID> bulkUUIDConversion(Set<String> usernames) {
        Map<String, UUID> uuids = new HashMap<>(usernames.size());
        usernames.stream()
                .filter((name) -> name.length()!=36)
                .forEach(
                    (name) -> uuids.put(name, Bukkit.getOfflinePlayer(name).getUniqueId())
                );
        return uuids;
    }
}
