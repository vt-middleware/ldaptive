/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapUtils;

/**
 * Class for parsing a sync repl cookie.
 *
 * See https://www.openldap.org/doc/admin23/syncrepl.html and https://www.openldap.org/faq/data/cache/1145.html
 *
 * @author  Middleware Services
 */
public class SyncReplCookie
{

  /** Cookie RID. */
  private final String rid;

  /** Cookie CSN. */
  private final CSN csn;


  /**
   * Creates a new sync repl cookie.
   *
   * @param  cookie  to parse
   */
  public SyncReplCookie(final String cookie)
  {
    final Map<String, String> nameValues  = parseCookie(LdapUtils.assertNotNullArg(cookie, "Cookie cannot be null"));
    if (!nameValues.containsKey("rid")) {
      throw new IllegalArgumentException("Could not parse 'rid' from " + cookie);
    }
    if (!nameValues.containsKey("csn")) {
      throw new IllegalArgumentException("Could not parse 'csn' from " + cookie);
    }
    rid = nameValues.get("rid");
    csn = new CSN(nameValues.get("csn"));
  }


  /**
   * Returns the RID.
   *
   * @return  cookie RID
   */
  public String getRid()
  {
    return rid;
  }


  /**
   * Returns the CSN.
   *
   * @return  cookie CSN
   */
  public CSN getCsn()
  {
    return csn;
  }


  /**
   * Parses the name/value pairs in the supplied cookie.
   *
   * @param  cookie  to parse
   *
   * @return  map of name/value pairs
   */
  private static Map<String, String> parseCookie(final String cookie)
  {
    final Map<String, String> parsedCookie = new HashMap<>(2);
    final String[] nameValuePairs = cookie.split(",");
    for (String nameValuePair : nameValuePairs) {
      final String[] nameValue = nameValuePair.split("=");
      parsedCookie.put(nameValue[0], nameValue[1]);
    }
    return parsedCookie;
  }


  /** Class representing a Change Sequence Number. */
  public static class CSN
  {

    /** Entire CSN value. */
    private final String value;

    /** CSN time. */
    private final String time;

    /** CSN count. */
    private final String count;

    /** CSN sid. */
    private final String sid;

    /** CSN mod. */
    private final String mod;


    /**
     * Creates a new CSN with the supplied string.
     *
     * @param  csn  to parse
     */
    public CSN(final String csn)
    {
      // CheckStyle:MagicNumber OFF
      value = csn;
      final String[] csnParts = csn.split("#");
      if (csnParts.length != 4) {
        throw new IllegalArgumentException("CSN does not contain 4 parts: " + csn);
      }
      time = csnParts[0];
      count = csnParts[1];
      sid = csnParts[2];
      mod = csnParts[3];
      // CheckStyle:MagicNumber ON
    }


    /**
     * Returns the entire value of the CSN.
     *
     * @return  entire value
     */
    public String getValue()
    {
      return value;
    }


    /**
     * Returns the time part of the CSN
     *
     * @return  CSN time
     */
    public String getTime()
    {
      return time;
    }


    /**
     * Returns the count part of the CSN
     *
     * @return  CSN count
     */
    public String getCount()
    {
      return count;
    }


    /**
     * Returns the sid part of the CSN
     *
     * @return  CSN sid
     */
    public String getSid()
    {
      return sid;
    }


    /**
     * Returns the mod part of the CSN
     *
     * @return  CSN mod
     */
    public String getMod()
    {
      return mod;
    }
  }
}
