---
layout: default
title: Ldaptive - operations
redirect_from: "/docs/guide/operations/"
---

# Operations

Operations provide the core functionality for interacting with LDAP servers. Ldaptive provides the following operation implementations:

- Search
- Compare
- Bind
- Add
- Modify
- Modify DN
- Delete

The operation interface looks like:

{% highlight java %}
public interface Operation<Q extends Request, S extends Result>
{
  OperationHandle<Q, S> send(Q request) throws LdapException;

  S execute(Q request) throws LdapException;
}
{% endhighlight %}

## Usage

### Synchronous calls

Ldaptive is asynchronous at its core but provides an *execute* to wait for operations to complete.

{% highlight java %}
{% include source/operations/1.java %}
{% endhighlight %}

Note that the *throwIf* function is only applicable for synchronous usage when it is desirable to raise an exception for certain server responses in a try-catch block. It will not invoke the *onException* function. By default non-success responses are not considered exceptional.

### Asynchronous calls

For asynchronous usage use the *send* method and process results in the *onResult* function.

{% highlight java %}
{% include source/operations/2.java %}
{% endhighlight %}

