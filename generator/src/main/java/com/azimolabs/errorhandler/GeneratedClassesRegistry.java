package com.azimolabs.errorhandler;

import com.azimolabs.errorhandler.utils.Collections;
import com.azimolabs.errorhandler.utils.Strings;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.azimolabs.errorhandler.AutoHandler.GENERATED_CLASS_SUFFIX;

public class GeneratedClassesRegistry {

    private final Map<String, HandleErrorAnnotatedClass> handlers;
    private final MethodWritter methodWritter;

    public GeneratedClassesRegistry(MethodWritter methodWritter) {
        this.methodWritter = methodWritter;
        this.handlers = new LinkedHashMap<>();
    }

    public void add(HandleErrorAnnotatedClass toInsert) {
        handlers.put(toInsert.getId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        for (HandleErrorAnnotatedClass annotatedClass : handlers.values()) {
            // write file for each class
            TypeElement listenerElement = annotatedClass.getTypeElement();
            com.azimolabs.errorhandler.model.ErrorHandler errorHandler = new com.azimolabs.errorhandler.model.ErrorHandler(annotatedClass.getTypeElement());
            String className;
            if (com.google.common.base.Strings.isNullOrEmpty(errorHandler.requestedClassName)) {
                className = listenerElement.getSimpleName() + GENERATED_CLASS_SUFFIX;
            } else {
                className = errorHandler.requestedClassName;
            }
            PackageElement pkg = elementUtils.getPackageOf(listenerElement);
            String packageName = pkg.isUnnamed() ? "empty" : pkg.getQualifiedName().toString();

            // when defer is found, create new class implementing delegate, pass on validation
            TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "com.azimolabs.errorhandler").build())
                    .addSuperinterface(errorHandler.isDelegate ? com.azimolabs.errorhandler.DelegatedErrorHandler.class : ErrorHandler.class)
                    .addField(com.azimolabs.errorhandler.ErrorLogger.class, "errorLogger", Modifier.FINAL, Modifier.PRIVATE)
                    .addField(TypeName.get(listenerElement.asType()), "listener", Modifier.FINAL, Modifier.PRIVATE)
                    // method should take name of method defined in 'implementing' interface
                    .addMethod(MethodSpec.methodBuilder("handle")
                            .returns(TypeName.BOOLEAN)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ErrorPayload.class, "error")
                            .addStatement("$T<$T, $T> errors = error.errors()", Map.class, String.class, Object.class)
                            .addStatement("boolean handled = false")
                            .addCode(handlingBody(errorHandler))
                            .build());
            if (errorHandler.dynamicMethod) {
                builder.addMethod(unpack());
            }
            MethodSpec.Builder constructor = buildConstructor(listenerElement, ErrorLogger.class);
            if (Collections.notEmpty(errorHandler.delegatedCodes)) {
                ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(Map.class, String.class, DelegatedErrorHandler.class);
                builder.addField(parameterizedTypeName, "delegate", Modifier.FINAL, Modifier.PRIVATE);
                constructor.addParameter(parameterizedTypeName, "delegate")
                        .addStatement("this.delegate = delegate");
            }
            builder.addMethod(constructor.build());
            JavaFile.builder(packageName, builder
                    .build())
                    .build()
                    .writeTo(filer);
        }
    }

    private CodeBlock handlingBody(com.azimolabs.errorhandler.model.ErrorHandler errorHandler) {
        if (errorHandler.isDelegate) {
            return ifBody(errorHandler);
        } else {
            return CodeBlock.builder()
                    .beginControlFlow("switch(error.code())")
                    .add(switchBody(errorHandler))
                    .endControlFlow()
                    .build();
        }
    }

    private MethodSpec.Builder buildConstructor(TypeElement classElement, Class<?>... clazzes) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addAnnotation(Inject.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(classElement.asType()), "listener")
                .addStatement("this.listener = listener");
        for (Class<?> clazz : clazzes) {
            String variableName = Strings.variableName(clazz);
            builder.addParameter(clazz, variableName)
                    .addStatement("this.$N = $N", variableName, variableName);
        }
        return builder;
    }

    private CodeBlock switchBody(com.azimolabs.errorhandler.model.ErrorHandler errorHandler) {
        return methodWritter.write(errorHandler)
                .beginControlFlow("default: ")
                .addStatement("return false")
                .endControlFlow()
                .build();
    }

    private CodeBlock ifBody(com.azimolabs.errorhandler.model.ErrorHandler errorHandler) {
        return methodWritter.ifBody(errorHandler)
                .addStatement("return handled").build();
    }

    private MethodSpec unpack() {
        ParameterizedTypeName mapResult = ParameterizedTypeName.get(Map.class, String.class, String.class);
        ParameterizedTypeName mapInput = ParameterizedTypeName.get(Map.class, String.class, Object.class);
        ParameterizedTypeName entry = ParameterizedTypeName.get(Map.Entry.class, String.class, Object.class);

        return MethodSpec.methodBuilder("unpack")
                .returns(mapResult)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(mapInput, "value")
                .addParameter(mapResult, "result")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("for ($T entry : value.entrySet())", entry)
                        .beginControlFlow("if (entry.getValue() instanceof $T)", Map.class)
                        .addStatement("result.putAll(unpack(($T) entry.getValue(), result))", mapInput)
                        .nextControlFlow("else")
                        .addStatement("result.put(entry.getKey(), (String) entry.getValue())")
                        .endControlFlow()
                        .endControlFlow()
                        .build())
                .addStatement("return result")
                .build();
    }

}