/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for parsing LDAP URLs. Supports a space delimited format for
 * representing multiple URLs. Expects <scheme><hostname>:<port>, where the port
 * is optional.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class LdapURL
{

  /** Default LDAP port, value is {@value}. */
  public static final int DEFAULT_LDAP_PORT = 389;

  /** Default LDAPS port, value is {@value}. */
  public static final int DEFAULT_LDAPS_PORT = 636;

  /** Default delimiter for ldap urls. */
  private static final String DEFAULT_DELIMITER = " ";

  /** URL entries. */
  private final List<Entry> ldapEntries = new ArrayList<>();


  /**
   * Creates a new ldap url.
   *
   * @param  url  space delimited list of ldap urls
   */
  public LdapURL(final String url)
  {
    this(url, DEFAULT_DELIMITER);
  }


  /**
   * Creates a new ldap url.
   *
   * @param  url  space delimited list of ldap urls
   * @param  delimiter  to split url with
   */
  public LdapURL(final String url, final String delimiter)
  {
    final String[] urls = url.split(delimiter);

    for (String s : urls) {
      String hostname = s;
      int port = DEFAULT_LDAP_PORT;
      // remove scheme, if it exists
      if (hostname.startsWith("ldap://")) {
        hostname = hostname.substring("ldap://".length());
      } else if (hostname.startsWith("ldaps://")) {
        hostname = hostname.substring("ldaps://".length());
        port = DEFAULT_LDAPS_PORT;
      }

      // remove port, if it exist
      if (hostname.contains(":")) {
        port = Integer.parseInt(
          hostname.substring(hostname.indexOf(":") + 1, hostname.length()));
        hostname = hostname.substring(0, hostname.indexOf(":"));
      }

      ldapEntries.add(new Entry(hostname, port));
    }
  }


  /**
   * Returns the first entry of this ldap url.
   *
   * @return  first entry
   */
  public Entry getEntry()
  {
    return ldapEntries.get(0);
  }


  /**
   * Returns the last entry of this ldap url.
   *
   * @return  last entry
   */
  public Entry getLastEntry()
  {
    return ldapEntries.get(ldapEntries.size() - 1);
  }


  /**
   * Returns a list of all the ldap url entries in this ldap url.
   *
   * @return  ldap url entries
   */
  public List<Entry> getEntries()
  {
    return Collections.unmodifiableList(ldapEntries);
  }


  /**
   * Returns a list of all the hostnames in this ldap url.
   *
   * @return  ldap url hostnames
   */
  public String[] getEntriesAsString()
  {
    final String[] entries = new String[ldapEntries.size()];
    for (int i = 0; i < ldapEntries.size(); i++) {
      entries[i] = ldapEntries.get(i).getHostname();
    }
    return entries;
  }


  /**
   * Returns the number of entries in this ldap url.
   *
   * @return  number of entries in this ldap url
   */
  public int size()
  {
    return ldapEntries.size();
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::ldapEntries=%s]",
        getClass().getName(),
        hashCode(),
        ldapEntries);
  }


  /** Represents a single LDAP URL entry. */
  public static class Entry
  {

    /** Hostname of the ldap url. */
    private final String hostname;

    /** Port of the ldap url. */
    private final int port;


    /**
     * Creates a new entry.
     *
     * @param  h  hostname
     * @param  p  port
     */
    public Entry(final String h, final int p)
    {
      hostname = h;
      port = p;
    }


    /**
     * Returns the hostname.
     *
     * @return  hostname
     */
    public String getHostname()
    {
      return hostname;
    }


    /**
     * Returns the hostname:port.
     *
     * @return  hostname:port
     */
    public String getHostnameWithPort()
    {
      return String.format("%s:%s", hostname, port);
    }


    /**
     * Returns the port.
     *
     * @return  port
     */
    public int getPort()
    {
      return port;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::hostname=%s, port=%s]",
          getClass().getName(),
          hashCode(),
          hostname,
          port);
    }
  }
}
