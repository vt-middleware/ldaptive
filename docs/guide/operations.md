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
  S execute(Q request) throws LdapException;
}
{% endhighlight %}

