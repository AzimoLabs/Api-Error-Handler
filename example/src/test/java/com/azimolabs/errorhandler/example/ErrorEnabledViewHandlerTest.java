package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.ErrorLogger;
import com.azimolabs.errorhandler.ErrorPayload;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 20/04/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class ErrorEnabledViewHandlerTest {

    ErrorEnabledViewHandler tested;
    @Mock
    ErrorEnabledView listener;
    @Mock
    ErrorLogger logger;
    @Mock
    ErrorPayload payload;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tested = new ErrorEnabledViewHandler(listener, logger);
    }

    @Test
    public void test_passErrors() {
        Map<String, Object> errors = Collections.singletonMap("error", "value");

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("11");

        boolean result = tested.handle(payload);

        verify(listener).passErrors(Collections.singletonMap("error", "value"));
        verifyNoMoreInteractions(logger);

        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("22");
        result = tested.handle(payload);

        verify(listener).passErrors(Collections.singletonMap("error", "value"));
        verifyNoMoreInteractions(logger);

        assertTrue(result);
    }

    @Test
    public void test_handle_WhenComplex() {
        Map<String, Object> errors = new HashMap<>();
        errors.put("value1", Collections.singletonMap("key11", "value11"));
        Map<String, Object> errors2 = new HashMap<>();
        errors2.put("key21", ImmutableMap.of("key31", "value31", "key32", "value32"));
        errors.put("value2", errors2);

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("11");

        boolean result = tested.handle(payload);

        verify(listener).passErrors(ImmutableMap.of("key11", "value11",
                "key31", "value31", "key32", "value32"));
        verifyNoMoreInteractions(logger);

        assertTrue(result);
    }

    @Test
    public void test_showFormError() {
        Map<String, Object> errors = new HashMap<>();
        errors.put("value1", Collections.singletonMap("key11", "value11"));
        Map<String, Object> errors2 = new HashMap<>();
        errors2.put("key21", ImmutableMap.of("key31", "value31", "key32", "value32"));
        errors.put("value2", errors2);

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("11");

        boolean result = tested.handle(payload);

        verify(listener).passErrors(ImmutableMap.of("key11", "value11",
                "key31", "value31", "key32", "value32"));

        assertTrue(result);
    }
}