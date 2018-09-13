package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.ErrorLogger;
import com.azimolabs.errorhandler.ErrorPayload;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 28/05/2018.
 */
public class FieldsErrorListenerHandlerTest {
    FieldsErrorListenerHandler tested;
    @Mock
    FieldsErrorListener listener;
    @Mock
    ErrorLogger logger;
    @Mock
    ErrorPayload payload;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tested = new FieldsErrorListenerHandler(listener, logger);
    }

    @Test
    public void test_oneFieldError() {
        Map<String, Object> errors = new HashMap<>();
        errors.put("levelOne", Collections.singletonMap("levelTwo", "errorValue"));

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("404");

        boolean result = tested.handle(payload);

        verify(listener).oneFieldError("errorValue");
        verifyNoMoreInteractions(listener);

        assertTrue(result);
    }

    @Test
    public void test_multipleFieldsError() {
        Map<String, Object> errors = new HashMap<>();
        errors.put("one", Collections.singletonMap("two", "errorValueOne"));

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("404");

        boolean result = tested.handle(payload);

        verify(listener).multipleFieldsError("errorValueOne");
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("422");

        result = tested.handle(payload);
        verify(listener).multipleFieldsError("errorValueOne");
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        errors = new HashMap<>();
        Map<String, Object> errorsLevel2 = new HashMap<>();
        Map<String, Object> errorsLevel3 = new HashMap<>();
        errorsLevel3.put("other3", "errorValueThree");
        errorsLevel2.put("other2", errorsLevel3);
        errors.put("other", errorsLevel2);

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("404");

        result = tested.handle(payload);

        verify(listener).multipleFieldsError("errorValueThree");
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("422");

        result = tested.handle(payload);

        verify(listener).multipleFieldsError("errorValueThree");
        verifyNoMoreInteractions(listener);
        assertTrue(result);
    }

    @Test
    public void test_clientError() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("408");

        boolean result = tested.handle(payload);

        verify(listener).clientError();
        verifyNoMoreInteractions(listener);
        verify(logger).logException(any());
        assertTrue(result);
    }

    @Test
    public void test_defaultError() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("404");

        boolean result = tested.handle(payload);

        verify(listener).defaultError();
        verify(logger).logException(any());
        assertFalse(result);
    }

    @Test
    public void test_unrecognisedError() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("420");

        boolean result = tested.handle(payload);

        verifyNoMoreInteractions(listener);
        verifyNoMoreInteractions(logger);
        assertFalse(result);
    }

    @Test
    public void test_unexpectedError() {
        Map<String, Object> errors = new HashMap<>();
        Map<String, Object> errors2 = new HashMap<>();
        errors2.put("two", "errorMessage");
        errors.put("one", errors2);

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("425");

        boolean result = tested.handle(payload);

        verify(listener).unexpected("errorMessage");
        verify(logger).logException(any());
        assertTrue(result);
    }

    @Test
    public void test_unexpectedError_whenErrorMissing() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("425");

        boolean result = tested.handle(payload);

        verify(listener, never()).unexpected(anyString());
        // one for 'unexpected'
        // one for 'unhandled'
        verify(logger, times(2)).logException(any());
        verify(listener).defaultError();
        assertFalse(result);
    }
}