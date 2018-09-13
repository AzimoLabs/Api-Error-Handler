import com.azimolabs.errorhandler.HandleErrorProcessor
import com.google.testing.compile.JavaFileObjects
import spock.lang.Specification
import spock.lang.Stepwise

import javax.tools.JavaFileObject

import static com.google.testing.compile.CompilationSubject.assertThat
import static com.google.testing.compile.Compiler.javac

@Stepwise
class ProcessorOutputPassErrorsTest extends Specification {

    public static
    final JavaFileObject PASS_ERRORS_WITHOUT_ERROR_MAP = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode(code = "errorCode", passErrors = true)
                        void dynamicErrors();
                    }""")
    public static
    final JavaFileObject PASS_ERRORS_WITH_DEFAULT_ERROR = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode(code = "errorCode", passErrors = true)
                        void dynamicErrors(Map<String, String> errors);
                        
                        @DefaultError
                        void defaultError();
                    }""")
    public static
    final JavaFileObject PASS_ERRORS_WITH_DEFAULT_ERROR_AND_CALL_POST_HANDLING = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode(code = "errorCode", passErrors = true)
                        void dynamicErrors(Map<String, String> errors);
                        
                        @DefaultError(callPostHandling = true)
                        void defaultError();
                    }""")
    public static
    final JavaFileObject PASS_ERRORS_WITH_DEFAULT_ERROR_AND_ANOTHER_REQUIRING_DEFAULT = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;import ErrorField;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode(code = "errorCode", passErrors = true)
                        void dynamicErrors(Map<String, String> errors);
                        
                        @ErrorCode("other")
                        void anotherMethod(@ErrorField("other") String error);
                    }""")

    def "Class with passErrors must have single parameter Map<String, String>"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(PASS_ERRORS_WITHOUT_ERROR_MAP)
        expect:
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContainingMatch("Method dynamicErrors marked with `passErrors=true` must have parameter of type Map<String, String>")
    }

    def "Class with passErrors shouldn't contain DefaultError if callPostHandling = false"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(PASS_ERRORS_WITH_DEFAULT_ERROR)
        expect:
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Dynamic method (one that passes errors directly as map) and DefaultError doesn't make any sense together, unless default method is expected to be called at all times (callPostHandling = true)")
    }

    def "Class with passErrors shouldn't complain about DefaultError if callPostHandling = true"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(PASS_ERRORS_WITH_DEFAULT_ERROR_AND_CALL_POST_HANDLING)
        expect:
        assertThat(compilation).succeeded()
    }

    def "Class with passErrors and another method with dynamic field should contain DefaultError"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(PASS_ERRORS_WITH_DEFAULT_ERROR_AND_ANOTHER_REQUIRING_DEFAULT)
        expect:
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContainingMatch("You need to annotate one argument-less method with DefaultError")
    }

}
