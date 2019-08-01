---
layout: default_v1
title: Ldaptive - who am i
redirect_from: "/v1/docs/guide/operations/extended/whoami/"
---

# Who Am I

Returns the name of the authorization entity established on the connection as described in [RFC 4532](http://www.ietf.org/rfc/rfc4532.txt).

{% highlight java %}
{% include source_v1/operations/extended/whoami/1.java %}
{% endhighlight %}

## Provider Support

| JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
| <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#f1c232">✶</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>

{% include provider-support-legend.md %}

