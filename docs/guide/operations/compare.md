---
layout: default
title: Ldaptive - compare
redirect_from: "/docs/guide/operations/compare/"
---

# Compare Operation

Determines if an entry contains a specific attribute and value. An LDAP attribute may contain sensitive information which a client is not allowed to read, but is allowed to compare.

Compares the *mail: dfisher@ldaptive.org* attribute on the entry uid=dfisher,ou=people,dc=ldaptive,dc=org

{% highlight java %}
{% include source/operations/compare/1.java %}
{% endhighlight %}

