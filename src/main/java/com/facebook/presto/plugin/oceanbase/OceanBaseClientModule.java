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
package com.facebook.presto.plugin.oceanbase;

import com.facebook.airlift.configuration.ConfigBinder;
import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;
import com.facebook.presto.plugin.jdbc.JdbcClient;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;


/**
 * Guice implementation to create the correct DI and binds
 *
 * @author Marcelo Paes Rech
 */
public class OceanBaseClientModule implements Module {

    public OceanBaseClientModule() {
    }

    public void configure(Binder binder) {
        binder.bind(JdbcClient.class).to(OceanBaseClient.class).in(Scopes.SINGLETON);
        ConfigBinder.configBinder(binder).bindConfig(BaseJdbcConfig.class);
        ConfigBinder.configBinder(binder).bindConfig(OceanBaseConfig.class);
    }
}