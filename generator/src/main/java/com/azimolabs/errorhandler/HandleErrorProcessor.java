package com.azimolabs.errorhandler;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class HandleErrorProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private ClassValidityChecker classValidityChecker;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        classValidityChecker = new ClassValidityChecker();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(AutoHandler.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        GeneratedClassesRegistry registry = new GeneratedClassesRegistry(new com.azimolabs.errorhandler.MethodWritter());
        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoHandler.class)) {
                if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                    throw new com.azimolabs.errorhandler.ProcessingException(annotatedElement, "Only interfaces can be annotated with @%s",
                            AutoHandler.class.getSimpleName());
                }
                HandleErrorAnnotatedClass annotatedClass = new HandleErrorAnnotatedClass((TypeElement) annotatedElement);

                classValidityChecker.checkValidClass(annotatedClass);

                registry.add(annotatedClass);
            }
            registry.generateCode(elementUtils, filer);
        } catch (com.azimolabs.errorhandler.ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.fillInStackTrace().getMessage());
            error(null, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return true;
    }

    /**
     * Prints an error message
     *
     * @param e   The element which has caused the error. Can be null
     * @param msg The error message
     */
    public void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
