---
layout: default
title: Ldaptive - session tracking
redirect_from: "/docs/guide/controls/sessiontracking/"
---

# Session Tracking

Include tracking information about a client session in an LDAP operation. See [Draft RFC](https://tools.ietf.org/html/draft-wahl-ldap-session-03).

## Using the Session Tracking Control

### When binding and the user is not yet known

{% highlight java %}
{% include source/controls/sessiontracking/1.java %}
{% endhighlight %}

### When searching and the user is known

{% highlight java %}
{% include source/controls/sessiontracking/2.java %}
{% endhighlight %}

## Provider Support

| JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
| <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#cc0000">✗</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>

{% include provider-support-legend.md %}

