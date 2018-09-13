## Introduction

Declarative error handling. Define expected behaviour of API and don't worry about actual implementation. It uses your annotations to generate code that will make sure your interface methods get called when specific error code is returned from API. 

## Usage
Specify interface with methods that you want called for different API error codes.

```java
    @AutoHandler // let compiler know to analyse your class
    public interface SimplerErrorListener { // your code has to implement this interface

        @ErrorCode("422") 
        void userError();

        @ErrorCode(codes = {"500", "501", "503"}) // expected error codes
        void multiple();

    }
```
    
This will generate implementation calling matching methods when specified error codes are encountered:

```java
    @Generated("com.azimolabs.errorhandler")
    public class SimplerErrorListenerHandler implements ErrorHandler {
      private final ErrorLogger errorLogger;
    
      private final SimplerErrorListener listener;
    
      @Inject
      public SimplerErrorListenerHandler(SimplerErrorListener listener, ErrorLogger errorLogger) {
        this.listener = listener;
        this.errorLogger = errorLogger;
      }
    
      public boolean handle(ErrorPayload error) {
        Map<String, Object> errors = error.errors();
        boolean handled = false;
        switch(error.code()) {
          case "422": {
            listener.userError();
            return true;
          }
          case "500": {
            listener.multiple();
            return true;
          }
          case "501": {
            listener.multiple();
            return true;
          }
          case "503": {
            listener.multiple();
            return true;
          }
          default:  {
            return false;
          }
        }
      }
    }
```

### Advanced features: 
* payload fields to give meaningful error messages to the user, 

```json
      {
        "error": "422",
        "errors": {
          "user": "Email already taken"
        }
      }    
      
```
* nested fields: 

```json
      {
        "error": "422",
        "errors": {
          "user": {
            "phoneNumber": "Invalid phone number"
          }
        }
      }    
```
  
* report error to your bug tracking backend with one annotation,
* collapse field messages and pass to UI when form config is dynamic,
* delegated error handler when logic is too complex or awkward to use regular way,

## Public API
* @AutoHandler - class level annotation to let javac annotation processor know that it should analyse contents of your class.
* @ErrorCode - response field code returned by your backend. Optionally allows you to specify whether error is not expected and should be reported to your crash service. 
* @ErrorField - response field name returned by your backend. Supports nesting and reporting to crash services. 
* @ErrorLogger - if you want to report errors, you need to provide implementation of this interface. 
* @ErrorPayload - as error responses vary, this is the minimal model you need to provide. Your error response should expose `String code()` (used by `@ErrorCode` to match errors to methods), `String requestId()` (required for crash tracking), `String message()` (developer friendly message) and `Map<String, Object> errors` that are going to be analysed for error messages for the user. 
 
// TODO: add to your project

## Example usages
* [SimplerErrorListener - basic example, error codes without messages](example/src/main/java/com/azimolabs/errorhandler/example/SimplerErrorListener.java)
* [FieldsErrorListener - basic example, error codes without messages](example/src/main/java/com/azimolabs/errorhandler/example/FieldsErrorListener.java)
* [ErrorEnabledView - you can use generic interface, additional, non-annotated methods are also available](example/src/main/java/com/azimolabs/errorhandler/example/ErrorEnabledView.java)
* [DynamicErrorListener - collapse error messages and pass it to dynamic form, with unspecified number of fields](example/src/main/java/com/azimolabs/errorhandler/example/DynamicErrorListener.java)
* [DelegatingErrorListener - specify which case requires custom logic](example/src/main/java/com/azimolabs/errorhandler/example/DelegatingErrorListener.java)
* [DelegatedErrorListener - custom handling, write logic for exceptional flow only](example/src/main/java/com/azimolabs/errorhandler/example/DelegatedErrorListener.java)


