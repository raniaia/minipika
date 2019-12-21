package org.laniakeamly.recycle.build;

import org.laniakeamly.poseidon.framework.exception.runtime.DynamicSQLException;
import org.laniakeamly.poseidon.framework.sql.ProvideConstant;
import org.laniakeamly.poseidon.framework.sql.xml.node.XMLMapperNode;
import org.laniakeamly.poseidon.framework.sql.xml.node.XMLNode;
import org.laniakeamly.poseidon.framework.sql.xml.node.XMLDynamicSqlNode;
import org.laniakeamly.poseidon.framework.sql.xml.token.Token;
import org.laniakeamly.poseidon.framework.sql.xml.token.TokenValue;
import org.laniakeamly.poseidon.framework.tools.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将CrudNode转换成Java代码
 * Create by 2BKeyboard on 2019/12/17 17:51
 */
public class ParserCrudNode {

    /**
     * mapper name用于异常追踪
     */
    private String mapperName;

    /**
     * builder name用于异常追踪
     */
    private String builderName;

    public PrecompiledMethod parse(XMLDynamicSqlNode mapperNode, XMLMapperNode builderNode) {
        mapperName = mapperNode.getName();
        builderName = builderNode.getName();
        PrecompiledMethod dynamic = new PrecompiledMethod(mapperNode.getName(), mapperNode.getResult(), mapperNode.getType());
        parseNode(mapperNode.getNodes(), mapperNode.getType(), dynamic);
        return dynamic;
    }

    public void parseNode(List<XMLNode> xmlNodes, String type, PrecompiledMethod dynamic) {
        for (XMLNode node : xmlNodes) {

            //
            // text
            //
            if (ProvideConstant.TEXT.equals(node.getName())) {
                dynamic.appendSql(node.getContent());
                continue;
            }

            //
            // choose
            //
            if(ProvideConstant.CHOOSE.equals(node.getName())){
                parseNode(node.getChildren(),type,dynamic);
            }

            //
            // if
            //
            if (ProvideConstant.IF.equals(node.getName())) {
                List<TokenValue> test = testProcess(node.getAttribute("test"));
                String _if = buildIf(test);
                dynamic.append(_if);
                dynamic.append("{");
                parseNode(node.getChildren(),type,dynamic);
                dynamic.append("}");
            }

            //
            // else
            //
            if (ProvideConstant.ELSE.equals(node.getName())) {
                dynamic.append("else");
                dynamic.append("{");
                parseNode(node.getChildren(),type,dynamic);
                dynamic.append("}");
            }

        }
    }

    /**
     * 构建if语句
     * @return
     */
    private String buildIf(List<TokenValue> test) {
        StringBuilder builder = new StringBuilder();
        builder.append("if(");
        for (TokenValue token : test) {
            if (token.getToken() == Token.IDEN) {
                builder.append(
                        StringUtils.format("(#{}#) {}.get(\"{}\")",
                                token.getValue(),ProvideConstant.PARAMS_MAP, token.getValue())
                );
            }else{
                builder.append(token.getValue());
            }

        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 将test中的内容解析成Token
     * @param test
     * @return
     */
    public List<TokenValue> testProcess(String test) {
        boolean isString = false;
        // test = test.replaceAll("'", "\"");
        test = test.replaceAll(" or ", " || ");
        test = test.replaceAll(" and ", " && ");
        test = test.replaceAll(" ", "");
        char[] charArray = test.toCharArray();
        StringBuilder builder = new StringBuilder();
        List<TokenValue> tokens = new LinkedList<>();
        // Map<String, TestToken> testMap = new LinkedHashMap<>();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            // 处理String
            if (isString) {
                if (c == '\'') {
                    isString = false;
                    builder.append("\"");
                    tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                    clear(builder);
                } else {
                    builder.append(c);
                }
                continue;
            }

            if (c == ' ') continue; // 如果不是String内扫描到空格直接跳过

            switch (c) {
                case '=': {
                    String builderValue = builder.toString();
                    if (!builderValue.equals("=")
                            && !builderValue.equals("!")
                            && !builderValue.equals("<")
                            && !builderValue.equals(">")) {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                        }
                        clear(builder);
                        builder.append(c);
                        break;
                    } else {
                        builder.append(c);
                        if (!StringUtils.isEmpty(builder.toString())) {
                            String value = builder.toString();
                            if (value.equals("=="))
                                tokens.add(TokenValue.buildToken(Token.EQ, Token.OP, value));
                            if (value.equals("!="))
                                tokens.add(TokenValue.buildToken(Token.NE, Token.OP, value));
                            if (value.equals("<"))
                                tokens.add(TokenValue.buildToken(Token.LT, Token.OP, value));
                            if (value.equals("<="))
                                tokens.add(TokenValue.buildToken(Token.LE, Token.OP, value));
                            if (value.equals(">"))
                                tokens.add(TokenValue.buildToken(Token.GE, Token.OP, value));
                            if (value.equals(">="))
                                tokens.add(TokenValue.buildToken(Token.GT, Token.OP, value));
                        }
                        clear(builder);
                        break;
                    }
                }
                case '!': {
                    if (!StringUtils.isEmpty(builder.toString())) {
                        tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                    }
                    clear(builder);
                    builder.append(c);
                    break;
                }
                case '<': {
                    i++;
                    char value = charArray[i];
                    if (value == '=') {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                            clear(builder);
                        }
                        tokens.add(TokenValue.buildToken(Token.LE, Token.OP, "<="));
                    } else {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            if (value == '\'') {
                                tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                                clear(builder);
                                builder.append("\"");
                                isString = true;
                            } else {
                                tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                                clear(builder);
                                builder.append(value);
                            }
                        }
                        tokens.add(TokenValue.buildToken(Token.LT, Token.OP, "<"));
                    }
                    break;
                }
                case '>': {
                    i++;
                    char value = charArray[i];
                    if (value == '=') {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                            clear(builder);
                        }
                        tokens.add(TokenValue.buildToken(Token.GE, Token.OP, ">="));
                    } else {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            if (value == '\'') {
                                tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                                clear(builder);
                                builder.append("\"");
                                isString = true;
                            } else {
                                tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                                clear(builder);
                                builder.append(value);
                            }
                        }
                        tokens.add(TokenValue.buildToken(Token.GT, Token.OP, ">"));
                    }
                    break;
                }
                case '&': {
                    String builderValue = builder.toString();
                    if (!builderValue.equals("&")) {
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
                        }
                        clear(builder);
                        builder.append(c);
                    } else {
                        builder.append(c);
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.AND, builder.toString()));
                        }
                        clear(builder);
                    }
                    break;
                }
                case '|': {
                    String builderValue = builder.toString();
                    if (!builderValue.equals("|")) {
                        if (!StringUtils.isEmpty(builder.toString()))
                            tokens.add(TokenValue.buildToken(Token.IDEN, builderValue));
                        clear(builder);
                        builder.append(c);
                    } else {
                        builder.append(c);
                        if (!StringUtils.isEmpty(builder.toString())) {
                            tokens.add(TokenValue.buildToken(Token.OR, builder.toString()));
                        }
                        clear(builder);
                    }
                    break;
                }
                case '\'': {
                    if (!isString) {
                        builder.append("\"");
                        isString = true;
                    }
                    break;
                }
                default: {
                    builder.append(c);
                }
            }
        }
        String value = builder.toString();
        if (!StringUtils.isEmpty(value)) {
            tokens.add(TokenValue.buildToken(Token.IDEN, builder.toString()));
        }
        clear(builder);
        return tokens;
    }

    private void clear(StringBuilder builder) {
        builder.delete(0, builder.length());
    }


}
