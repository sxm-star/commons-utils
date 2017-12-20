
package com.songxm.commons;

import com.google.common.base.Preconditions;
import com.songxm.commons.annotation.PrimaryKey;
import com.songxm.commons.annotation.UniqueIndex;
import com.songxm.commons.annotation.UpdateIgnore;
import com.songxm.commons.model.Criteria;
import com.songxm.commons.model.Criteria.Type;
import com.songxm.commons.model.JdbcResult;
import com.songxm.commons.model.PageRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
@SuppressWarnings("unchecked")
public class BaseJdbcUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseJdbcUtils.class);
    private static Map<Class, List<String>> updateFieldsMap = new ConcurrentHashMap();

    public BaseJdbcUtils() {
    }

    public static JdbcResult getInsert(String table, Object entity, Map<String, String> dbMapping) {
        Preconditions.checkArgument(entity != null, "数据库插入数据时entity对象不能为空");
        return insertSql(table, Arrays.asList(new Object[]{entity}), dbMapping, false);
    }

    public static JdbcResult getInsertIgnore(String table, Object entity, Map<String, String> dbMapping) {
        Preconditions.checkArgument(entity != null, "数据库插入数据时entity对象不能为空");
        return insertSql(table, Arrays.asList(new Object[]{entity}), dbMapping, true);
    }

    public static JdbcResult getInsertOrUpdate(String table, Object entity, Map<String, String> dbMapping) {
        Preconditions.checkArgument(entity != null, "数据库插入数据时entity对象不能为空");
        return getBatchInsertOrUpdate(table, Arrays.asList(new Object[]{entity}), dbMapping);
    }

    public static JdbcResult getBatchInsert(String table, List<? extends Object> entities, Map<String, String> dbMapping) {
        return insertSql(table, entities, dbMapping, false);
    }

    public static JdbcResult getBatchInsertIgnore(String table, List<? extends Object> entities, Map<String, String> dbMapping) {
        return insertSql(table, entities, dbMapping, true);
    }

    public static JdbcResult getBatchInsertOrUpdate(String table, List<? extends Object> entities, Map<String, String> dbMapping) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(entities), "数据库插入数据时entity对象不能为空");
        JdbcResult jdbcResult = insertSql(table, entities, dbMapping, false);
        StringBuilder sql = new StringBuilder(jdbcResult.getSql());
        sql.append(" ON DUPLICATE KEY UPDATE ");
        List<String> updateFields = new ArrayList();
        Class entityCls = entities.get(0).getClass();
        if(updateFieldsMap.containsKey(entityCls)) {
            updateFields = (List)updateFieldsMap.get(entityCls);
        } else {
            Iterator paramList = dbMapping.keySet().iterator();

            while(paramList.hasNext()) {
                String i = (String)paramList.next();

                try {
                    Field params = entityCls.getDeclaredField(i);
                    if(params.getAnnotation(PrimaryKey.class) == null && params.getAnnotation(UniqueIndex.class) == null && params.getAnnotation(UpdateIgnore.class) == null) {
                        updateFields.add(i);
                    }
                } catch (Throwable var14) {
                    log.error("dbMapping中存在无法识别的entity属性{}", i);
                }
            }

            updateFieldsMap.put(entityCls, updateFields);
        }

        ArrayList var15 = new ArrayList();

        for(int var16 = 0; var16 < entities.size(); ++var16) {
            Object[] var17 = (Object[])jdbcResult.getParamsList().get(var16);
            Map entityMap = BaseBeanUtils.beanToMap(entities.get(var16));
            if(var16 == 0) {
                for(int updateParams = 0; updateParams < ((List)updateFields).size(); ++updateParams) {
                    if(updateParams != 0) {
                        sql.append(",");
                    }

                    String index = (String)((List)updateFields).get(updateParams);
                    sql.append((String)dbMapping.get(index)).append("=?");
                }
            }

            ArrayList var18 = new ArrayList();

            for(int var19 = 0; var19 < ((List)updateFields).size(); ++var19) {
                String fieldName = (String)((List)updateFields).get(var19);
                var18.add(entityMap.get(fieldName));
            }

            var15.add(ArrayUtils.addAll(var17, var18.toArray()));
        }

        jdbcResult.setSql(sql.toString());
        jdbcResult.setParamsList(var15);
        return jdbcResult;
    }

    public static JdbcResult getSelect(String table, Criteria criterion) {
        List criteria = criterion == null?Collections.emptyList():Arrays.asList(new Criteria[]{criterion});
        return getSelect(table, (List)criteria, (PageRequest)null);
    }

    public static JdbcResult getSelect(String table, List<Criteria> criteria) {
        return getSelect(table, (List)criteria, (PageRequest)null);
    }

    public static JdbcResult getSelect(String table, Criteria criterion, PageRequest pageRequest) {
        List criteria = criterion == null?Collections.emptyList():Arrays.asList(new Criteria[]{criterion});
        return getSelect(table, criteria, pageRequest);
    }

    public static JdbcResult getSelect(String table, List<Criteria> criteria, PageRequest pageRequest) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        StringBuilder sql = (new StringBuilder("SELECT * FROM ")).append(table);
        JdbcResult whereSql = whereSql(criteria);
        JdbcResult jdbcResult = new JdbcResult(sql.append(whereSql.getSql()).toString(), whereSql.getParams());
        if(pageRequest != null && pageRequest.getPage() != null) {
            String pageSql = pageSql(pageRequest);
            jdbcResult.setSql(jdbcResult.getSql() + pageSql);
        }

        return jdbcResult;
    }

    public static JdbcResult getSelectForCount(String table, Criteria criterion) {
        List criteria = criterion == null?Collections.emptyList():Arrays.asList(new Criteria[]{criterion});
        return getSelectForCount(table, criteria);
    }

    public static JdbcResult getSelectForCount(String table, List<Criteria> criteria) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        StringBuilder sql = (new StringBuilder("SELECT count(*) FROM ")).append(table);
        JdbcResult whereSql = whereSql(criteria);
        return new JdbcResult(sql.append(whereSql.getSql()).toString(), whereSql.getParams());
    }

    public static JdbcResult getSelectForSum(String table, Criteria criterion, String... dbColumns) {
        List criteria = criterion == null?Collections.emptyList():Arrays.asList(new Criteria[]{criterion});
        return getSelectForSum(table, criteria, dbColumns);
    }

    public static JdbcResult getSelectForSum(String table, List<Criteria> criteria, String... dbColumns) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        Preconditions.checkArgument(dbColumns != null && dbColumns.length > 0, "表[" + table + "]进行sum运算的列名不能为空");
        StringBuilder sql = new StringBuilder("SELECT ");
        IntStream.range(0, dbColumns.length).forEach((index) -> {
            if(index != 0) {
                sql.append(",");
            }

            sql.append("sum(").append(dbColumns[index]).append(")");
        });
        sql.append(" FROM ").append(table);
        JdbcResult whereSql = whereSql(criteria);
        return new JdbcResult(sql.append(whereSql.getSql()).toString(), whereSql.getParams());
    }

    public static JdbcResult getPatch(String table, Object entity, Map<String, String> dbMapping, String... dbColumns) {
        Preconditions.checkArgument(dbColumns != null && dbColumns.length > 0, "更新数据时必须指定dbColumns条件");
        return getPatch(table, entity, dbMapping, Arrays.asList(dbColumns));
    }

    public static JdbcResult getPatch(String table, Object entity, Map<String, String> dbMapping, Criteria criterion) {
        Preconditions.checkArgument(criterion != null, "更新数据时criteria不能为空");
        return getPatch(table, entity, dbMapping, Arrays.asList(new Criteria[]{criterion}));
    }

    public static JdbcResult getPatch(String table, Object entity, Map<String, String> dbMapping, List<? extends Object> criteria) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        Preconditions.checkArgument(entity != null, "数据库插入数据时entity对象不能为空");
        Preconditions.checkArgument(dbMapping != null && dbMapping.size() > 0, "数据库字段名和实体属性名的对应关系不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(criteria), "更新数据时criteria不能为空");
        StringBuilder sql = (new StringBuilder("UPDATE ")).append(table).append(" SET ");
        Map colValueMap = colValues(entity, dbMapping, true);
        ArrayList colNames = new ArrayList(colValueMap.keySet());
        IntStream.range(0, colNames.size()).forEach((index) -> {
            if(index != 0) {
                sql.append(",");
            }

            sql.append((String)colNames.get(index)).append("=?");
        });
        JdbcResult whereSql = whereSql(criteria, colValueMap);
        sql.append(whereSql.getSql());
        ArrayList params = new ArrayList();
        IntStream.range(0, colNames.size()).forEach((index) -> {
            params.add(colValueMap.get(colNames.get(index)));
        });
        IntStream.range(0, whereSql.getParams().length).forEach((index) -> {
            params.add(whereSql.getParams()[index]);
        });
        return new JdbcResult(sql.toString(), params.toArray());
    }

    public static JdbcResult getUpdate(String table, Object entity, Map<String, String> dbMapping, Criteria criterion) {
        Preconditions.checkArgument(criterion != null, "更新数据时criteria不能为空");
        return getUpdate(table, entity, dbMapping, Arrays.asList(new Criteria[]{criterion}));
    }

    public static JdbcResult getUpdate(String table, Object entity, Map<String, String> dbMapping, String... dbColumns) {
        Preconditions.checkArgument(dbColumns != null && dbColumns.length > 0, "更新数据时必须指定dbColumns条件");
        return getUpdate(table, entity, dbMapping, Arrays.asList(dbColumns));
    }

    public static JdbcResult getUpdate(String table, Object entity, Map<String, String> dbMapping, List<? extends Object> criteria) {
        return getBatchUpdate(table, Arrays.asList(new Object[]{entity}), dbMapping, criteria);
    }

    public static JdbcResult getBatchUpdate(String table, List<? extends Object> entities, Map<String, String> dbMapping, String... dbColumns) {
        Preconditions.checkArgument(dbColumns != null && dbColumns.length > 0, "更新数据时必须指定dbColumn条件");
        return getBatchUpdate(table, entities, dbMapping, Arrays.asList(dbColumns));
    }

    public static JdbcResult getBatchUpdate(String table, List<? extends Object> entities, Map<String, String> dbMapping, List<? extends Object> criteria) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(entities), "数据库插入数据时entity对象不能为空");
        Preconditions.checkArgument(dbMapping != null && dbMapping.size() > 0, "数据库字段名和实体属性名的对应关系不能为空");
        StringBuilder sql = (new StringBuilder("UPDATE ")).append(table).append(" SET ");
        ArrayList fieldNames = new ArrayList(dbMapping.keySet());
        IntStream.range(0, dbMapping.size()).forEach((index) -> {
            if(index != 0) {
                sql.append(",");
            }

            sql.append((String)dbMapping.get(fieldNames.get(index))).append("=?");
        });
        sql.append(" WHERE ");
        IntStream.range(0, criteria.size()).forEach((index) -> {
            if(index != 0) {
                sql.append(" AND ");
            }

            Object column = criteria.get(index);
            if(column instanceof String) {
                sql.append(column).append("=?");
            } else {
                Criteria criterion = (Criteria)column;
                sql.append(whereSql(criterion));
            }

        });
        ArrayList paramsList = new ArrayList();
        entities.forEach((entity) -> {
            ArrayList params = new ArrayList();
            Map entityMap = BaseBeanUtils.beanToMap(entity);
            IntStream.range(0, dbMapping.size()).forEach((index) -> {
                params.add(entityMap.get(fieldNames.get(index)));
            });
            IntStream.range(0, criteria.size()).forEach((index) -> {
                Object column = criteria.get(index);
                if(column instanceof String) {
                    Entry criterion = (Entry)dbMapping.entrySet().stream().filter((entry) -> {
                        return ((String)entry.getValue()).equalsIgnoreCase((String)column);
                    }).findAny().orElse(null);
                    String field = (String)criterion.getKey();
                    params.add(entityMap.get(field));
                } else if(column instanceof Criteria) {
                    Criteria criterion1 = (Criteria)column;
                    if(criterion1.getType() != Type.IN && criterion1.getType() != Type.NIN) {
                        params.add(criterion1.getValue());
                    } else {
                        params.addAll((List)criterion1.getValue());
                    }
                }

            });
            paramsList.add(params.toArray());
        });
        return new JdbcResult(sql.toString(), paramsList);
    }

    public static JdbcResult getDelete(String table, Criteria criterion) {
        Preconditions.checkArgument(criterion != null, "数据库删除必须要指定条件");
        return getDelete(table, Arrays.asList(new Criteria[]{criterion}));
    }

    public static JdbcResult getDelete(String table, List<Criteria> criteria) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(criteria), "数据库删除必须要指定条件");
        StringBuilder sql = (new StringBuilder("DELETE FROM ")).append(table);
        JdbcResult whereSql = whereSql(criteria);
        return new JdbcResult(sql.append(whereSql.getSql()).toString(), whereSql.getParams());
    }

    public static <T> T dbRowToPo(Map<String, Object> dbRow, Map<String, String> dbMapping, Class<T> poClass) {
        return dbRowToEntity(dbRow, dbMapping, poClass);
    }

    /** @deprecated */
    @Deprecated
    public static <T> T dbRowToEntity(Map<String, Object> dbRow, Map<String, String> dbMapping, Class<T> entityClass) {
        if(dbRow != null && dbRow.size() != 0) {
            Object entity = BaseBeanUtils.newInstance(entityClass);
            if(entity == null) {
                return null;
            } else {
                Arrays.stream(entityClass.getDeclaredFields()).forEach((f) -> {
                    try {
                        f.setAccessible(true);
                        String e = (String)dbMapping.get(f.getName());
                        if(e != null) {
                            Object value = dbRow.get(e);
                            if(value != null) {
                                if(value instanceof Date || value instanceof Timestamp || value instanceof Time) {
                                    value = new java.util.Date(((java.util.Date)value).getTime());
                                }

                                if(f.getType() != String.class && value.getClass() == String.class) {
                                    PropertyEditor editor = PropertyEditorManager.findEditor(f.getType());
                                    editor.setAsText(value.toString());
                                    value = editor.getValue();
                                }

                                f.set(entity, value);
                            }
                        } else {
                            log.warn("类\'{}\'中属性\'{}\'没有对应的数据库字段", entityClass.getSimpleName(), f.getName());
                        }
                    } catch (Throwable var8) {
                        log.error("用数据库返回的数据设置类[{}]字段[{}]异常", new Object[]{entityClass.getSimpleName(), f.getName(), ExceptionUtils.getStackTrace(var8)});
                    }

                });
                return (T) entity;
            }
        } else {
            return null;
        }
    }

    private static JdbcResult insertSql(String table, List<? extends Object> entities, Map<String, String> dbMapping, boolean ignore) {
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "表名不能为空");
        Preconditions.checkArgument(entities != null && entities.size() > 0, "数据库插入数据时entity对象不能为空");
        Preconditions.checkArgument(dbMapping != null && dbMapping.size() > 0, "数据库字段名和实体属性名的对应关系不能为空");
        StringBuilder sql = new StringBuilder("INSERT ");
        if(ignore) {
            sql.append("IGNORE INTO ").append(table);
        } else {
            sql.append("INTO ").append(table);
        }

        ArrayList fieldNames = new ArrayList(dbMapping.keySet());
        sql.append(" (");
        IntStream.range(0, dbMapping.size()).forEach((index) -> {
            if(index != 0) {
                sql.append(",");
            }

            sql.append((String)dbMapping.get(fieldNames.get(index)));
        });
        sql.append(") VALUES (");
        IntStream.range(0, dbMapping.size()).forEach((index) -> {
            if(index != 0) {
                sql.append(",");
            }

            sql.append("?");
        });
        sql.append(")");
        ArrayList paramsList = new ArrayList();
        entities.forEach((entity) -> {
            Object[] params = new Object[dbMapping.size()];
            paramsList.add(params);
            Map entityMap = BaseBeanUtils.beanToMap(entity);
            IntStream.range(0, dbMapping.size()).forEach((index) -> {
                params[index] = entityMap.get(fieldNames.get(index));
            });
        });
        return new JdbcResult(sql.toString(), paramsList);
    }

    private static JdbcResult whereSql(List<? extends Object> criteria) {
        return whereSql(criteria, (Map)null);
    }

    private static JdbcResult whereSql(List<? extends Object> criteria, Map<String, Object> colValues) {
        if(CollectionUtils.isEmpty(criteria)) {
            return new JdbcResult("", new Object[0]);
        } else {
            ArrayList params = new ArrayList();
            StringBuilder sql = new StringBuilder(" WHERE ");
            IntStream.range(0, criteria.size()).forEach((index) -> {
                if(index != 0) {
                    sql.append(" AND ");
                }

                Object criterion = criteria.get(index);
                if(criterion instanceof String) {
                    sql.append(criterion).append("=?");
                    params.add(colValues.get(criterion));
                } else {
                    Criteria criterion1 = (Criteria)criterion;
                    sql.append(whereSql(criterion1));
                    if(criterion1.getType() != Type.IN && criterion1.getType() != Type.NIN) {
                        params.add(criterion1.getValue());
                    } else {
                        params.addAll((List)criterion1.getValue());
                    }
                }

            });
            return new JdbcResult(sql.toString(), params.toArray());
        }
    }

    private static String whereSql(Criteria criterion) {
        StringBuilder sql = new StringBuilder("");
        if(criterion.getType() == Type.EQ) {
            sql.append(criterion.getColName()).append("=?");
        } else if(criterion.getType() == Type.NE) {
            sql.append(criterion.getColName()).append("!=?");
        } else if(criterion.getType() == Type.GT) {
            sql.append(criterion.getColName()).append(">?");
        } else if(criterion.getType() == Type.GE) {
            sql.append(criterion.getColName()).append(">=?");
        } else if(criterion.getType() == Type.LT) {
            sql.append(criterion.getColName()).append("<?");
        } else if(criterion.getType() == Type.LE) {
            sql.append(criterion.getColName()).append("<=?");
        } else if(criterion.getType() != Type.IN && criterion.getType() != Type.NIN) {
            if(criterion.getType() == Type.LIKE) {
                sql.append(criterion.getColName()).append(" LIKE ?");
            } else if(criterion.getType() == Type.IS_NULL) {
                sql.append("ISNULL(").append(criterion.getColName()).append(")");
            } else if(criterion.getType() == Type.IS_NOT_NULL) {
                sql.append(criterion.getColName()).append(" IS NOT NULL");
            }
        } else {
            char[] temp = new char[((List)criterion.getValue()).size()];
            Arrays.fill(temp, '?');
            String innerSql = StringUtils.join(temp, ',');
            if(criterion.getType() == Type.IN) {
                sql.append(criterion.getColName()).append(" IN(").append(innerSql).append(")");
            } else {
                sql.append(criterion.getColName()).append(" NOT IN(").append(innerSql).append(")");
            }
        }

        return sql.toString();
    }

    private static String pageSql(PageRequest pageRequest) {
        if(pageRequest == null) {
            return "";
        } else {
            Integer page = pageRequest.getPage();
            Integer pageSize = pageRequest.getPageSize();
            String pageSql = "";
            if(StringUtils.isNotBlank(pageRequest.getOrderedCol())) {
                pageSql = " ORDER BY " + pageRequest.getOrderedCol() + " " + pageRequest.getOrder().name();
            }

            return pageSql + " LIMIT " + (page.intValue() - 1) * pageSize.intValue() + "," + pageSize;
        }
    }

    private static Map<String, Object> colValues(Object entity, Map<String, String> dbMapping, boolean noneNull) {
        Map<String,Object> entityMap;
        if(noneNull) {
            entityMap = BaseBeanUtils.beanToMapNonNull(entity);
        } else {
            entityMap = BaseBeanUtils.beanToMap(entity);
        }

        HashMap colValues = new HashMap();
        entityMap.entrySet().forEach((entry) -> {
            if(dbMapping.containsKey(entry.getKey())) {
                colValues.put(dbMapping.get(entry.getKey()), entry.getValue());
            }

        });
        return colValues;
    }
}
