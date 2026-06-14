package com.hellokaton.blade.mvc.route;

import com.hellokaton.blade.mvc.RouteContext;
import com.hellokaton.blade.mvc.handler.RouteHandler;
import com.hellokaton.blade.mvc.hook.WebHook;
import com.hellokaton.blade.mvc.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RouteMatcher
 */
public class RouteMatcherTest {

    private RouteMatcher routeMatcher;

    @BeforeEach
    void setUp() {
        routeMatcher = new RouteMatcher();
    }

    // ======================== addRoute & lookupRoute ========================

    @Test
    void testAddRouteWithGet() throws Exception {
        routeMatcher.addRoute("/hello", ctx -> ctx.text("Hello"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/hello");
        assertNotNull(route);
        assertEquals(HttpMethod.GET, route.getHttpMethod());
        assertEquals("/hello", route.getPath());
    }

    @Test
    void testAddRouteWithPost() throws Exception {
        routeMatcher.addRoute("/save", ctx -> ctx.text("Saved"), HttpMethod.POST);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("POST", "/save");
        assertNotNull(route);
        assertEquals(HttpMethod.POST, route.getHttpMethod());
        assertEquals("/save", route.getPath());
    }

    @Test
    void testAddRouteWithPut() throws Exception {
        routeMatcher.addRoute("/update", ctx -> ctx.text("Updated"), HttpMethod.PUT);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("PUT", "/update");
        assertNotNull(route);
        assertEquals(HttpMethod.PUT, route.getHttpMethod());
    }

    @Test
    void testAddRouteWithDelete() throws Exception {
        routeMatcher.addRoute("/remove", ctx -> ctx.text("Removed"), HttpMethod.DELETE);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("DELETE", "/remove");
        assertNotNull(route);
        assertEquals(HttpMethod.DELETE, route.getHttpMethod());
    }

    @Test
    void testAddRouteWithBuilder() {
        Route route = Route.builder()
                .httpMethod(HttpMethod.POST)
                .targetType(RouteHandler.class)
                .target((RouteHandler) ctx -> ctx.text("post request"))
                .path("/save")
                .build();
        routeMatcher.addRoute(route);
        routeMatcher.register();

        Route found = routeMatcher.lookupRoute("POST", "/save");
        assertNotNull(found);
        assertEquals("/save", found.getPath());
    }

    @Test
    void testLookupRouteNotFound() throws Exception {
        routeMatcher.addRoute("/exists", ctx -> ctx.text("ok"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/does-not-exist");
        assertNull(route);
    }

    @Test
    void testLookupRouteWrongMethod() throws Exception {
        routeMatcher.addRoute("/hello", ctx -> ctx.text("ok"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("POST", "/hello");
        assertNull(route);
    }

    @Test
    void testLookupRouteRoot() throws Exception {
        routeMatcher.addRoute("/", ctx -> ctx.text("Root"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/");
        assertNotNull(route);
        assertEquals("/", route.getPath());
    }

    @Test
    void testRouteToStringContainsMethodAndPath() throws Exception {
        routeMatcher.addRoute("/test", ctx -> ctx.text("ok"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/test");
        assertNotNull(route);
        String routeStr = route.toString();
        assertTrue(routeStr.contains("GET"));
        assertTrue(routeStr.contains("/test"));
    }

    // ======================== route() with class/method ========================

    @Test
    void testRouteWithClassMethod() {
        routeMatcher.route("/index", DemoController.class, "index");
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/index");
        assertNotNull(route);
        assertEquals(HttpMethod.ALL, route.getHttpMethod());
    }

    @Test
    void testRouteWithClassMethodAndHttpMethod() {
        routeMatcher.route("/remove", DemoController.class, "remove", HttpMethod.DELETE);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("DELETE", "/remove");
        assertNotNull(route);
        assertEquals(HttpMethod.DELETE, route.getHttpMethod());
    }

    @Test
    void testRouteWithMethodNameContainingHttpMethod() {
        routeMatcher.route("/list", DemoController.class, "GET:list");
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/list");
        assertNotNull(route);
        assertEquals(HttpMethod.GET, route.getHttpMethod());
    }

    // ======================== Hooks (Before/After) ========================

    @Test
    void testHasBeforeHook_true() throws Exception {
        routeMatcher.addRoute("/*", ctx -> ctx.text("before"), HttpMethod.BEFORE);
        assertTrue(routeMatcher.hasBeforeHook());
    }

    @Test
    void testHasBeforeHook_false() {
        assertFalse(routeMatcher.hasBeforeHook());
    }

    @Test
    void testHasAfterHook_true() throws Exception {
        routeMatcher.addRoute("/*", ctx -> ctx.text("after"), HttpMethod.AFTER);
        assertTrue(routeMatcher.hasAfterHook());
    }

    @Test
    void testHasAfterHook_false() {
        assertFalse(routeMatcher.hasAfterHook());
    }

    @Test
    void testGetBefore() throws Exception {
        routeMatcher.addRoute("/*", ctx -> ctx.text("before"), HttpMethod.BEFORE);
        routeMatcher.register();

        List<Route> befores = routeMatcher.getBefore("/anything");
        assertNotNull(befores);
        assertEquals(1, befores.size());
    }

    @Test
    void testGetAfter() throws Exception {
        routeMatcher.addRoute("/*", ctx -> ctx.text("after"), HttpMethod.AFTER);
        routeMatcher.register();

        List<Route> afters = routeMatcher.getAfter("/anything");
        assertNotNull(afters);
        assertEquals(1, afters.size());
    }

    @Test
    void testGetBeforeEmpty() {
        List<Route> befores = routeMatcher.getBefore("/test");
        assertNotNull(befores);
        assertTrue(befores.isEmpty());
    }

    @Test
    void testGetAfterEmpty() {
        List<Route> afters = routeMatcher.getAfter("/test");
        assertNotNull(afters);
        assertTrue(afters.isEmpty());
    }

    // ======================== Middleware ========================

    @Test
    void testMiddlewareCountInitiallyZero() {
        assertEquals(0, routeMatcher.middlewareCount());
    }

    @Test
    void testGetMiddlewareInitiallyNull() {
        assertNull(routeMatcher.getMiddleware());
    }

    @Test
    void testAddMiddleware() {
        WebHook webHook = ctx -> true;
        routeMatcher.addMiddleware(webHook);

        assertEquals(1, routeMatcher.middlewareCount());
        assertNotNull(routeMatcher.getMiddleware());
        assertEquals(1, routeMatcher.getMiddleware().size());
    }

    @Test
    void testAddMultipleMiddleware() {
        WebHook hook1 = ctx -> true;
        WebHook hook2 = ctx -> true;
        routeMatcher.addMiddleware(hook1);
        routeMatcher.addMiddleware(hook2);

        assertEquals(2, routeMatcher.middlewareCount());
    }

    // ======================== Path Variables ========================

    @Test
    void testLookupRouteWithPathVariable() throws Exception {
        routeMatcher.addRoute("/user/:id", ctx -> ctx.text("user"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/user/123");
        assertNotNull(route);
    }

    @Test
    void testLookupRouteWithMultiplePathVariables() throws Exception {
        routeMatcher.addRoute("/user/:userId/post/:postId", ctx -> ctx.text("post"), HttpMethod.GET);
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/user/1/post/42");
        assertNotNull(route);
    }

    // ======================== Duplicate Route ========================

    @Test
    void testAddDuplicateRoute() throws Exception {
        routeMatcher.addRoute("/dup", ctx -> ctx.text("first"), HttpMethod.GET);
        routeMatcher.addRoute("/dup", ctx -> ctx.text("second"), HttpMethod.GET);
        routeMatcher.register();

        // Should still resolve to a route (the second one overwrites)
        Route route = routeMatcher.lookupRoute("GET", "/dup");
        assertNotNull(route);
    }

    // ======================== Multiple routes ========================

    @Test
    void testMultipleRoutesForDifferentMethods() throws Exception {
        routeMatcher.addRoute("/resource", ctx -> ctx.text("get"), HttpMethod.GET);
        routeMatcher.addRoute("/resource", ctx -> ctx.text("post"), HttpMethod.POST);
        routeMatcher.addRoute("/resource", ctx -> ctx.text("put"), HttpMethod.PUT);
        routeMatcher.register();

        assertNotNull(routeMatcher.lookupRoute("GET", "/resource"));
        assertNotNull(routeMatcher.lookupRoute("POST", "/resource"));
        assertNotNull(routeMatcher.lookupRoute("PUT", "/resource"));
        assertNull(routeMatcher.lookupRoute("DELETE", "/resource"));
    }

    // ======================== Helper Controller ========================

    public static class DemoController {
        public void index() {
        }

        public void remove() {
        }

        public void list() {
        }
    }
}
