/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-net-partial-url.
 *
 * ao-net-partial-url is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-net-partial-url is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-net-partial-url.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.net.partialurl;

import com.aoindustries.net.HostAddress;
import com.aoindustries.net.Path;
import com.aoindustries.net.Port;
import com.aoindustries.util.AoCollections;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A {@link PartialURL} matches and completes URLs.
 * All fields are optional.
 * <p>
 * This is not a general-purpose representation of a URL.  It only contains the
 * fields specifically used for matching a request to a virtual host (TODO: Links to projects).  For
 * an instance of {@link URL}, see {@link #toURL(com.aoindustries.net.partialurl.FieldSource)}.
 * </p>
 */
// TODO: Should this be an interface?  All methods are abstract.
abstract public class PartialURL {

	/**
	 * The http scheme.
	 */
	public static final String HTTP = "http";

	/**
	 * The https scheme.
	 */
	public static final String HTTPS = "https";

	/**
	 * The character used to represent request-value substitutions.
	 *
	 * @see  #WILDCARD_STRING
	 */
	protected static final char WILDCARD_CHAR = '*';

	/**
	 * The character used to represent request-value substitutions.
	 *
	 * @see  #WILDCARD_CHAR
	 */
	protected static final String WILDCARD_STRING = String.valueOf(WILDCARD_CHAR);

	/**
	 * The value used to represent {@code null} {@link SinglePartialURL#getContextPath()} or {@link MultiPartialURL#getContextPaths()}.
	 */
	protected static final String NULL_CONTEXT_PATH = Path.SEPARATOR_STRING + WILDCARD_CHAR;

	/**
	 * The value used to represent {@code null} {@link SinglePartialURL#getPrefix()} or {@link MultiPartialURL#getPrefixes()}.
	 */
	protected static final String NULL_PREFIX = Path.SEPARATOR_STRING + WILDCARD_CHAR + WILDCARD_CHAR;

	/**
	 * A {@link SinglePartialURL} consisting of all null fields that will match
	 * all requests and can serve as a match for a default host.
	 * <p>
	 * This default host is after all others in {@link SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)}
	 * and {@link PartialURLMap#get(com.aoindustries.net.partialurl.FieldSource)}.
	 * </p>
	 */
	public static final SinglePartialURL DEFAULT = new SinglePartialURL(null, null, null, null, null);

	/**
	 * Gets a partial URL supporting requests across multiple schemes/hosts/ports/...
	 * 
	 * @param scheme       (Optional) The scheme (http/https/...) to match and/or link to, converted to lower-case.
	 * @param host         (Optional) The IP/host to match and/or link to
	 * @param port         (Optional) The port to match and/or link to
	 * @param contextPath  (Optional) The contextPath to match and/or link to
	 * @param prefix       (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  SinglePartialURL#SinglePartialURL(java.lang.String, com.aoindustries.net.HostAddress, com.aoindustries.net.Port, com.aoindustries.net.Path, com.aoindustries.net.Path)
	 */
	// TODO: String/integer-only overloads?
	public static SinglePartialURL of(String scheme, HostAddress host, Port port, Path contextPath, Path prefix) {
		if(
			scheme == null
			&& host == null
			&& port == null
			&& contextPath == null
			&& prefix == null
		) {
			return DEFAULT;
		} else {
			return new SinglePartialURL(
				(scheme == null) ? null : scheme.toLowerCase(Locale.ROOT),
				host,
				port,
				contextPath,
				prefix
			);
		}
	}

	/**
	 * Gets a partial URL always within the current request.
	 *
	 * @param prefix       (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #of(java.lang.String, com.aoindustries.net.HostAddress, com.aoindustries.net.Port, com.aoindustries.net.Path, com.aoindustries.net.Path)
	 */
	public static SinglePartialURL of(Path prefix) {
		return of(null, null, null, null, prefix);
	}

	/**
	 * Gets a partial URL supporting requests across multiple schemes/hosts/ports/...
	 * 
	 * @param schemes       (Optional) The scheme (http/https/...) to match and/or link to, converted to lower-case.
	 * @param hosts         (Optional) The IP/host to match and/or link to
	 * @param ports         (Optional) The port to match and/or link to
	 * @param contextPaths  (Optional) The contextPath to match and/or link to
	 * @param prefixes      (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                 Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  MultiPartialURL#MultiPartialURL(java.util.Set, java.util.Set, java.util.Set, java.util.Set, java.util.Set)
	 */
	public static PartialURL of(
		Iterable<? extends String> schemes,
		Iterable<? extends HostAddress> hosts,
		Iterable<? extends Port> ports,
		Iterable<? extends Path> contextPaths,
		Iterable<? extends Path> prefixes
	) {
		Set<String> schemeSet;
		if(schemes == null) schemeSet = null;
		else {
			Set<String> schemesLower = new LinkedHashSet<String>();
			for(String scheme : schemes) schemesLower.add(scheme.toLowerCase(Locale.ROOT));
			schemeSet = AoCollections.optimalUnmodifiableSet(schemesLower);
			if(schemeSet.isEmpty()) schemeSet = null;
		}
		Set<HostAddress> hostSet;
		if(hosts == null) hostSet = null;
		else {
			hostSet = AoCollections.unmodifiableCopySet(hosts);
			if(hostSet.isEmpty()) hostSet = null;
		}
		Set<Port> portSet;
		if(ports == null) portSet = null;
		else {
			portSet = AoCollections.unmodifiableCopySet(ports);
			if(portSet.isEmpty()) portSet = null;
		}
		Set<Path> contextPathSet;
		if(contextPaths == null) contextPathSet = null;
		else {
			contextPathSet = AoCollections.unmodifiableCopySet(contextPaths);
			if(contextPathSet.isEmpty()) contextPathSet = null;
		}
		Set<Path> prefixSet;
		if(prefixes == null) prefixSet = null;
		else {
			prefixSet = AoCollections.unmodifiableCopySet(prefixes);
			if(prefixSet.isEmpty()) prefixSet = null;
		}
		if(
			(schemeSet == null || schemeSet.size() == 1)
			&& (hostSet == null || hostSet.size() == 1)
			&& (portSet == null || portSet.size() == 1)
			&& (contextPathSet == null || contextPathSet.size() == 1)
			&& (prefixSet == null || prefixSet.size() == 1)
		) {
			return of(
				schemeSet == null ? null : schemeSet.iterator().next(),
				hostSet == null ? null : hostSet.iterator().next(),
				portSet == null ? null : portSet.iterator().next(),
				contextPathSet == null ? null : contextPathSet.iterator().next(),
				prefixSet == null ? null : prefixSet.iterator().next()
			);
		} else {
			return new MultiPartialURL(schemeSet, hostSet, portSet, contextPathSet, prefixSet);
		}
	}

	/**
	 * Gets a partial URL supporting requests across multiple schemes/hosts/ports/...
	 * 
	 * @param schemes       (Optional) The scheme (http/https/...) to match and/or link to, converted to lower-case.
	 * @param hosts         (Optional) The IP/host to match and/or link to
	 * @param ports         (Optional) The port to match and/or link to
	 * @param contextPaths  (Optional) The contextPath to match and/or link to
	 * @param prefixes      (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                 Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #of(java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	public static PartialURL of(
		String[] schemes,
		HostAddress[] hosts,
		Port[] ports,
		Path[] contextPaths,
		Path ... prefixes
	) {
		return of(
			schemes == null ? null : Arrays.asList(schemes),
			hosts == null ? null : Arrays.asList(hosts),
			ports == null ? null : Arrays.asList(ports),
			contextPaths == null ? null : Arrays.asList(contextPaths),
			prefixes == null ? null : Arrays.asList(prefixes)
		);
	}

	/**
	 * Gets a partial URL always within the current request.
	 *
	 * @param prefix       (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #of(java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	public static PartialURL of(Iterable<? extends Path> prefixes) {
		return of(null, null, null, null, prefixes);
	}

	/**
	 * Gets a partial URL always within the current request.
	 *
	 * @param prefix       (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #of(java.lang.Iterable)
	 */
	public static PartialURL of(Path ... prefixes) {
		return of(Arrays.asList(prefixes));
	}

	protected PartialURL() {
	}

	@Override
	abstract public String toString();

	@Override
	abstract public boolean equals(Object obj);

	@Override
	abstract public int hashCode();

	/**
	 * Checks if the given {@link FieldSource} matches this partial URL.
	 * <p>
	 * The {@link SinglePartialURL} returned, if any, is also in the set of partial URLs provided by
	 * {@link #getCombinations()}.  Specifically, the match must be equivalent to
	 * the first match found by iterative calls to the results of {@link #getCombinations()},
	 * using {@link SinglePartialURL#matches(com.aoindustries.net.partialurl.FieldSource)}
	 * on each single partial.  The implementation, however, does not need to be iterative.
	 * </p>
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link #getCombinations()}</li>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)}</li>
	 *   <li>{@link PartialURLMap#get(com.aoindustries.net.partialurl.FieldSource)}</li>
	 * </ul>
	 *
	 * @param  fieldSource  When all fields are {@code null} (this is {@link #DEFAULT}), this is not used and may be {@code null}.
	 *
	 * @return  The {@link SinglePartialURL} with non-null fields selected to match the field source, or {@code null} when does not match.
	 */
	abstract public SinglePartialURL matches(FieldSource fieldSource) throws MalformedURLException;

	/**
	 * Checks if this partial URL is complete (has no {@code null} fields other than prefix).
	 * A complete URL may be converted to a {@link URL} without any
	 * {@link FieldSource field source} provided.
	 *
	 * @see  #toURL(com.aoindustries.net.partialurl.FieldSource)
	 */
	abstract public boolean isComplete();

	/**
	 * Gets the primary single partial URL for this partial URL.
	 * This will always be the same as the first value returned from
	 * {@link #getCombinations()}.
	 *
	 * @see  #getCombinations()
	 */
	abstract public SinglePartialURL getPrimary();

	/**
	 * Gets all combinations of single partial URLs represented by this partial URL.
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link #matches(com.aoindustries.net.partialurl.FieldSource)}</li>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)}</li>
	 *   <li>{@link PartialURLMap#get(com.aoindustries.net.partialurl.FieldSource)}</li>
	 * </ul>
	 * <p>
	 * The first result of iteration will always be the same as {@link #getPrimary()}.
	 * </p>
	 *
	 * @see  #getPrimary()
	 * @see  SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)
	 */
	abstract public Iterable<SinglePartialURL> getCombinations();

	/**
	 * Gets the general-purpose representation of {@link URL} for this partial URL.
	 *
	 * @param  fieldSource  Only used when at least one field is {@code null} and uses the value
	 *                      from the source.  May be {@code null} when this {@link PartialURL} is known
	 *                      to have all fields specified.
	 *
	 * @throws NullPointerException when {@code fieldSource} not provided and at least one field is {@code null}.
	 *
	 * @see  #isComplete()
	 */
	abstract public URL toURL(FieldSource fieldSource) throws MalformedURLException;
}
