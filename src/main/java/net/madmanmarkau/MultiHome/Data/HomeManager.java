package net.madmanmarkau.MultiHome.Data;

import java.util.ArrayList;
import java.util.UUID;

import net.madmanmarkau.MultiHome.MultiHome;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Base class for home location database objects.
 * @author MadManMarkAu
 */
public abstract class HomeManager {
	protected final MultiHome plugin;
	
	/**
	 * @param plugin The plug-in.
	 */
	public HomeManager(MultiHome plugin) {
		this.plugin = plugin;
	}

	/**
	 * Deletes all homes from the database.
	 */
	abstract public void clearHomes();

	/**
	 * Returns a HomeEntry object for the specified home. If home is not found, returns null. 
	 * @param player Owner of the home.
	 * @param name Name of the owner's home location.
	 */
	public final HomeEntry getHome(Player player, String name) {
		return this.getHome(player.getUniqueId(), name);
	}

	/**
	 * Returns a HomeEntry object for the specified home. If home is not found, returns null. 
	 * @param ownerUUID UUID of the owner of the home.
	 * @param name Name of the owner's home location.
	 */
	abstract public HomeEntry getHome(UUID ownerUUID, String name);

	/**
	 * Adds the home location for the specified player. If home location already exists, updates the location.
	 * @param player Owner of the home.
	 * @param name Name of the owner's home.
	 * @param location Location the home.
	 */
	public final void addHome(Player player, String name, Location location) {
		this.addHome(player.getUniqueId(), name, location);
	}
	
	/**
	 * Adds the home location for the specified player. If home location already exists, updates the location.
	 * @param ownerUUID UUID of the owner of the home
	 * @param name Name of the owner's home.
	 * @param location Location the home.
	 */
	abstract public void addHome(UUID ownerUUID, String name, Location location);

	/**
	 * Remove an existing home.
	 * @param player Owner of the home.
	 * @param name Name of the owner's home location.
	 */
	public final void removeHome(Player player, String name) {
		this.removeHome(player.getUniqueId(), name);
	}
	
	/**
	 * Remove an existing home.
	 * @param ownerUUID UUID of the owner of the home
	 * @param name Name of the owner's home location.
	 */
	abstract public void removeHome(UUID ownerUUID, String name);
	
	/**
	 * Check the home database for a player.
	 * @param player Player to check database for.
	 * @return boolean True if player exists in database, otherwise false.
	 */
	public final boolean getUserExists(Player player) {
		return this.getUserExists(player.getUniqueId());
	}
	
	/**
	 * Check the home database for a player.
	 * @param playerUUID UUID of the player to check database for.
	 * @return boolean True if player exists in database, otherwise false.
	 */
	abstract public boolean getUserExists(UUID playerUUID);

	/**
	 * Get the number of homes a player has set.
	 * @param player Player to check home list for.
	 * @return int Number of home locations set.
	 */
	public final int getUserHomeCount(Player player) {
		return this.getUserHomeCount(player.getUniqueId());
	}

	/**
	 * Get the number of homes a player has set.
	 * @param playerUUID UUID of the player to check database for.
	 * @return int Number of home locations set.
	 */
	abstract public int getUserHomeCount(UUID playerUUID);
	
	/**
	 * Retrieve a list of player home locations from the database. If player not found, returns a blank list.
	 * @param player Player to retrieve home list for.
	 * @return ArrayList<HomeEntry> List of home locations.
	 */
	public final ArrayList<HomeEntry> listUserHomes(Player player) {
		return this.listUserHomes(player.getUniqueId());
	}
	
	/**
	 * Retrieve a list of player home locations from the database. If player not found, returns a blank list.
	 * @param playerUUID UUID of the player to check database for.
	 * @return ArrayList<HomeEntry> List of home locations.
	 */
	abstract public ArrayList<HomeEntry> listUserHomes(UUID playerUUID);
	
	/**
	 * Imports the list of home locations passed. Does not overwrite existing home locations.
	 * @param homes List of players and homes to import.
	 * @param overwrite True to overwrite existing entries.
	 */
	abstract public void importHomes(ArrayList<HomeEntry> homes, boolean overwrite);
}
