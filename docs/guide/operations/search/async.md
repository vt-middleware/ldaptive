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

