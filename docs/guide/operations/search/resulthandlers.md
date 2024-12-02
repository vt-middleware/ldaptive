---
layout: default
title: Ldaptive - result handlers
redirect_from: "/docs/guide/operations/search/resulthandlers/"
---

# Search Result Handlers

Search result handlers provide an interface for common operations that are performed on the search response returned from a search operation. A `SearchResultHandler` is simply a `Function<SearchResponse, SearchResponse>` where the response entries and references can be modified and returned. Note that the result code, matched DN, diagnostic message and referral URLs cannot be modified. 

Ldaptive provides the following search result handler implementations:

## FollowSearchReferralHandler

Chases a search referral and returns that result instead.

## FollowSearchResultReferenceHandler

Chases a search result reference and includes that result in the original response.

## FreezeResultHandler

Makes the search response and all its' properties immutable.

## MergeResultHandler

Merges all the entries into a single entry in the result. References are merged in the same manner.

## RecursiveResultHandler

Recursively searches based on an attribute value and includes those results in the original response.

## SortResultHandler

Sorts the entries, attributes and attribute values in the search response.

