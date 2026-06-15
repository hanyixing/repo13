package com.hellokaton.blade.benchmark;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.kit.StringKit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for Blade framework core operations.
 *
 * Benchmarks:
 *   - Route matching performance
 *   - StringKit utility operations
 *   - Request/Response creation overhead
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class BladeBenchmark {

    private Blade blade;

    @Setup(Level.Trial)
    public void setUp() {
        blade = Blade.create();
        blade.get("/hello", ctx -> ctx.text("Hello World"));
        blade.get("/json", ctx -> ctx.json("{\"status\":\"ok\"}"));
        blade.get("/users/:id", ctx -> ctx.text(ctx.pathString("id")));
        blade.post("/data", ctx -> ctx.text("received"));
        blade.get("/static/file.txt", ctx -> ctx.text("static content"));

        // Register many routes to simulate realistic routing table
        for (int i = 0; i < 100; i++) {
            blade.get("/api/v1/resource" + i + "/:id", ctx -> ctx.text("ok"));
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkRouteMatching() {
        // Test route resolution throughput
        blade.routeMatcher().lookupRoute("GET", "/hello");
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkPathParamRoute() {
        blade.routeMatcher().lookupRoute("GET", "/users/12345");
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkDeepRoute() {
        blade.routeMatcher().lookupRoute("GET", "/api/v1/resource50/999");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void benchmarkStringKit() {
        StringKit.isNotBlank("hello world blade framework");
        StringKit.isBlank("");
        StringKit.rand(16);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void benchmarkStringKitRand() {
        StringKit.rand(32);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BladeBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("target/jmh-result.json")
                .build();
        new Runner(opt).run();
    }
}
