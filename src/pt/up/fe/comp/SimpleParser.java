package pt.up.fe.comp;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

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

public class SimpleParser implements JmmParser {

    @Override
    public JmmParserResult parse(String jmmCode, Map<String, String> config) {
        return parse(jmmCode, "Start", config);
    }

    @Override
    public JmmParserResult parse(String jmmCode, String startingRule, Map<String, String> config) {
        try {

            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));
            SpecsSystem.invoke(parser, startingRule);
            Node root = parser.rootNode();

            if (root == null) {
                throw new ParseException(parser, "Parsing problems, root is null");
            }

            root.dump("");

            if (!(root instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }

            return new JmmParserResult((JmmNode) root, Collections.emptyList(), config);

        } catch (ParseException e) {
            return handleParseException(e, config);
        } catch (RuntimeException e) { // Thrown by SpecsSystem when it encounters ParseException
            Throwable err = e.getCause();

            while (err != null) {
                if (err instanceof ParseException)
                    return handleParseException((ParseException) err, config);
                err = err.getCause();
            }

            return JmmParserResult.newError(Report.newError(
                    Stage.SYNTATIC, -1, -1,
                    "Unexpected RuntimeException during parsing", e
            ));

        } catch (Exception e) {
            return JmmParserResult.newError(Report.newError(
                    Stage.SYNTATIC, -1, -1,
                    "Unexpected exception during parsing", e
            ));
        }
    }

    private JmmParserResult handleParseException(ParseException e, Map<String, String> config) {
        boolean isLexicalError = e.getToken().getType() == JmmGrammarConstants.TokenType.INVALID;

        final List<Report> reports = Collections.singletonList(new Report(
                ReportType.ERROR,
                isLexicalError ? Stage.LEXICAL : Stage.SYNTATIC,
                e.getToken().getBeginLine(),
                e.getToken().getBeginColumn(),
                e.getMessage()
        ));

        return new JmmParserResult(null, reports, config);
    }
}
