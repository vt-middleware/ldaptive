---
layout: default
title: Ldaptive - proxy authorization
redirect_from: "/docs/guide/controls/proxyauthz/"
---

# Proxy Authorization

Request that an operation be processed by a different authorization identity than the one that is currently associated with the connection. See [RFC 4370](http://tools.ietf.org/html/rfc4370).

## Using the Proxy Authorization Control

{% highlight java %}
{% include source/controls/proxyauthz/1.java %}
{% endhighlight %}

## Provider Support

| JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
| <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#cc0000">✗</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>

{% include provider-support-legend.md %}

