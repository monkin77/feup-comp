/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.parser;

import pt.up.fe.comp.TestUtils;

public abstract class ParserTest {
    private final String START_RULE = "Start";

    protected String getStartRule() {
        return START_RULE;
    }

    protected void noErrors(String code) {
        this.noErrors(code, getStartRule());
    }

    protected void mustFail(String code) {
        this.mustFail(code, getStartRule());
    }

    protected void noErrors(String code, String startingRule) {
        var result = TestUtils.parse(code, startingRule);
        TestUtils.noErrors(result);
    }

    protected void mustFail(String code, String startingRule) {
        var result = TestUtils.parse(code, startingRule);
        TestUtils.mustFail(result);
    }
}
