/**
 * Copyright (c) 2022, katon (hellokaton@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hellokaton.blade.benchmark;

import com.hellokaton.blade.kit.StringKit;
import com.hellokaton.blade.kit.UUID;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * JMH micro-benchmarks for hot blade-kit utilities.
 * <p>
 * Build and run:
 * <pre>
 *   mvn -pl blade-benchmark -am package
 *   java -jar blade-benchmark/target/benchmarks.jar           # full run
 *   java -jar blade-benchmark/target/benchmarks.jar -f 1 -wi 1 -i 1   # quick smoke run
 * </pre>
 * Each @Benchmark returns its result so JMH can sink it and prevent dead-code elimination.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class BladeKitBenchmark {

    private static final String CAMEL_CASE = "userAccountName";
    private static final String UNDERLINE  = "user_account_name";

    @Benchmark
    public String uu64() {
        return UUID.UU64();
    }

    @Benchmark
    public String toUnderlineName() {
        return StringKit.toUnderlineName(CAMEL_CASE);
    }

    @Benchmark
    public String toCamelCase() {
        return StringKit.toCamelCase(UNDERLINE);
    }

}
