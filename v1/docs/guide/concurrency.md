---
layout: default_v1
title: Ldaptive - concurrency
redirect_from: "/v1/docs/guide/concurrency/"
---

# Concurrent Operations

Ldaptive provides some useful wrappers around LDAP operations to support threading and concurrency. This allows an application to execute an operation and then wait for the result as appropriate. By default a cached thread pool is used by the underlying ExecutorService.

## Operation Workers

Searching is the most useful operation to perform concurrently. Multiple requests can be combined together and the client can wait until all operations have completed. All operations are executed on the same connection.

{% highlight java %}
{% include source_v1/concurrency/1.java %}
{% endhighlight %}

Implementations also exist for compare, bind, add, modify, modify DN, and delete operations.

## Search Executors

Search Executors provide a useful abstraction for reusing search criteria and consequently it's desirable to have some concurrency support built around them. Ldaptive provides two types of concurrent Search Executors.

### ParallelSearchExecutor

Executes a list of search filters on the same connection. This allows multiple searches to execute in parallel while the client waits for all responses. A connection is retrieved from the factory and subsequently opened, then closed. If you need support for connection pooling see the `ParallelPooledSearchExecutor`.

{% highlight java %}
{% include source_v1/concurrency/2.java %}
{% endhighlight %}

### AggregateSearchExecutor

Executes a list of search filters over a list of connection factories. This allows the same search to be run against multiple LDAPs with responses combined into a single collection. A connection is retrieved from each factory and subsequently opened, then closed. If you need support for connection pooling see the `AggregatePooledSearchExecutor`.

{% highlight java %}
{% include source_v1/concurrency/3.java %}
{% endhighlight %}

