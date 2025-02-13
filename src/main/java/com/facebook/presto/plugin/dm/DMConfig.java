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


import com.facebook.airlift.configuration.Config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.RoundingMode;

public class DMConfig {
    private int varcharMaxSize = 4000;
    private int timestampDefaultPrecision = 6;
    private int numberDefaultScale = 10;
    private RoundingMode numberRoundingMode = RoundingMode.HALF_UP;

    @Min(0)
    @Max(38)
    public int getNumberDefaultScale() {
        return numberDefaultScale;
    }

    @Config("dm.number.default-scale")
    public DMConfig setNumberDefaultScale(int numberDefaultScale) {
        this.numberDefaultScale = numberDefaultScale;
        return this;
    }

    @NotNull
    public RoundingMode getNumberRoundingMode() {
        return numberRoundingMode;
    }

    @Config("dm.number.rounding-mode")
    public DMConfig setNumberRoundingMode(RoundingMode numberRoundingMode) {
        this.numberRoundingMode = numberRoundingMode;
        return this;
    }

    @Min(4000)
    public int getVarcharMaxSize() {
        return varcharMaxSize;
    }

    @Config("dm.varchar.max-size")
    public DMConfig setVarcharMaxSize(int varcharMaxSize) {
        this.varcharMaxSize = varcharMaxSize;
        return this;
    }

    @Min(0)
    @Max(9)
    public int getTimestampDefaultPrecision() {
        return timestampDefaultPrecision;
    }

    @Config("dm.timestamp.precision")
    public DMConfig setTimestampDefaultPrecision(int timestampDefaultPrecision) {
        this.timestampDefaultPrecision = timestampDefaultPrecision;
        return this;
    }
}
