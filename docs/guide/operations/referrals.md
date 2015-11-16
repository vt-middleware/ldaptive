---
layout: default
title: Ldaptive - referrals
redirect_from: "/docs/guide/operations/referrals/"
---

# Referrals

An LDAP server can include a referral result code for all operations. (Except Unbind and Abandon.) Ldaptive provides an interface called `ReferralHandler` to allow users to manage referrals however they like. If no referral handler is provided and a referral result code is received, the referral URL(s) can be inspected at `Response.getReferralURLs()`.

The referral handler interface looks like:

{% highlight java %}
public interface ReferralHandler<Q extends Request, S> extends Handler<Q, Response<S>>
{
  @Override
  HandlerResult<Response<S>> handle(Connection conn, Q request, Response<S> response)
    throws LdapException;

  void initializeRequest(Q request);
}
{% endhighlight %}

The most common use case for referrals is for searching. If you wish to follow referrals, Ldaptive provides an implementation called `SearchReferralHandler` for this purpose. This implementation will also follow search references.

{% highlight java %}
{% include source/operations/referrals/1.java %}
{% endhighlight %}

Handlers are also provided for Add, Compare, Delete, ModifyDn and Modify operations.
