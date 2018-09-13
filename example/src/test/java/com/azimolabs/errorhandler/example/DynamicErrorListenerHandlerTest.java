package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.ErrorLogger;
import com.azimolabs.errorhandler.ErrorPayload;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 20/04/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicErrorListenerHandlerTest {

    DynamicErrorListenerHandler tested;
    @Mock
    DynamicErrorListener listener;
    @Mock
    ErrorLogger logger;
    @Mock
    ErrorPayload payload;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tested = new DynamicErrorListenerHandler(listener, logger);
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
    }
}