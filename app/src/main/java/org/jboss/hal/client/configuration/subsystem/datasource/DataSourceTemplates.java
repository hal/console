/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.datasource.JdbcDriver;

import static org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate.Vendor.DB2;
import static org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate.Vendor.POSTGRE_SQL;
import static org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate.Vendor.SQL_SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** List of well known datasource templates */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class DataSourceTemplates implements Iterable<DataSourceTemplate> {

    private static final String ADMIN = "admin";
    private static final String DATABASE_NAME = "DatabaseName";
    private static final String H2 = "h2";
    private static final String LOCALHOST = "localhost";
    private static final String MYSQL = "mysql";
    private static final String ORACLE = "oracle";
    private static final String POSTGRESQL = "postgresql";
    private static final String SA = "sa";
    private static final String SERVER_NAME = "ServerName";
    private static final String SQLSERVER = "sqlserver";
    private static final String SYBASE = "sybase";

    private final List<DataSourceTemplate> pool;

    public DataSourceTemplates() {
        List<DataSourceTemplate> setup = new ArrayList<>();


        // ------------------------------------------------------ H2

        Supplier<JdbcDriver> h2Driver = () -> {
            JdbcDriver driver = new JdbcDriver(H2);
            driver.get(DRIVER_MODULE_NAME).set("com.h2database.h2");
            driver.get(DRIVER_CLASS_NAME).set("org.h2.Driver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.h2.jdbcx.JdbcDataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(H2, DataSourceTemplate.Vendor.H2,
                () -> {
                    DataSource dataSource = new DataSource("H2DS", false);
                    dataSource.get(JNDI_NAME).set("java:/H2DS");
                    dataSource.get(DRIVER_NAME).set(H2);
                    dataSource.get(CONNECTION_URL).set("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
                    dataSource.get(USER_NAME).set(SA);
                    dataSource.get(PASSWORD).set(SA);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    return dataSource;
                },
                h2Driver));
        setup.add(new DataSourceTemplate("h2-xa", DataSourceTemplate.Vendor.H2,
                () -> {
                    DataSource dataSource = new DataSource("H2XADS", true);
                    dataSource.get(JNDI_NAME).set("java:/H2XADS");
                    dataSource.get(DRIVER_NAME).set(H2);
                    dataSource.get(USER_NAME).set(SA);
                    dataSource.get(PASSWORD).set(SA);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    return dataSource;
                },
                h2Driver,
                properties("URL", "jdbc:h2:mem:test")));


        // ------------------------------------------------------ PostgreSQL

        Supplier<JdbcDriver> postgresDriver = () -> {
            JdbcDriver driver = new JdbcDriver(POSTGRESQL);
            driver.get(DRIVER_MODULE_NAME).set("org.postgresql");
            driver.get(DRIVER_CLASS_NAME).set("org.postgresql.Driver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.postgresql.xa.PGXADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(POSTGRESQL, POSTGRE_SQL,
                () -> {
                    DataSource dataSource = new DataSource("PostgresDS", false);
                    dataSource.get(JNDI_NAME).set("java:/PostgresDS");
                    dataSource.get(DRIVER_NAME).set(POSTGRESQL);
                    dataSource.get(CONNECTION_URL).set("jdbc:postgresql://localhost:5432/postgresdb");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
                    return dataSource;
                },
                postgresDriver));
        setup.add(new DataSourceTemplate("postgresql-xa", POSTGRE_SQL,
                () -> {
                    DataSource dataSource = new DataSource("PostgresXADS", true);
                    dataSource.get(JNDI_NAME).set("java:/PostgresXADS");
                    dataSource.get(DRIVER_NAME).set(POSTGRESQL);
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
                    return dataSource;
                },
                postgresDriver,
                properties(SERVER_NAME, "servername", "PortNumber", "5432", DATABASE_NAME, "postgresdb")));


        // ------------------------------------------------------ MySQL

        Supplier<JdbcDriver> mySqlDriver = () -> {
            JdbcDriver driver = new JdbcDriver(MYSQL);
            driver.get(DRIVER_MODULE_NAME).set("com.mysql");
            driver.get(DRIVER_CLASS_NAME).set("com.mysql.cj.jdbc.Driver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.mysql.cj.jdbc.MysqlXADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(MYSQL, DataSourceTemplate.Vendor.MYSQL,
                () -> {
                    DataSource dataSource = new DataSource("MySqlDS", false);
                    dataSource.get(JNDI_NAME).set("java:/MySqlDS");
                    dataSource.get(DRIVER_NAME).set(MYSQL);
                    dataSource.get(CONNECTION_URL).set("jdbc:mysql://localhost:3306/mysqldb");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
                    return dataSource;
                },
                mySqlDriver));
        setup.add(new DataSourceTemplate("mysql-xa", DataSourceTemplate.Vendor.MYSQL,
                () -> {
                    DataSource dataSource = new DataSource("MysqlXADS", true);
                    dataSource.get(JNDI_NAME).set("java:/MysqlXADS");
                    dataSource.get(DRIVER_NAME).set(MYSQL);
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
                    return dataSource;
                },
                mySqlDriver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "mysqldb")));


        // ------------------------------------------------------ Oracle

        Supplier<JdbcDriver> oracleDriver = () -> {
            JdbcDriver driver = new JdbcDriver(ORACLE);
            driver.get(DRIVER_MODULE_NAME).set("com.oracle");
            driver.get(DRIVER_CLASS_NAME).set("oracle.jdbc.driver.OracleDriver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("oracle.jdbc.xa.client.OracleXADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(ORACLE, DataSourceTemplate.Vendor.ORACLE,
                () -> {
                    DataSource dataSource = new DataSource("OracleDS", false);
                    dataSource.get(JNDI_NAME).set("java:/OracleDS");
                    dataSource.get(DRIVER_NAME).set(ORACLE);
                    dataSource.get(CONNECTION_URL).set("jdbc:oracle:thin:@localhost:1521:orcalesid");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
                    return dataSource;
                },
                oracleDriver));
        setup.add(new DataSourceTemplate("oracle-xa", DataSourceTemplate.Vendor.ORACLE,
                () -> {
                    DataSource dataSource = new DataSource("XAOracleDS", true);
                    dataSource.get(JNDI_NAME).set("java:/XAOracleDS");
                    dataSource.get(DRIVER_NAME).set(ORACLE);
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
                    dataSource.get(NO_TX_SEPARATE_POOL).set(true);
                    dataSource.get(SAME_RM_OVERRIDE).set(false);
                    return dataSource;
                },
                oracleDriver,
                properties("URL", "jdbc:oracle:oci8:@tc")));


        // ------------------------------------------------------ Microsoft SQL Server

        Supplier<JdbcDriver> msSqlDriver = () -> {
            JdbcDriver driver = new JdbcDriver(SQLSERVER);
            driver.get(DRIVER_MODULE_NAME).set("com.microsoft");
            driver.get(DRIVER_CLASS_NAME).set("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(SQLSERVER, SQL_SERVER,
                () -> {
                    DataSource dataSource = new DataSource("MSSQLDS", false);
                    dataSource.get(JNDI_NAME).set("java:/MSSQLDS");
                    dataSource.get(DRIVER_NAME).set(SQLSERVER);
                    dataSource.get(CONNECTION_URL)
                            .set("jdbc:sqlserver://localhost:1433;DatabaseName=MyDatabase");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
                    return dataSource;
                },
                msSqlDriver));
        setup.add(new DataSourceTemplate("sqlserver-xa", SQL_SERVER,
                () -> {
                    DataSource dataSource = new DataSource("MSSQLXADS", true);
                    dataSource.get(JNDI_NAME).set("java:/MSSQLXADS");
                    dataSource.get(DRIVER_NAME).set(SQLSERVER);
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
                    dataSource.get(SAME_RM_OVERRIDE).set(false);
                    return dataSource;
                },
                msSqlDriver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "mssqldb", "SelectMethod", "cursor")));


        // ------------------------------------------------------ DB2

        Supplier<JdbcDriver> db2Driver = () -> {
            JdbcDriver driver = new JdbcDriver("ibmdb2");
            driver.get(DRIVER_MODULE_NAME).set("com.ibm");
            driver.get(DRIVER_CLASS_NAME).set("com.ibm.db2.jcc.DB2Driver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("COM.ibm.db2.jdbc.DB2XADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate("db2", DB2,
                () -> {
                    DataSource dataSource = new DataSource("DB2DS", false);
                    dataSource.get(JNDI_NAME).set("java:/DB2DS");
                    dataSource.get(DRIVER_NAME).set("ibmdb2");
                    dataSource.get(CONNECTION_URL).set("jdbc:db2:yourdatabase");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
                    dataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
                    dataSource.get(MIN_POOL_SIZE).set(0);
                    dataSource.get(MAX_POOL_SIZE).set(50);
                    return dataSource;
                },
                db2Driver));
        setup.add(new DataSourceTemplate("db2-xa", DB2,
                () -> {
                    DataSource dataSource = new DataSource("DB2XADS", true);
                    dataSource.get(JNDI_NAME).set("java:/DB2XADS");
                    dataSource.get(DRIVER_NAME).set("ibmdb2");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
                    dataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
                    dataSource.get(RECOVERY_PLUGIN_CLASS_NAME)
                            .set("org.jboss.jca.core.recovery.ConfigurableRecoveryPlugin");
                    // TODO Add missing recovery plugin properties
                    dataSource.get(SAME_RM_OVERRIDE).set(false);
                    return dataSource;
                },
                db2Driver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "ibmdb2db", "PortNumber", "446")));


        // ------------------------------------------------------ Sybase

        Supplier<JdbcDriver> sybaseDriver = () -> {
            JdbcDriver driver = new JdbcDriver(SYBASE);
            driver.get(DRIVER_MODULE_NAME).set("com.sybase");
            driver.get(DRIVER_CLASS_NAME).set("com.sybase.jdbc.SybDriver");
            driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.sybase.jdbc4.jdbc.SybXADataSource");
            return driver;
        };
        setup.add(new DataSourceTemplate(SYBASE, DataSourceTemplate.Vendor.SYBASE,
                () -> {
                    DataSource dataSource = new DataSource("SybaseDB", false);
                    dataSource.get(JNDI_NAME).set("java:/SybaseDB");
                    dataSource.get(DRIVER_NAME).set(SYBASE);
                    dataSource.get(CONNECTION_URL).set("jdbc:sybase:Tds:localhost:5000/mydatabase?JCONNECT_VERSION=6");
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
                    return dataSource;
                },
                sybaseDriver));
        setup.add(new DataSourceTemplate("sybase-xa", DataSourceTemplate.Vendor.SYBASE,
                () -> {
                    DataSource dataSource = new DataSource("SybaseXADS", true);
                    dataSource.get(JNDI_NAME).set("java:/SybaseXADS");
                    dataSource.get(DRIVER_NAME).set(SYBASE);
                    dataSource.get(USER_NAME).set(ADMIN);
                    dataSource.get(PASSWORD).set(ADMIN);
                    dataSource.get(VALIDATE_ON_MATCH).set(true);
                    dataSource.get(BACKGROUND_VALIDATION).set(false);
                    dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
                    dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                            .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
                    return dataSource;
                },
                sybaseDriver,
                properties("NetworkProtocol", "Tds", SERVER_NAME, LOCALHOST, "PortNumber", "4100", DATABASE_NAME,
                        "mydatabase")));

        pool = Collections.unmodifiableList(setup);
    }

    @Override
    public Iterator<DataSourceTemplate> iterator() {
        return pool.iterator();
    }

    public DataSourceTemplate getTemplate(String id) {
        for (DataSourceTemplate template : this) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        return null;
    }

    private Map<String, String> properties(String... properties) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (int i = 0; i < properties.length; i += 2) {
            builder.put(properties[i], properties[i + 1]);
        }
        return builder.build();
    }
}
