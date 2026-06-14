package com.hellokaton.blade.mvc.handler;

import com.hellokaton.blade.mvc.RouteContext;
import com.hellokaton.blade.mvc.WebContext;
import com.hellokaton.blade.mvc.hook.WebHook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for handler functional interfaces: RequestHandler, RouteHandler,
 * ExceptionHandler, and WebHook
 */
public class RequestHandlerTest {

    // ======================== RequestHandler ========================

    @Test
    void testRequestHandlerFunctionalInterface() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        RequestHandler handler = webContext -> called.set(true);

        WebContext mockContext = mock(WebContext.class);
        handler.handle(mockContext);

        assertTrue(called.get());
    }

    @Test
    void testRequestHandlerWithException() {
        RequestHandler handler = webContext -> {
            throw new RuntimeException("test error");
        };

        WebContext mockContext = mock(WebContext.class);
        assertThrows(RuntimeException.class, () -> handler.handle(mockContext));
    }

    // ======================== RouteHandler ========================

    @Test
    void testRouteHandlerFunctionalInterface() {
        AtomicBoolean called = new AtomicBoolean(false);
        RouteHandler handler = context -> called.set(true);

        RouteContext mockContext = mock(RouteContext.class);
        handler.handle(mockContext);

        assertTrue(called.get());
    }

    @Test
    void testRouteHandlerCapturesContext() {
        AtomicReference<RouteContext> captured = new AtomicReference<>();
        RouteHandler handler = captured::set;

        RouteContext mockContext = mock(RouteContext.class);
        handler.handle(mockContext);

        assertSame(mockContext, captured.get());
    }

    // ======================== ExceptionHandler.isResetByPeer ========================

    @Test
    void testIsResetByPeer_true() {
        Exception e = new IOException("Connection reset by peer");
        assertTrue(ExceptionHandler.isResetByPeer(e));
    }

    @Test
    void testIsResetByPeer_false() {
        Exception e = new IOException("File not found");
        assertFalse(ExceptionHandler.isResetByPeer(e));
    }

    @Test
    void testIsResetByPeer_nullMessage() {
        Exception e = new Exception((String) null);
        assertFalse(ExceptionHandler.isResetByPeer(e));
    }

    @Test
    void testIsResetByPeer_connectException() {
        ConnectException e = new ConnectException("Connection reset by peer");
        assertTrue(ExceptionHandler.isResetByPeer(e));
    }

    // ======================== ExceptionHandler.handle ========================

    @Test
    void testExceptionHandlerHandle() {
        AtomicReference<Exception> captured = new AtomicReference<>();
        ExceptionHandler handler = captured::set;

        Exception testException = new RuntimeException("test");
        handler.handle(testException);

        assertSame(testException, captured.get());
    }

    @Test
    void testExceptionHandlerVariableStacktrace() {
        assertEquals("stackTrace", ExceptionHandler.VARIABLE_STACKTRACE);
    }

    // ======================== WebHook ========================

    @Test
    void testWebHookBefore() {
        WebHook hook = context -> true;

        RouteContext mockContext = mock(RouteContext.class);
        assertTrue(hook.before(mockContext));
    }

    @Test
    void testWebHookBeforeReturnsFalse() {
        WebHook hook = context -> false;

        RouteContext mockContext = mock(RouteContext.class);
        assertFalse(hook.before(mockContext));
    }

    @Test
    void testWebHookAfterDefault() {
        // after() has a default implementation returning true
        WebHook hook = context -> true;

        RouteContext mockContext = mock(RouteContext.class);
        assertTrue(hook.after(mockContext));
    }
}
