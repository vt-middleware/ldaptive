---
layout: default
title: Ldaptive - concurrency
redirect_from: "/docs/guide/concurrency/"
---

# Concurrent Operations

Ldaptive provides some useful wrappers around LDAP operations to support threading and concurrency. This allows an application to execute multiple requests at once and then wait for the result as appropriate.

Searching is the most useful operation to perform concurrently. Multiple requests can be combined together and the client can wait until all operations have completed. All operations are executed on the same connection.

{% highlight java %}
{% include source/concurrency/1.java %}
{% endhighlight %}

Implementations also exist for compare, add, modify, modify DN, and delete operations.

