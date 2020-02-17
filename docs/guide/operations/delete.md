---
layout: default
title: Ldaptive - delete
redirect_from: "/docs/guide/operations/delete/"
---

# Delete Operation

Removes an LDAP entry from the directory.

{% highlight java %}
{% include source/operations/delete/1.java %}
{% endhighlight %}

The operation can be configured to throw so the result code doesn't need to be checked.

{% highlight java %}
{% include source/operations/delete/2.java %}
{% endhighlight %}
