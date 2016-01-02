package net.madmanmarkau.MultiHome.converter;

import net.madmanmarkau.MultiHome.MultiHome;

import java.sql.*;
import java.util.*;

public class MySQLUUIDConverter extends UUIDConverter {

    public MySQLUUIDConverter(MultiHome main) {
        super(main);
    }

    @Override
    protected Set<String> readInOld() {

        Connection conn = null;
        PreparedStatement query = null;
        ResultSet rs = null;

        try {
            conn = this.plugin.getHikari().getConnection();
            query = conn.prepareStatement("SELECT DISTINCT owner FROM homes");
            rs = query.executeQuery();

            ArrayList<String> ownerList = new ArrayList<>();
            while(rs.next()) {
                ownerList.add(rs.getString("owner"));
            }
            Set<String> ownerSet = new HashSet<>(ownerList);
            return ownerSet;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                if (query != null) query.close();
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return new HashSet<>();
    }

    @Override
    protected void updateData(Map<String, UUID> uuidMappings) {
        Connection conn = null;
        PreparedStatement stmt = null;


        try {
            conn = this.plugin.getHikari().getConnection();
            //Add the UUID column
            stmt = conn.prepareStatement(
                    "ALTER TABLE homes" +
                    "MODIFY COLUMN owner VARCHAR(36)"
            );

            //Do batch updates on owner
            stmt = conn.prepareStatement("UPDATE homes " +
                                         "SET owner=? " +
                                         "WHERE owner=?"
            );
            int i = 0;
            for(String ownerName : uuidMappings.keySet()) {
                stmt.setString(1, uuidMappings.get(ownerName).toString());
                stmt.setString(2, ownerName);
                stmt.addBatch();
                if(i%500==0 || i >= uuidMappings.size()) {
                    stmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean needConversion() {
        //TODO Temporary, must find way to see if table has been converted
        return true;
    }
}
