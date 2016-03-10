package org.jboss.hal.client.configuration.subsystem.datasource;

/**
 * Datasource template for a quick and easy ways to setup vendor specific datasources.
 *
 * @author Harald Pehl
 */
public class DataSourceTemplate {

    @SuppressWarnings("HardCodedStringLiteral")
    enum Vendor {
        H2("H2"),
        POSTGRE_SQL("PostgreSQL"),
        MYSQL("MySQL"),
        ORACLE("Oracle"),
        SQL_SERVER("Microsoft SQLServer"),
        DB2("IBM DB2"),
        SYBASE("Sybase");

        private final String label;

        Vendor(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

    }


    private final String id;
    private final Vendor vendor;
    private final DataSource dataSource;
    private final JdbcDriver driver;

    DataSourceTemplate(String id, Vendor vendor, DataSource dataSource, JdbcDriver driver) {
        this.id = id;
        this.vendor = vendor;
        this.dataSource = dataSource;
        this.driver = driver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof DataSourceTemplate)) { return false; }

        DataSourceTemplate that = (DataSourceTemplate) o;

        if (!id.equals(that.id)) { return false; }
        return vendor == that.vendor;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + vendor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(vendor.label).append(" ");
        if (dataSource.isXa()) {
            builder.append("XA ");
        }
        builder.append("Datasource");
        return builder.toString();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcDriver getDriver() {
        return driver;
    }

    public String getId() {
        return id;
    }

    public Vendor getVendor() {
        return vendor;
    }
}
