/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.ldaptive.schema.AttributeType;
import org.ldaptive.schema.Schema;

/**
 * Normalizes the string values of an LDAP attribute and sets the values to the first unique value found in the
 * attribute. This implementation applies the matching rules for caseExactMatch, caseIgnoreMatch and
 * caseIgnoreListMatch. If no matching rule can be found in the supplied schema or no normalizer can be found for a
 * matching rule, then this consumer is a no-op. Binary and single valued attributes are also ignored, see {@link
 * LdapAttribute#isBinary()}.
 *
 * @author  Middleware Services
 */
public class RemoveDuplicateAttributeValueBiConsumer implements BiConsumer<Schema, LdapAttribute>
{

  /** OIDs and names to use the caseExactMatch normalizer. */
  private static final Set<String> CASE_EXACT_NAMES = Set.of(
    "2.5.13.5",
    "caseExactMatch",
    "1.3.6.1.4.1.1466.109.114.1",
    "caseExactIA5Match"
  );

  /** OIDs and names to use the caseIgnoreMatch normalizer. */
  private static final Set<String> CASE_IGNORE_NAMES = Set.of(
    "2.5.13.2",
    "caseIgnoreMatch",
    "1.3.6.1.4.1.1466.109.114.2",
    "caseIgnoreIA5Match"
  );

  /** OIDs and names to use the caseIgnoreListMatch normalizer. */
  private static final Set<String> CASE_IGNORE_LIST_NAMES = Set.of(
    "2.5.13.11",
    "caseIgnoreListMatch"
  );

  /** Map of OID or name to normalizer. */
  private static final Map<String, AttributeValueNormalizer> NORMALIZER_REGISTRY;

  // initialize the registry
  static {
    final CaseIgnoreAttributeValueNormalizer caseIgnore = new CaseIgnoreAttributeValueNormalizer();
    final CaseExactAttributeValueNormalizer caseExact = new CaseExactAttributeValueNormalizer();
    final CaseIgnoreListAttributeValueNormalizer caseIgnoreList = new CaseIgnoreListAttributeValueNormalizer();

    final Map<String, AttributeValueNormalizer> registry = new HashMap<>();
    CASE_EXACT_NAMES.forEach(n -> registry.put(LdapUtils.toLowerCase(n), caseExact));
    CASE_IGNORE_NAMES.forEach(n -> registry.put(LdapUtils.toLowerCase(n), caseIgnore));
    CASE_IGNORE_LIST_NAMES.forEach(n -> registry.put(LdapUtils.toLowerCase(n), caseIgnoreList));

    NORMALIZER_REGISTRY = Collections.unmodifiableMap(registry);
  }


  @Override
  public void accept(final Schema schema, final LdapAttribute attr)
  {
    LdapUtils.assertNotNullArg(schema, "Schema cannot be null");
    LdapUtils.assertNotNullArgOr(attr, la -> la.getName() == null, "Attribute name cannot be null");
    if (!attr.isBinary()) {
      final AttributeType type = schema.getAttributeType(attr.getName());
      if (type != null) {
        final String matchingRuleName = type.getEqualityMatchingRule();
        if (matchingRuleName != null && !type.isSingleValued()) {
          final AttributeValueNormalizer normalizer = NORMALIZER_REGISTRY.get(LdapUtils.toLowerCase(matchingRuleName));
          if (normalizer != null) {
            final Map<String, List<String>> normalizedValues = normalizer.normalize(attr.getStringValues());
            attr.clear();
            normalizedValues.values().stream().map(l -> l.get(0)).forEach(attr::addStringValues);
          }
        }
      }
    }
  }
}
