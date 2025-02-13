/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.plugin.dm;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.common.type.Decimals;
import com.facebook.presto.common.type.VarcharType;
import com.facebook.presto.plugin.jdbc.*;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.SchemaTableName;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import static com.facebook.presto.common.type.DecimalType.createDecimalType;
import static com.facebook.presto.common.type.VarcharType.createUnboundedVarcharType;
import static com.facebook.presto.common.type.VarcharType.createVarcharType;
import static com.facebook.presto.plugin.jdbc.JdbcErrorCode.JDBC_ERROR;
import static com.facebook.presto.plugin.jdbc.StandardReadMappings.*;
import static com.facebook.presto.spi.StandardErrorCode.NOT_SUPPORTED;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

public class DMClient extends BaseJdbcClient {

    private static final Logger log = Logger.get(DMClient.class);
    private final int numberDefaultScale;

    @Inject
    public DMClient(
            JdbcConnectorId connectorId,
            BaseJdbcConfig config,
            DMConfig dmConfig,
            ConnectionFactory connectionFactory) throws SQLException {
        super(connectorId, config, "", connectionFactory);
        log.info("dmConfig", dmConfig.toString());
        log.info("config", config.toString());
        this.numberDefaultScale = dmConfig.getNumberDefaultScale();

    }

    @Override
    protected String getTableSchemaName(ResultSet resultSet) throws SQLException {
        return super.getTableSchemaName(resultSet);
    }

    private String[] getTableTypes() {
        return new String[]{"TABLE", "VIEW"};
    }

    @Override
    protected ResultSet getTables(Connection connection, Optional<String> schemaName, Optional<String> tableName)
            throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        String escape = metadata.getSearchStringEscape();
        return metadata.getTables(
                connection.getCatalog(),
                escapeNamePattern(schemaName, Optional.of(escape)).orElse(null),
                escapeNamePattern(tableName, Optional.of(escape)).orElse(null),
                getTableTypes());
    }

    @Override
    public PreparedStatement getPreparedStatement(ConnectorSession session, Connection connection, String sql)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setFetchSize(1000);
        return statement;
    }

    @Override
    protected String generateTemporaryTableName() {
        return "presto_tmp_" + System.nanoTime();
    }

    @Override
    protected void renameTable(JdbcIdentity identity, String catalogName, SchemaTableName oldTable, SchemaTableName newTable) {
        if (!oldTable.getSchemaName().equalsIgnoreCase(newTable.getSchemaName())) {
            throw new PrestoException(NOT_SUPPORTED, "Table rename across schemas is not supported in dm");
        }

        String newTableName = newTable.getTableName().toUpperCase(ENGLISH);
        String oldTableName = oldTable.getTableName().toUpperCase(ENGLISH);
        String sql = format(
                "ALTER TABLE %s RENAME TO %s",
                quoted(catalogName, oldTable.getSchemaName(), oldTableName),
                quoted(newTableName));

        try (Connection connection = connectionFactory.openConnection(identity)) {
            execute(connection, sql);
        } catch (SQLException e) {
            throw new PrestoException(JDBC_ERROR, e);
        }
    }

    @Override
    public Optional<ReadMapping> toPrestoType(ConnectorSession session, JdbcTypeHandle typeHandle) {
        int columnSize = typeHandle.getColumnSize();
        log.info("typeHandle==============================");
        log.info(String.valueOf(typeHandle));
        log.info(String.valueOf(typeHandle.getJdbcType()));
        switch (typeHandle.getJdbcType()) {
            case Types.CLOB:
                return Optional.of(varcharReadMapping(createUnboundedVarcharType()));
            case Types.SMALLINT:
                return Optional.of(smallintReadMapping());
            case Types.FLOAT:
            case Types.DOUBLE:
                return Optional.of(doubleReadMapping());
            case Types.REAL:
                return Optional.of(realReadMapping());
            case Types.DECIMAL:
            case Types.NUMERIC:
                int precision = columnSize == 0 ? Decimals.MAX_PRECISION : columnSize;
                int scale = typeHandle.getDecimalDigits();

                if (scale == 0) {
                    return Optional.of(bigintReadMapping());
                }
                if (scale < 0 || scale > precision) {
                    return Optional.of(decimalReadMapping(createDecimalType(precision, numberDefaultScale)));
                }

                return Optional.of(decimalReadMapping(createDecimalType(precision, scale)));
            case Types.LONGVARCHAR:
                if (columnSize > VarcharType.MAX_LENGTH || columnSize == 0) {
                    return Optional.of(varcharReadMapping(createUnboundedVarcharType()));
                }
                return Optional.of(varcharReadMapping(createVarcharType(columnSize)));
            case Types.VARCHAR:
                return Optional.of(varcharReadMapping(createVarcharType(columnSize)));
        }
        return super.toPrestoType(session, typeHandle);
    }
}