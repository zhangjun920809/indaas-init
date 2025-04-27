package com.macrowing.indaas.upm.entity;

import javax.validation.constraints.NotNull;

/**
 * @author User
 * @date 2025/3/2 10:23
 */
public class ConfInfo {
    @NotNull
    private String host;
    @NotNull
    private String port;

    @NotNull
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
