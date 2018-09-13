import com.azimolabs.errorhandler.HandleErrorProcessor
import com.google.testing.compile.JavaFileObjects
import spock.lang.Specification

import javax.tools.JavaFileObject

import static com.google.testing.compile.CompilationSubject.assertThat
import static com.google.testing.compile.Compiler.javac

class ProcessorOutputPassesTest extends Specification {


    public static final JavaFileObject EMPTY = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    @AutoHandler
                    public interface TestListener {
                    }""")

    public static final JavaFileObject MINIMAL = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode("errorCode")
                        void errorFound();
                    }""")


    public static
    final JavaFileObject DYNAMIC_PARAMS = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import ErrorField;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode("errorCode")
                        void errorFound(@ErrorField("errorField") String error);
                        
                        @DefaultError
                        void defaultError();
                    }""")
    public static
    final JavaFileObject PASSING_ERRORS = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import ErrorField;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode(value = "errorCode", passErrors = true)
                        void errorFound(Map<String, String> errors);
                    }""")


    def "Class with only annotation should compile"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(EMPTY)
        expect:
        assertThat(compilation).succeeded()
    }

    def "Class minimal correct compile"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(MINIMAL)
        expect:
        assertThat(compilation).succeeded()
    }

    def "Class dynamic parameters"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(DYNAMIC_PARAMS)
        expect:
        assertThat(compilation).succeeded()
    }

    def "Class pass errors"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(PASSING_ERRORS)
        expect:
        assertThat(compilation).succeeded()
    }
}
