---
layout: default
title: Ldaptive - fast bind
redirect_from: "/docs/guide/ad/fastbind/"
---

# Fast Bind

The fast bind extended operation allows an Active Directory server to process concurrent bind requests on the same connection. Successful binds do not perform authorization steps and the connection remains authorized as an anonymous user. Once the fast bind operation is performed it cannot be disabled and the operation must be performed before any other bind is attempted. Only simple binds are supported.

{% highlight java %}
{% include source/ad/fastbind/1.java %}
{% endhighlight %}

#### Useful Links

- [Using FastBind](http://msdn.microsoft.com/en-us/library/cc223503.aspx)

