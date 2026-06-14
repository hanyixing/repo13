package com.hellokaton.blade.server;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.mvc.WebContext;
import com.hellokaton.blade.mvc.handler.ExceptionHandler;
import com.hellokaton.blade.mvc.http.HttpMethod;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HttpServerHandler
 */
@ExtendWith(MockitoExtension.class)
public class HttpServerHandlerTest {

    @Mock
    private ChannelHandlerContext ctx;

    @BeforeAll
    static void initBlade() {
        WebContext.init(Blade.create(), "/");
    }

    // ======================== channelReadComplete ========================

    @Test
    void testChannelReadComplete() {
        HttpServerHandler handler = new HttpServerHandler();
        handler.channelReadComplete(ctx);

        verify(ctx).flush();
    }

    // ======================== exceptionCaught ========================

    @Test
    void testExceptionCaught_nonResetByPeer() {
        HttpServerHandler handler = new HttpServerHandler();
        Throwable cause = new IOException("some error");

        // ctx.writeAndFlush() must return a ChannelFuture (code chains .addListener())
        ChannelFuture mockFuture = mock(ChannelFuture.class);
        when(ctx.writeAndFlush(any(FullHttpResponse.class))).thenReturn(mockFuture);

        handler.exceptionCaught(ctx, cause);

        // Should write a 500 response and close the connection
        verify(ctx).writeAndFlush(any(FullHttpResponse.class));
        verify(mockFuture).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    void testExceptionCaught_resetByPeer() {
        HttpServerHandler handler = new HttpServerHandler();
        Throwable cause = new IOException("Connection reset by peer");

        // Should NOT write a response when connection is reset by peer
        handler.exceptionCaught(ctx, cause);

        verify(ctx, never()).writeAndFlush(any(FullHttpResponse.class));
    }

    // ======================== isStaticFile (via reflection) ========================

    @Test
    void testIsStaticFile_faviconPath() throws Exception {
        HttpServerHandler handler = new HttpServerHandler();
        Method method = HttpServerHandler.class.getDeclaredMethod("isStaticFile", HttpMethod.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, HttpMethod.GET, "/favicon.ico");
        assertTrue(result);
    }

    @Test
    void testIsStaticFile_nonGetMethod() throws Exception {
        HttpServerHandler handler = new HttpServerHandler();
        Method method = HttpServerHandler.class.getDeclaredMethod("isStaticFile", HttpMethod.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, HttpMethod.POST, "/static/app.js");
        assertFalse(result);
    }

    @Test
    void testIsStaticFile_nonStaticPath() throws Exception {
        HttpServerHandler handler = new HttpServerHandler();
        Method method = HttpServerHandler.class.getDeclaredMethod("isStaticFile", HttpMethod.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, HttpMethod.GET, "/api/users");
        assertFalse(result);
    }

    @Test
    void testIsStaticFile_staticPath() throws Exception {
        HttpServerHandler handler = new HttpServerHandler();
        Method method = HttpServerHandler.class.getDeclaredMethod("isStaticFile", HttpMethod.class, String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(handler, HttpMethod.GET, "/static/app.js");
        assertTrue(result);
    }

    // ======================== ExceptionHandler.isResetByPeer integration ========================

    @Test
    void testIsResetByPeer_nullException() {
        // Test the static utility from ExceptionHandler used by HttpServerHandler
        assertFalse(ExceptionHandler.isResetByPeer(new Exception((String) null)));
    }

    @Test
    void testIsResetByPeer_validResetMessage() {
        assertTrue(ExceptionHandler.isResetByPeer(new IOException("Connection reset by peer")));
    }
}
