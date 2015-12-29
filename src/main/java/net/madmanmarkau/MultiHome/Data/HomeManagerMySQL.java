package net.madmanmarkau.MultiHome.Data;

import net.madmanmarkau.MultiHome.Messaging;
import net.madmanmarkau.MultiHome.MultiHome;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;


public class HomeManagerMySQL extends HomeManager {

	public HomeManagerMySQL(MultiHome plugin) {
		super(plugin);

		// Test connection
        Connection connection = null;
        try {
            connection = plugin.getHikari().getConnection();
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `homes` (\n" +
                    "  `owner` varchar(36) NOT NULL,\n" +
                    "  `home` varchar(50) NOT NULL,\n"  +
                    "  `world` varchar(50) NOT NULL,\n" +
                    "  `x` double NOT NULL,\n"          +
                    "  `y` double NOT NULL,\n"          +
                    "  `z` double NOT NULL,\n"          +
                    "  `pitch` float NOT NULL,\n"       +
                    "  `yaw` float NOT NULL,\n"         +
                    "  PRIMARY KEY (`owner`,`home`)\n"  +
                    ");"
            );
        } catch(SQLException ex) {
            Messaging.logSevere("Failed to contact MySQL server or create Home table", this.plugin);
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
        }
    }

	@Override
	public void clearHomes() {
		PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = plugin.getHikari().getConnection();
			statement = connection.prepareStatement("DELETE FROM `homes`;");
			statement.execute();
		} catch (SQLException e) {
			Messaging.logSevere("Failed to clear home locations: " + e.getMessage(), this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}

        plugin.getCache().clearHomeCache();

	}

	@Override
	public HomeEntry getHome(UUID playerUUID, String name) {

        HomeEntry homeEntry = null;
        if((homeEntry=plugin.getCache().getHome(playerUUID, name))!=null) {
            return homeEntry;
        }

        PreparedStatement statement = null;
		ResultSet resultSet = null;

        Connection connection = null;
        try {
            connection = plugin.getHikari().getConnection();
            statement = connection.prepareStatement(
                    "SELECT * FROM `homes` WHERE LOWER(`owner`) = LOWER(?) AND LOWER(`home`) = LOWER(?);"
            );
			statement.setString(1, playerUUID.toString());
			statement.setString(2, name);
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				try {
                    homeEntry = new HomeEntry(playerUUID, name,
                                                        resultSet.getString("world"),
                                                        resultSet.getDouble("x"),
                                                        resultSet.getDouble("y"),
                                                        resultSet.getDouble("z"),
                                                        resultSet.getFloat("pitch"),
                                                        resultSet.getFloat("yaw"));

                    plugin.getCache().addHome(homeEntry);

                    return homeEntry;

				} catch (Exception ex) {ex.printStackTrace();}

			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get home location: " + e.getMessage(), this.plugin);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}

		return null;
	}

	@Override
	public void addHome(UUID playerUUID, String name, Location location) {
        PreparedStatement statement = null;
		ResultSet resultSet;
		boolean exists = false;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();

            HomeEntry homeEntry = new HomeEntry(playerUUID, name, location);

            statement = connection.prepareStatement(
                    "INSERT INTO `homes`\n" +
                    " (`owner`, `home`, `world`, `x`, `y`, `z`, `pitch`, `yaw`)\n" +
                    " VALUES\n" +
                    " (?, ?, ?, ?, ?, ?, ?, ?)\n" +
                    " ON DUPLICATE KEY UPDATE\n" +
                    " `owner` = VALUES(owner),\n" +
                    " `home` = VALUES(home),\n" +
                    " `world` = VALUES(world),\n" +
                    " `x` = VALUES(x),\n" +
                    " `y` = VALUES(y),\n" +
                    " `z` = VALUES(z),\n" +
                    " `pitch` = VALUES(pitch),\n" +
                    " `yaw` = VALUES(yaw),\n"
            );

            statement.setString(1, playerUUID.toString());
            statement.setString(2, name);
            statement.setString(3, location.getWorld().getName());
            statement.setDouble(4, location.getX());
            statement.setDouble(5, location.getY());
            statement.setDouble(6, location.getZ());
            statement.setFloat(7, location.getPitch());
            statement.setFloat(8, location.getYaw());
            statement.execute();

            plugin.getCache().addHome(homeEntry);

		} catch (SQLException e) {
			Messaging.logSevere("Failed to add home location: " + e.getMessage(), this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}
	}

	@Override
	public void removeHome(UUID playerUUID, String name) {
        PreparedStatement statement = null;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();
            statement = connection.prepareStatement("DELETE FROM `homes` WHERE LOWER(`owner`) = LOWER(?) AND LOWER(`home`) = LOWER(?);");
			statement.setString(1, playerUUID.toString());
			statement.setString(2, name);
			statement.execute();
		} catch (SQLException e) {
			Messaging.logSevere("Failed to remove home location: " + e.getMessage(), this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}

        plugin.getCache().removeHome(playerUUID, name);
	}

	@Override
	public boolean getUserExists(UUID playerUUID) {

        if(plugin.getCache().homeContainsPlayer(playerUUID)) {
            return true;
        }

        PreparedStatement statement = null;
		ResultSet resultSet = null;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();
			statement = connection.prepareStatement("SELECT COUNT(*) FROM `homes` WHERE LOWER(`owner`) = LOWER(?);");
			statement.setString(1, playerUUID.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				return resultSet.getInt(1) > 0;
			}
		} catch (SQLException e) {
			Messaging.logSevere("Failed to determine if user exists: " + e.getMessage(), this.plugin);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}
		
		return false;
	}

	@Override
	public int getUserHomeCount(UUID playerUUID) {
        PreparedStatement statement = null;
		ResultSet resultSet = null;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();

            statement = connection.prepareStatement("SELECT COUNT(*) FROM `homes` WHERE LOWER(`owner`) = LOWER(?);");
			statement.setString(1, playerUUID.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				return resultSet.getInt(1);
			}
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get user home count: " + e.getMessage(), this.plugin);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}
		
		return 0;
	}

	@Override
	public ArrayList<HomeEntry> listUserHomes(UUID playerUUID) {

        ResultSet resultSet = null;
        ArrayList<HomeEntry> output = new ArrayList<> ();
        PreparedStatement statement = null;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();

            statement = connection.prepareStatement("SELECT * FROM `homes` WHERE LOWER(`owner`) = LOWER(?);");
			statement.setString(1, playerUUID.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				do {
                    HomeEntry homeEntry = new HomeEntry(UUID.fromString(resultSet.getString("owner")),
                                                resultSet.getString("home"),
                                                resultSet.getString("world"),
                                                resultSet.getDouble("x"),
                                                resultSet.getDouble("y"),
                                                resultSet.getDouble("z"),
                                                resultSet.getFloat("yaw"),
                                                resultSet.getFloat("pitch"));
                    plugin.getCache().addHome(homeEntry);
                    output.add(homeEntry);
				} while (resultSet.next());
			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get all home locations for player: " + e.getMessage(), this.plugin);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}

		return output;
	}

	@Override
	public void importHomes(ArrayList<HomeEntry> homes, boolean overwrite) {
        PreparedStatement statementExists = null;
		PreparedStatement statementInsert = null;
		PreparedStatement statementUpdate;
		ResultSet resultSet = null;
		boolean recordExists;

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();
            statementExists = connection.prepareStatement("SELECT COUNT(*) FROM `homes` WHERE LOWER(`owner`) = LOWER(?) AND LOWER(`home`) = LOWER(?);");
			statementInsert = connection.prepareStatement("INSERT INTO `homes`(`owner`, `home`, `world`, `x`, `y`, `z`, `pitch`, `yaw`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
			statementUpdate = connection.prepareStatement("UPDATE `homes` SET `owner` = ?, `home` = ?, `world` = ?, `x` = ?, `y` = ?, `z` = ?, `pitch` = ?, `yaw` = ? WHERE LOWER(`owner`) = LOWER(?) AND LOWER(`home`) = LOWER(?);");
		
			for (HomeEntry thisEntry : homes) {
				// Determine if entry exists.
				recordExists = false;
				statementExists.setString(1,  thisEntry.getOwnerUUID().toString());
				statementExists.setString(2,  thisEntry.getHomeName());
				resultSet = statementExists.executeQuery();
				if (resultSet.first()) {
					recordExists = resultSet.getInt(1) > 0;
				}
				resultSet.close();
				resultSet = null;
				
				// Save the entry, if required.
				if (recordExists) {
					if (overwrite) {
						statementUpdate.setString(1, thisEntry.getOwnerUUID().toString());
						statementUpdate.setString(2, thisEntry.getHomeName());
						statementUpdate.setString(3, thisEntry.getWorld());
						statementUpdate.setDouble(4, thisEntry.getX());
						statementUpdate.setDouble(5, thisEntry.getY());
						statementUpdate.setDouble(6, thisEntry.getZ());
						statementUpdate.setFloat(7, thisEntry.getPitch());
						statementUpdate.setFloat(8, thisEntry.getYaw());
						statementUpdate.setString(9, thisEntry.getOwnerUUID().toString());
						statementUpdate.setString(10, thisEntry.getHomeName());
						statementUpdate.execute();
					}
				} else {
					statementInsert.setString(1, thisEntry.getOwnerUUID().toString());
					statementInsert.setString(2, thisEntry.getHomeName());
					statementInsert.setString(3, thisEntry.getWorld());
					statementInsert.setDouble(4, thisEntry.getX());
					statementInsert.setDouble(5, thisEntry.getY());
					statementInsert.setDouble(6, thisEntry.getZ());
					statementInsert.setFloat(7, thisEntry.getPitch());
					statementInsert.setFloat(8, thisEntry.getYaw());
					statementInsert.execute();
				}
			}

		} catch (SQLException e) {
			Messaging.logSevere("Failed to import home locations: " + e.getMessage(), this.plugin);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statementExists != null) {
				try {
					statementExists.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

			if (statementInsert != null) {
				try {
					statementInsert.close();
				} catch (SQLException ex) {ex.printStackTrace();} // Eat errors
			}

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {} // Eat errors
            }
		}
	}
}
