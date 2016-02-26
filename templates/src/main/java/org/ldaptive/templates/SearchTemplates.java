/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ldaptive.SearchFilter;

/**
 * Contains a list of common search filter templates that can be formatted for any given query.
 *
 * @author  Middleware Services
 */
public class SearchTemplates
{

  /** Search filter templates. */
  private final String[] filterTemplates;

  /** Appended to every search filter to restrict results. */
  private String searchRestrictions;

  /** Term parsers for creating filter parameters. */
  private TermParser[] termParsers = new TermParser[] {
    new DefaultTermParser(),
    new InitialTermParser(),
  };


  /**
   * Creates a new search templates.
   *
   * @param  templates  list of search filters
   */
  public SearchTemplates(final String... templates)
  {
    filterTemplates = templates;
  }


  /**
   * Returns the filter to use for search restrictions.
   *
   * @return  search restrictions
   */
  public String getSearchRestrictions()
  {
    return searchRestrictions;
  }


  /**
   * Sets the filter to use for search restrictions.
   *
   * @param  restrictions  search restrictions
   */
  public void setSearchRestrictions(final String restrictions)
  {
    searchRestrictions = restrictions;
  }


  /**
   * Returns the term parsers used for creating filter parameters.
   *
   * @return  term parsers
   */
  public TermParser[] getTermParsers()
  {
    return termParsers;
  }


  /**
   * Sets the term parsers used for creating filter parameters.
   *
   * @param  parsers  term parsers
   */
  public void setTermParsers(final TermParser... parsers)
  {
    termParsers = parsers;
  }


  /**
   * Creates the search filters using configured templates and the supplied query.
   *
   * @param  query  to create search filter with
   *
   * @return  search filters
   */
  public SearchFilter[] format(final Query query)
  {
    final List<SearchFilter> filters = new ArrayList<>(filterTemplates.length);
    for (String template : filterTemplates) {
      final SearchFilter filter = new SearchFilter(
        concatFilters(template, query.getSearchRestrictions(), searchRestrictions));
      for (TermParser parser : termParsers) {
        for (Map.Entry<String, String> e : parser.parse(query.getTerms()).entrySet()) {
          filter.setParameter(e.getKey(), e.getValue());
        }
      }
      filters.add(filter);
    }
    return filters.toArray(new SearchFilter[filters.size()]);
  }


  /**
   * Concatenates the supplied filters into a single filter will all arguments ANDED together. Null array values are
   * ignored.
   *
   * @param  filters  to concatenate
   *
   * @return  search filter
   */
  private String concatFilters(final String... filters)
  {
    final List<String> nonNullFilters = new ArrayList<>(filters.length);
    for (String s : filters) {
      if (s != null) {
        nonNullFilters.add(s);
      }
    }
    if (nonNullFilters.size() > 1) {
      final StringBuilder sb = new StringBuilder("(&");
      nonNullFilters.forEach(sb::append);
      sb.append(")");
      return sb.toString();
    } else {
      return nonNullFilters.get(0);
    }
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::filterTemplates=%s, searchRestrictions=%s, termParsers=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(filterTemplates),
        searchRestrictions,
        Arrays.toString(termParsers));
  }


  /** Converts query terms into search filter parameters. */
  public interface TermParser
  {


    /**
     * Returns search filter parameters for the supplied query terms.
     *
     * @param  terms  to parse
     *
     * @return  search filter parameters
     */
    Map<String, String> parse(final String[] terms);
  }


  /**
   * Adds each term as a filter parameter using the name 'termX' where X is the index of the term. For the argument:
   * {'fname', 'lname' }, produces:
   *
   * <pre>
     {
       'term1' =&gt; 'fname',
       'term2' =&gt; 'lname',
     }
   * </pre>
   */
  public class DefaultTermParser implements TermParser
  {


    @Override
    public Map<String, String> parse(final String[] terms)
    {
      final Map<String, String> filterParams = new HashMap<>(terms.length);
      for (int i = 1; i <= terms.length; i++) {
        filterParams.put("term" + i, terms[i - 1]);
      }
      return filterParams;
    }
  }


  /**
   * Adds the first letter of each term as a filter parameter using the name 'initialX' where X is the index of the
   * term. For the argument: {'fname', 'lname' }, produces:
   *
   * <pre>
     {
       'initial1' =&gt; 'f',
       'initial2' =&gt; 'l',
     }
   * </pre>
   */
  public class InitialTermParser implements TermParser
  {


    @Override
    public Map<String, String> parse(final String[] terms)
    {
      final Map<String, String> filterParams = new HashMap<>(terms.length);
      final String[] initialParams = getInitials(terms);
      for (int i = 1; i <= initialParams.length; i++) {
        filterParams.put("initial" + i, initialParams[i - 1]);
      }
      return filterParams;
    }


    /**
     * This converts an array of names into an array of initials.
     *
     * @param  names  to convert to initials
     *
     * @return  initials
     */
    private String[] getInitials(final String[] names)
    {
      final String[] initials = new String[names.length];
      for (int i = 0; i < initials.length; i++) {
        if (names[i] != null && names[i].length() > 0) {
          initials[i] = names[i].substring(0, 1);
        } else {
          initials[i] = null;
        }
      }
      return initials;
    }
  }
}
