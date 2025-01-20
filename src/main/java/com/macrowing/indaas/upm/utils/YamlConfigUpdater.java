package com.macrowing.indaas.upm.utils;

import com.macrowing.indaas.upm.controller.IndaasController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class YamlConfigUpdater{

    private static Log log = LogFactory.getLog(YamlConfigUpdater.class);

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

            System.out.println("YAML 文件更新成功！");
        }catch(Exception e){
            System.out.println("配置文件更新失败！" +filePath);
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
}


