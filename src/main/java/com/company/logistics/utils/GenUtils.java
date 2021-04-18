package com.company.logistics.utils;
import com.company.logistics.entity.ColumnEntity;
import com.company.logistics.entity.TableEntity;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class GenUtils {

    private static String currentTableName;

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/Entity.java.vm");
        templates.add("template/Vo.java.vm");
        templates.add("template/Mapper.xml.vm");
        //templates.add("template/menu.sql.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");
        templates.add("template/Controller.java.vm");
        templates.add("template/Mapper.java.vm");
        //templates.add("template/index.vue.vm");
        //templates.add("template/add-or-update.vue.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        Configuration config = getConfig();
        TableEntity tableEntity = setTableEntity(table, columns);
        List<ColumnEntity> columnLists = tableEntity.getColumns();
        Boolean hasBigDecimal = columnLists.stream().filter(o -> o.getAttrType().equals("BigDecimal")).count() > 0 ? Boolean.TRUE : Boolean.FALSE;
        Boolean hasList = columnLists.stream().filter(o -> "array".equals(o.getExtra())).count() > 0 ? Boolean.TRUE : Boolean.FALSE;
        List<ColumnEntity> collect = columnLists.stream().filter(o -> o.getIsPrimary()).collect(Collectors.toList());
        ColumnEntity pk = Optional.ofNullable(collect.size() > 0 ? collect.get(0) : null).orElse(tableEntity.getColumns().get(0));
        tableEntity.setPk(pk);
        VelocityContext context = setVelocityContext(tableEntity, hasBigDecimal, hasList);
        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package"), config.getString("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 设置表数据
     * @author wangzhijun
     * @date 2021/3/23 17:05
     * @param
     * @return
     */
    private static TableEntity setTableEntity(Map<String, String> table,List<Map<String, String>> columns){
        Configuration config = getConfig();
        TableEntity tableEntity = new TableEntity(
                table.get("tableName"),table.get("tableComment"), null,null,
                tableToJava(table.get("tableName"), config.getStringArray("tablePrefix")),null

        );
        List<ColumnEntity> columnLists = setColumns(columns);
        tableEntity.setColumns(columnLists);
        tableEntity.setClassname(StringUtils.uncapitalize(tableEntity.getClassName()));
        return tableEntity;
    }

    /**
     * 设置表的列名数据
     * @author wangzhijun
     * @date 2021/3/23 17:04
     * @param
     * @return
     */
    private static List<ColumnEntity> setColumns(List<Map<String, String>> columns){
        Configuration config = getConfig();
        List<ColumnEntity> columnLists = Lists.newArrayList();
        columns.forEach(item -> {
            ColumnEntity columnEntity = new ColumnEntity(
                    item.get("columnName"),item.get("dataType"),item.get("columnComment"),columnToJava(item.get("columnName")),
                    StringUtils.uncapitalize(columnToJava(item.get("columnName"))),config.getString(item.get("dataType"), columnToJava(item.get("dataType"))),
                    item.get("extra"),"PRI".equalsIgnoreCase(item.get("columnKey"))
            );
            columnLists.add(columnEntity);
        });
        return columnLists;
    }

    /**
     * 设置velocity资源加载器
     * @author wangzhijun
     * @date 2021/3/23 16:41
     * @return
     */
    private static VelocityContext setVelocityContext(TableEntity tableEntity,Boolean hasBigDecimal,Boolean hasList){
        Configuration config = getConfig();
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = StringUtils.isBlank(config.getString("mainPath")) ? "com.company.logistics" : config.getString("mainPath");
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().substring(1).toLowerCase() + tableEntity.getClassname().substring(1,tableEntity.getClassname().length()));
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("hasList", hasList);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);
        return context;
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String[] tablePrefixArray) {
        if (null != tablePrefixArray && tablePrefixArray.length > 0) {
            for (String tablePrefix : tablePrefixArray) {
                  if (tableName.startsWith(tablePrefix)){
                    tableName = tableName.replaceFirst(tablePrefix, "");
                }
            }
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName) {
        //File.separator：系统默认的文件分隔符号，相当于 '\'
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }
        if (template.contains("MongoChildrenEntity.java.vm")) {
            return packagePath + "entity" + File.separator + "inner" + File.separator + currentTableName+ File.separator + splitInnerName(className)+ "InnerEntity.java";
        }
        if (template.contains("Entity.java.vm") || template.contains("MongoEntity.java.vm")) {
            return packagePath + "entity" + File.separator + className + ".java";
        }
        if (template.contains("Vo.java.vm") || template.contains("MongoEntity.java.vm")) {
            return packagePath + "entity" + File.separator + className + "Vo.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }

        if (template.contains("Service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + moduleName + File.separator + className + "Mapper.xml";
        }

        if (template.contains("menu.sql.vm")) {
            return className.toLowerCase() + "_menu.sql";
        }

        if (template.contains("index.vue.vm")) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator + "modules" +
                    File.separator + moduleName + File.separator + className.toLowerCase() + ".vue";
        }

        if (template.contains("add-or-update.vue.vm")) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator + "views" + File.separator + "modules" +
                    File.separator + moduleName + File.separator + className.toLowerCase() + "-add-or-update.vue";
        }

        return null;
    }

    private static String splitInnerName(String name){
          name = name.replaceAll("\\.","_");
          return name;
    }
}
