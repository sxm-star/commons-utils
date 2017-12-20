package com.songxm.commons;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.songxm.commons.model.DbInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
@SuppressWarnings("unchecked")
public class BaseDaoUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseDaoUtils.class);
    private static final String NAME = "COLUMN_NAME";
    private static final String TYPE = "TYPE_NAME";
    private static final String REMARKS = "REMARKS";
    private static final Map<String, String> typeMap = new HashMap();
    private static String basePath;
    private static Map<String, List<String>> primaryKeysMap = new HashMap();
    private static Map<String, List<String>> uniqueIndexMap = new HashMap();
    private static Map<String, List<String>> indexMap = new HashMap();
    private static List<String> UPDATE_IGNORE_COLS = Arrays.asList(new String[]{"create_time", "create_at", "createTime", "createAt", "created_at"});
    private static List<String> SQL_KEY_WORDS = Arrays.asList(new String[]{"asc", "desc", "range", "match", "delayed", "alter", "create"});

    public BaseDaoUtils() {
    }

    public static void generateCode(DbInfo dbInfo, List<String> tables, String basePackage) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(dbInfo.getUrl()), "数据库url不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(dbInfo.getUser()), "数据库用户名不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(basePackage), "basePackage不能为空");
        generateCode(dbInfo, tables, basePackage, false);
    }

    public static void generateCode(DbInfo dbInfo, List<String> tables, String basePackage, boolean override) throws Exception {
        DataSource ds = dataSource(dbInfo);
        Connection conn = null;

        try {
            conn = ds.getConnection();
            DatabaseMetaData e = conn.getMetaData();
            generateCode4SpringFactoryFile(basePackage);
            String schema = StringUtils.substringAfterLast(dbInfo.getUrl(), "/");
            schema = StringUtils.substringBefore(schema, "?");
            schema = BaseStringUtils.underScoreToCamel(schema, false);
            generateCode4Config(schema, basePackage);
            generateCode4Aspect(basePackage);
            if(BaseCollectionUtils.isEmpty((Collection)tables)) {
                tables = new ArrayList();
                ResultSet finalSchema = e.getTables((String)null, (String)null, "%", (String[])null);
                if(finalSchema != null) {
                    while(finalSchema.next()) {
                        ((List)tables).add(finalSchema.getString(3));
                    }
                } else {
                    log.error("当前数据库schema下未发现任务表存在");
                }
            }

            final String const_schema = schema;
            tables.forEach((table) -> {
                generateCode4Table(const_schema, e, table, basePackage, override);
            });
        } catch (Throwable var17) {
            log.error("dao代码生成异常", var17);
        } finally {
            try {
                conn.close();
            } catch (Throwable var16) {
                ;
            }

        }

    }

    private static void generateCode4SpringFactoryFile(String basePackage) throws Exception {
        File springFactoryFile = ensureFile(BaseDaoUtils.Type.factory, (String)null, (String)null, false);
        if(springFactoryFile != null) {
            StringBuilder buf = new StringBuilder();
            buf.append("org.springframework.boot.autoconfigure.EnableAutoConfiguration=\\\n");
            buf.append("  ").append(basePackage).append(".").append(BaseDaoUtils.Type.config.name()).append(".DaoConfig");
            FileUtils.writeByteArrayToFile(springFactoryFile, buf.toString().getBytes("UTF-8"));
        }
    }

    private static void generateCode4Config(String schema, String basePackage) throws Exception {
        File configFile = ensureFile(BaseDaoUtils.Type.config, (String)null, basePackage, false);
        String fileContent = Files.asCharSource(configFile, Charsets.UTF_8).read();
        if(!fileContent.contains(schema + ".mysql.url")) {
            StringBuilder buf = new StringBuilder();
            if(fileContent.length() > 0) {
                Iterator var5 = FileUtils.readLines(configFile, "UTF-8").iterator();

                while(var5.hasNext()) {
                    String line = (String)var5.next();
                    if(line.contains("@ConditionalOnProperty")) {
                        int index = line.lastIndexOf("\"");
                        buf.append(line.substring(0, index + 1)).append(", \"").append(schema).append(".mysql.url\"").append(line.substring(index + 1)).append("\n");
                    } else if(line.contains("@PostConstruct")) {
                        buf.deleteCharAt(buf.lastIndexOf("\n"));
                        buf.append("    @Value(\"${").append(schema).append(".mysql.url}\")\n");
                        buf.append("    private String ").append(schema).append("Url;\n\n");
                        buf.append(line).append("\n");
                    } else if(line.contains("private Map<String, Object> dbProps")) {
                        buf.append(generateCode4ConfigOfOneScheme(schema, false));
                        buf.append(line).append("\n");
                    } else {
                        buf.append(line).append("\n");
                    }
                }
            } else {
                buf.append("package ").append(basePackage).append(".config;").append("\n\n");
                buf.append("import com.alibaba.druid.pool.DruidDataSourceFactory;\n");
                buf.append("import lombok.extern.slf4j.Slf4j;\n");
                buf.append("import org.apache.commons.lang3.StringUtils;\n");
                buf.append("import org.apache.commons.lang3.exception.ExceptionUtils;\n");
                buf.append("import org.springframework.beans.factory.annotation.Value;\n");
                buf.append("import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;\n");
                buf.append("import org.springframework.context.annotation.Bean;\n");
                buf.append("import org.springframework.context.annotation.ComponentScan;\n");
                buf.append("import org.springframework.context.annotation.Primary;\n");
                buf.append("import org.springframework.context.annotation.Configuration;\n");
                buf.append("import org.springframework.jdbc.core.JdbcTemplate;\n");
                buf.append("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;\n");
                buf.append("import org.springframework.jdbc.datasource.DataSourceTransactionManager;\n\n");
                buf.append("import javax.annotation.PostConstruct;\n");
                buf.append("import javax.sql.DataSource;\n");
                buf.append("import java.util.HashMap;\n");
                buf.append("import java.util.Map;\n\n");
                buf.append("@Slf4j\n");
                buf.append("@Configuration\n");
                buf.append("@ConditionalOnProperty({\"").append(schema).append(".mysql.url\"})\n");
                buf.append("@ComponentScan(basePackages = {\"").append(basePackage).append(".dao\", \"").append(basePackage).append(".aspect\"})\n");
                buf.append("public class DaoConfig {\n");
                buf.append("    private Map<String, Object> initMap;\n");
                buf.append("    @Value(\"${mysql.user}\")\n");
                buf.append("    private String user;\n");
                buf.append("    @Value(\"${mysql.pass}\")\n");
                buf.append("    private String pass;\n");
                buf.append("    @Value(\"${mysql.max.active:10}\")\n");
                buf.append("    private String maxActive;\n");
                buf.append("    @Value(\"${").append(schema).append(".mysql.url}\")\n");
                buf.append("    private String ").append(schema).append("Url;\n\n");
                buf.append("    @PostConstruct\n");
                buf.append("    public void init() {\n");
                buf.append("        initMap = new HashMap<>(16);\n");
                buf.append("        initMap.put(\"driverClassName\", \"com.mysql.jdbc.Driver\");\n");
                buf.append("        initMap.put(\"initialSize\", \"1\");\n");
                buf.append("        initMap.put(\"minIdle\", \"1\");\n");
                buf.append("        initMap.put(\"maxWait\", \"20000\");\n");
                buf.append("        initMap.put(\"removeAbandoned\", \"true\");\n");
                buf.append("        initMap.put(\"removeAbandonedTimeout\", \"180\");\n");
                buf.append("        initMap.put(\"timeBetweenEvictionRunsMillis\", \"60000\");\n");
                buf.append("        initMap.put(\"minEvictableIdleTimeMillis\", \"300000\");\n");
                buf.append("        initMap.put(\"validationQuery\", \"SELECT 1\");\n");
                buf.append("        initMap.put(\"testWhileIdle\", \"true\");\n");
                buf.append("        initMap.put(\"testOnBorrow\", \"false\");\n");
                buf.append("        initMap.put(\"testOnReturn\", \"false\");\n");
                buf.append("        initMap.put(\"poolPreparedStatements\", \"true\");\n");
                buf.append("        initMap.put(\"maxPoolPreparedStatementPerConnectionSize\", \"50\");\n");
                buf.append("        initMap.put(\"initConnectionSqls\", \"SELECT 1\");\n");
                buf.append("        initMap.put(\"maxActive\", maxActive + \"\");\n");
                buf.append("    }\n\n");
                buf.append(generateCode4ConfigOfOneScheme(schema, true));
                buf.append("    private Map<String, Object> dbProps(String url) {\n");
                buf.append("        Map<String, Object> dbProperties = new HashMap<>(initMap);\n");
                buf.append("        dbProperties.put(\"url\", url);\n");
                buf.append("        dbProperties.put(\"username\", user);\n");
                buf.append("        if (StringUtils.isNotBlank(pass)) {\n");
                buf.append("            dbProperties.put(\"password\", pass);\n");
                buf.append("        }\n");
                buf.append("        return dbProperties;\n");
                buf.append("    }\n");
                buf.append("}");
            }

            FileUtils.writeByteArrayToFile(configFile, buf.toString().getBytes("UTF-8"));
        }
    }

    private static String generateCode4ConfigOfOneScheme(String schema, boolean firstSchema) {
        String schemaFirstUpper = schema.substring(0, 1).toUpperCase() + schema.substring(1);
        StringBuilder buf = new StringBuilder();
        if(firstSchema) {
            buf.append("    @Primary\n");
        }

        buf.append("    @Bean(name = \"ds").append(schemaFirstUpper).append("\")\n");
        buf.append("    public DataSource ds").append(schemaFirstUpper).append("() {\n");
        buf.append("        log.info(\"初始化").append(schema).append("数据源\");\n");
        buf.append("        try {\n");
        buf.append("            return DruidDataSourceFactory.createDataSource(dbProps(").append(schema).append("Url));\n");
        buf.append("        } catch (Exception e) {\n");
        buf.append("            log.error(\"无法获得数据源[{}]:{}\", ").append(schema).append("Url, ExceptionUtils.getStackTrace(e));\n");
        buf.append("            throw new RuntimeException(\"无法获得数据源\");\n");
        buf.append("        }\n");
        buf.append("    }\n\n");
        buf.append("    @Bean(name = \"template").append(schemaFirstUpper).append("\")\n");
        buf.append("    public JdbcTemplate template").append(schemaFirstUpper).append("() {\n");
        buf.append("        return new JdbcTemplate(this.ds").append(schemaFirstUpper).append("());\n");
        buf.append("    }\n\n");
        buf.append("    @Bean(name = \"namedTemplate").append(schemaFirstUpper).append("\")\n");
        buf.append("    public NamedParameterJdbcTemplate namedTemplate").append(schemaFirstUpper).append("() {\n");
        buf.append("        return new NamedParameterJdbcTemplate(this.ds").append(schemaFirstUpper).append("());\n");
        buf.append("    }\n\n");
        buf.append("    @Bean(name = \"tm").append(schemaFirstUpper).append("\")\n");
        buf.append("    public DataSourceTransactionManager ts").append(schemaFirstUpper).append("() {\n");
        buf.append("        return new DataSourceTransactionManager(this.ds").append(schemaFirstUpper).append("());\n");
        buf.append("    }\n\n");
        return buf.toString();
    }

    private static void generateCode4Table(String schema, DatabaseMetaData meta, String table, String basePackage, boolean override) {
        try {
            ResultSet e = meta.getColumns((String)null, (String)null, table, (String)null);
            if(e != null) {
                LinkedHashMap nameTypes = new LinkedHashMap();
                HashMap remarks = new HashMap();
                ArrayList errorList = new ArrayList();

                while(e.next()) {
                    String primaryKeyRs = e.getString("COLUMN_NAME");
                    if(SQL_KEY_WORDS.contains(primaryKeyRs.toLowerCase())) {
                        errorList.add("表中含数据库关键字:" + primaryKeyRs);
                    }

                    nameTypes.put(primaryKeyRs, e.getString("TYPE_NAME"));
                    remarks.put(primaryKeyRs, e.getString("REMARKS"));
                }

                if(BaseCollectionUtils.isNotEmpty(errorList)) {
                    throw new Exception("异常:" + errorList);
                }

                ResultSet primaryKeyRs1 = meta.getPrimaryKeys((String)null, (String)null, table);

                String primaryKeyName;
                List<String> indexInfoRs;
                for(primaryKeyName = null; primaryKeyRs1.next(); BaseCollectionUtils.add((List)indexInfoRs, primaryKeyRs1.getShort("KEY_SEQ") - 1, primaryKeyRs1.getString("COLUMN_NAME"))) {
                    if(primaryKeyName == null) {
                        primaryKeyName = StringUtils.defaultString(primaryKeyRs1.getString("PK_NAME"), "primary");
                    }

                    indexInfoRs = (List)primaryKeysMap.get(table);
                    if(indexInfoRs == null) {
                        indexInfoRs = new ArrayList();
                        primaryKeysMap.put(table, indexInfoRs);
                    }
                }

                ResultSet indexInfoRs1 = meta.getIndexInfo((String)null, (String)null, table, false, false);
                String uniqueIndexName = null;
                String otherIndexName = null;

                while(true) {
                    String entityName;
                    boolean unique;
                    List<String> indexCols;
                    do {
                        do {
                            do {
                                do {
                                    if(!indexInfoRs1.next()) {
                                        entityName = entityName(table);
                                        generateEntityFile(table, entityName, basePackage, nameTypes, remarks, override);
                                        generateDaoFile(schema, entityName, basePackage, table, nameTypes, override);
                                        return;
                                    }

                                    entityName = indexInfoRs1.getString("INDEX_NAME");
                                } while(entityName == null);
                            } while(entityName.equals(primaryKeyName));

                            unique = !indexInfoRs1.getBoolean("NON_UNIQUE");
                            List<String> uniqueCols = (List)uniqueIndexMap.get(table);
                            if(unique && (uniqueCols == null || entityName.equalsIgnoreCase(uniqueIndexName))) {
                                if(uniqueCols == null) {
                                    uniqueCols = new ArrayList();
                                    uniqueIndexMap.put(table, uniqueCols);
                                }

                                uniqueIndexName = entityName;
                                BaseCollectionUtils.add((List)uniqueCols, indexInfoRs1.getShort("ORDINAL_POSITION") - 1, indexInfoRs1.getString("COLUMN_NAME"));
                            }

                            indexCols = (List)indexMap.get(table);
                        } while(unique);
                    } while(indexCols != null && !entityName.equalsIgnoreCase(otherIndexName));

                    if(indexCols == null) {
                        indexCols = new ArrayList(8);
                        indexMap.put(table, indexCols);
                    }

                    otherIndexName = entityName;
                    BaseCollectionUtils.add((List)indexCols, indexInfoRs1.getShort("ORDINAL_POSITION") - 1, indexInfoRs1.getString("COLUMN_NAME"));
                }
            }
        } catch (Throwable var18) {
            log.error("为表[{}]生成dao代码时异常: {}", table, ExceptionUtils.getStackTrace(var18));
        }

    }

    private static void generateCode4Aspect(String basePackage) throws Exception {
        File aspectFile = ensureFile(BaseDaoUtils.Type.aspect, (String)null, basePackage, false);
        if(aspectFile != null) {
            StringBuilder buf = new StringBuilder();
            buf.append("package ").append(basePackage).append(".aspect;\n\n");
            buf.append("import com.google.common.collect.ImmutableMap;\n");
            buf.append("import com.moxie.cloud.commons.constant.MetricsConst;\n");
            buf.append("import com.moxie.cloud.commons.metrics.BaseCounter;\n");
            buf.append("import com.moxie.cloud.commons.metrics.BaseGauge;\n");
            buf.append("import com.moxie.commons.BaseAspectUtils;\n");
            buf.append("import org.aspectj.lang.ProceedingJoinPoint;\n");
            buf.append("import org.aspectj.lang.annotation.Around;\n");
            buf.append("import org.aspectj.lang.annotation.Aspect;\n");
            buf.append("import org.aspectj.lang.reflect.MethodSignature;\n");
            buf.append("import org.springframework.beans.factory.annotation.Autowired;\n");
            buf.append("import org.springframework.stereotype.Component;\n\n");
            buf.append("import java.lang.reflect.Method;\n\n");
            buf.append("@Component\n");
            buf.append("@Aspect\n");
            buf.append("public class DaoAspect {\n");
            buf.append("    @Autowired(required = false)\n");
            buf.append("    private BaseCounter baseCounter;\n");
            buf.append("    @Autowired(required = false)\n");
            buf.append("    private BaseGauge baseGauge;\n\n");
            buf.append("    @Around(\"execution(public * ").append(basePackage).append(".dao.*Dao.*(..))\")\n");
            buf.append("    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {\n");
            buf.append("        long start = System.currentTimeMillis();\n");
            buf.append("        try {\n");
            buf.append("            return BaseAspectUtils.logAround(joinPoint, 300L);\n");
            buf.append("        } finally {\n");
            buf.append("            if (baseCounter != null && baseGauge != null) {\n");
            buf.append("                long duration = System.currentTimeMillis() - start;\n");
            buf.append("                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();\n");
            buf.append("                String methodInfo = method.getDeclaringClass().getSimpleName() + \".\" + method.getName();\n");
            buf.append("                baseCounter.inc(MetricsConst.MYSQL_COUNT, 1, ImmutableMap.of(MetricsConst.METHOD, methodInfo));\n");
            buf.append("                baseGauge.set(MetricsConst.MYSQL_DURATION, duration, ImmutableMap.of(MetricsConst.METHOD, methodInfo));\n");
            buf.append("            }\n");
            buf.append("        }\n");
            buf.append("    }\n");
            buf.append("}");
            FileUtils.writeByteArrayToFile(aspectFile, buf.toString().getBytes("UTF-8"));
        }
    }

    private static void generateEntityFile(String table, String entityName, String basePackage, Map<String, String> nameTypes, Map<String, String> remarks, boolean override) throws Exception {
        File file = ensureFile(BaseDaoUtils.Type.po, entityName, basePackage, override);
        if(file != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(entityContent(table, basePackage, entityName, nameTypes, remarks).getBytes("UTF-8"));
            FileUtils.copyInputStreamToFile(in, file);
        }

    }

    private static void generateDaoFile(String schema, String entityName, String basePackage, String table, Map<String, String> nameTypes, boolean override) throws Exception {
        File file = ensureFile(BaseDaoUtils.Type.dao, entityName, basePackage, override);
        if(file != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(daoContent(schema, basePackage, entityName, table, nameTypes).getBytes("UTF-8"));
            FileUtils.copyInputStreamToFile(in, file);
        }

    }

    private static File ensureFile(BaseDaoUtils.Type type, String entityName, String basePackage, boolean override) throws Exception {
        File file;
        File dir;
        if(type == BaseDaoUtils.Type.factory) {
            dir = new File(basePath + "/src/main/resources/META-INF");
            FileUtils.forceMkdir(dir);
            file = new File(dir, "spring.factories");
            if(file.exists()) {
                return null;
            }
        } else {
            dir = new File(basePath + "/src/main/java/" + basePackage.replace('.', '/') + "/" + type.name());
            FileUtils.forceMkdir(dir);
            if(type != BaseDaoUtils.Type.dao && type != BaseDaoUtils.Type.po) {
                if(type == BaseDaoUtils.Type.aspect) {
                    file = new File(dir, "DaoAspect.java");
                    if(file.exists()) {
                        return null;
                    }
                } else {
                    file = new File(dir, "DaoConfig.java");
                }
            } else {
                String suffix = type == BaseDaoUtils.Type.dao?"Dao":"Po";
                file = new File(dir, entityName + suffix + ".java");
                if(!override && file.exists()) {
                    return null;
                }

                if(file.exists()) {
                    FileUtils.forceDelete(file);
                }
            }
        }

        FileUtils.touch(file);
        return file;
    }

    private static String entityContent(String table, String basePackage, String entityName, Map<String, String> nameTypes, Map<String, String> remarks) {
        List primaryKeys = (List)primaryKeysMap.get(table);
        List uniqueKeys = (List)uniqueIndexMap.get(table);
        StringBuilder bufHeader = (new StringBuilder("package ")).append(basePackage).append(".po;").append("\n\n");
        bufHeader.append("import lombok.Data;").append("\n\n");
        AtomicBoolean containAnno = new AtomicBoolean(false);
        AtomicBoolean containBigDecimal = new AtomicBoolean(false);
        StringBuilder buf = new StringBuilder();
        buf.append("import java.util.*;").append("\n\n");
        buf.append("@Data").append("\n");
        buf.append("public class ").append(entityName).append("Po {").append("\n");
        nameTypes.entrySet().forEach((entry) -> {
            if(StringUtils.isNotBlank((CharSequence)remarks.get(entry.getKey()))) {
                buf.append("    /**\n");
                buf.append("     * ").append((String)remarks.get(entry.getKey())).append("\n");
                buf.append("     */\n");
            }

            if(BaseCollectionUtils.contains(primaryKeys, entry.getKey())) {
                buf.append("    @PrimaryKey\n");
                containAnno.set(true);
            } else if(BaseCollectionUtils.contains(uniqueKeys, entry.getKey())) {
                buf.append("    @UniqueIndex\n");
                containAnno.set(true);
            } else if(BaseCollectionUtils.containsIgnoreCase(UPDATE_IGNORE_COLS, (String)entry.getKey())) {
                buf.append("    @UpdateIgnore\n");
                containAnno.set(true);
            }

            String javaType = javaType((String)entry.getValue());
            buf.append("    private ").append(javaType).append(" ").append(toCamel((String)entry.getKey(), false)).append(";\n");
            if("BigDecimal".equalsIgnoreCase(javaType)) {
                containBigDecimal.set(true);
            }

        });
        buf.append("}");
        if(containAnno.get()) {
            bufHeader.append("import com.moxie.commons.annotation.*;\n");
        }

        if(containBigDecimal.get()) {
            bufHeader.append("import java.math.BigDecimal;\n");
        }

        return bufHeader.toString() + buf.toString();
    }

    private static String daoContent(String schema, String basePackage, String entityName, String table, Map<String, String> nameTypes) {
        String schemaFirstUpper = schema.substring(0, 1).toUpperCase() + schema.substring(1);
        String entityCamelName = toCamel(entityName, false);
        StringBuilder buf = (new StringBuilder("package ")).append(basePackage).append(".dao;").append("\n\n");
        buf.append("import com.moxie.commons.BaseJdbcUtils;").append("\n");
        buf.append("import com.moxie.commons.model.*;").append("\n");
        buf.append("import " + basePackage + ".po.").append(entityName).append("Po;").append("\n");
        buf.append("import org.springframework.dao.EmptyResultDataAccessException;").append("\n");
        buf.append("import org.springframework.jdbc.core.JdbcTemplate;").append("\n");
        buf.append("import org.springframework.stereotype.Repository;").append("\n\n");
        buf.append("import javax.annotation.PostConstruct;").append("\n");
        buf.append("import javax.annotation.Resource;").append("\n");
        buf.append("import java.util.*;").append("\n");
        buf.append("import java.util.stream.Collectors;").append("\n");
        buf.append("import java.util.stream.IntStream;").append("\n\n");
        buf.append("@Repository").append("\n");
        buf.append("public class ").append(entityName).append("Dao {").append("\n");
        buf.append("    private final static String TABLE_NAME = \"").append(table).append("\";\n");
        buf.append("    private Map<String, String> dbMapping = new HashMap<>();").append("\n");
        buf.append("    @Resource(name = \"template").append(schemaFirstUpper).append("\")").append("\n");
        buf.append("    private JdbcTemplate template;").append("\n\n");
        HashMap dbMapping = new HashMap();
        buf.append("    @PostConstruct").append("\n");
        buf.append("    public void init() {").append("\n");
        nameTypes.entrySet().forEach((entry) -> {
            buf.append("        dbMapping.put(\"").append(toCamel((String)entry.getKey(), false)).append("\", \"").append((String)entry.getKey()).append("\");\n");
            dbMapping.put(toCamel((String)entry.getKey(), false), entry.getKey());
        });
        buf.append("    }\n\n");
        buf.append("    public boolean insert(").append(entityName).append("Po ").append(ensure(entityCamelName)).append(") {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getInsert(getTable(), ").append(ensure(entityCamelName)).append(", dbMapping);\n");
        buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams()) == 1;\n");
        buf.append("    }\n\n");
        if(primaryKeysMap.containsKey(table)) {
            buf.append("    public boolean insertIgnore(").append(entityName).append("Po ").append(ensure(entityCamelName)).append(") {\n");
            buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getInsertIgnore(getTable(), ").append(ensure(entityCamelName)).append(", dbMapping);\n");
            buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams()) == 1;\n");
            buf.append("    }\n\n");
            buf.append("    /**\n");
            buf.append("     * @return true when insert\n");
            buf.append("     */\n");
            buf.append("    public boolean insertOrUpdate(").append(entityName).append("Po ").append(ensure(entityCamelName)).append(") {\n");
            buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getInsertOrUpdate(getTable(), ").append(ensure(entityCamelName)).append(", dbMapping);\n");
            buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams()) == 1;\n");
            buf.append("    }\n\n");
        }

        buf.append("    public int batchInsert(List<").append(entityName).append("Po> ").append(entityCamelName).append("s) {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getBatchInsert(getTable(), ").append(entityCamelName).append("s, dbMapping);\n");
        buf.append("        return IntStream.of(template.batchUpdate(jdbcResult.getSql(), jdbcResult.getBatchParams())).sum();\n");
        buf.append("    }\n\n");
        buf.append("    public boolean update(").append(entityName).append("Po ").append(ensure(entityCamelName)).append(") {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getUpdate(getTable(), ").append(ensure(entityCamelName)).append(", dbMapping, ").append(primaryKeySimpleArgs(table, nameTypes)).append(");\n");
        buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams()) == 1;\n");
        buf.append("    }\n\n");
        buf.append("    public boolean patch(").append(entityName).append("Po ").append(ensure(entityCamelName)).append(") {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getPatch(getTable(), ").append(ensure(entityCamelName)).append(", dbMapping, ").append(primaryKeySimpleArgs(table, nameTypes)).append(");\n");
        buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams()) == 1;\n");
        buf.append("    }\n\n");
        if(BaseCollectionUtils.isNotEmpty((Collection)primaryKeysMap.get(table))) {
            buf.append(getByCols((List)primaryKeysMap.get(table), nameTypes, entityName, true));
        }

        if(BaseCollectionUtils.isNotEmpty((Collection)uniqueIndexMap.get(table))) {
            buf.append(getByCols((List)uniqueIndexMap.get(table), nameTypes, entityName, false));
        }

        List keyCols;
        if(indexMap.containsKey(table) || primaryKeysMap.containsKey(table) && ((List)primaryKeysMap.get(table)).size() > 1 || uniqueIndexMap.containsKey(table) && ((List)uniqueIndexMap.get(table)).size() > 1) {
            keyCols = (List)indexMap.get(table);
            if(keyCols == null && primaryKeysMap.containsKey(table) && ((List)primaryKeysMap.get(table)).size() > 1) {
                keyCols = Arrays.asList(new String[]{(String)((List)primaryKeysMap.get(table)).get(0)});
            } else if(keyCols == null) {
                keyCols = Arrays.asList(new String[]{(String)((List)uniqueIndexMap.get(table)).get(0)});
            }

            buf.append("    public List<").append(entityName).append("Po> list(").append(queryArgs(keyCols, nameTypes)).append(") {\n");
            buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getSelect(getTable(), ");
            buf.append(criteriaArgs(16, keyCols)).append(");\n");
            buf.append("        return template.queryForList(jdbcResult.getSql(), jdbcResult.getParams()).stream()\n");
            buf.append("                ").append(".map(dbRow -> BaseJdbcUtils.dbRowToPo(dbRow, dbMapping, ").append(entityName).append("Po.class))\n");
            buf.append("                ").append(".collect(Collectors.toList());\n");
            buf.append("    }\n\n");
        }

        String primaryKeyArgs;
        if(primaryKeysMap.containsKey(table)) {
            String keyCols1 = ensure(entityCamelName);
            primaryKeyArgs = getArgs(keyCols1, uniqueIndexMap.containsKey(table)?(List)uniqueIndexMap.get(table):(List)primaryKeysMap.get(table), dbMapping);
            buf.append("    public ").append(entityName).append("Po getOrInsert(").append(entityName).append("Po ").append(keyCols1).append(") {\n");
            buf.append("        ").append(entityName).append("Po po = this.get").append(uniqueIndexMap.containsKey(table)?"ByIndex":"").append("(").append(primaryKeyArgs).append(");\n");
            buf.append("        if (po == null) {\n");
            buf.append("            if (!this.insertIgnore(").append(keyCols1).append(")) {\n");
            buf.append("                return this.get").append(uniqueIndexMap.containsKey(table)?"ByIndex":"").append("(").append(primaryKeyArgs).append(");\n");
            buf.append("            }\n");
            buf.append("            return ").append(keyCols1).append(";\n");
            buf.append("        }\n");
            buf.append("        return po;\n");
            buf.append("    }\n\n");
            if(uniqueIndexMap.containsKey(table)) {
                buf.append("    public ").append(entityName).append("Po getAfterPut(").append(entityName).append("Po ").append(keyCols1).append(") {\n");
                buf.append("        if (this.insertOrUpdate(").append(keyCols1).append(")) {\n");
                buf.append("            return ").append(keyCols1).append(";\n");
                buf.append("        } else {\n");
                buf.append("            return this.getByIndex(").append(primaryKeyArgs).append(");\n");
                buf.append("        }\n");
                buf.append("    }\n\n");
            }
        }

        buf.append("    public PageResponse<").append(entityName).append("Po> getPage(PageRequest pageRequest) {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getSelectForCount(getTable(), (Criteria) null);\n");
        buf.append("        Integer total = template.queryForObject(jdbcResult.getSql(), jdbcResult.getParams(), Integer.class);\n");
        buf.append("        if(total == 0) {\n");
        buf.append("            return new PageResponse<>(0, null);\n");
        buf.append("        }\n\n");
        buf.append("        jdbcResult = BaseJdbcUtils.getSelect(getTable(), (Criteria) null, pageRequest);\n");
        buf.append("        List<").append(entityName).append("Po> datas = template.queryForList(jdbcResult.getSql(), jdbcResult.getParams()).stream()\n");
        buf.append("                .map(dbRow -> BaseJdbcUtils.dbRowToPo(dbRow, dbMapping, ").append(entityName).append("Po.class))\n");
        buf.append("                .collect(Collectors.toList());\n");
        buf.append("        return new PageResponse<>(total, datas);\n");
        buf.append("    }\n\n");
        keyCols = primaryKeys(table, nameTypes);
        primaryKeyArgs = keyCols == null?"String id":queryArgs(keyCols, nameTypes);
        buf.append("    public int delete(").append(primaryKeyArgs).append(") {\n");
        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getDelete(getTable(), ").append(criteriaArgs(16, keyCols)).append(");\n");
        buf.append("        return template.update(jdbcResult.getSql(), jdbcResult.getParams());\n");
        buf.append("    }\n\n");
        buf.append("    private String getTable() {\n");
        buf.append("        return TABLE_NAME;\n");
        buf.append("    }\n");
        buf.append("}");
        return buf.toString();
    }

    private static String getByCols(List<String> keyCols, Map<String, String> nameTypes, String entityName, boolean primaryKey) {
        String keyArgs = keyCols == null?"String id":queryArgs(keyCols, nameTypes);
        StringBuilder buf = new StringBuilder();
        if(primaryKey) {
            buf.append("    public ").append(entityName).append("Po get(").append(keyArgs).append(") {\n");
        } else {
            buf.append("    public ").append(entityName).append("Po getByIndex(").append(keyArgs).append(") {\n");
        }

        buf.append("        JdbcResult jdbcResult = BaseJdbcUtils.getSelect(getTable(), ");
        buf.append(criteriaArgs(16, keyCols)).append(");\n");
        buf.append("        try {\n");
        buf.append("            Map<String, Object> dbRow = template.queryForMap(jdbcResult.getSql(), jdbcResult.getParams());\n");
        buf.append("            return BaseJdbcUtils.dbRowToPo(dbRow, dbMapping, ").append(entityName).append("Po.class);\n");
        buf.append("        } catch (EmptyResultDataAccessException e) {\n");
        buf.append("            return null;\n");
        buf.append("        }\n");
        buf.append("    }\n\n");
        return buf.toString();
    }

    private static String javaType(String sqlType) {
        sqlType = sqlType.toUpperCase();
        if(sqlType.contains("UNSIGNED")) {
            sqlType = sqlType.replace("UNSIGNED", "").trim();
        }

        if(typeMap.containsKey(sqlType.toUpperCase())) {
            return (String)typeMap.get(sqlType.toUpperCase());
        } else {
            log.warn("无法识别的mysql数据类型[{}]将映射为String", sqlType);
            return "String";
        }
    }

    private static String toCamel(String src, boolean firstUpper) {
        return BaseStringUtils.underScoreToCamel(src, firstUpper);
    }

    private static String entityName(String table) {
        int index = table.indexOf("t_");
        if(index == 0) {
            table = table.substring(2);
        }

        index = table.indexOf("T_");
        if(index == 0) {
            table = table.substring(2);
        }

        table = table.replaceAll("_\\d{1,}$", "");
        return toCamel(table, true);
    }

    private static DataSource dataSource(DbInfo dbInfo) throws Exception {
        HashMap datasourceMap = new HashMap();
        datasourceMap.put("url", dbInfo.getUrl());
        datasourceMap.put("username", dbInfo.getUser());
        if(StringUtils.isNotBlank(dbInfo.getPass())) {
            datasourceMap.put("password", dbInfo.getPass());
        }

        return DruidDataSourceFactory.createDataSource(datasourceMap);
    }

    private static String criteriaArgs(int space, List<String> cols) {
        StringBuilder buf = new StringBuilder();
        if(BaseCollectionUtils.isEmpty(cols)) {
            buf.append("Criteria.column(\"").append("id").append("\").eq(").append("id").append(")");
        } else if(cols.size() == 1) {
            buf.append("Criteria.column(\"").append((String)cols.get(0)).append("\").eq(").append(toCamel((String)cols.get(0), false)).append(")");
        } else {
            buf.append("CriteriaBuilder.newBuilder()\n");
            IntStream.range(0, cols.size()).forEach((index) -> {
                buf.append(StringUtils.repeat(" ", space)).append(".column(\"").append((String)cols.get(index)).append("\").eq(").append(toCamel((String)cols.get(index), false)).append(")\n");
            });
            buf.append(StringUtils.repeat(" ", space)).append(".build()");
        }

        return buf.toString();
    }

    private static List<String> primaryKeys(String table, Map<String, String> nameTypes) {
        return BaseCollectionUtils.isNotEmpty((Collection)primaryKeysMap.get(table))?(List)primaryKeysMap.get(table):(BaseCollectionUtils.isNotEmpty((Collection)uniqueIndexMap.get(table))?(List)uniqueIndexMap.get(table):(BaseCollectionUtils.isNotEmpty((Collection)indexMap.get(table))?(List)indexMap.get(table):Arrays.asList(new String[]{(String)((Entry)nameTypes.entrySet().stream().filter((entry) -> {
            return StringUtils.isNotBlank((CharSequence)entry.getKey());
        }).findFirst().get()).getKey()})));
    }

    private static String primaryKeySimpleArgs(String table, Map<String, String> nameTypes) {
        StringBuilder buf = new StringBuilder();
        List cols = primaryKeys(table, nameTypes);
        IntStream.range(0, cols.size()).forEach((index) -> {
            if(index != 0) {
                buf.append(", ");
            }

            buf.append("\"").append((String)cols.get(index)).append("\"");
        });
        return buf.toString();
    }

    private static String primaryKeyArgs(String table, Map<String, String> nameTypes) {
        List cols;
        if(BaseCollectionUtils.isNotEmpty((Collection)primaryKeysMap.get(table))) {
            cols = (List)primaryKeysMap.get(table);
        } else {
            if(!BaseCollectionUtils.isNotEmpty((Collection)uniqueIndexMap.get(table))) {
                return "String id";
            }

            cols = (List)uniqueIndexMap.get(table);
        }

        return queryArgs(cols, nameTypes);
    }

    private static String queryArgs(List<String> cols, Map<String, String> nameTypes) {
        StringBuilder buf = new StringBuilder();
        IntStream.range(0, cols.size()).forEach((index) -> {
            if(index != 0) {
                buf.append(", ");
            }

            String colName = (String)cols.get(index);
            buf.append(javaType((String)nameTypes.get(colName))).append(" ").append(toCamel(colName, false));
        });
        return buf.toString();
    }

    private static String getArgs(String prefix, List<String> cols, Map<String, String> dbMapping) {
        StringBuilder buf = new StringBuilder();
        IntStream.range(0, cols.size()).forEach((index) -> {
            if(index != 0) {
                buf.append(", ");
            }

            buf.append(prefix).append(".get");
            String colName = (String)cols.get(index);
            Entry entry = (Entry)dbMapping.entrySet().stream().filter((item) -> {
                return ((String)item.getValue()).equals(colName);
            }).findAny().orElse(null);
            String fieldName = (String)entry.getKey();
            buf.append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1)).append("()");
        });
        return buf.toString();
    }

    private static String ensure(String entityCamelName) {
        return entityCamelName.equals("case")?"casePo":entityCamelName;
    }

    static {
        URL baseUrl = BaseDaoUtils.class.getClassLoader().getResource(".");

        try {
            basePath = URLDecoder.decode(baseUrl.getPath(), "UTF-8");
            basePath = StringUtils.substringBefore(basePath, "/target");
        } catch (Exception var2) {
            log.error("获取项目路径异常", var2);
        }

        typeMap.put("CHAR", "String");
        typeMap.put("VARCHAR", "String");
        typeMap.put("BLOB", "String");
        typeMap.put("MEDIUMBLOB", "String");
        typeMap.put("LONGBLOB", "String");
        typeMap.put("TEXT", "String");
        typeMap.put("TINYTEXT", "String");
        typeMap.put("MEDIUMTEXT", "String");
        typeMap.put("LONGTEXT", "String");
        typeMap.put("ENUM", "String");
        typeMap.put("SET", "String");
        typeMap.put("FLOAT", "BigDecimal");
        typeMap.put("REAL", "BigDecimal");
        typeMap.put("DOUBLE", "BigDecimal");
        typeMap.put("NUMERIC", "BigDecimal");
        typeMap.put("DECIMAL", "BigDecimal");
        typeMap.put("BOOLEAN", "Boolean");
        typeMap.put("BIT", "Boolean");
        typeMap.put("TINYINT", "Integer");
        typeMap.put("SMALLINT", "Integer");
        typeMap.put("MEDIUMINT", "Integer");
        typeMap.put("INT", "Integer");
        typeMap.put("INTEGER", "Integer");
        typeMap.put("INT UNSIGNED", "Integer");
        typeMap.put("BIGINT", "Long");
        typeMap.put("DATE", "Date");
        typeMap.put("TIME", "Date");
        typeMap.put("DATETIME", "Date");
        typeMap.put("TIMESTAMP", "Date");
    }

    static enum Type {
        dao,
        po,
        config,
        aspect,
        factory;

        private Type() {
        }
    }
}
