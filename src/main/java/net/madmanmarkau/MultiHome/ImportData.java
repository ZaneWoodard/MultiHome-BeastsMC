package net.madmanmarkau.MultiHome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.madmanmarkau.MultiHome.Data.HomeEntry;
import net.madmanmarkau.MultiHome.Data.InviteEntry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ImportData {

	public static ArrayList<HomeEntry> importHomesFromMultiHomeFile(MultiHome plugin) {
		File homesFile = new File(plugin.getDataFolder(), "homes.txt");
		ArrayList<HomeEntry> homes = new ArrayList<HomeEntry>();

		if (homesFile.exists()) {
			try {
				FileReader fstream = new FileReader(homesFile);
				BufferedReader reader = new BufferedReader(fstream);

				String line = reader.readLine().trim();

				while (line != null) {
					if (!line.startsWith("#") && line.length() > 0) {
						HomeEntry thisHome;

						String[] values = line.split(";");
						double X = 0, Y = 0, Z = 0;
						float pitch = 0, yaw = 0;
						String world = "";
						String name = "";
						String playerUUID = "";

						try {
							if (values.length == 7) {
								playerUUID = values[0];
								X = Double.parseDouble(values[1]);
								Y = Double.parseDouble(values[2]);
								Z = Double.parseDouble(values[3]);
								pitch = Float.parseFloat(values[4]);
								yaw = Float.parseFloat(values[5]);

								world = values[6];
								name = "";
							} else if (values.length == 8) {
								playerUUID = values[0];
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
						}

						if (values.length == 7 || values.length == 8) {
							boolean save = true;

							thisHome = new HomeEntry(UUID.fromString(playerUUID), name.toLowerCase(), world, X, Y, Z, pitch, yaw);

							for (HomeEntry home : homes) {
								if (home.getHomeName().compareToIgnoreCase(thisHome.getHomeName()) == 0 && home.getOwnerUUID().equals(thisHome.getOwnerUUID())) {
									save = false;
								}
							}

							if (save) {
								homes.add(thisHome);
							}
						}
					}

					line = reader.readLine();
				}

				reader.close();
			} catch (Exception e) {
				Messaging.logSevere("Could not read the homes file.", plugin);
				e.printStackTrace();
				return new ArrayList<HomeEntry>();
			}
		}

		return homes;
	}

	public static ArrayList<HomeEntry> importHomesFromMultiHomeMySQL(MultiHome plugin) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<HomeEntry> homes = new ArrayList<HomeEntry>();

		try {
			connection = DriverManager.getConnection(Settings.getDataStoreSettingString("sql", "url"),
					Settings.getDataStoreSettingString("sql", "user"),
					Settings.getDataStoreSettingString("sql", "pass"));
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			statement = connection.prepareStatement("SELECT * FROM `homes`;");
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				do {
					homes.add(new HomeEntry(UUID.fromString(resultSet.getString("owner")),
							resultSet.getString("home").toLowerCase(),
							resultSet.getString("world").toLowerCase(),
							resultSet.getDouble("x"),
							resultSet.getDouble("y"),
							resultSet.getDouble("z"),
							resultSet.getFloat("yaw"),
							resultSet.getFloat("pitch")));
				} while (resultSet.next());
			}

		} catch (SQLException e) {
			// Ignore errors
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {
				} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
				} // Eat errors
			}
		}

		return homes;
	}
}
