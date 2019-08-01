---
layout: default_v1
title: Ldaptive - compare
redirect_from: "/v1/docs/guide/operations/compare/"
---

# Compare Operation

Determines if an entry contains a specific attribute and value. An LDAP attribute may contain sensitive information which a client is not allowed to read, but is allowed to compare. Note that compare only uses a single attribute value, but the LdapAttribute object may contain more than one value. This implementation only uses the first value, all other values are ignored.

Compares the *mail: dfisher@ldaptive.org* attribute on the entry uid=dfisher,ou=people,dc=ldaptive,dc=org

{% highlight java %}
{% include source_v1/operations/compare/1.java %}
{% endhighlight %}

