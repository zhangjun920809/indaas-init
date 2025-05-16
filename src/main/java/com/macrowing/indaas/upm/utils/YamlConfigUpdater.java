package com.macrowing.indaas.upm.utils;

import com.macrowing.indaas.upm.controller.IndaasController;
import com.macrowing.indaas.upm.entity.ApiConfInfo;
import com.macrowing.indaas.upm.entity.DatabaseInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlConfigUpdater{

    private static Log log = LogFactory.getLog(YamlConfigUpdater.class);

    public static void main(String[] args) {
            try {
                // 遍历所有网络接口
                for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    // 过滤条件：接口已启用、非回环、非虚拟
//                    if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()) {
                    if (iface.isUp() && !iface.isLoopback() ) {
                        // 遍历接口的所有IP地址
                        for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                            // 筛选IPv4地址
                            if (addr instanceof Inet4Address) {
                                System.out.println("Server IP: " + addr.getHostAddress());
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("IP地址未找到！");
    }

    /**
     *  更新各个配置文件的 ip配置信息
     * @param filePath
     * @param host
     */
    public static void updateIPConfig(String filePath, String host)  {
        try{
            // 读取 YAML 文件
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(filePath);
            Map<String, Object> yamlMap = yaml.load(inputStream);
            inputStream.close();

            try{
                // 定位到macro.transport.http
                Map<String, Object> macroTransportHttp = (Map<String, Object>) yamlMap.get("macro.transport.http");
                List<Map<String, Object>> listenerConfigurations = (List<Map<String, Object>>) macroTransportHttp.get("listenerConfigurations");
                // 更新host
                for (Map<String, Object> dataSource : listenerConfigurations) {
                    dataSource.put("host",host);
                }
            }catch(Exception e){

            }


            try{
                // 定位到indaas.stores.query.api
                Map<String, Object> indaasstoresqueryapi = (Map<String, Object>) yamlMap.get("indaas.stores.query.api");
                List<Map<String, Object>> listenerConfigurations2 = (List<Map<String, Object>>) indaasstoresqueryapi.get("listenerConfigurations");
                // 更新host
                for (Map<String, Object> dataSource : listenerConfigurations2) {
                    dataSource.put("host",host);
                }
            }catch(Exception e){

            }

            try{
                // 定位到databridge.config
                Map<String, Object> databridgeconfig = (Map<String, Object>) yamlMap.get("databridge.config");
                if(databridgeconfig != null){
                    Object object = databridgeconfig.get("dataReceivers");
                    if (object != null){
                        List<Map<String, Object>> dataReceivers = (List<Map<String, Object>>)object;
                        // 更新host
                        for (Map<String, Object> temp : dataReceivers) {
                            Map<String, Object> dataReceiver = (Map<String, Object>)temp.get("dataReceiver");
                            Map<String, Object> properties = (Map<String, Object>)dataReceiver.get("properties");
                            if (properties.containsKey("hostName")){
                                properties.put("hostName",host);
                            }
                        }
                    }
                }
            }catch(Exception e){

            }

            // 保存更新后的 YAML 文件
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yamlWriter = new Yaml(new Constructor(), new Representer(), options);

            FileWriter writer = new FileWriter(filePath);
            yamlWriter.dump(yamlMap, writer);
            writer.close();

            log.info("YAML文件更新成功！");
        }catch(Exception e){
            e.printStackTrace();
            log.error("配置文件更新失败！" +filePath);
        }

    }

    public static List getLocalIp(){
        ArrayList<String> result = new ArrayList<>();
        try {
            // 遍历所有网络接口
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                // 过滤条件：接口已启用、非回环、非虚拟
//                    if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()) {
                if (iface.isUp() && !iface.isLoopback() ) {
                    // 遍历接口的所有IP地址
                    for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                        // 筛选IPv4地址
                        if (addr instanceof Inet4Address) {
                            System.out.println("Server IP: " + addr.getHostAddress());
                            result.add(addr.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void updateYamlAPIconf(String filePath, ApiConfInfo apiConfInfo)  {

        try{
            // 读取 YAML 文件
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(filePath);
            Map<String, Object> yamlMap = yaml.load(inputStream);
            inputStream.close();

            // 定位到数据源部分
//            Map<String, Object> macroDatasources = (Map<String, Object>) yamlMap.get("macro.datasources");
            Map<String, Object> confmap = (Map<String, Object>) yamlMap.get("api.manager");

            if (confmap != null){
                confmap.put("hostAndPort",apiConfInfo.getHostAndPort());
                confmap.put("tokenHostAndPort",apiConfInfo.getTokenHostAndPort());
                confmap.put("keyAndsecret",apiConfInfo.getKeyAndsecret());
            } else {
                HashMap<String, Object> map = new HashMap<>();
                map.put("hostAndPort",apiConfInfo.getHostAndPort());
                map.put("tokenHostAndPort",apiConfInfo.getTokenHostAndPort());
                map.put("keyAndsecret",apiConfInfo.getKeyAndsecret());
                yamlMap.put("api.manager",map);
            }

            // 保存更新后的 YAML 文件
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yamlWriter = new Yaml(new Constructor(), new Representer(), options);

            FileWriter writer = new FileWriter(filePath);
            yamlWriter.dump(yamlMap, writer);
            writer.close();

           log.info("YAML文件API配置更新成功！");
        }catch(Exception e){
            e.printStackTrace();
            log.error("YAML文件API配置更新失败！" +filePath);
        }
    }

    public static void updateYaml(String filePath, String host, String port,
                                  String username, String password)  {
        try{
            // 读取 YAML 文件
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(filePath);
            Map<String, Object> yamlMap = yaml.load(inputStream);
            inputStream.close();

            // 定位到数据源部分
            Map<String, Object> macroDatasources = (Map<String, Object>) yamlMap.get("macro.datasources");
            List<Map<String, Object>> dataSources = (List<Map<String, Object>>) macroDatasources.get("dataSources");

            // 更新指定数据源
            for (Map<String, Object> dataSource : dataSources) {
                String name = (String) dataSource.get("name");
//                if ("INDAAS_DATASOURCE_COLLECTION".equals(name) || "INDAAS_EDITOR".equals(name)) {
                if (IndaasController.dblist.contains(name)) {
                    Map<String, Object> definition = (Map<String, Object>) dataSource.get("definition");
                    Map<String, Object> configuration = (Map<String, Object>) definition.get("configuration");

                    // 替换 jdbcUrl 中的 host, port 和 database
                    String originalJdbcUrl = (String) configuration.get("jdbcUrl");
                    if (originalJdbcUrl != null) {
                        String updatedJdbcUrl = replaceJdbcUrl(originalJdbcUrl, host, port);
                        configuration.put("jdbcUrl", updatedJdbcUrl);
                    }

                    // 替换用户名和密码
                    configuration.put("username", username);
                    configuration.put("password", password);
                }
            }

            // 保存更新后的 YAML 文件
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yamlWriter = new Yaml(new Constructor(), new Representer(), options);

            FileWriter writer = new FileWriter(filePath);
            yamlWriter.dump(yamlMap, writer);
            writer.close();

            log.info("YAML文件更新成功！");
        }catch(Exception e){
            e.printStackTrace();
            log.error("配置文件更新失败！" +filePath);
        }

    }


//    editorconf:
//    host: 192.168.1.253
//    port: '9391'
//    modelconf:
//    host: 192.168.1.58
//    port: '8978'

    public static void updateYamlEditorMDCconf(String filePath, String host, String port,String type)  {
        try{
            // 读取 YAML 文件
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(filePath);
            Map<String, Object> yamlMap = yaml.load(inputStream);
            inputStream.close();

            if ("editor".equalsIgnoreCase(type)){
                type = "editorconf";
            } else {
                type = "modelconf";
            }

            // 定位到数据源部分
//            Map<String, Object> macroDatasources = (Map<String, Object>) yamlMap.get("macro.datasources");
            Map<String, Object> confmap = (Map<String, Object>) yamlMap.get(type);

            if (confmap != null){
                confmap.put("host",host);
                confmap.put("port",port);
            } else {
                HashMap<String, Object> map = new HashMap<>();
                map.put("host",host);
                map.put("port",port);
                yamlMap.put(type,map);
            }

            // 保存更新后的 YAML 文件
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yamlWriter = new Yaml(new Constructor(), new Representer(), options);

            FileWriter writer = new FileWriter(filePath);
            yamlWriter.dump(yamlMap, writer);
            writer.close();

            log.info("YAML 文件editor/mdc更新成功！");
        }catch(Exception e){
            e.printStackTrace();
            log.error("YAML 文件editor/mdc更新失败！" +filePath);
        }
    }

    private static String replaceJdbcUrl(String originalUrl, String host, String port) {
        // 使用正则表达式替换 host, port, 和 database 部分
        return originalUrl
                .replaceFirst("//[^:/]+", "//" + host) // 替换 host
                .replaceFirst(":\\d+/", ":" + port + "/"); // 替换 port
    }


    public static List<String> getDirectoriesWithPrefix(String parentPath ) {
        // 创建结果集合
        List<String> matchingDirectories = new ArrayList<>();

        // 创建 File 对象表示父目录
        File parentDir = new File(parentPath);

        // 检查父目录是否存在且是目录
        if (parentDir.exists() && parentDir.isDirectory()) {
            // 遍历子文件和子目录
            File[] files = parentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 筛选符合条件的目录
                    if (file.isDirectory() && file.getName().startsWith("indaas-si")) {
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"server" +  File.separator +"deployment.yaml");
                    } else if(file.isDirectory() && file.getName().equalsIgnoreCase("indaas")){
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"server" +  File.separator +"deployment.yaml");
                    } else if(file.isDirectory() && file.getName().equalsIgnoreCase("indaas-ad")){
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"monitor" +  File.separator +"deployment.yaml");
                    }
                }
            }
        } else {
            log.info("指定路径不是有效的目录：" + parentPath);
        }

        return matchingDirectories;
    }

    /**
     * 将两个参数写入指定目录中的 init.conf 文件中。
     *
     * @param directoryPath 指定的目录路径
     * @param username            参数 1
     * @param password            参数 2
     */
    public static void writeToInitConf(String directoryPath, String username, String password) throws IOException {
        // 确定 init.conf 文件路径
        File directory = new File(directoryPath);
        File configFile = new File(directory, "init.conf");

        // 如果目录不存在，则创建目录
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("无法创建目录：" + directoryPath);
            }
        }

        // 使用 try-with-resources 写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(username);
            writer.newLine();
            writer.write(password);
            writer.newLine();
        }

        log.info("" + configFile.getAbsolutePath());
    }

    public static List<String> getDirectoriesOsgi(String parentPath ) {
        // 创建结果集合
        List<String> matchingDirectories = new ArrayList<>();

        // 创建 File 对象表示父目录
        File parentDir = new File(parentPath);

        // 检查父目录是否存在且是目录
        if (parentDir.exists() && parentDir.isDirectory()) {
            // 遍历子文件和子目录
            File[] files = parentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 筛选符合条件的目录
                    if(file.isDirectory() && file.getName().equalsIgnoreCase("indaas")){
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"osgi" +  File.separator +"jdbc.properties");
                    } else if(file.isDirectory() && file.getName().equalsIgnoreCase("indaas-ad")){
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"osgi" +  File.separator +"jdbc.properties");
                    }
                }
            }
        } else {
            log.info("指定路径不是有效的目录：" + parentPath);
        }

        return matchingDirectories;
    }


    public static List<String> getDirectoriesDMC(String parentPath ) {
        // 创建结果集合
        List<String> matchingDirectories = new ArrayList<>();

        // 创建 File 对象表示父目录
        File parentDir = new File(parentPath);

        // 检查父目录是否存在且是目录
        if (parentDir.exists() && parentDir.isDirectory()) {
            // 遍历子文件和子目录
            File[] files = parentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 筛选符合条件的目录
                    if(file.isDirectory() && file.getName().equalsIgnoreCase("indaasmdc")){
                        matchingDirectories.add(file.toString() + File.separator +"conf"+ File.separator +"indaasmdc.conf");
                    }
                }
            }
        } else {
            log.info("指定路径不是有效的目录：" + parentPath);
        }

        return matchingDirectories;
    }
    public static void updateOsgiconfig(String filePath, String host, String port,
                                  String username, String password)  {
        try{
            Properties properties = new Properties();

            // 读取已有文件内容
            FileInputStream input = new FileInputStream(filePath);
            properties.load(input);
            input.close();

//            // 打印已有内容
//            System.out.println("原始配置：");
//            properties.forEach((key, value) -> System.out.println(key + "=" + value));

            String url = properties.getProperty("url");
            url = replaceJdbcUrl(url,host,port);
            // 添加键值对
            //todo 驱动逻辑后续完善
            properties.setProperty("driver", "com.mysql.cj.jdbc.Driver");
            properties.setProperty("url", url);
            properties.setProperty("username", username);
            properties.setProperty("password", password);

            try (FileOutputStream output = new FileOutputStream(filePath)) {
                // 写入到文件
                properties.store(output, "Application Configuration");
                log.info(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch(Exception e){
                log.error(e.getMessage());
        }
    }

    public static void updateMDCconfig(String filePath, String host, String port,
                                        String username, String password)  {
        try{

            byte[] bytes = Files.readAllBytes(Paths.get(filePath));

            String configContent = new String(bytes);
            // 2. 正则替换：URL 的 IP 和端口
            configContent = replaceUrlHostPort(configContent, host, port);

            // 3. 正则替换：用户名和密码
            configContent = replaceUserPassword(configContent, username, password);

            try (FileWriter writer = new FileWriter(filePath)) { // 默认覆盖文件
                writer.write(configContent);
                log.info("mdc配置文件写入成功！" + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }catch(Exception e){
            log.error(e.getMessage());
        }
    }

    // 替换 URL 中的 IP 和端口
    private static String replaceUrlHostPort(String content, String newIp, String newPort) {
        String regex = "(url:\\s*[\"']jdbc:)([^/:]+)(://)([^/:]+)(:(\\d+)?)?([/\"'][^\"']*[\"'])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuffer updated = new StringBuffer();

        while (matcher.find()) {
            String dbType = matcher.group(2);  // 数据库类型（如 mysql）
            String replacement = matcher.group(1) + dbType + matcher.group(3) + newIp;
            if (newPort != null && !newPort.isEmpty()) {
                replacement += ":" + newPort;
            }
            replacement += matcher.group(7);   // 保留后续路径
            matcher.appendReplacement(updated, replacement);
        }
        matcher.appendTail(updated);
        return updated.toString();
    }

    // 替换用户名和密码
    private static String replaceUserPassword(String content, String newUser, String newPassword) {
        // 替换用户名的正则
        String userRegex = "(user:\\s*[\"'])([^\"']*)([\"'])";
        content = content.replaceAll(userRegex, "$1" + newUser + "$3");

        // 替换密码的正则
        String passwordRegex = "(password:\\s*[\"'])([^\"']*)([\"'])";
        content = content.replaceAll(passwordRegex, "$1" + newPassword + "$3");

        return content;
    }

}


