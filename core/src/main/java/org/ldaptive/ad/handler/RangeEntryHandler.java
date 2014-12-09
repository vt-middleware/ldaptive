/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.AbstractSearchEntryHandler;

/**
 * Rewrites attributes returned from Active Directory to include all values by
 * performing additional searches. This behavior is based on the expired RFC
 * "Incremental Retrieval of Multi-valued Properties"
 * http://www.ietf.org/proceedings/53/I-D/draft-kashi-incremental-00.txt.
 *
 * <p>For example, when the membership of a group exceeds 1500, requests for the
 * member attribute will likely return an attribute with name
 * "member;Range=0-1499" and 1500 values. For a group with just over 3000
 * members, subsequent searches will request "member;Range=1500-2999" and then
 * "member;Range=3000-4499". When the returned attribute is of the form
 * "member;Range=3000-*", all values have been retrieved.</p>
 *
 * @author  Middleware Services
 * @author  Tom Zeller
 */
public class RangeEntryHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 839;

  /** The character indicating that the end of the range has been reached. */
  private static final String END_OF_RANGE = "*";

  /** The format used to calculate attribute IDs for subsequent searches. */
  private static final String RANGE_FORMAT = "%1$s;Range=%2$s-%3$s";

  /** The expression matching the range attribute ID "<id>;range=<X>-<Y>". */
  private static final String RANGE_PATTERN_STRING =
    "^(.*?);Range=([\\d\\*]+)-([\\d\\*]+)";

  /** The pattern matching the range attribute ID. */
  private static final Pattern RANGE_PATTERN = Pattern.compile(
    RANGE_PATTERN_STRING,
    Pattern.CASE_INSENSITIVE);


  @Override
  protected void handleAttributes(
    final Connection conn,
    final SearchRequest request,
    final SearchEntry entry)
    throws LdapException
  {
    final Map<LdapAttribute, Matcher> matchingAttrs = new HashMap<>();
    for (LdapAttribute la : entry.getAttributes()) {
      // Match attribute ID against the pattern
      final Matcher matcher = RANGE_PATTERN.matcher(la.getName());

      // If the attribute ID matches the pattern
      if (matcher.find()) {
        matchingAttrs.put(la, matcher);
      }
    }

    for (Map.Entry<LdapAttribute, Matcher> mEntry : matchingAttrs.entrySet()) {
      final LdapAttribute la = mEntry.getKey();
      final Matcher matcher = mEntry.getValue();
      final String msg = String.format(
        "attribute '%s' entry '%s'",
        la.getName(),
        entry.getDn());

      // Determine the attribute name without the range syntax
      final String attrTypeName = matcher.group(1);
      logger.debug("Found Range option {}", msg);
      if (attrTypeName == null || attrTypeName.isEmpty()) {
        logger.error("Unable to determine the attribute type name for {}", msg);
        throw new IllegalArgumentException(
          "Unable to determine the attribute type name for " + msg);
      }

      // Create or update the attribute whose ID has the range syntax removed
      LdapAttribute newAttr = entry.getAttribute(attrTypeName);
      if (newAttr == null) {
        newAttr = new LdapAttribute(la.getSortBehavior(), la.isBinary());
        newAttr.setName(attrTypeName);
        entry.addAttribute(newAttr);
      }

      // Copy values
      if (la.isBinary()) {
        newAttr.addBinaryValues(la.getBinaryValues());
      } else {
        newAttr.addStringValues(la.getStringValues());
      }

      // Remove original attribute with range syntax from returned attributes
      entry.removeAttribute(la);

      // If the attribute ID ends with * we're done, otherwise increment
      if (!la.getName().endsWith(END_OF_RANGE)) {

        // Determine next attribute ID
        // CheckStyle:MagicNumber OFF
        final int start = Integer.parseInt(matcher.group(2));
        final int end = Integer.parseInt(matcher.group(3));
        // CheckStyle:MagicNumber ON
        final int diff = end - start;
        final String nextAttrID = String.format(
          RANGE_FORMAT,
          attrTypeName,
          end + 1,
          end + diff + 1);

        // Search for next increment of values
        logger.debug("Searching for '{}' to increment {}", nextAttrID, msg);

        final SearchOperation search = new SearchOperation(conn);
        final SearchRequest sr = SearchRequest.newObjectScopeSearchRequest(
          entry.getDn(),
          new String[] {nextAttrID});
        final SearchResult result = search.execute(sr).getResult();

        // Add all attributes to the search result
        entry.addAttributes(result.getEntry().getAttributes());

        // Iterate
        handleAttributes(conn, request, entry);
      }
    }
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object[]) null);
  }
}
