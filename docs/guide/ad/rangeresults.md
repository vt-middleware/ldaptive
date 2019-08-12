---
layout: default
title: Ldaptive - range results
redirect_from: "/docs/guide/ad/rangeresults/"
---

# Range Results

Active Directory may not return all the values of an attribute, electing instead to provide the client with a range of attribute values. This practice is documented in an expired RFC: [Incremental Retrieval of Multi-valued Properties](http://www.ietf.org/proceedings/53/I-D/draft-kashi-incremental-00.txt). For instance, requests for the member attribute may return a result like: member;Range=0-1000. The client is then expected to request additional attribute values of the form member;Range=1001-2000 and so forth until all values have been retrieved. An `EntryHandler` is provided to handle this behavior.

## RangeEntryHandler

{% highlight java %}
{% include source/ad/rangeresults/1.java %}
{% endhighlight %}

Note that this entry handler can only be used with the blocking search methods as it requires the connection to remain open while additional searches are performed.

#### Useful Links

- [http://www.openldap.org/its/index.cgi?findid=5472](http://www.openldap.org/its/index.cgi?findid=5472)
- [http://www.openldap.org/lists/ietf-ldapbis/200404/msg00047.html](http://www.openldap.org/lists/ietf-ldapbis/200404/msg00047.html)
- [http://www.openldap.org/lists/openldap-bugs/200406/msg00108.html](http://www.openldap.org/lists/openldap-bugs/200406/msg00108.html)

