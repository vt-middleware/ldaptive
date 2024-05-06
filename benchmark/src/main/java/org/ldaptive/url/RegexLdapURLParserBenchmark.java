/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

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
 * Benchmark for {@link RegexUrlParser}.
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
public class RegexLdapURLParserBenchmark
{

  /** URL to parse. */
  private static final String URL  = "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)";

  /** LDAP URL Parser to benchmark. */
  private UrlParser urlParser;


  /**
   * Prepare objects for benchmark.
   */
  @Setup
  public void setup()
  {
    urlParser = new RegexUrlParser();
  }


  /**
   * Benchmark {@link RegexUrlParser#parse(String)}.
   *
   * @param  blackhole  to consume objects
   */
  @Benchmark
  public void parse(final Blackhole blackhole)
  {
    final Url parsedUrl = urlParser.parse(URL);
    blackhole.consume(parsedUrl);
  }
}
