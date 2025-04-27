package com.macrowing.indaas.upm.utils;

import com.macrowing.indaas.upm.entity.DatabaseInfo;

import java.sql.*;

/**
 * @author User
 * @date 2025/4/27 11:26
 */
public class DBUtils {


    public static boolean checkConnection(DatabaseInfo databaseInfo){
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String jdbcurl = generateJdbcUrl(databaseInfo.getDbType(), databaseInfo.getHost(), databaseInfo.getPort());

            connection = DriverManager.getConnection(jdbcurl,databaseInfo.getUsername(),databaseInfo.getPassword());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try{
                if(connection != null){
                    connection.close();
                }
            }catch(Exception e){

            }

        }
    }

    private static String generateJdbcUrl(String dbType, String ip, String port ) throws Exception {
        switch (dbType.toLowerCase()) {
            case "mysql":
                Class.forName("com.mysql.cj.jdbc.Driver");
                return String.format("jdbc:mysql://%s:%s/mysql?serverTimezone=UTC", ip, port);
            case "postgresql":
                Class.forName("org.postgresql.Driver");
                return String.format("jdbc:postgresql://%s:%s/postgres", ip, port);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%s:%s", ip, port);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s", ip, port);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}
