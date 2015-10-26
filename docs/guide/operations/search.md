---
layout: default
title: Ldaptive - search
redirect_from: "/docs/guide/operations/search/"
---

# Search Operation

The primary operation of LDAP servers. Provides the ability to retrieve multiple entries containing variable attribute sets using a defined query syntax. The syntax for LDAP filters is defined in [RFC 2254](http://www.ietf.org/rfc/rfc2254.txt).

Searches for entries matching: _(&(givenName=daniel)(sn=fisher))_ over the _dc=ldaptive,dc=org_ DIT and returns entries containing just the mail and displayName attributes.

{% highlight java %}
{% include source/operations/search/1.java %}
{% endhighlight %}

### SearchRequest Properties

The following properties can be configured on a per request basis:

Name | Default Value | Description
baseDn | "" | DN to search; An empty value searches the rootDSE;
searchFilter | null | LDAP filter to execute
returnAttributes | null | names of attributes to include in the search result
searchScope | SUBTREE | scope of the search; Valid values include: OBJECT, ONELEVEL, SUBTREE
timeLimit | 0 | length of time in milliseconds that a search operation should execute; A value of 0 means execute indefinitely; When time limit arrives result will contain any result returned up to that point
sizeLimit | 0 | maximum number of entries to include in the search result; A value of 0 means includes all entries
derefAliases | null | how aliases are dereferences; Valid values include: NEVER, SEARCHING, FINDING, ALWAYS; A null value means use the provider default
followReferrals | false | whether to attempt to follow referrals
typesOnly | false | whether to return attribute types only
binaryAttributes | null | attribute names that should be considered binary regardless of how they are stored
sortBehavior | UNORDERED | how result data should be sorted; Valid values include: UNORDERED, ORDERED, SORTED
searchEntryHandlers | null | entry handlers to process each entry in the result
searchReferenceHandlers | null | reference handlers to process each reference in the result

## Search Filters

The SearchFilter object provides support for both positional and named parameters. Values provided as parameters are escaped according to [RFC 2254](http://www.ietf.org/rfc/rfc2254.txt).

#### Positional

{% highlight java %}
{% include source/operations/search/2.java %}
{% endhighlight %}

#### Named

{% highlight java %}
{% include source/operations/search/3.java %}
{% endhighlight %}

In this manner applications can define custom, readable filters for their users and then set the parameters accordingly.

## Search Result Order

The result data stored in the SearchResult, LdapEntry, and LdapAttribute objects is unordered by default. The following implementations are provided:

- UNORDERED - stores results in an undetermined order as dictated by internal implementation
- ORDERED - stores results in the order they were returned from the LDAP
- SORTED - stores results sorted by entry DN, attribute name, and attribute value

The sort behavior can be controlled on a per request basis with:

{% highlight java %}
searchRequest.setSortBehavior(SortBehavior.SORTED);
{% endhighlight %}

The default sort behavior can be controlled with a JVM switch:

{% highlight bash %}
-Dorg.ldaptive.sortBehavior=org.ldaptive.SortBehavior.ORDERED
{% endhighlight %}

## Search Result Caching

The search operation supports supplying a cache when executing a request. The interface for Cache looks like:

{% highlight java %}
public interface Cache<Q extends SearchRequest>
{
  SearchResult get(Q request);
  void put(Q request, SearchResult result);
}
{% endhighlight %}

If a cache is supplied, it will be inspected for each request and will forgo sending the request to the LDAP if a cached result is found. Ldaptive provides one implementation called LRUCache which leverages a LinkedHashMap to store requests and responses. To use a cache invoke the search operation like this:

{% highlight java %}
{% include source/operations/search/4.java %}
{% endhighlight %}

