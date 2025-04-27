package com.macrowing.indaas.upm.entity;

import javax.validation.constraints.NotNull;

/**
 * @author User
 * @date 2025/1/17 22:50
 */
public class ApiConfInfo {
    @NotNull
    private String hostAndPort;
    @NotNull
    private String tokenHostAndPort;
    @NotNull
    private String keyAndsecret;

    public String getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getTokenHostAndPort() {
        return tokenHostAndPort;
    }

    public void setTokenHostAndPort(String tokenHostAndPort) {
        this.tokenHostAndPort = tokenHostAndPort;
    }

    public String getKeyAndsecret() {
        return keyAndsecret;
    }

    public void setKeyAndsecret(String keyAndsecret) {
        this.keyAndsecret = keyAndsecret;
    }
}
