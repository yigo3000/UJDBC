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
package com.facebook.presto.jdbc;

import com.facebook.airlift.log.Logging;
import com.facebook.presto.Session;
import com.facebook.presto.execution.QueryManager;
import com.facebook.presto.plugin.mysql.MySqlPlugin;
import com.facebook.presto.server.testing.TestingPrestoServer;
import com.facebook.presto.tests.DistributedQueryRunner;
import com.facebook.presto.tpch.TpchPlugin;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static com.facebook.presto.jdbc.TestPrestoDriver.closeQuietly;
import static com.facebook.presto.testing.TestingSession.testSessionBuilder;
import static java.lang.String.format;
import static java.lang.Thread.sleep;

@Test(singleThreaded = true)
public class TestUjdbc
{
    private TestingPrestoServer server;
    private QueryManager qm;

    @BeforeClass
    public void setup()
            throws Exception
    {
        Logging.initialize();
    }
    @Test
    public void testBasicPostPreprocess()
            throws Exception
    {
        String extra = "";
        try (Connection connection = createConnection(extra)) { //
            try (Statement statement = connection.createStatement()) { // Statement是Java标准，用于执行SQL。一个Statemnet在一个时刻只能执行一个SQL。
                try (ResultSet ignored = statement.executeQuery("select * from mysql3.ytymysql.goods")) {
                    printResultSet(ignored);
                }
                while (true) {
                    sleep(10000);
                    System.out.println("sleeping");
                }
            }
        }
    }

    private Connection createConnection(String extra)
            throws SQLException
    {
        String url = format("jdbc:presto://%s/?%s", "localhost:6666", extra); //Connect to the bogus coordinator
        return DriverManager.getConnection(url, "test", null);
    }

    public static void printResultSet(ResultSet rs) throws SQLException
    {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + ": " + rs.getObject(i) + " ");
            }
            System.out.println();
        }
    }
}
