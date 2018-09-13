import com.azimolabs.errorhandler.HandleErrorProcessor
import com.google.testing.compile.JavaFileObjects
import spock.lang.Specification
import spock.lang.Unroll

import javax.tools.JavaFileObject

import static com.google.testing.compile.CompilationSubject.assertThat
import static com.google.testing.compile.Compiler.javac

class ProcessorOutputErrorsTest extends Specification {

    public static
    final JavaFileObject MISSING_ERROR_CODE = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import ErrorCode;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode
                        void errorFound(String error);
                    }""")
    public static
    final JavaFileObject MISSING_ERROR_FIELD = JavaFileObjects.forSourceLines("TestListener",
            """package test;
                    import AutoHandler;
                    import DefaultError;
                    import ErrorCode;
                    import java.util.Map;
                    @AutoHandler
                    public interface TestListener {
                        @ErrorCode("errorCode")
                        void errorFound(String error);
                    }""")

    @Unroll
    def "Class with #fault"() {
        given:
        def compilation = javac()
                .withProcessors(new HandleErrorProcessor())
                .compile(file)
        expect:
        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContainingMatch(errorMessage)
        where:
        file << [MISSING_ERROR_CODE, MISSING_ERROR_FIELD]
        errorMessage << ["Method errorFound marked with @ErrorCode must have one of those specified: `value`, `code` or `codes`",
                         "Parameter error in method errorFound must be annotated with @ErrorField"]
        fault << ["missing error code", "missing error field"]
    }
}
