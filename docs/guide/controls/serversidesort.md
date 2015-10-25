---
layout: default
title: Ldaptive - server side sort
redirect_from: "/docs/guide/controls/serversidesort/"
---

# Server Side Sorting

Request that the server sort results before returning them to the client. See [RFC 2891](http://www.ietf.org/rfc/rfc2891.txt). The SortKey class accepts three parameters that control the sorting:

- attributeDescription - name of the attribute to sort on
- matchingRuleId - matching rule defined by [RFC 3698](http://www.ietf.org/rfc/rfc3698.txt); optional, if not present the attribute's default matching rule is used
- reverseOrder - whether the entries should be presented in reverse sorted order; default is false

{% highlight java %}
{% include source/controls/serversidesort/1.java %}
{% endhighlight %}

## Provider Support

JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
✓    | ✓     | ✓           | ✓         | ✓

{% include provider-support-legend.md %}

