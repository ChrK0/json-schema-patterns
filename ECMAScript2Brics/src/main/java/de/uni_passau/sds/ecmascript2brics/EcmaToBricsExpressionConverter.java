package de.uni_passau.sds.ecmascript2brics;

public class EcmaToBricsExpressionConverter {

    public static String convert(String ecmaScriptPattern) {
        ExpressionTree ecmaScriptTree = Builder.buildExpressionTree(ecmaScriptPattern);
        ExpressionTree bricsTree = EcmaToBricsTreeConverter.convert(ecmaScriptTree);
        return bricsTree.convertToString();
    }

}
