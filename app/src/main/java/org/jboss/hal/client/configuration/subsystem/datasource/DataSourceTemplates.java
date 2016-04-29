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

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate.Vendor.*;

/**
 * List of well known datasource templates
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class DataSourceTemplates implements Iterable<DataSourceTemplate> {

    private final List<DataSourceTemplate> pool;

    public DataSourceTemplates() {
        DataSource dataSource;
        DataSource xaDataSource;
        JdbcDriver driver;
        List<DataSourceTemplate> setup = new ArrayList<>();


        // ------------------------------------------------------ H2
        // Driver
        driver = new JdbcDriver("h2");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.h2database.h2");
        driver.get("driver-class-name").set("org.h2.Driver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.h2.jdbcx.JdbcDataSource");

        // DS
        dataSource = new DataSource("H2DS", false);
        dataSource.get("pool-name").set("H2DS_Pool");
        dataSource.get("jndi-name").set("java:/H2DS");
        dataSource.get("driver-name").set("h2");
        dataSource.get("connection-url").set("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.get("user-name").set("sa");
        dataSource.get("password").set("sa");
        dataSource.get("background-validation").set(false);
        setup.add(new DataSourceTemplate("h2", H2, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("H2XADS", true);
        xaDataSource.get("pool-name").set("H2XADS_Pool");
        xaDataSource.get("jndi-name").set("java:/H2XADS");
        xaDataSource.get("driver-name").set("h2");
        xaDataSource.get("xa-datasource-properties").set(properties("URL", "jdbc:h2:mem:test"));
        xaDataSource.get("user-name").set("sa");
        xaDataSource.get("password").set("sa");
        xaDataSource.get("background-validation").set(false);
        setup.add(new DataSourceTemplate("h2-xa", H2, xaDataSource, driver));


        // ------------------------------------------------------ PostgreSQL
        // Driver
        driver = new JdbcDriver("postgresql");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("org.postgresql");
        driver.get("driver-class-name").set("org.postgresql.Driver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("org.postgresql.xa.PGXADataSource");

        // DS
        dataSource = new DataSource("PostgresDS", false);
        dataSource.get("pool-name").set("PostgresDS_Pool");
        dataSource.get("jndi-name").set("java:/PostgresDS");
        dataSource.get("driver-name").set("postgresql");
        dataSource.get("connection-url").set("jdbc:postgresql://localhost:5432/postgresdb");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
        dataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
        setup.add(new DataSourceTemplate("postgresql", POSTGRE_SQL, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("PostgresXADS", true);
        xaDataSource.get("pool-name").set("PostgresXADS_Pool");
        xaDataSource.get("jndi-name").set("java:/PostgresXADS");
        xaDataSource.get("driver-name").set("postgresql");
        xaDataSource.get("xa-datasource-properties").set(properties("ServerName", "servername",
                "PortNumber", "5432",
                "DatabaseName", "postgresdb"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
        xaDataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
        setup.add(new DataSourceTemplate("postgresql-xa", POSTGRE_SQL, xaDataSource, driver));


        // ------------------------------------------------------ MySQL
        // Driver
        driver = new JdbcDriver("mysql");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.mysql");
        driver.get("driver-class-name").set("com.mysql.jdbc.Driver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");

        // DS
        dataSource = new DataSource("MySqlDS", false);
        dataSource.get("pool-name").set("MySqlDS_Pool");
        dataSource.get("jndi-name").set("java:/MySqlDS");
        dataSource.get("driver-name").set("mysql");
        dataSource.get("connection-url").set("jdbc:mysql://localhost:3306/mysqldb");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
        dataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
        setup.add(new DataSourceTemplate("mysql", MYSQL, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("MysqlXADS", true);
        xaDataSource.get("pool-name").set("MysqlXADS_Pool");
        xaDataSource.get("jndi-name").set("java:/MysqlXADS");
        xaDataSource.get("driver-name").set("mysql");
        xaDataSource.get("xa-datasource-properties").set(properties("ServerName", "localhost",
                "DatabaseName", "mysqldb"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker");
        xaDataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter");
        setup.add(new DataSourceTemplate("mysql-xa", MYSQL, xaDataSource, driver));


        // ------------------------------------------------------ Oracle
        // Driver
        driver = new JdbcDriver("oracle");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.oracle");
        driver.get("driver-class-name").set("oracle.jdbc.driver.OracleDriver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("oracle.jdbc.xa.client.OracleXADataSource");

        // DS
        dataSource = new DataSource("OracleDS", false);
        dataSource.get("pool-name").set("OracleDS_Pool");
        dataSource.get("jndi-name").set("java:/OracleDS");
        dataSource.get("driver-name").set("oracle");
        dataSource.get("connection-url").set("jdbc:oracle:thin:@localhost:1521:orcalesid");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
        dataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
        dataSource.get("stale-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker");
        setup.add(new DataSourceTemplate("oracle", ORACLE, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("XAOracleDS", true);
        xaDataSource.get("pool-name").set("XAOracleDS_Pool");
        xaDataSource.get("jndi-name").set("java:/XAOracleDS");
        xaDataSource.get("driver-name").set("oracle");
        xaDataSource.get("xa-datasource-properties").set(properties("URL", "jdbc:oracle:oci8:@tc"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker");
        xaDataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter");
        xaDataSource.get("stale-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker");
        xaDataSource.get("no-tx-separate-pool").set(true);
        xaDataSource.get("same-rm-override").set(false);
        setup.add(new DataSourceTemplate("oracle-xa", ORACLE, xaDataSource, driver));


        // ------------------------------------------------------ Microsoft SQL Server
        // Driver
        driver = new JdbcDriver("sqlserver");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.microsoft");
        driver.get("driver-class-name").set("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");

        // DS
        dataSource = new DataSource("MSSQLDS", false);
        dataSource.get("pool-name").set("MSSQLDS_Pool");
        dataSource.get("jndi-name").set("java:/MSSQLDS");
        dataSource.get("driver-name").set("sqlserver");
        dataSource.get("connection-url").set("jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=MyDatabase");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
        setup.add(new DataSourceTemplate("sqlserver", SQL_SERVER, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("MSSQLXADS", true);
        xaDataSource.get("pool-name").set("MSSQLXADS_Pool");
        xaDataSource.get("jndi-name").set("java:/MSSQLXADS");
        xaDataSource.get("driver-name").set("sqlserver");
        xaDataSource.get("xa-datasource-properties").set(properties("ServerName", "localhost",
                "DatabaseName", "mssqldb",
                "SelectMethod", "cursor"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker");
        xaDataSource.get("same-rm-override").set(false);
        setup.add(new DataSourceTemplate("sqlserver-xa", SQL_SERVER, xaDataSource, driver));


        // ------------------------------------------------------ DB2
        // Driver
        driver = new JdbcDriver("ibmdb2");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.ibm");
        driver.get("driver-class-name").set("COM.ibm.db2.jdbc.app.DB2Driver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("COM.ibm.db2.jdbc.DB2XADataSource");

        // DS
        dataSource = new DataSource("DB2DS", false);
        dataSource.get("pool-name").set("DB2DS_Pool");
        dataSource.get("jndi-name").set("java:/DB2DS");
        dataSource.get("driver-name").set("ibmdb2");
        dataSource.get("connection-url").set("jdbc:db2:yourdatabase");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
        dataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
        dataSource.get("stale-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
        dataSource.get("min-pool-size").set(0);
        dataSource.get("max-pool-size").set(50);
        setup.add(new DataSourceTemplate("db2", DB2, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("DB2XADS", true);
        xaDataSource.get("pool-name").set("DB2XADS_Pool");
        xaDataSource.get("jndi-name").set("java:/DB2XADS");
        xaDataSource.get("driver-name").set("ibmdb2");
        xaDataSource.get("xa-datasource-properties").set(properties("ServerName", "localhost",
                "DatabaseName", "ibmdb2db",
                "PortNumber", "446"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker");
        xaDataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter");
        xaDataSource.get("stale-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker");
        xaDataSource.get("recovery-plugin-class-name").set("org.jboss.jca.core.recovery.ConfigurableRecoveryPlugin");
        // TODO Add missing recovery plugin properties
        xaDataSource.get("same-rm-override").set(false);
        setup.add(new DataSourceTemplate("db2-xa", DB2, xaDataSource, driver));


        // ------------------------------------------------------ Sybase
        // Driver
        driver = new JdbcDriver("sybase");
        driver.get(ModelDescriptionConstants.DRIVER_MODULE_NAME).set("com.sybase");
        driver.get("driver-class-name").set("com.sybase.jdbc.SybDriver");
        driver.get(ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME).set("com.sybase.jdbc4.jdbc.SybXADataSource");

        // DS
        dataSource = new DataSource("SybaseDB", false);
        dataSource.get("pool-name").set("SybaseDB_Pool");
        dataSource.get("jndi-name").set("java:/SybaseDB");
        dataSource.get("driver-name").set("sybase");
        dataSource.get("connection-url").set("jdbc:sybase:Tds:localhost:5000/mydatabase?JCONNECT_VERSION=6");
        dataSource.get("user-name").set("admin");
        dataSource.get("password").set("admin");
        dataSource.get("background-validation").set(true);
        dataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
        dataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
        setup.add(new DataSourceTemplate("sybase", SYBASE, dataSource, driver));

        // XA DS
        xaDataSource = new DataSource("SybaseXADS", true);
        xaDataSource.get("pool-name").set("SybaseXADS_Pool");
        xaDataSource.get("jndi-name").set("java:/SybaseXADS");
        xaDataSource.get("driver-name").set("sybase");
        xaDataSource.get("xa-datasource-properties").set(properties("NetworkProtocol", "Tds",
                "ServerName", "localhost",
                "PortNumber", "4100",
                "DatabaseName", "mydatabase"));
        xaDataSource.get("user-name").set("admin");
        xaDataSource.get("password").set("admin");
        xaDataSource.get("background-validation").set(true);
        xaDataSource.get("valid-connection-checker-class-name").set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker");
        xaDataSource.get("exception-sorter-class-name").set("org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter");
        setup.add(new DataSourceTemplate("sybase-xa", SYBASE, xaDataSource, driver));

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

    private ModelNode properties(String... properties) {
        ModelNode node = new ModelNode();
        for (int i = 0; i < properties.length; i+=2) {
            node.get(properties[i]).set(properties[i + 1]);
        }
        return node;
    }
}
