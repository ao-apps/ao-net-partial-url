/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
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
 * along with ao-net-partial-url.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.net.partialurl;

import com.aoapps.collections.AoCollections;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.functors.NotNullPredicate;

/**
 * A {@link PartialURL} matches and completes URLs.
 * All fields are optional.
 * <p>
 * This is not a general-purpose representation of a URL.  It only contains the
 * fields specifically used for matching a request to a virtual host (TODO: Links to projects).  For
 * an instance of {@link URL}, see {@link #toURL(com.aoapps.net.partialurl.FieldSource)}.
 * </p>
 * <p>
 * All partial URLs are immutable value types.
 * </p>
 */
// TODO: Should this be an interface?  All methods are abstract.
public abstract class PartialURL {

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
	 * This default host is after all others in {@link SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)}
	 * and {@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}.
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
	 * @see  SinglePartialURL#SinglePartialURL(java.lang.String, com.aoapps.net.HostAddress, com.aoapps.net.Port, com.aoapps.net.Path, com.aoapps.net.Path)
	 */
	// TODO: String/integer-only overloads?
	public static SinglePartialURL valueOf(String scheme, HostAddress host, Port port, Path contextPath, Path prefix) {
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
	 * @see  #valueOf(java.lang.String, com.aoapps.net.HostAddress, com.aoapps.net.Port, com.aoapps.net.Path, com.aoapps.net.Path)
	 */
	public static SinglePartialURL valueOf(Path prefix) {
		return PartialURL.valueOf(null, null, null, null, prefix);
	}

	/**
	 * Gets a partial URL supporting requests across multiple schemes/hosts/ports/...
	 *
	 * @param schemes       (Optional) The scheme (http/https/...) to match and/or link to, converted to lower-case.
	 *                                 {@code null} elements are skipped.
	 * @param hosts         (Optional) The IP/host to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param ports         (Optional) The port to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param contextPaths  (Optional) The contextPath to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param prefixes      (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                 Must be either {@code null} or always ends in a slash (/).
	 *                                 {@code null} elements are skipped.
	 *
	 * @see  MultiPartialURL#MultiPartialURL(java.util.Set, java.util.Map, java.util.Set, java.util.Set, java.util.Set)
	 */
	public static PartialURL valueOf(
		Iterable<? extends String> schemes,
		Iterable<? extends HostAddress> hosts,
		Iterable<? extends Port> ports,
		Iterable<? extends Path> contextPaths,
		Iterable<? extends Path> prefixes
	) {
		Set<String> schemeSet;
		if(schemes == null) schemeSet = null;
		else {
			Set<String> schemesLower = new LinkedHashSet<>();
			for(String scheme : IterableUtils.filteredIterable(schemes, NotNullPredicate.notNullPredicate())) {
				schemesLower.add(scheme.toLowerCase(Locale.ROOT));
			}
			schemeSet = AoCollections.optimalUnmodifiableSet(schemesLower);
			if(schemeSet.isEmpty()) schemeSet = null;
		}
		Map<HostAddress, HostAddress> hostMap;
		if(hosts == null) hostMap = null;
		else {
			Map<HostAddress, HostAddress> copyMap = new LinkedHashMap<>();
			for(HostAddress host : IterableUtils.filteredIterable(hosts, NotNullPredicate.notNullPredicate())) {
				copyMap.put(host, host);
			}
			hostMap = copyMap.isEmpty() ? null : AoCollections.optimalUnmodifiableMap(copyMap);
		}
		Set<Port> portSet;
		if(ports == null) portSet = null;
		else {
			portSet = AoCollections.unmodifiableCopySet(
				IterableUtils.filteredIterable(ports, NotNullPredicate.notNullPredicate())
			);
			if(portSet.isEmpty()) portSet = null;
		}
		Set<Path> contextPathSet;
		if(contextPaths == null) contextPathSet = null;
		else {
			contextPathSet = AoCollections.unmodifiableCopySet(
				IterableUtils.filteredIterable(contextPaths, NotNullPredicate.notNullPredicate())
			);
			if(contextPathSet.isEmpty()) contextPathSet = null;
		}
		Set<Path> prefixSet;
		if(prefixes == null) prefixSet = null;
		else {
			prefixSet = AoCollections.unmodifiableCopySet(
				IterableUtils.filteredIterable(prefixes, NotNullPredicate.notNullPredicate())
			);
			if(prefixSet.isEmpty()) prefixSet = null;
		}
		if(
			(schemeSet == null || schemeSet.size() == 1)
			&& (hostMap == null || hostMap.size() == 1)
			&& (portSet == null || portSet.size() == 1)
			&& (contextPathSet == null || contextPathSet.size() == 1)
			&& (prefixSet == null || prefixSet.size() == 1)
		) {
			return PartialURL.valueOf(
				schemeSet == null ? null : schemeSet.iterator().next(),
				hostMap == null ? null : hostMap.keySet().iterator().next(),
				portSet == null ? null : portSet.iterator().next(),
				contextPathSet == null ? null : contextPathSet.iterator().next(),
				prefixSet == null ? null : prefixSet.iterator().next()
			);
		} else {
			return new MultiPartialURL(schemeSet, hostMap, portSet, contextPathSet, prefixSet);
		}
	}

	/**
	 * Gets a partial URL supporting requests across multiple schemes/hosts/ports/...
	 *
	 * @param schemes       (Optional) The scheme (http/https/...) to match and/or link to, converted to lower-case.
	 *                                 {@code null} elements are skipped.
	 * @param hosts         (Optional) The IP/host to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param ports         (Optional) The port to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param contextPaths  (Optional) The contextPath to match and/or link to
	 *                                 {@code null} elements are skipped.
	 * @param prefixes      (Optional) The prefix to match against the path or {@code null} to match all.
	 *                                 Must be either {@code null} or always ends in a slash (/).
	 *                                 {@code null} elements are skipped.
	 *
	 * @see  #valueOf(java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	public static PartialURL valueOf(
		String[] schemes,
		HostAddress[] hosts,
		Port[] ports,
		Path[] contextPaths,
		Path ... prefixes
	) {
		return PartialURL.valueOf(
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
	 * @param prefixes  (Optional) The prefix to match against the path or {@code null} to match all.
	 *                             Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #valueOf(java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	public static PartialURL valueOf(Iterable<? extends Path> prefixes) {
		return PartialURL.valueOf(null, null, null, null, prefixes);
	}

	/**
	 * Gets a partial URL always within the current request.
	 *
	 * @param prefixes  (Optional) The prefix to match against the path or {@code null} to match all.
	 *                             Must be either {@code null} or always ends in a slash (/).
	 *
	 * @see  #valueOf(java.lang.Iterable)
	 */
	public static PartialURL valueOf(Path ... prefixes) {
		return PartialURL.valueOf(Arrays.asList(prefixes));
	}

	protected PartialURL() {
		// Do nothing
	}

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Checks if the given {@link FieldSource} matches this partial URL.
	 * <p>
	 * The {@link SinglePartialURL} returned, if any, is also in the set of partial URLs provided by
	 * {@link #getCombinations()}.  Specifically, the match must be equivalent to
	 * the first match found by iterative calls to the results of {@link #getCombinations()},
	 * using {@link SinglePartialURL#matches(com.aoapps.net.partialurl.FieldSource)}
	 * on each single partial.  The implementation, however, does not need to be iterative.
	 * </p>
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link #getCombinations()}</li>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)}</li>
	 *   <li>{@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}</li>
	 * </ul>
	 *
	 * @param  fieldSource  When all fields are {@code null} (this is {@link #DEFAULT}), this is not used and may be {@code null}.
	 *
	 * @return  The {@link SinglePartialURL} with non-null fields selected to match the field source, or {@code null} when does not match.
	 *          When the match is {@link SinglePartialURL#equals(java.lang.Object) equals} the {@link #getPrimary() primary}, returns
	 *          the same object as {@link #getPrimary()}.
	 */
	public abstract SinglePartialURL matches(FieldSource fieldSource) throws MalformedURLException;

	/**
	 * Checks if this partial URL is complete (has no {@code null} fields other than prefix).
	 * A complete URL may be converted to a {@link URL} without any
	 * {@link FieldSource field source} provided.
	 *
	 * @see  #toURL(com.aoapps.net.partialurl.FieldSource)
	 */
	public abstract boolean isComplete();

	/**
	 * Gets the primary single partial URL for this partial URL.
	 * <p>
	 * This will always be found in {@link #getCombinations()}.
	 * </p>
	 *
	 * @see  #getCombinations()
	 */
	public abstract SinglePartialURL getPrimary();

	/**
	 * Gets all combinations of single partial URLs represented by this partial URL.
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link #matches(com.aoapps.net.partialurl.FieldSource)}</li>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)}</li>
	 *   <li>{@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}</li>
	 * </ul>
	 * <p>
	 * When one of the results is {@link SinglePartialURL#equals(java.lang.Object) equal} to the
	 * {@link #getPrimary() primary}, returns the same object instance as {@link #getPrimary()}.
	 * </p>
	 *
	 * @see  #getPrimary()
	 * @see  SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)
	 */
	public abstract Iterable<SinglePartialURL> getCombinations();

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
	public abstract URL toURL(FieldSource fieldSource) throws MalformedURLException;
}
