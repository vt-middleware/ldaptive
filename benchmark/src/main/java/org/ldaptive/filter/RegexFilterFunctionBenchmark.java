/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark for {@link RegexFilterFunction}.
 *
 * @author  Middleware Services
 */
// CheckStyle:MagicNumber OFF
@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
// CheckStyle:MagicNumber ON
public class RegexFilterFunctionBenchmark
{

  /** Filter to parse. */
  private static final String FILTER  = "(cn=daniel*fisher)";

  /** Filter function to benchmark. */
  private FilterFunction filterFunction;


  /**
   * Prepare objects for benchmark.
   */
  @Setup
  public void setup()
  {
    filterFunction = new RegexFilterFunction();
  }


  /**
   * Benchmark {@link RegexFilterFunction#parse(String)}.
   *
   * @param  blackhole  to consume objects
   */
  @Benchmark
  public void parse(final Blackhole blackhole)
  {
    try {
      final Filter filter = filterFunction.parse(FILTER);
      blackhole.consume(filter);
    } catch (FilterParseException e) {
      throw new IllegalStateException("Could not parse filter: " + FILTER, e);
    }
  }
}
