package com.macrowing.indaas.upm.controller;

import com.macrowing.indaas.upm.entity.ApiConfInfo;
import com.macrowing.indaas.upm.entity.ConfInfo;
import com.macrowing.indaas.upm.utils.DBUtils;
import com.macrowing.indaas.upm.utils.Response;
import com.macrowing.indaas.upm.entity.DatabaseInfo;
import com.macrowing.indaas.upm.entity.UserInfo;
import com.macrowing.indaas.upm.utils.YamlConfigUpdater;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author User
 * @date 2022/7/14 18:16
 */
@RestController
@RequestMapping("/indaas")
public class IndaasController {

    private static Log log = LogFactory.getLog(IndaasController.class);
    public static List<String> dblist = new ArrayList<>();
    @Value("${indaas.home:default}")
    private String indaasHome;

    /**
     *  初始化系统数据源列表
     */
    public IndaasController() {
        dblist.add("INDAAS_DATASOURCE_COLLECTION");
        dblist.add("INDAAS_EDITOR");
        dblist.add("INDAAS_LOG");
        dblist.add("STATUS_DASHBOARD_DB");
        dblist.add("INDAAS_USER_MANAGER");
        dblist.add("METRICS_DB");
        dblist.add("INDAAS_MONITOR");
        dblist.add("CLUSTER_DB");
        dblist.add("INDAAS_ERROR");

    }

    @PostMapping("/api/conf")
    public Response updateYamlAPIconf(@RequestBody @Valid ApiConfInfo apiConfInfo) {

        if (apiConfInfo == null || StringUtils.isEmpty(apiConfInfo.getHostAndPort()) || StringUtils.isEmpty(apiConfInfo.getKeyAndsecret())){
            return Response.error().message("参数不能为空！");
        }
        String rootpath ="";
        // 初始jar包放置于indaas-ad/bin目录
        // 如果配置文件没有配置项目根目录，则使用indaas-ad/bin/../../作为项目根目录 （即 user.dir/../../）
        if ("default".equalsIgnoreCase(indaasHome)){
            rootpath = System.getProperty("user.dir") +File.separator +".."+ File.separator +".." +  File.separator;
        } else {
            rootpath = indaasHome;
        }
        // 获取dri项目目录,
        log.info(rootpath);
        List<String> directoriesWithPrefix = YamlConfigUpdater.getDirectoriesWithPrefix(rootpath);
        // 处理yaml文件
        directoriesWithPrefix.forEach(v->{
            if (v.contains("indaas-ad/conf")){
                log.info(v);
                YamlConfigUpdater.updateYamlAPIconf(
                        v,apiConfInfo);
            }
        });

        return Response.ok();
    }

    @PostMapping("/editormdc/conf")
    public Response updateYamlEditorMDCconf(@RequestBody @Valid ConfInfo confInfo) {

        if (confInfo == null || StringUtils.isEmpty(confInfo.getHost()) || StringUtils.isEmpty(confInfo.getPort())){
            return Response.error().message("参数不能为空！");
        }
        String rootpath ="";
        // 初始jar包放置于indaas-ad/bin目录
        // 如果配置文件没有配置项目根目录，则使用indaas-ad/bin/../../作为项目根目录 （即 user.dir/../../）
        if ("default".equalsIgnoreCase(indaasHome)){
            rootpath = System.getProperty("user.dir") +File.separator +".."+ File.separator +".." +  File.separator;
        } else {
            rootpath = indaasHome;
        }
        // 获取dri项目目录,
        log.info(rootpath);
        List<String> directoriesWithPrefix = YamlConfigUpdater.getDirectoriesWithPrefix(rootpath);
        // 处理yaml文件
        directoriesWithPrefix.forEach(v->{
            if (v.contains("indaas-ad/conf")){
                log.info(v);
                YamlConfigUpdater.updateYamlEditorMDCconf(
                        v,
                        confInfo.getHost().trim(),
                        confInfo.getPort().trim(),
                        confInfo.getType().trim());
            }
        });

        return Response.ok();
    }

    @PostMapping("/database/save")
    public Response saveDatabaseInfo(@RequestBody @Valid DatabaseInfo databaseInfo) {

        if (databaseInfo == null || StringUtils.isEmpty(databaseInfo.getPassword()) || StringUtils.isEmpty(databaseInfo.getUsername()) ||
        StringUtils.isEmpty(databaseInfo.getHost()) || StringUtils.isEmpty(databaseInfo.getPort())){
            return Response.error().message("参数不能为空！");
        }
        String rootpath ="";
        // 初始jar包放置于indaas-ad/bin目录
        // 如果配置文件没有配置项目根目录，则使用indaas-ad/bin/../../作为项目根目录 （即 user.dir/../../）
        if ("default".equalsIgnoreCase(indaasHome)){
            rootpath = System.getProperty("user.dir") +File.separator +".."+ File.separator +".." +  File.separator;
        } else {
            rootpath = indaasHome;
        }
        // 获取dri项目目录,
        log.info(rootpath);
        List<String> directoriesWithPrefix = YamlConfigUpdater.getDirectoriesWithPrefix(rootpath);
        // 处理yaml文件
        directoriesWithPrefix.forEach(v->{
            log.info(v);
            YamlConfigUpdater.updateYaml(
                    v,
                    databaseInfo.getHost().trim(),
                    databaseInfo.getPort().trim(),
                    databaseInfo.getUsername().trim(),
                    databaseInfo.getPassword().trim());
        });

        //处理 /indaas-ad/conf/osgi 文件内容,工作单元不用处理
        List<String> directoriesOsgi = YamlConfigUpdater.getDirectoriesOsgi(rootpath);
        directoriesOsgi.forEach(v->{
            YamlConfigUpdater.updateOsgiconfig(
                    v,
                    databaseInfo.getHost().trim(),
                    databaseInfo.getPort().trim(),
                    databaseInfo.getUsername().trim(),
                    databaseInfo.getPassword().trim());
        });

        //更新模型中心配置
        List<String> directoriesmdc = YamlConfigUpdater.getDirectoriesDMC(rootpath);
        directoriesmdc.forEach(v->{
            YamlConfigUpdater.updateMDCconfig(
                    v,
                    databaseInfo.getHost().trim(),
                    databaseInfo.getPort().trim(),
                    databaseInfo.getUsername().trim(),
                    databaseInfo.getPassword().trim());
        });

        return Response.ok();
    }

    @GetMapping("/database")
    public Response getDatabaseInfo() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        return Response.ok().data(map);
    }


    @PostMapping("/database/check")
    public Response check(@RequestBody @Valid DatabaseInfo databaseInfo) {

        if (databaseInfo == null || StringUtils.isEmpty(databaseInfo.getPassword()) || StringUtils.isEmpty(databaseInfo.getUsername()) ||
                StringUtils.isEmpty(databaseInfo.getHost()) || StringUtils.isEmpty(databaseInfo.getPort())){
            return Response.error().message("参数不能为空！");
        }
        boolean result = DBUtils.checkConnection(databaseInfo);
        HashMap<String, Object> map = new HashMap<>();
        map.put("result",result);
        return Response.ok().data(map);
    }

    @PostMapping("/user/save")
    public Response saveAdminUserInfo(@RequestBody UserInfo userInfo) throws Exception {
        if (userInfo == null || StringUtils.isEmpty(userInfo.getPassword()) || StringUtils.isEmpty(userInfo.getUsername())){
            return Response.error().message("参数不能为空！");
        }

        String password = userInfo.getPassword();

        // 进行两次md5加密
        String md51 = DigestUtils.md5DigestAsHex(password.getBytes());
        String md52 = DigestUtils.md5DigestAsHex(md51.getBytes());

        String rootpath = "";
        // 初始jar包放置于indaas-ad/bin目录
        // 如果配置文件没有配置项目根目录，则使用indaas-ad/bin/../../作为项目根目录 （即 user.dir/../../）
        if ("default".equalsIgnoreCase(indaasHome)){
            rootpath = System.getProperty("user.dir") +File.separator +".."+ File.separator +"conf" +  File.separator +"monitor";
        } else {
            rootpath = indaasHome;
        }
        // 获取dri项目目录
        log.info(rootpath);
        YamlConfigUpdater.writeToInitConf(rootpath,userInfo.getUsername(),md52);
        return Response.ok();
    }

    @GetMapping("/user")
    public Response getAdminInfo() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
//        map.put("username","roots");
//        map.put("password","zhang@123");
        return Response.ok().data(map);
    }

}
