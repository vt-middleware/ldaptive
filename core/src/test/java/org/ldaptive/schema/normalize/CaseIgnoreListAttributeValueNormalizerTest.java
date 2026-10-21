/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.List;
import java.util.Map;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link CaseIgnoreListAttributeValueNormalizer}.
 *
 * @author  Middleware Services
 */
public class CaseIgnoreListAttributeValueNormalizerTest extends AbstractAttributeValueNormalizerTest
{

  /** Normalizer to test. */
  private static final CaseIgnoreListAttributeValueNormalizer NORMALIZER =
    new CaseIgnoreListAttributeValueNormalizer();


  /**
   * Normalizer test data.
   *
   * @return  normalizer test data
   */
  @DataProvider(name = "normalizeValues")
  public Object[][] normalizeValues()
  {
    return
      new Object[][] {
        new Object[] {
          NORMALIZER,
          List.of(""),
          Map.of("", List.of("")),
        },
        new Object[] {
          NORMALIZER,
          List.of(" "),
          Map.of(" ", List.of(" ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("  "),
          Map.of(" ", List.of("  ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("  ", " ", ""),
          Map.of(" ", List.of("  ", " "), "", List.of("")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo"),
          Map.of("foo", List.of("foo")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo", "foo ", " foo", " foo "),
          Map.of("foo", List.of("foo", "foo ", " foo", " foo ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo", "foo ", " foo", " foo ", "Foo", "Foo ", " Foo", " Foo "),
          Map.of("foo", List.of("foo", "foo ", " foo", " foo ", "Foo", "Foo ", " Foo", " Foo ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo bar", " foo bar ", "foo  bar", " foo   bar "),
          Map.of("foo bar", List.of("foo bar", " foo bar ", "foo  bar", " foo   bar ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo bar", " foo bar ", "foo  bar", " foo   bar ", " Foo Bar", "  Foo  Bar", "Foo Bar   "),
          Map.of(
            "foo bar",
            List.of("foo bar", " foo bar ", "foo  bar", " foo   bar ", " Foo Bar", "  Foo  Bar", "Foo Bar   ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("Luččić", "Luččić ", " Luččić", " Luččić ", "  Luččić  "),
          Map.of("luččić", List.of("Luččić", "Luččić ", " Luččić", " Luččić ", "  Luččić  ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("Runic Letter PERTHO PEORTH \u16C8", " Runic   Letter PERTHO PEORTH  \u16C8 "),
          Map.of(
            "runic letter pertho peorth \u16C8",
            List.of("Runic Letter PERTHO PEORTH \u16C8", " Runic   Letter PERTHO PEORTH  \u16C8 ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("Cuneiform Sign UR4 \uD808\uDF34", "Cuneiform Sign  UR4 \uD808\uDF34  "),
          Map.of(
            "cuneiform sign ur4 \uD808\uDF34",
            List.of("Cuneiform Sign UR4 \uD808\uDF34", "Cuneiform Sign  UR4 \uD808\uDF34  ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F", "Pirate   Flag   \uD83C\uDFF4\u200D\u2620\uFE0F"),
          Map.of(
            "pirate flag \uD83C\uDFF4\u200D\u2620\uFE0F",
            List.of("Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F", "Pirate   Flag   \uD83C\uDFF4\u200D\u2620\uFE0F")),
        },
        new Object[] {
          NORMALIZER,
          List.of("Лаборатория", " Лаборатория", "  Лаборатория  "),
          Map.of("лаборатория", List.of("Лаборатория", " Лаборатория", "  Лаборатория  ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo$bar"),
          Map.of("foo$bar", List.of("foo$bar")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo$bar", " foo $ bar ", " Foo $ Bar "),
          Map.of("foo$bar", List.of("foo$bar", " foo $ bar ", " Foo $ Bar ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("foo$$bar", "foo$ $bar"),
          Map.of("foo$$bar", List.of("foo$$bar"), "foo$ $bar", List.of("foo$ $bar")),
        },
        new Object[] {
          NORMALIZER,
          List.of("  foo  $  bar  $  baz  $ qux ", "  FOO  $  BAR  $  baz  $ Qux "),
          Map.of("foo$bar$baz$qux", List.of("  foo  $  bar  $  baz  $ qux ", "  FOO  $  BAR  $  baz  $ Qux ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("  foo  $$$ qux ", "  FOO  $$$ Qux "),
          Map.of("foo$$$qux", List.of("  foo  $$$ qux ", "  FOO  $$$ Qux ")),
        },
        new Object[] {
          NORMALIZER,
          List.of("  Foo  $  $  $ Qux ", "  FOO  $    $    $ Qux "),
          Map.of("foo$ $ $qux", List.of("  Foo  $  $  $ Qux ", "  FOO  $    $    $ Qux ")),
        },
      };
  }
}
