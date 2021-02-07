package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

import java.util.Arrays;
import java.util.Objects;
/**
 * Joins the values of the attributes in all entries into a single entry.
 *
 * @author  Middleware Services
 */
public class JoinEntriesResultHandler extends AbstractEntryHandler<SearchResponse> implements SearchResultHandler {

    public JoinEntriesResultHandler() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return LdapUtils.computeHashCode(829);
    }

    public String toString() {
        return "[" + this.getClass().getName() + "@" + this.hashCode() + "]";
    }

    @Override
    public SearchResponse apply(SearchResponse searchResponse) {
        return SearchResponse.merge(searchResponse);
    }
}
