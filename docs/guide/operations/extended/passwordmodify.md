---
layout: default
title: Ldaptive - password modify
redirect_from: "/docs/guide/operations/extended/passwordmodify/"
---

# Password Modify

Modifies an entry's userPassword attribute as described in [RFC 3062](http://www.ietf.org/rfc/rfc3062.txt).

{% highlight java %}
{% include source/operations/extended/passwordmodify/1.java %}
{% endhighlight %}

This operation can also be used to generate passwords:

{% highlight java %}
{% include source/operations/extended/passwordmodify/2.java %}
{% endhighlight %}

## Provider Support

JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
✓    | ✓     | ✶           | ✓         | ✓

{% include provider-support-legend.md %}

