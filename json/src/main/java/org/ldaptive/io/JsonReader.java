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
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;

/**
 * Reads JSON from a {@link Reader} and returns a {@link SearchResponse}.
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
    jsonReader = reader;
    final GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(SearchResponse.class, new SearchResultDeserializer());
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
  public SearchResponse read()
    throws IOException
  {
    try {
      return gson.fromJson(jsonReader, SearchResponse.class);
    } catch (JsonParseException e) {
      throw new IOException(e);
    }
  }


  /**
   * Deserializes a {@link SearchResponse} by iterating over the json elements.
   */
  private static class SearchResultDeserializer implements JsonDeserializer<SearchResponse>
  {


    @Override
    public SearchResponse deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
      throws JsonParseException
    {
      final SearchResponse result = new SearchResponse();
      final JsonArray jsonResult = json.getAsJsonArray();
      for (JsonElement jsonEntry : jsonResult) {
        LdapEntry entry = null;
        for (Map.Entry<String, JsonElement> jsonAttr : jsonEntry.getAsJsonObject().entrySet()) {
          if ("ref".equals(jsonAttr.getKey())) {
            final SearchResultReference ref = new SearchResultReference();
            jsonAttr.getValue().getAsJsonArray().forEach(i -> ref.addUris(i.getAsString()));
            result.addReferences(ref);
            result.addReferences(ref);
          } else if ("dn".equals(jsonAttr.getKey())) {
            entry = new LdapEntry();
            entry.setDn(jsonAttr.getValue().getAsString());
          } else {
            final LdapAttribute attr = new LdapAttribute();
            attr.setName(jsonAttr.getKey());
            jsonAttr.getValue().getAsJsonArray().forEach(i -> attr.addStringValues(i.getAsString()));
            entry.addAttributes(attr);
          }
        }
        if (entry != null) {
          result.addEntries(entry);
        }
      }
      return result;
    }
  }
}
