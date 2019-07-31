---
layout: default
title: Ldaptive - entry handlers
redirect_from: "/docs/guide/operations/search/entryhandlers/"
---

# Search Entry Handlers

Search entry handlers provide a programmatic interface for inline processing of search results. As each entry is returned from the provider it is processed by each handler. This allows custom logic to be injected into your search results. In addition, memory constrained applications can use this functionality to remove entries from the result and abort a search based on custom criteria. The interface for ldap entry handlers looks like:

{% highlight java %}
public interface SearchEntryHandler extends Handler<SearchRequest, SearchEntry>
{
  HandlerResult<SearchEntry> handle(Connection conn, SearchRequest request, SearchEntry entry) throws LdapException;
}
{% endhighlight %}

Ldaptive provides the following search entry handler implementations:

## CaseChangeEntryHandler

Provides the ability to modify the case of entry DNs, attribute names, and attribute values.

## DnAttributeEntryHandler

Provides the ability to inject an attribute containing the entry DN into each entry. Provides a client side implementation of [RFC 5020](http://tools.ietf.org/html/rfc5020).

## MergeAttributeEntryHandler

Provides the ability to merge the values of one or more attributes into a single attribute. The merged attribute may or may not already exist int the entry. If it does exist its existing values will remain intact.

## RecursiveEntryHandler

Performs a recursive search based on a supplied attribute and merges the results of the recursive search into the original entry. This handler can be used to provide convenient access to nested structures like groups.

