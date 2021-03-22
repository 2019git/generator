package com.company.logistics.dao;

import java.util.List;
import java.util.Map;

/**
 * 数据库接口
 * @author logistics
 * @date 2020/12/23 13:36
 */
public interface GeneratorDao {
    List<Map<String, Object>> queryList(Map<String, Object> map);

    Map<String, String> queryTable(String tableName);

    List<Map<String, String>> queryColumns(String tableName);
}
