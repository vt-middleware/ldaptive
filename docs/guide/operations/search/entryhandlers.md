---
layout: default
title: Ldaptive - entry handlers
redirect_from: "/docs/guide/operations/search/entryhandlers/"
---

# Entry Handlers

Entry handlers provide an interface for common operations that are performed on all entries returned in a search operation. An `LdapEntryHandler` is simply a `Function<LdapEntry, LdapEntry>` where the entry can be modified and returned. If you do not need a reference to the entry in the `SearchResponse`, you can return `null` from your function and the underlying entry will be discarded. This is benefical in memory constrained applications where you want the entry to be available for garbage collection immediately after you have processed it with a handler. Note that handler implementation cannot modify fundamental message properties such as the message ID and response controls.

Ldaptive provides the following entry handler implementations:

## CaseChangeEntryHandler

Provides the ability to modify the case of entry DNs, attribute names, and attribute values.

## DnAttributeEntryHandler

Provides the ability to inject an attribute containing the entry DN into each entry. This is essentially a client side implementation of [RFC 5020](http://tools.ietf.org/html/rfc5020).

## MergeAttributeEntryHandler

Provides the ability to merge the values of one or more attributes into a single attribute. The merged attribute may or may not already exist in the entry. If it does exist its existing values will remain intact.

