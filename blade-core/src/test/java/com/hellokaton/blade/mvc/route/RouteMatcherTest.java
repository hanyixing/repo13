package com.hellokaton.blade.mvc.route;

import com.hellokaton.blade.mvc.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RouteMatcher}, complementing {@code ANYMatcherTest}.
 */
public class RouteMatcherTest {

    private RouteMatcher routeMatcher;

    @BeforeEach
    public void setUp() {
        routeMatcher = new RouteMatcher();
    }

    @Test
    public void testLookupRouteReturnsNullWhenNotRegistered() {
        routeMatcher.addRoute("/", ctx -> ctx.text("Ok"), HttpMethod.GET);
        routeMatcher.register();

        assertNotNull(routeMatcher.lookupRoute("GET", "/"));
        assertNull(routeMatcher.lookupRoute("GET", "/not-exist"));
    }

    @Test
    public void testHasBeforeAndAfterHook() {
        assertFalse(routeMatcher.hasBeforeHook());
        assertFalse(routeMatcher.hasAfterHook());

        routeMatcher.addRoute("/**", ctx -> ctx.text("before"), HttpMethod.BEFORE);
        routeMatcher.addRoute("/**", ctx -> ctx.text("after"), HttpMethod.AFTER);

        assertTrue(routeMatcher.hasBeforeHook());
        assertTrue(routeMatcher.hasAfterHook());
    }

    @Test
    public void testGetAfterHooks() {
        routeMatcher.addRoute("/*", ctx -> ctx.text("after"), HttpMethod.AFTER);

        List<Route> afters = routeMatcher.getAfter("/");
        assertEquals(1, afters.size());
    }

    @Test
    public void testMiddleware() {
        assertEquals(0, routeMatcher.middlewareCount());

        routeMatcher.addMiddleware(ctx -> true);

        assertEquals(1, routeMatcher.middlewareCount());
        assertNotNull(routeMatcher.getMiddleware());
        assertEquals(1, routeMatcher.getMiddleware().size());
    }

    @Test
    public void testRouteWithHttpMethodPrefix() {
        routeMatcher.route("/index", RouteMatcherDemoController.class, "GET:index");
        routeMatcher.register();

        Route route = routeMatcher.lookupRoute("GET", "/index");
        assertNotNull(route);
        assertEquals(HttpMethod.GET, route.getHttpMethod());
    }

}
