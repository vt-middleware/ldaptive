/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Base class for attribute value normalizer tests.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAttributeValueNormalizerTest
{


  /**
   * @param  normalizer  to test
   * @param  input  to normalize
   * @param  expected  expected value
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "normalizeValues")
  public void normalize(
    final AttributeValueNormalizer normalizer, final Collection<String> input, final Map<String, List<String>> expected)
    throws Exception
  {
    assertThat(normalizer.normalize(input)).containsExactlyInAnyOrderEntriesOf(expected);
  }
}
