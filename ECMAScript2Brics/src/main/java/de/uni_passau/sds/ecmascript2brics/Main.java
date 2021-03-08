package de.uni_passau.sds.ecmascript2brics;

import org.apache.commons.text.StringEscapeUtils;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("This command must have exactly one argument.");
            return;
        }

        try {
            String bricsRegex = EcmaToBricsExpressionConverter.convert(args[0]);
            System.out.println("The Brics expression is: " + StringEscapeUtils.escapeJava(bricsRegex));
        } catch (ECMAScriptLookaroundException e) {
            System.err.println("Lookahead/lookbehind is not supported.");
        } catch (ECMAScriptNotSupportedException e) {
            System.err.println("This expression is not supported.");
        } catch (ECMAScriptSyntaxException e) {
            System.err.println("This expression is not a valid ECMAScript expression.");
        } catch (RuntimeException e) {
            System.err.println("Unexpected error.");
        }
    }

}
