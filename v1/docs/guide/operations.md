---
layout: default_v1
title: Ldaptive - operations
redirect_from: "/v1/docs/guide/operations/"
---

# Operations

Operations provide the core functionality for interacting with LDAP servers. Ldaptive provides the following operation implementations:

- Search
- Compare
- Bind
- Add
- Modify
- Rename
- Delete

The operation interface looks like:

{% highlight java %}
public interface Operation<Q extends Request, S>
{
  Response<S> execute(Q request) throws LdapException;
}
{% endhighlight %}

