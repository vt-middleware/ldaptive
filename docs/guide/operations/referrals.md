---
layout: default
title: Ldaptive - referrals
redirect_from: "/docs/guide/operations/referrals/"
---

# Referrals

An LDAP server can include a referral result code for most operations. The most common use case for referrals is for searching. If you wish to follow referrals, Ldaptive provides an implementation called `FollowSearchReferralHandler` for this purpose. If no referral handler is provided and a referral result code is received, the referral URL(s) can be inspected on the response with `getReferralURLs()`.

{% highlight java %}
{% include source/operations/referrals/1.java %}
{% endhighlight %}
