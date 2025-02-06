# UJDBC 通用JDBC驱动
A universal JDBC driver that supports simultaneous connection to multiple types of data sources and supports federated queries.

When initializing a new project, you can use the following command to create a new bogus server of Presto, may use 400-500MB memory:
The first connection will be established in 4 seconds on CPU i5-1135G7.

Currently, it has been tested with Mysql, Oracle and SQLServer databases.

## Mission and Architecture

## Requirements
* Windows or Mac OS X or Linux
* Java 8 Update 151 or higher (8u151+), 64-bit. Both Oracle JDK and OpenJDK are supported.
* Maven 3.6.1+ (for building)

## Build 
It is recommended to use maven wapper for building, and the Maven address has been modified to a mirror of China located at:
.mvn\wrapper\maven-wrapper.properties
If your are not in China, you can change it to the official Maven repository.
The build command is as follows: 
./mvnw package -DskipTests -T4

# 通用JDBC驱动
一个通用的JDBC驱动，可同时连接多种类型数据源，并支持联邦查询（跨数据库的表之间做关联）。使用者只需要建立一个连接，就可以查询多个数据库。
当前已经测试过Mysql、Oracle、SQLServer数据库。

## 架构设计
本驱动基于Presto，并对其进行了一定程度的封装。当JDBC驱动建立一个连接（PrestoConnection）时，它会在进程里启动一个虚拟的“Presto服务器”（PrestoConnection.server），并建立一个查询执行器（queryExecutor），然后通过JDBC标准的connection建立一个statement，调用statement.executeQuery()执行SQL语句。

未来计划实现的功能：一是支持更多XC数据库，如达梦、OceanBase、Inceptor、人大金仓、南大通用等。二是从认证、端口保护等途径实现更高的安全性。三是项目结构、代码的规范化。四是用户建议的其他功能。

## 使用要求
* Windows or Mac OS X or Linux
* Java 8 Update 151 or higher (8u151+), 64-bit. Both Oracle JDK and OpenJDK are supported.
* Maven 3.6.1+ (for building)
当初始化一个新项目时，可以使用以下命令创建一个新的Presto的Bogus服务器，约使用400-500MB内存。
第一次连接时，在CPU i5-1135G7上需要约4秒钟。

## 使用方法

## 构建命令
建议使用maven wapper构建，maven地址已经被修改为国内镜像，位于：
.mvn\wrapper\maven-wrapper.properties
命令如下： 
./mvnw package -DskipTests -T4