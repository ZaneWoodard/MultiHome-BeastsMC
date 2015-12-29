package net.madmanmarkau.MultiHome.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.madmanmarkau.MultiHome.Messaging;
import net.madmanmarkau.MultiHome.MultiHome;
import net.madmanmarkau.MultiHome.Util;

import org.bukkit.Location;

/**
 * Manages a database of player home locations.
 * @author MadManMarkAu, Zane Woodard
 */

public class HomeManagerFile extends HomeManager {
    private final File homesFile;
	private HashMap<UUID, ArrayList<HomeEntry>> homeEntries = new HashMap<>();
	
	public HomeManagerFile(MultiHome plugin) {
		super(plugin);
		this.homesFile = new File(plugin.getDataFolder(), "homes.txt");
		
		loadHomes();
	}

	@Override
	public void clearHomes() {
		this.homeEntries.clear();

		saveHomes();
	}

	@Override
	public HomeEntry getHome(UUID playerUUID, String name) {
		if (this.homeEntries.containsKey(playerUUID)) {
			ArrayList<HomeEntry> homes = this.homeEntries.get(playerUUID);
	
			for (HomeEntry thisLocation : homes) {
				if (thisLocation.getHomeName().compareToIgnoreCase(name) == 0) {
					return thisLocation;
				}
			}
		}

		return null;
	}

	@Override
	public void addHome(UUID playerUUID, String name, Location location) {
		ArrayList<HomeEntry> homes;
		
		// Get the ArrayList of homes for this player
		if (this.homeEntries.containsKey(playerUUID)) {
			homes = this.homeEntries.get(playerUUID);
		} else {
			homes = new ArrayList<>();
		}

		boolean homeSet = false;
		
		for (int index = 0; index < homes.size(); index++) {
			HomeEntry thisHome = homes.get(index);
			if (thisHome.getHomeName().compareToIgnoreCase(name) == 0) {
				// An existing home was found. Overwrite it.
				thisHome.setOwnerUUID(playerUUID);
				thisHome.setHomeName(name);
				thisHome.setHomeLocation(location);
				homes.set(index, thisHome);
				homeSet = true;
			}
		}
		
		if (!homeSet) {
			// No existing location found. Create new entry.
			HomeEntry home = new HomeEntry(playerUUID, name.toLowerCase(), location);
			homes.add(home);
		}
		
		// Replace the ArrayList in the homes HashMap
		this.homeEntries.remove(playerUUID);
		this.homeEntries.put(playerUUID, homes);

		// Save
		this.saveHomes();
	}

	@Override
	public void removeHome(UUID playerUUID, String name) {
		if (this.homeEntries.containsKey(playerUUID)) {
			ArrayList<HomeEntry> playerHomeList = this.homeEntries.get(playerUUID);
			ArrayList<HomeEntry> removeList = new ArrayList<>();

			// Find all homes matching "name"
			for (HomeEntry thisHome : playerHomeList) {
				if (thisHome.getHomeName().compareToIgnoreCase(name) == 0) {
					// Found match. Mark it for deletion.
					removeList.add(thisHome);
				}
			}

			// Remove all matching homes.
			playerHomeList.removeAll(removeList);

			// Replace the ArrayList in the homes HashMap
			this.homeEntries.remove(playerUUID);
			if (!playerHomeList.isEmpty()) {
				this.homeEntries.put(playerUUID, playerHomeList);
			}

			// Save
			this.saveHomes();
		}
	}

	@Override
	public boolean getUserExists(UUID playerUUID) {
		return this.homeEntries.containsKey(playerUUID);
	}

	@Override
	public int getUserHomeCount(UUID playerUUID) {
		if (this.homeEntries.containsKey(playerUUID)) {
			return this.homeEntries.get(playerUUID).size();
		} else {
			return 0;
		}
	}

	@Override
	public ArrayList<HomeEntry> listUserHomes(UUID playerUUID) {
		if (this.homeEntries.containsKey(playerUUID)) {
			return this.homeEntries.get(playerUUID);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public void importHomes(ArrayList<HomeEntry> homes, boolean overwrite) {
		ArrayList<HomeEntry> playerHomes;

		for (HomeEntry thisEntry : homes) {
			// Get the ArrayList of homes for this player
			if (this.homeEntries.containsKey(thisEntry.getOwnerUUID())) {
				playerHomes = this.homeEntries.get(thisEntry.getOwnerUUID());
			} else {
				playerHomes = new ArrayList<>();
			}

			boolean homeFound = false;
			
			for (int index = 0; index < playerHomes.size(); index++) {
				HomeEntry thisHome = playerHomes.get(index);
				if (thisHome.getHomeName().compareToIgnoreCase(thisEntry.getHomeName()) == 0) {
					// An existing home was found.
					if (overwrite) {
						thisHome.setOwnerUUID(thisEntry.getOwnerUUID());
						thisHome.setHomeName(thisEntry.getHomeName());
						thisHome.setHomeLocation(thisEntry.getHomeLocation(plugin.getServer()));
						playerHomes.set(index, thisHome);
					}
					
					homeFound = true;
				}
			}
			
			if (!homeFound) {
				// No existing location found. Create new entry.
				HomeEntry newHome = new HomeEntry(thisEntry.getOwnerUUID(), thisEntry.getHomeName(), thisEntry.getHomeLocation(plugin.getServer()));
				playerHomes.add(newHome);
			}

			// Replace the ArrayList in the homes HashMap
			this.homeEntries.remove(thisEntry.getOwnerUUID());
			this.homeEntries.put(thisEntry.getOwnerUUID(), playerHomes);
		}

		// Save
		this.saveHomes();
	}

	
	
	/**
	 * Save homes list to file. Clears the saveRequired flag.
	 */
	private void saveHomes() {
		try {
			FileWriter fstream = new FileWriter(this.homesFile);
			BufferedWriter writer = new BufferedWriter(fstream);

			writer.write("# Stores user home locations." + Util.newLine());
			writer.write("# <username>;<x>;<y>;<z>;<pitch>;<yaw>;<world>[;<name>]" + Util.newLine());
			writer.write(Util.newLine());

			for (Entry<UUID, ArrayList<HomeEntry>> entry : this.homeEntries.entrySet()) {
				for (HomeEntry thisHome : entry.getValue()) {
					writer.write(thisHome.getOwnerUUID() + ";" + thisHome.getX() + ";" + thisHome.getY() + ";" + thisHome.getZ() + ";"
							+ thisHome.getPitch() + ";" + thisHome.getYaw() + ";"
							+ thisHome.getWorld() + ";" + thisHome.getHomeName() + Util.newLine());
				}
			}
			writer.close();
		} catch (Exception e) {
			Messaging.logSevere("Could not write the homes file.", this.plugin);
		}
	}

	/**
	 * Load the homes list from file.
	 */
	private void loadHomes() {
		if (this.homesFile.exists()) {
			try {
				FileReader fstream = new FileReader(this.homesFile);
				BufferedReader reader = new BufferedReader(fstream);
	
				String line = reader.readLine().trim();
	
				this.homeEntries.clear();
	
				while (line != null) {
					if (!line.startsWith("#") && line.length() > 0) {
						HomeEntry thisHome;
						
						thisHome = parseHomeLine(line);
						
						if (thisHome != null) {
							ArrayList<HomeEntry> homeList;
	
							// Find HashMap entry for player
							if (!this.homeEntries.containsKey(thisHome.getOwnerUUID())) {
								homeList = new ArrayList<>();
							} else {
								// Player not exist. Create dummy entry.
								homeList = this.homeEntries.get(thisHome.getOwnerUUID());
							}
							
							// Don't save if this is a duplicate entry.
							boolean save = true;
							for (HomeEntry home : homeList) {
								if (home.getHomeName().compareToIgnoreCase(thisHome.getHomeName()) == 0) {
									save = false;
								}
							}
							
							if (save) {
								homeList.add(thisHome);
							}
	
							this.homeEntries.put(thisHome.getOwnerUUID(), homeList);
						}
					}
	
					line = reader.readLine();
				}
	
				reader.close();
			} catch (Exception e) {
				Messaging.logSevere("Could not read the homes file.", this.plugin);
				return;
			}
		}
		
		saveHomes();
	}


	private HomeEntry parseHomeLine(String line) {
		String[] values = line.split(";");
		double X = 0, Y = 0, Z = 0;
		float pitch = 0, yaw = 0;
		String world = "";
		String name = "";
		String player = "";

		try {
			if (values.length == 7) {
				player = values[0];
				X = Double.parseDouble(values[1]);
				Y = Double.parseDouble(values[2]);
				Z = Double.parseDouble(values[3]);
				pitch = Float.parseFloat(values[4]);
				yaw = Float.parseFloat(values[5]);

				world = values[6];
				name = "";
			} else if (values.length == 8) {
				player = values[0];
				X = Double.parseDouble(values[1]);
				Y = Double.parseDouble(values[2]);
				Z = Double.parseDouble(values[3]);
				pitch = Float.parseFloat(values[4]);
				yaw = Float.parseFloat(values[5]);

				world = values[6];
				name = values[7];
			}
		} catch (Exception e) {
			// This entry failed. Ignore and continue.
			if (line!=null) {
				Messaging.logWarning("Failed to load home location! Line: " + line, this.plugin);
			}
		}

		if (values.length == 7 || values.length == 8) {
			return new HomeEntry(UUID.fromString(player), name, world, X, Y, Z, pitch, yaw);
		}
		
		return null;
	}
}
