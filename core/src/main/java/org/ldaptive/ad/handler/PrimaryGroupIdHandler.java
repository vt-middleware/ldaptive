/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.handler.AbstractEntryHandler;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Constructs the primary group SID and then searches for that group and puts it's DN in the 'memberOf' attribute of the
 * original search entry. This handler requires that entries contain both the 'objectSid' and 'primaryGroupID'
 * attributes. If those attributes are not found this handler is a no-op. This handler should be used in conjunction
 * with the {@link ObjectSidHandler} to ensure the 'objectSid' attribute is in the proper form. See
 * http://support2.microsoft.com/kb/297951
 *
 * This handler should only be used with the {@link org.ldaptive.SearchOperation#execute()} method since it leverages
 * the connection to make further searches.
 *
 * @author  Middleware Services
 */
public class PrimaryGroupIdHandler extends AbstractEntryHandler<SearchResponse> implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1831;

  /** search filter used to find the primary group. */
  private String groupFilter = "(&(objectClass=group)(objectSid={0}))";

  /** base DN used for searching for the primary group. */
  private String baseDn;


  /**
   * Returns the search filter used to find the primary group.
   *
   * @return  group search filter
   */
  public String getGroupFilter()
  {
    return groupFilter;
  }


  /**
   * Sets the search filter used to find the primary group.
   *
   * @param  filter  search filter
   */
  public void setGroupFilter(final String filter)
  {
    groupFilter = filter;
  }


  /**
   * Returns the base DN to search for the primary group. If this is not set the base DN from the original search is
   * used.
   *
   * @return  base DN to search for the primary group
   */
  public String getBaseDn()
  {
    return baseDn;
  }


  /**
   * Sets the base DN to search for the primary group. If this is not set the base DN from the original search is used.
   *
   * @param  dn  base DN
   */
  public void setBaseDn(final String dn)
  {
    baseDn = dn;
  }


  @Override
  public SearchResponse apply(final SearchResponse response)
  {
    response.getEntries().forEach(this::handleEntry);
    return response;
  }


  @Override
  protected void handleAttributes(final LdapEntry entry)
  {
    final LdapAttribute objectSid = entry.getAttribute("objectSid");
    final LdapAttribute primaryGroupId = entry.getAttribute("primaryGroupID");

    logger.debug("found objectSid {} and primaryGroupID {}", objectSid, primaryGroupId);
    if (objectSid != null && primaryGroupId != null) {
      final String sid;
      if (objectSid.isBinary()) {
        sid = SecurityIdentifier.toString(objectSid.getBinaryValue());
      } else {
        sid = objectSid.getStringValue();
      }

      final String groupSid = sid.substring(0, sid.lastIndexOf('-') + 1) + primaryGroupId.getStringValue();
      logger.debug(
        "created primary group SID {} from object SID {} and primaryGroupID {}",
        groupSid,
        sid,
        primaryGroupId.getStringValue());

      try {
        final SearchRequest sr = SearchRequest.builder()
          .dn(baseDn != null ? baseDn : getRequest().getBaseDn())
          .attributes(ReturnAttributes.NONE.value())
          .filter(new SearchFilter(groupFilter, new Object[] {groupSid}).format())
          .build();

        final SearchResponse result = getConnection().operation(sr).execute();
        if (result.entrySize() == 0) {
          logger.debug("could not find primary group for SID {}", groupSid);
        } else {
          LdapAttribute memberOf = entry.getAttribute("memberOf");
          if (memberOf == null) {
            memberOf = new LdapAttribute("memberOf");
            entry.addAttributes(memberOf);
          }
          memberOf.addStringValues(result.getEntry().getDn());
        }
      } catch (LdapException e) {
        logger.warn("Error retrieving group ID: {}", groupSid, e);
      }
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof PrimaryGroupIdHandler) {
      final PrimaryGroupIdHandler v = (PrimaryGroupIdHandler) o;
      return LdapUtils.areEqual(groupFilter, v.groupFilter) &&
             LdapUtils.areEqual(baseDn, v.baseDn);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, groupFilter, baseDn);
  }
}
