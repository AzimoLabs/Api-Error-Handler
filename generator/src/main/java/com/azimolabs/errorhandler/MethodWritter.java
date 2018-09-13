package com.azimolabs.errorhandler;

import com.google.common.base.Strings;
import com.squareup.javapoet.CodeBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MethodWritter {

    public CodeBlock.Builder write(com.azimolabs.errorhandler.model.ErrorHandler errorHandler) {
        CodeBlock.Builder switchBody = CodeBlock.builder();
        for (Map.Entry<String, List<com.azimolabs.errorhandler.model.ErrorMethod>> codeMethod : errorHandler.methods.entrySet()) {
            String key = codeMethod.getKey();
            CodeBlock.Builder caseBody = CodeBlock.builder()
                    .beginControlFlow("case $S:", key);
            if (errorHandler.delegatedCodes.contains(key)) {
                caseBody.addStatement("handled = delegate.get(\"$N\").handle(error)", key);
                if (!errorHandler.handleAfterSuccessfulDelegate) {
                    caseBody.beginControlFlow("if(handled)")
                            .addStatement("return true")
                            .endControlFlow();
                }
            }
            for (int i = 0; i < codeMethod.getValue().size(); i++) {
                com.azimolabs.errorhandler.model.ErrorMethod method = codeMethod.getValue().get(i);
                String simpleName = method.methodName;
                if (method.clientError) {
                    caseBody.addStatement("errorLogger.logException(new $T(error.requestId()))", IllegalArgumentException.class);
                }
                if (method.passErrors && !Strings.isNullOrEmpty(method.dynamicParam)) {
                    caseBody.addStatement("listener.$N(unpack(error.errors(), new $T<>()))", simpleName, HashMap.class);
                    if (errorHandler.defaultAlwaysCalled) {
                        caseBody.addStatement("listener.$L()", errorHandler.defaultError);
                    }
                    caseBody.addStatement("return true");
                } else if (method.parameters.isEmpty()) {
                    caseBody.addStatement("listener.$N()", simpleName)
                            .addStatement("return true");
                } else {
                    caseBody.addStatement("errors = error.errors()");
                    for (com.azimolabs.errorhandler.model.ErrorParameter parameter : method.parameters) {
                        for (String errorField : parameter.errorFields) {
                            String[] errorFieldNesting = errorField.split("\\.");
                            Iterator<String> iterator = Arrays.asList(errorFieldNesting).iterator();
                            String last = errorFieldNesting[errorFieldNesting.length - 1];
                            caseBody.add(codeBlockForVariable(CodeBlock.builder()
                                    .beginControlFlow("if(errors.containsKey($S) && errors.get($S) != null && ((String) errors.get($S)).length() != 0)", last, last, last)
                                    .addStatement("listener.$N((String) errors.get(\"$N\"))", simpleName, last)
                                    .addStatement("handled = true")
                                    .addStatement(parameter.clientError ?
                                            "errorLogger.logException(new $T(error.requestId()))" : "", IllegalArgumentException.class)
                                    .endControlFlow()
                                    .build(), iterator));
                        }
                    }
                    if (i + 1 == codeMethod.getValue().size()) {
                        if (errorHandler.defaultAlwaysCalled) {
                            caseBody.addStatement("listener.$L()", errorHandler.defaultError);
                        }
                        if (errorHandler.returnHandled) {
                            caseBody.addStatement("return handled");
                        } else {
                            caseBody.beginControlFlow("if(!handled)");
                            if (errorHandler.defaultUnexpected) {
                                caseBody.addStatement("errorLogger.logException(new $T(error.requestId()))", IllegalArgumentException.class);
                            }
                            caseBody
                                    .addStatement(errorHandler.defaultError == null ||
                                            errorHandler.defaultAlwaysCalled ? "" : "listener.$L()", errorHandler.defaultError)
                                    .addStatement("return $L", !errorHandler.defaultUnexpected)
                                    .nextControlFlow("else")
                                    .addStatement("return true")
                                    .endControlFlow();
                        }
                    }
                }
            }
            switchBody.add(caseBody.endControlFlow().build());
        }
        for (String delegatedCode : errorHandler.delegatedCodes) {
            if (!errorHandler.methods.containsKey(delegatedCode)) {
                switchBody.add(CodeBlock.builder()
                        .beginControlFlow("case $S:", delegatedCode)
                        .addStatement("return delegate.get(\"$N\").handle(error)", delegatedCode)
                        .endControlFlow()
                        .build());
            }
        }
        return switchBody;
    }

    private CodeBlock codeBlockForVariable(CodeBlock block, Iterator<String> path) {
        String last = path.next();
        if (path.hasNext()) {
            return CodeBlock.builder()
                    .beginControlFlow("if(errors.containsKey($S))", last)
                    .addStatement("errors = (Map<String, Object>) errors.get($S)", last)
                    .add(codeBlockForVariable(block, path))
                    .endControlFlow()
                    .build();
        } else {
            return block;
        }
    }

    public CodeBlock.Builder ifBody(com.azimolabs.errorhandler.model.ErrorHandler errorHandler) {
        CodeBlock.Builder switchBody = CodeBlock.builder();
        for (com.azimolabs.errorhandler.model.DelegateErrorMethod delegateErrorMethod : errorHandler.delegatedMethods) {
            CodeBlock.Builder caseBody = CodeBlock.builder();
            String simpleName = delegateErrorMethod.methodName;
            List<com.azimolabs.errorhandler.model.ErrorParameter> parameters = delegateErrorMethod.parameters;
            if (parameters.isEmpty()) {
                throw new ProcessingException(delegateErrorMethod.methodElement, String.format("Delegated handler does not support error codes without error fields (method %s)", simpleName));
            } else {
                caseBody.addStatement("errors = error.errors()");
                for (com.azimolabs.errorhandler.model.ErrorParameter parameter : parameters) {
                    for (String errorField : parameter.errorFields) {
                        String[] errorFieldNesting = errorField.split("\\.");
                        Iterator<String> iterator = Arrays.asList(errorFieldNesting).iterator();
                        String last = errorFieldNesting[errorFieldNesting.length - 1];
                        caseBody.add(codeBlockForVariable(CodeBlock.builder()
                                .beginControlFlow("if(errors.containsKey($S) && errors.get($S) != null && ((String) errors.get($S)).length() != 0)", last, last, last)
                                .addStatement("listener.$N((String) errors.get(\"$N\"))", simpleName, last)
                                .addStatement("handled = true")
                                .addStatement(parameter.clientError ?
                                        "errorLogger.logException(new $T(error.requestId()))" : "", IllegalArgumentException.class)
                                .endControlFlow()
                                .build(), iterator));
                    }
                }
            }
            switchBody.add(caseBody.build());
        }
        return switchBody;
    }
}
