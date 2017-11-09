/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
        DataSource dataSource;
        DataSource xaDataSource;
        JdbcDriver driver;
        List<DataSourceTemplate> setup = new ArrayList<>();


        // ------------------------------------------------------ H2
        // Driver
        driver = new JdbcDriver(H2);
        driver.get(DRIVER_MODULE_NAME).set("com.h2database.h2");
        driver.get(DRIVER_CLASS_NAME).set("org.h2.Driver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.h2.jdbcx.JdbcDataSource");

        // DS
        dataSource = new DataSource("H2DS", false);
        dataSource.get(POOL_NAME).set("H2DS_Pool");
        dataSource.get(JNDI_NAME).set("java:/H2DS");
        dataSource.get(DRIVER_NAME).set(H2);
        dataSource.get(CONNECTION_URL).set("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.get(USER_NAME).set(SA);
        dataSource.get(PASSWORD).set(SA);
        dataSource.get(BACKGROUND_VALIDATION).set(false);
        setup.add(new DataSourceTemplate(H2, DataSourceTemplate.Vendor.H2, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("H2XADS", true);
        xaDataSource.get(POOL_NAME).set("H2XADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/H2XADS");
        xaDataSource.get(DRIVER_NAME).set(H2);
        xaDataSource.get(USER_NAME).set(SA);
        xaDataSource.get(PASSWORD).set(SA);
        xaDataSource.get(BACKGROUND_VALIDATION).set(false);
        setup.add(new DataSourceTemplate("h2-xa", DataSourceTemplate.Vendor.H2, xaDataSource, driver,
                properties("URL", "jdbc:h2:mem:test")));


        // ------------------------------------------------------ PostgreSQL
        // Driver
        driver = new JdbcDriver(POSTGRESQL);
        driver.get(DRIVER_MODULE_NAME).set("org.postgresql");
        driver.get(DRIVER_CLASS_NAME).set("org.postgresql.Driver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.postgresql.xa.PGXADataSource");

        // DS
        dataSource = new DataSource("PostgresDS", false);
        dataSource.get(POOL_NAME).set("PostgresDS_Pool");
        dataSource.get(JNDI_NAME).set("java:/PostgresDS");
        dataSource.get(DRIVER_NAME).set(POSTGRESQL);
        dataSource.get(CONNECTION_URL).set("jdbc:postgresql://localhost:5432/postgresdb");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
        dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
        setup.add(new DataSourceTemplate(POSTGRESQL, POSTGRE_SQL, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("PostgresXADS", true);
        xaDataSource.get(POOL_NAME).set("PostgresXADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/PostgresXADS");
        xaDataSource.get(DRIVER_NAME).set(POSTGRESQL);
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
        xaDataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
        setup.add(new DataSourceTemplate("postgresql-xa", POSTGRE_SQL, xaDataSource, driver,
                properties(SERVER_NAME, "servername", "PortNumber", "5432", DATABASE_NAME, "postgresdb")));


        // ------------------------------------------------------ MySQL
        // Driver
        driver = new JdbcDriver(MYSQL);
        driver.get(DRIVER_MODULE_NAME).set("com.mysql");
        driver.get(DRIVER_CLASS_NAME).set("com.mysql.jdbc.Driver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");

        // DS
        dataSource = new DataSource("MySqlDS", false);
        dataSource.get(POOL_NAME).set("MySqlDS_Pool");
        dataSource.get(JNDI_NAME).set("java:/MySqlDS");
        dataSource.get(DRIVER_NAME).set(MYSQL);
        dataSource.get(CONNECTION_URL).set("jdbc:mysql://localhost:3306/mysqldb");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
        dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
        setup.add(new DataSourceTemplate(MYSQL, DataSourceTemplate.Vendor.MYSQL, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("MysqlXADS", true);
        xaDataSource.get(POOL_NAME).set("MysqlXADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/MysqlXADS");
        xaDataSource.get(DRIVER_NAME).set(MYSQL);
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
        xaDataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
        setup.add(new DataSourceTemplate("mysql-xa", DataSourceTemplate.Vendor.MYSQL, xaDataSource, driver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "mysqldb")));


        // ------------------------------------------------------ Oracle
        // Driver
        driver = new JdbcDriver(ORACLE);
        driver.get(DRIVER_MODULE_NAME).set("com.oracle");
        driver.get(DRIVER_CLASS_NAME).set("oracle.jdbc.driver.OracleDriver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("oracle.jdbc.xa.client.OracleXADataSource");

        // DS
        dataSource = new DataSource("OracleDS", false);
        dataSource.get(POOL_NAME).set("OracleDS_Pool");
        dataSource.get(JNDI_NAME).set("java:/OracleDS");
        dataSource.get(DRIVER_NAME).set(ORACLE);
        dataSource.get(CONNECTION_URL).set("jdbc:oracle:thin:@localhost:1521:orcalesid");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
        dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
        dataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker");
        setup.add(new DataSourceTemplate(ORACLE, DataSourceTemplate.Vendor.ORACLE, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("XAOracleDS", true);
        xaDataSource.get(POOL_NAME).set("XAOracleDS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/XAOracleDS");
        xaDataSource.get(DRIVER_NAME).set(ORACLE);
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
        xaDataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
        xaDataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker");
        xaDataSource.get(NO_TX_SEPARATE_POOL).set(true);
        xaDataSource.get(SAME_RM_OVERRIDE).set(false);
        setup.add(new DataSourceTemplate("oracle-xa", DataSourceTemplate.Vendor.ORACLE, xaDataSource, driver,
                properties("URL", "jdbc:oracle:oci8:@tc")));


        // ------------------------------------------------------ Microsoft SQL Server
        // Driver
        driver = new JdbcDriver(SQLSERVER);
        driver.get(DRIVER_MODULE_NAME).set("com.microsoft");
        driver.get(DRIVER_CLASS_NAME).set("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");

        // DS
        dataSource = new DataSource("MSSQLDS", false);
        dataSource.get(POOL_NAME).set("MSSQLDS_Pool");
        dataSource.get(JNDI_NAME).set("java:/MSSQLDS");
        dataSource.get(DRIVER_NAME).set(SQLSERVER);
        dataSource.get(CONNECTION_URL).set("jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=MyDatabase");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
        setup.add(new DataSourceTemplate(SQLSERVER, SQL_SERVER, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("MSSQLXADS", true);
        xaDataSource.get(POOL_NAME).set("MSSQLXADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/MSSQLXADS");
        xaDataSource.get(DRIVER_NAME).set(SQLSERVER);
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
        xaDataSource.get(SAME_RM_OVERRIDE).set(false);
        setup.add(new DataSourceTemplate("sqlserver-xa", SQL_SERVER, xaDataSource, driver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "mssqldb", "SelectMethod", "cursor")));


        // ------------------------------------------------------ DB2
        // Driver
        driver = new JdbcDriver("ibmdb2");
        driver.get(DRIVER_MODULE_NAME).set("com.ibm");
        driver.get(DRIVER_CLASS_NAME).set("COM.ibm.db2.jdbc.app.DB2Driver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("COM.ibm.db2.jdbc.DB2XADataSource");

        // DS
        dataSource = new DataSource("DB2DS", false);
        dataSource.get(POOL_NAME).set("DB2DS_Pool");
        dataSource.get(JNDI_NAME).set("java:/DB2DS");
        dataSource.get(DRIVER_NAME).set("ibmdb2");
        dataSource.get(CONNECTION_URL).set("jdbc:db2:yourdatabase");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
        dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
        dataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
        dataSource.get(MIN_POOL_SIZE).set(0);
        dataSource.get(MAX_POOL_SIZE).set(50);
        setup.add(new DataSourceTemplate("db2", DB2, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("DB2XADS", true);
        xaDataSource.get(POOL_NAME).set("DB2XADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/DB2XADS");
        xaDataSource.get(DRIVER_NAME).set("ibmdb2");
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
        xaDataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
        xaDataSource.get(STALE_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
        xaDataSource.get(RECOVERY_PLUGIN_CLASS_NAME).set("org.jboss.jca.core.recovery.ConfigurableRecoveryPlugin");
        // TODO Add missing recovery plugin properties
        xaDataSource.get(SAME_RM_OVERRIDE).set(false);
        setup.add(new DataSourceTemplate("db2-xa", DB2, xaDataSource, driver,
                properties(SERVER_NAME, LOCALHOST, DATABASE_NAME, "ibmdb2db", "PortNumber", "446")));


        // ------------------------------------------------------ Sybase
        // Driver
        driver = new JdbcDriver(SYBASE);
        driver.get(DRIVER_MODULE_NAME).set("com.sybase");
        driver.get(DRIVER_CLASS_NAME).set("com.sybase.jdbc.SybDriver");
        driver.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.sybase.jdbc4.jdbc.SybXADataSource");

        // DS
        dataSource = new DataSource("SybaseDB", false);
        dataSource.get(POOL_NAME).set("SybaseDB_Pool");
        dataSource.get(JNDI_NAME).set("java:/SybaseDB");
        dataSource.get(DRIVER_NAME).set(SYBASE);
        dataSource.get(CONNECTION_URL).set("jdbc:sybase:Tds:localhost:5000/mydatabase?JCONNECT_VERSION=6");
        dataSource.get(USER_NAME).set(ADMIN);
        dataSource.get(PASSWORD).set(ADMIN);
        dataSource.get(BACKGROUND_VALIDATION).set(true);
        dataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
        dataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
        setup.add(new DataSourceTemplate(SYBASE, DataSourceTemplate.Vendor.SYBASE, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("SybaseXADS", true);
        xaDataSource.get(POOL_NAME).set("SybaseXADS_Pool");
        xaDataSource.get(JNDI_NAME).set("java:/SybaseXADS");
        xaDataSource.get(DRIVER_NAME).set(SYBASE);
        xaDataSource.get(USER_NAME).set(ADMIN);
        xaDataSource.get(PASSWORD).set(ADMIN);
        xaDataSource.get(BACKGROUND_VALIDATION).set(true);
        xaDataSource.get(VALID_CONNECTION_CHECKER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
        xaDataSource.get(EXCEPTION_SORTER_CLASS_NAME)
                .set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
        setup.add(new DataSourceTemplate("sybase-xa", DataSourceTemplate.Vendor.SYBASE, xaDataSource, driver,
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
