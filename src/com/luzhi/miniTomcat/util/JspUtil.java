package com.luzhi.miniTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.catalina.Context;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;

import java.io.File;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/20
 * <p>
 * 生成对Jsp解析的工具类.
 */
public abstract class JspUtil {

    /**
     * @see #JAVA_KEY_WORDS
     * <p>
     * java关键字集合.
     */
    private static final String[] JAVA_KEY_WORDS = {"abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "for", "float",
            "goto", "if", "implements", "import", "interface", "int", "instanceof", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"};

    /**
     * @see #DEFAULT_PATH
     * <p>
     * 默认路径
     */
    private static final String DEFAULT_PATH = "/";

    public static void main(String[] args) {
        try {
            Context context = new Context("/javaweb", "/Users/apple/IdeaProjects/javaweb/web", null, true);
            File file = new File("/Users/apple/IdeaProjects/javaweb/web/index.jsp");
            compileJsp(context, file);
        } catch (Exception exception) {
            LogFactory.get().error(exception.getMessage());
        }
    }

    public static void compileJsp(Context context, File file) throws JasperException {
        String subFolder;
        String path = context.getPath();
        if (DEFAULT_PATH.equals(path)) {
            subFolder = "_";
        } else {
            subFolder = StrUtil.subAfter(path, DEFAULT_PATH, false);
        }
        String workPath = new File(ConstantTomcat.WORK_FOLDER, subFolder).getAbsolutePath() + File.separator;
        String[] args = new String[]{"-webapp", context.getDocBase().toLowerCase(), "-d", workPath.toLowerCase(), "-compile"};

        JspC jspC = new JspC();
        jspC.setArgs(args);
        jspC.execute(file);
    }

    public static String makeJavaIdentifier(String identifier) {
        return makeJavaIdentifier(identifier, true);
    }

    public static String makeJavaIdentifier(String identifier, boolean periodToUnderCase) {
        StringBuilder modifiedIdentifier = new StringBuilder(identifier.length());
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            modifiedIdentifier.append('_');
        }
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            boolean javaIdentifierPart = Character.isJavaIdentifierPart(ch);
            boolean resultNotToUnderCase = (ch != '_') || !periodToUnderCase;
            if (javaIdentifierPart && resultNotToUnderCase) {
                modifiedIdentifier.append(ch);
            } else if (ch == '.' && periodToUnderCase) {
                modifiedIdentifier.append('_');
            } else {
                modifiedIdentifier.append(mangleChar(ch));
            }
        }
        if (isJavaKeyWord(modifiedIdentifier.toString())) {
            modifiedIdentifier.append('_');
        }
        return modifiedIdentifier.toString();
    }

    public static String mangleChar(char ch) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit((ch >> 12) & 0xf, 16);
        result[2] = Character.forDigit((ch >> 8) & 0xf, 16);
        result[3] = Character.forDigit((ch >> 4) & 0xf, 16);
        result[4] = Character.forDigit(ch & 0xf, 16);
        return new String(result);
    }

    public static boolean isJavaKeyWord(String key) {
        int i = 0;
        int j = JAVA_KEY_WORDS.length;
        while (i < j) {
            int k = (i + j) / 2;
            int result = JAVA_KEY_WORDS[k].compareTo(key);
            if (result == 0) {
                return true;
            }
            if (result < 0) {
                i = k + 1;
            } else {
                j = k;
            }
        }
        return false;
    }

    public static String getServletPath(String uri, String subFolder) {
        String tempPath = "org/apache/jsp/" + uri;
        File tempFile = FileUtil.file(ConstantTomcat.WORK_FOLDER, subFolder, tempPath);

        String fileNameOnly = tempFile.getName();
        String classFileName = JspUtil.makeJavaIdentifier(fileNameOnly);
        File servletFile = new File(tempFile.getParent(), classFileName);

        return servletFile.getAbsolutePath();
    }

    public static String getServletClassPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".class";
    }

    public static String getServletJavaPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".java";
    }

    public static String getJspServletClassName(String uri, String subFolder) {
        File tempFile = FileUtil.file(ConstantTomcat.WORK_FOLDER, subFolder);
        String tempPath = tempFile.getAbsolutePath() + File.separator;
        String servletPath = getServletPath(uri, subFolder);
        String jspServletClassPath = StrUtil.subAfter(servletPath, tempPath, false);
        return StrUtil.replace(jspServletClassPath, File.separator, ".");
    }
}
