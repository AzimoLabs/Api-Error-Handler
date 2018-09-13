package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.ErrorLogger;
import com.azimolabs.errorhandler.ErrorPayload;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 28/05/2018.
 */
public class SimplerErrorListenerHandlerTest {
    SimplerErrorListenerHandler tested;
    @Mock
    SimplerErrorListener listener;
    @Mock
    ErrorLogger logger;
    @Mock
    ErrorPayload payload;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tested = new SimplerErrorListenerHandler(listener, logger);
    }

    @Test
    public void test_handle_whenOneErrorCode() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("422");

        boolean result = tested.handle(payload);

        verify(listener).userError();
        verifyNoMoreInteractions(listener);

        assertTrue(result);
    }

    @Test
    public void test_handle_whenMultipleErrorCodes() {
        Map<String, Object> errors = new HashMap<>();

        when(payload.errors()).thenReturn(errors);
        when(payload.code()).thenReturn("500");

        boolean result = tested.handle(payload);

        verify(listener).multiple();
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("501");

        result = tested.handle(payload);

        verify(listener).multiple();
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("503");

        result = tested.handle(payload);

        verify(listener).multiple();
        verifyNoMoreInteractions(listener);
        assertTrue(result);

    }
}