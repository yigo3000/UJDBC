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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.Session;
import com.facebook.presto.plugin.dm.DMPlugin;
import com.facebook.presto.plugin.mysql.MySqlPlugin;
import com.facebook.presto.plugin.oracle.OraclePlugin;
import com.facebook.presto.plugin.sqlserver.SqlServerPlugin;
import com.facebook.presto.server.testing.TestingPrestoServer;
import com.facebook.presto.tests.DistributedQueryRunner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.facebook.presto.testing.TestingSession.testSessionBuilder;
import static com.facebook.presto.util.PropertiesUtil.loadProperties;
import static com.google.common.base.Preconditions.checkState;

public class BogusPrestoServer
{
    private static final Logger log = Logger.get(BogusPrestoServer.class);
    public DistributedQueryRunner queryRunner;
    public TestingPrestoServer get_BogusPrestoServer() throws Exception
    {
        Session session = testSessionBuilder()
                .setCatalog("bogus")
                .setSchema("bogus")
                .build();
        queryRunner = DistributedQueryRunner.builder(session).build(); // 建立一个虚拟的Presto服务器，具有联邦查询能力
        queryRunner.installPlugin(new MySqlPlugin()); // 安装MySql插件，用于访问MySQL数据库
        queryRunner.installPlugin(new OraclePlugin()); // 安装Oracle插件，用于访问Oracle数据库
        queryRunner.installPlugin(new SqlServerPlugin()); // 安装SqlServer插件，用于访问SqlServer数据库
        queryRunner.installPlugin(new DMPlugin());
        queryRunner.createCatalog("dm1", "dm", ImmutableMap.of("connection-url", "jdbc:dm://localhost:5236", "connection-user", "SYSDBA", "connection-password", "SYSDBA_dm001"));
//        StaticCatalogStore
        try {
            loadAllPropertiesFiles(BogusPrestoServer.class.getClassLoader());
        }
        catch (IOException e) {
            log.error("Error loadAllPropertiesFiles: " + e.getMessage());
        }
        return queryRunner.getCoordinator();
    }
    private void loadAllPropertiesFiles(ClassLoader classLoader) throws IOException, URISyntaxException
    {
        // 获取环境变量 UJDBC_PROPERTIES 的值
        String propertiesPath = System.getenv("UJDBC_PROPERTIES");
        if (propertiesPath == null || propertiesPath.isEmpty()) {
            log.info("Environment variable UJDBC_PROPERTIES is not set or is empty");
            return;
        }

        File directory = new File(propertiesPath);
        log.info("Loading properties files from directory: " + directory.getAbsolutePath());
        // 遍历resources目录下的所有文件
        for (File file : listFiles(directory)) {
            if (file.isFile() && file.getName().endsWith(".properties")) {
                try {
                    loadCatalog(file);
                }
                catch (Exception e) {
                    log.error("Error in loadCatalog(): " + e.getMessage());
                }
            }
        }
    }
    private static List<File> listFiles(File installedPluginsDir)
    {
        if (installedPluginsDir != null && installedPluginsDir.isDirectory()) {
            File[] files = installedPluginsDir.listFiles();
            if (files != null) {
                return ImmutableList.copyOf(files);
            }
        }
        return ImmutableList.of();
    }
    private void loadCatalog(File file)
            throws Exception
    {
        String catalogName = Files.getNameWithoutExtension(file.getName());

        log.info("-- Loading catalog properties " + file + "--");
        Map<String, String> properties = loadProperties(file);
        checkState(properties.containsKey("connector.name"), "Catalog configuration %s does not contain connector.name", file.getAbsoluteFile());
        loadCatalog(catalogName, properties);
//        this.queryRunner.createCatalog(catalogName, properties.get("connector.name"), properties);
    }
    private void loadCatalog(String catalogName, Map<String, String> properties)
    {
        log.info("-- Loading catalog %s --", catalogName);

        String connectorName = null;
        ImmutableMap.Builder<String, String> connectorProperties = ImmutableMap.builder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals("connector.name")) {
                connectorName = entry.getValue();
            }
            else {
                connectorProperties.put(entry.getKey(), entry.getValue());
            }
        }

        checkState(connectorName != null, "Configuration for catalog %s does not contain connector.name", catalogName);

        queryRunner.createCatalog(catalogName, connectorName, connectorProperties.build());
        log.info("-- Added catalog %s using connector %s --", catalogName, connectorName);
    }
}
