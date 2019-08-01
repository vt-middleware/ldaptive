---
layout: default_v1
title: Ldaptive - dir sync
redirect_from: "/v1/docs/guide/ad/dirsync/"
---

# Directory Synchronization (DirSync)

Active Directory provides it's own control for tracking changes in the directory. Note the following constraints when configuring your search:

baseDN | must be the root of a directory partition, which can be a domain partition, the configuration partition, or the schema partition
scope | must be the entire subtree of the partition
filter | any valid search filter
attributes | list of attributes to be returned when a change occurs

The DirSyncControl should be sent along with the ExtendedDnControl and the ShowDeletedControl. The DirSyncClient class encapsulates this behavior. Note that this example uses the `DefaultCookieManager`. Implementers will most likely want to provide a custom implementation of `CookieManager` to handle persistence of cookie data.

## DirSyncClient

{% highlight java %}
{% include source_v1/ad/dirsync/1.java %}
{% endhighlight %}

#### Useful Links

- [LDAP_SERVER_DIRSYNC_OID control code](http://msdn.microsoft.com/en-us/library/windows/desktop/aa366978(v=vs.85).aspx)
- [Polling for Changes Using the DirSync Control](http://msdn.microsoft.com/en-us/library/windows/desktop/ms677626(v=vs.85).aspx)
