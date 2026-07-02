package com.boot.scheduled.utils;

import com.boot.scheduled.common.BusinessException;
import lombok.extern.slf4j.Slf4j;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java源码运行时编译工具
 * <p>
 * 基于 javax.tools.JavaCompiler 实现将Java源码字符串编译为字节码并加载为Class。
 * 每次调用 compile() 创建独立的诊断收集器和文件管理器，确保线程安全。
 * </p>
 *
 * <p>使用示例:</p>
 * <pre>
 * ParseJavaCode compiler = new ParseJavaCode();
 * Class&lt;?&gt; clazz = compiler.compile("package com.example; public class Hello implements Runnable { ... }");
 * Runnable instance = (Runnable) clazz.getDeclaredConstructor().newInstance();
 * </pre>
 *
 * @author MiMoCode
 */
@Slf4j
public class ParseJavaCode {

    /** Java系统编译器（需要JDK环境，JRE下为null） */
    private static final JavaCompiler JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();

    /** 编译后的字节码缓存：key=类全名，value=编译输出的字节码对象 */
    private final Map<String, ByteJavaFileObject> compiledClasses = new ConcurrentHashMap<>();

    /** 编译器选项：启用类型检查、指定Java版本 */
    private static final List<String> COMPILER_OPTIONS;

    static {
        COMPILER_OPTIONS = new ArrayList<>();
        COMPILER_OPTIONS.add("-Xlint:unchecked");
        COMPILER_OPTIONS.add("-source");
        COMPILER_OPTIONS.add("11");
        COMPILER_OPTIONS.add("-target");
        COMPILER_OPTIONS.add("11");
    }

    /**
     * 编译Java源码并返回对应的Class对象
     *
     * @param code 完整的Java源代码（需包含package声明和类定义）
     * @return 编译后的Class对象
     * @throws BusinessException 编译失败或类加载失败时抛出
     */
    public Class<?> compile(String code) {
        if (JAVA_COMPILER == null) {
            throw new BusinessException("Java编译器不可用，需要JDK环境而非JRE");
        }

        String className = extractFullClassName(code);
        if (className.isEmpty()) {
            throw new BusinessException("无法从源码中提取类名，请检查package和class声明");
        }

        // 每次编译使用独立的DiagnosticCollector，避免线程安全问题
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileManager fileManager = createFileManager(diagnostics);

        try {
            // 构建源码对象
            CustomJavaObject sourceObject = new CustomJavaObject(className, code);

            // 创建编译任务
            JavaCompiler.CompilationTask task = JAVA_COMPILER.getTask(
                    null, fileManager, diagnostics, COMPILER_OPTIONS, null,
                    Collections.singletonList(sourceObject));

            // 执行编译
            Boolean success = task.call();
            if (!success || !diagnostics.getDiagnostics().isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("Java代码编译失败:\n");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errorMsg.append("  ").append(diagnostic.toString()).append("\n");
                }
                log.error("编译失败, className={}, errors={}", className, errorMsg);
                throw new BusinessException(errorMsg.toString());
            }

            // 加载编译后的字节码
            log.info("Java代码编译成功, className={}", className);
            return new CompiledClassClassLoader().findClass(className);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("编译过程异常, className={}", className, e);
            throw new BusinessException("编译过程异常: " + e.getMessage());
        } finally {
            closeFileManager(fileManager);
        }
    }

    /**
     * 从源码中提取完整类名（包含包名）
     *
     * @param sourceCode Java源码
     * @return 类全名（如 com.example.MyClass），提取失败返回空字符串
     */
    public static String extractFullClassName(String sourceCode) {
        String packageName = "";
        String className = "";

        // 提取package声明
        Pattern packagePattern = Pattern.compile("package\\s+\\S+\\s*;");
        Matcher packageMatcher = packagePattern.matcher(sourceCode);
        if (packageMatcher.find()) {
            packageName = packageMatcher.group()
                    .replaceFirst("package", "")
                    .replace(";", "")
                    .trim() + ".";
        }

        // 提取class名，支持 extends / implements / 无继承 三种格式
        Pattern classPattern = Pattern.compile("class\\s+\\S+\\s+(implements|extends|\\{)");
        Matcher classMatcher = classPattern.matcher(sourceCode);
        if (classMatcher.find()) {
            String matchStr = classMatcher.group();
            className = matchStr
                    .replaceFirst("class", "")
                    .replace("implements", "")
                    .replace("extends", "")
                    .replace("{", "")
                    .trim();
        }

        return packageName + className;
    }

    private JavaFileManager createFileManager(DiagnosticCollector<JavaFileObject> diagnostics) {
        JavaFileManager stdFileManager = JAVA_COMPILER.getStandardFileManager(diagnostics, null, null);
        return new CompiledClassFileManager(stdFileManager, compiledClasses);
    }

    private void closeFileManager(JavaFileManager fileManager) {
        try {
            if (fileManager != null) {
                fileManager.close();
            }
        } catch (Exception e) {
            log.warn("关闭文件管理器异常", e);
        }
    }

    // ======================== 内部类 ========================

    /** 编译后的字节码类加载器 */
    private static class CompiledClassClassLoader extends ClassLoader {

        private final Map<String, ByteJavaFileObject> classMap;

        CompiledClassClassLoader() {
            this.classMap = new ConcurrentHashMap<>();
        }

        void registerClass(String name, ByteJavaFileObject obj) {
            classMap.put(name, obj);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // 先从编译缓存中查找
            ByteJavaFileObject fileObject = classMap.isEmpty() ? null : classMap.get(name);
            if (fileObject == null) {
                // 尝试从系统类加载器加载
                try {
                    return ClassLoader.getSystemClassLoader().loadClass(name);
                } catch (Exception e) {
                    throw new ClassNotFoundException("无法加载类: " + name, e);
                }
            }
            byte[] bytes = fileObject.getCompiledBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    /** 源码字符串封装为JavaFileObject */
    private static class CustomJavaObject extends SimpleJavaFileObject {

        private final String contents;

        CustomJavaObject(String className, String contents) {
            super(URI.create("String:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return contents;
        }
    }

    /** 编译后的字节码封装为JavaFileObject */
    private static class ByteJavaFileObject extends SimpleJavaFileObject {

        private ByteArrayOutputStream byteStream;

        ByteJavaFileObject(String className, Kind kind) {
            super(URI.create("bytes:///" + className.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            byteStream = new ByteArrayOutputStream();
            return byteStream;
        }

        byte[] getCompiledBytes() {
            return byteStream != null ? byteStream.toByteArray() : new byte[0];
        }
    }

    /** 自定义文件管理器，将编译输出的字节码保存到内存 */
    private static class CompiledClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Map<String, ByteJavaFileObject> compiledClasses;

        CompiledClassFileManager(JavaFileManager delegate, Map<String, ByteJavaFileObject> compiledClasses) {
            super(delegate);
            this.compiledClasses = compiledClasses;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                                   String className,
                                                   JavaFileObject.Kind kind,
                                                   FileObject sibling) {
            ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
            compiledClasses.put(className, javaFileObject);
            return javaFileObject;
        }
    }
}
