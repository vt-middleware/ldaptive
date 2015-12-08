/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;

/**
 * Reads JSON from a {@link Reader} and returns a {@link SearchResult}.
 *
 * @author  Middleware Services
 */
public class JsonReader implements SearchResultReader
{

  /** Reader to read from. */
  private final Reader jsonReader;

  /** To convert JSON to a search result. */
  private final Gson gson;


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
    final GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(SearchResult.class, new SearchResultDeserializer(sb));
    gson = builder.disableHtmlEscaping().create();
  }


  /**
   * Reads JSON data from the reader and returns a search result.
   *
   * @return  search result derived from the JSON
   *
   * @throws  IOException  if an error occurs using the reader
   */
  @Override
  public SearchResult read()
    throws IOException
  {
    try {
      return gson.fromJson(jsonReader, SearchResult.class);
    } catch (JsonParseException e) {
      throw new IOException(e);
    }
  }


  /**
   * Deserializes a {@link SearchResult} by iterating over the json elements.
   */
  private static class SearchResultDeserializer implements JsonDeserializer<SearchResult>
  {

    /** Sort behavior. */
    private final SortBehavior sortBehavior;


    /**
     * Creates a new search result deserializer.
     *
     * @param  sb  sort behavior of the search result
     */
    public SearchResultDeserializer(final SortBehavior sb)
    {
      sortBehavior = sb;
    }


    @Override
    public SearchResult deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
      throws JsonParseException
    {
      final SearchResult result = new SearchResult(sortBehavior);
      final JsonArray jsonResult = json.getAsJsonArray();
      for (JsonElement jsonEntry : jsonResult) {
        final LdapEntry entry = new LdapEntry(sortBehavior);
        for (Map.Entry<String, JsonElement> jsonAttr : jsonEntry.getAsJsonObject().entrySet()) {
          if ("dn".equals(jsonAttr.getKey())) {
            entry.setDn(jsonAttr.getValue().getAsString());
          } else {
            final LdapAttribute attr = new LdapAttribute(sortBehavior);
            attr.setName(jsonAttr.getKey());
            jsonAttr.getValue().getAsJsonArray().forEach(i -> attr.addStringValue(i.getAsString()));
            entry.addAttribute(attr);
          }
        }
        result.addEntry(entry);
      }
      return result;
    }
  }
}
