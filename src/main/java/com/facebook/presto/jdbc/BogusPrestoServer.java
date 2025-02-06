package com.facebook.presto.jdbc;

import com.facebook.presto.Session;
import com.facebook.presto.plugin.mysql.MySqlPlugin;
import com.facebook.presto.plugin.oracle.OraclePlugin;
import com.facebook.presto.server.testing.TestingPrestoServer;
import com.facebook.presto.tests.DistributedQueryRunner;
import com.google.common.collect.ImmutableMap;

import static com.facebook.presto.testing.TestingSession.testSessionBuilder;

public class BogusPrestoServer
{
    static TestingPrestoServer get_BogusPrestoServer() throws Exception
    {
        Session session = testSessionBuilder()
            .setCatalog("bogus")
            .setSchema("bogus")
            .build();
        DistributedQueryRunner queryRunner = DistributedQueryRunner.builder(session).build(); // 建立一个虚拟的Presto服务器，具有联邦查询能力
        queryRunner.installPlugin(new MySqlPlugin()); // 安装MySql插件，用于访问MySQL数据库
        queryRunner.installPlugin(new OraclePlugin()); // 安装MySql插件，用于访问MySQL数据库
        queryRunner.createCatalog("mysql1", "mysql", ImmutableMap.of("connection-url", "jdbc:mysql://mysql.sqlpub.com:3306", "connection-user", "ytymysql", "connection-password", "0u5Tr2GELwcWP0kQ"));
        queryRunner.createCatalog("oracle1", "oracle", ImmutableMap.of("connection-url", "jdbc:oracle://10.4.7.90:1521:cdw", "connection-user", "readonly", "connection-password", "n2pchgt3h9ag2n"));
        return queryRunner.getCoordinator();

    }

}
