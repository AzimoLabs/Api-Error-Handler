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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 28/05/2018.
 */
public class DelegatedErrorListenerHandlerTest {

    DelegatedErrorListenerHandler tested;
    @Mock
    DelegatedErrorListener listener;
    @Mock
    ErrorLogger logger;
    @Mock
    ErrorPayload payload;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tested = new DelegatedErrorListenerHandler(listener, logger);
    }

    @Test
    public void test_multipleFieldsError() {
        Map<String, Object> errors = new HashMap<>();
        errors.put("one", Collections.singletonMap("two", "errorValueOne"));

        when(payload.errors()).thenReturn(errors);

        boolean result = tested.handle(payload);

        verify(listener).fieldsError("errorValueOne");
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        when(payload.code()).thenReturn("422");

        result = tested.handle(payload);
        verify(listener).fieldsError("errorValueOne");
        verify(payload, never()).code();
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

        result = tested.handle(payload);

        verify(listener).fieldsError("errorValueThree");
        verify(payload, never()).code();
        verifyNoMoreInteractions(listener);
        assertTrue(result);

        reset(listener);

        result = tested.handle(payload);

        verify(listener).fieldsError("errorValueThree");
        verify(payload, never()).code();
        verifyNoMoreInteractions(listener);
        assertTrue(result);
    }
}