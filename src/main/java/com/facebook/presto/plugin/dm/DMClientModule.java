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

import com.facebook.airlift.configuration.ConfigBinder;
import com.facebook.airlift.log.Logger;
import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;
import com.facebook.presto.plugin.jdbc.ConnectionFactory;
import com.facebook.presto.plugin.jdbc.DriverConnectionFactory;
import com.facebook.presto.plugin.jdbc.JdbcClient;
import com.google.inject.*;
import dm.jdbc.driver.DmDriver;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
public class DMClientModule implements Module {
	private static final Logger log = Logger.get(DMClientModule.class);
	@Override
	public void configure(Binder binder) {

		binder.bind(JdbcClient.class).to(DMClient.class)
				.in(Scopes.SINGLETON);
		ConfigBinder.configBinder(binder).bindConfig(BaseJdbcConfig.class);
		ConfigBinder.configBinder(binder).bindConfig(DMConfig.class);
	}
	@Provides
	@Singleton
	public static ConnectionFactory connectionFactory(BaseJdbcConfig config, DMConfig dmConfig)
			throws SQLException
	{
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty("user",config.getConnectionUser());
		connectionProperties.setProperty("password",config.getConnectionPassword());
		return new DriverConnectionFactory(
				new DmDriver(),
				config.getConnectionUrl(),
				Optional.empty(),
				Optional.empty(),
				connectionProperties);
	}
}
