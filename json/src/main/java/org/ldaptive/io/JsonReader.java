/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;

/**
 * Reads JSON from a {@link Reader} and returns a {@link SearchResult}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class JsonReader implements SearchResultReader
{

  /** Reader to read from. */
  private final Reader jsonReader;

  /** Sort behavior. */
  private final SortBehavior sortBehavior;


  /**
   * Creates a new json reader.
   *
   * @param  reader  to read JSON from
   */
  public JsonReader(final Reader reader)
  {
    this(reader, SortBehavior.getDefaultSortBehavior());
  }


  /**
   * Creates a new json reader.
   *
   * @param  reader  to read JSON from
   * @param  sb  sort behavior of the search result
   */
  public JsonReader(final Reader reader, final SortBehavior sb)
  {
    jsonReader = reader;
    if (sb == null) {
      throw new IllegalArgumentException("Sort behavior cannot be null");
    }
    sortBehavior = sb;
  }


  /**
   * Reads JSON data from the reader and returns a search result.
   *
   * @return  search result derived from the JSON
   *
   * @throws  IOException  if an error occurs using the reader
   */
  @Override
  @SuppressWarnings("unchecked")
  public SearchResult read()
    throws IOException
  {
    final SearchResult result = new SearchResult(sortBehavior);
    try {
      final JSONParser parser = new JSONParser();
      final JSONArray jsonArray = (JSONArray) parser.parse(jsonReader);
      for (Object o : jsonArray) {
        final LdapEntry entry = new LdapEntry(sortBehavior);
        final JSONObject jsonObject = (JSONObject) o;
        for (Object k : jsonObject.keySet()) {
          final String attrName = (String) k;
          if ("dn".equalsIgnoreCase(attrName)) {
            entry.setDn((String) jsonObject.get(k));
          } else {
            final LdapAttribute attr = new LdapAttribute(sortBehavior);
            attr.setName(attrName);
            attr.addStringValues((List<String>) jsonObject.get(k));
            entry.addAttribute(attr);
          }
        }
        result.addEntry(entry);
      }
    } catch (ParseException e) {
      throw new IOException(e);
    }
    return result;
  }
}
