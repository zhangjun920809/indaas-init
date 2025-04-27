package com.macrowing.indaas.upm.entity;

import javax.validation.constraints.NotNull;

/**
 * @author User
 * @date 2025/1/17 22:50
 */
public class DatabaseInfo {
    @NotNull
    private String host;
    @NotNull
    private String port;
    @NotNull
    private String username;
    @NotNull
    private String password;

    @NotNull
    private String dbType;


    @Override
    public String toString() {
        return "DatabaseInfo{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public DatabaseInfo(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
