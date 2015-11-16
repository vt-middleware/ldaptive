---
layout: default
title: Ldaptive - search
redirect_from: "/docs/guide/operations/search/async/"
---

{% include relative %}

# Asynchronous Searching

Ldaptive provides support for executing asynchronous searches, although some providers do not support all the LDAP features. It is recommended to use either the UnboundID, Apache, or JLDAP providers. JNDI does not support any asynchronous operations. 

If your goal is to execute non-blocking searches, also consider using one of the implementations in the [concurrent]({{ relative }}docs/guide/concurrency) package.

{% highlight java %}
{% include source/operations/search/async/1.java %}
{% endhighlight %}

Various handlers provide the hooks for receiving asynchronous events if you want to operate on results before the response is received. Of particular importance is the `AsyncRequest` object. If you wish to abandon or cancel the async operation you will need to obtain a reference to it. This reference is obtained by setting an `AsyncRequestHandler`. A single `AsyncRequest` is produced for each search execution.

{% highlight java %}
{% include source/operations/search/async/2.java %}
{% endhighlight %}

