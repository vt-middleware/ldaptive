---
layout: default
title: Ldaptive - search
redirect_from: "/docs/guide/operations/search/async/"
---

{% include relative %}

# Asynchronous Searching

Ldaptive is asynchronous at its core since it is built on Netty. The `#execute` method blocks until a result is received, however it's possible to configure handlers to process results as they arrive.

{% highlight java %}
{% include source/operations/search/async/1.java %}
{% endhighlight %}

If your goal is to execute non-blocking searches, also consider using one of the implementations in the [concurrent]({{ relative }}docs/guide/concurrency.html) package.

## Search Entry Handlers

Ldaptive provides a few entry handler implementations for convenience:

### CaseChangeEntryHandler

Provides the ability to modify the case of entry DNs, attribute names, and attribute values.

### DnAttributeEntryHandler

Provides the ability to inject an attribute containing the entry DN into each entry. Provides a client side implementation of [RFC 5020](http://tools.ietf.org/html/rfc5020).

### MergeAttributeEntryHandler

Provides the ability to merge the values of one or more attributes into a single attribute. The merged attribute may or may not already exist int the entry. If it does exist its existing values will remain intact.

### RecursiveEntryHandler

Performs a recursive search based on a supplied attribute and merges the results of the recursive search into the original entry. This handler can be used to provide convenient access to nested structures like groups.

