package com.hellokaton.blade.mvc.handler;

import com.hellokaton.blade.mvc.WebContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the {@link RequestHandler} functional interface contract.
 */
public class RequestHandlerTest {

    @Test
    public void testHandleReceivesWebContext() throws Exception {
        WebContext webContext = mock(WebContext.class);
        AtomicReference<WebContext> captured = new AtomicReference<>();

        RequestHandler handler = captured::set;
        handler.handle(webContext);

        assertSame(webContext, captured.get());
    }

    @Test
    public void testHandlePropagatesException() {
        RequestHandler handler = ctx -> {
            throw new IllegalStateException("boom");
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> handler.handle(mock(WebContext.class)));
        assertEquals("boom", ex.getMessage());
    }

}
