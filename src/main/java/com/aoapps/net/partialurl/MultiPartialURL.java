/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.net.partialurl;

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.math.SafeMath;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A {@link PartialURL} that may contains multiple values for each field matched.
 * All fields are optional.
 */
public class MultiPartialURL extends PartialURL {

	private final Set<String> schemes;
	/**
	 * Maps to self for canonicalization
	 */
	private final Map<HostAddress, HostAddress> hosts;
	private final Set<Port> ports;
	private final Set<Path> contextPaths;
	private final Set<Path> prefixes;

	private final SinglePartialURL primary;

	/**
	 * @see  #valueOf(java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	MultiPartialURL(Set<String> schemes, Map<HostAddress, HostAddress> hosts, Set<Port> ports, Set<Path> contextPaths, Set<Path> prefixes) {
		this.schemes = schemes;
		this.hosts = hosts;
		this.ports = ports;
		if(contextPaths != null) {
			for(Path contextPath : contextPaths) {
				if(contextPath != Path.ROOT) {
					String contextPathStr = contextPath.toString();
					if(contextPathStr.endsWith(Path.SEPARATOR_STRING)) {
						throw new IllegalArgumentException("Non-root context path may not end in slash (" + Path.SEPARATOR_CHAR + "): " + contextPath);
					}
				}
			}
		}
		this.contextPaths = contextPaths;
		if(prefixes != null) {
			for(Path prefix : prefixes) {
				if(!prefix.toString().endsWith(Path.SEPARATOR_STRING)) {
					throw new IllegalArgumentException("Prefix does not end in slash (" + Path.SEPARATOR_CHAR + "): " + prefix);
				}
			}
		}
		this.prefixes = prefixes;
		// Generate primary now
		primary = valueOf(
			schemes == null ? null : schemes.iterator().next(),
			hosts == null ? null : hosts.keySet().iterator().next(),
			ports == null ? null : ports.iterator().next(),
			contextPaths == null ? null : contextPaths.iterator().next(),
			prefixes == null ? null : prefixes.iterator().next()
		);
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		// scheme(s)
		if(schemes != null) {
			if(schemes.size() == 1) {
				toString.append(schemes.iterator().next());
			} else {
				toString.append('{');
				boolean didOne = false;
				for(String scheme : schemes) {
					if(didOne) toString.append(',');
					else didOne = true;
					toString.append(scheme);
				}
				toString.append('}');
			}
			toString.append(':');
		}
		toString.append("//");
		if(hosts == null) {
			toString.append(WILDCARD_CHAR);
		} else if(hosts.size() == 1) {
			toString.append(hosts.keySet().iterator().next().toBracketedString());
		} else {
			toString.append('{');
			boolean didOne = false;
			for(HostAddress host : hosts.keySet()) {
				if(didOne) toString.append(',');
				else didOne = true;
				toString.append(host.toBracketedString());
			}
			toString.append('}');
		}
		if(ports == null) {
			toString.append(':').append(WILDCARD_CHAR);
		} else if(ports.size() == 1) {
			// Check if there is only one scheme, hide port when is default
			String scheme;
			if(schemes == null || schemes.size() > 1) {
				scheme = null;
			} else {
				assert schemes.size() == 1;
				scheme = schemes.iterator().next();
			}
			int portNum = ports.iterator().next().getPort();
			if(
				!(HTTP.equals(scheme) && portNum == 80)
				&& !(HTTPS.equals(scheme) && portNum == 443)
			) {
				toString.append(':').append(portNum);
			}
		} else {
			toString.append(":{");
			boolean didOne = false;
			for(Port port : ports) {
				if(didOne) toString.append(',');
				else didOne = true;
				toString.append(port.getPort());
			}
			toString.append('}');
		}
		if(contextPaths == null) {
			toString.append(NULL_CONTEXT_PATH);
		} else if(contextPaths.size() == 1) {
			Path contextPath = contextPaths.iterator().next();
			if(contextPath != Path.ROOT) toString.append(contextPath);
		} else {
			toString.append('{');
			boolean didOne = false;
			for(Path contextPath : contextPaths) {
				if(didOne) toString.append(',');
				else didOne = true;
				if(contextPath != Path.ROOT) toString.append(contextPath);
			}
			toString.append('}');
		}
		if(prefixes == null) {
			toString.append(NULL_PREFIX);
		} else if(prefixes.size() == 1) {
			toString.append(prefixes.iterator().next());
		} else {
			toString.append('{');
			boolean didOne = false;
			for(Path prefix : prefixes) {
				if(didOne) toString.append(',');
				else didOne = true;
				toString.append(prefix);
			}
			toString.append('}');
		}
		return toString.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MultiPartialURL)) return false;
		MultiPartialURL other = (MultiPartialURL)obj;
		return
			Objects.equals(schemes, other.schemes)
			&& Objects.equals((hosts == null) ? null : hosts.keySet(), (other.hosts == null) ? null : other.hosts.keySet())
			&& Objects.equals(ports, other.ports)
			&& Objects.equals(contextPaths, other.contextPaths)
			&& Objects.equals(prefixes, other.prefixes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			schemes,
			hosts.keySet(),
			ports,
			contextPaths,
			prefixes
		);
	}

	/**
	 * Sequential implementation of {@link #matches(com.aoapps.net.partialurl.FieldSource)}, only
	 * used for assertions.  This verifies the matching defined in {@link PartialURL#matches(com.aoapps.net.partialurl.FieldSource)}.
	 *
	 * @see  #matches(com.aoapps.net.partialurl.FieldSource)
	 * @see  PartialURL#matches(com.aoapps.net.partialurl.FieldSource)
	 * @see  #getCombinations()
	 */
	private SinglePartialURL matchesSequential(FieldSource fieldSource) throws MalformedURLException {
		for(SinglePartialURL single : getCombinations()) {
			SinglePartialURL match = single.matches(fieldSource);
			if(match != null) {
				assert !(match.equals(primary) && match != primary) : "match must be same object as primary when they are equal";
				assert match == single;
				return single;
			}
		}
		return null;
	}

	@Override
	public SinglePartialURL matches(FieldSource fieldSource) throws MalformedURLException {
		SinglePartialURL match;
		String scheme = null;
		if(schemes != null && !schemes.contains(scheme = fieldSource.getScheme().toLowerCase(Locale.ROOT))) {
			match = null;
		} else {
			HostAddress host = null;
			if(hosts != null && !hosts.containsKey(host = fieldSource.getHost())) {
				match = null;
			} else {
				Port port = null;
				if(ports != null && !ports.contains(port = fieldSource.getPort())) {
					match = null;
				} else {
					Path contextPath = null;
					if(contextPaths != null && !contextPaths.contains(contextPath = fieldSource.getContextPath())) {
						match = null;
					} else {
						if(prefixes == null) {
							match = valueOf(scheme, host, port, contextPath, null);
						} else {
							Path path = fieldSource.getPath();
							if(path == null) {
								match = null;
							} else {
								String pathStr = path.toString();
								// TODO: Max path length like in index?
								// TODO: Max slashes like index?
								int lastSlash = pathStr.lastIndexOf(Path.SEPARATOR_CHAR);
								assert lastSlash != -1;
								match = null;
								do {
									Path prefix = path.prefix(lastSlash + 1);
									if(prefixes.contains(prefix)) {
										match = valueOf(scheme, host, port, contextPath, prefix);
										break;
									}
									lastSlash = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, lastSlash - 1);
								} while(lastSlash != -1);
							}
						}
					}
				}
			}
		}
		if(match != null && match.equals(primary)) match = primary;
		assert Objects.equals(match, matchesSequential(fieldSource)) : "matches inconsistent with matchesSequential";
		return match;
	}

	@Override
	public boolean isComplete() {
		return
			schemes != null
			&& hosts != null
			&& ports != null
			&& contextPaths != null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * When there is not more than one {@link #getPrefixes() prefix}, this will
	 * be the first value returned from {@link #getCombinations()}.
	 * </p>
	 * <p>
	 * <b>Implementation Note:</b><br>
	 * Uses the first value from each set.
	 * </p>
	 *
	 * @see  #getCombinations()
	 */
	@Override
	public SinglePartialURL getPrimary() {
		return primary;
	}

	/**
	 * Iterates a single null value.
	 */
	private static final Iterable<?> NULL_ITERABLE = Collections.singleton(null);
	@SuppressWarnings("unchecked")
	private static <T> Iterable<T> nullIterable() {
		return (Iterable<T>)NULL_ITERABLE;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets are iterated in the following order:
	 * </p>
	 * <ol>
	 * <li>{@link #getHosts()}</li>
	 * <li>{@link #getContextPaths()}</li>
	 * <li>{@link #getPrefixes()} (sorted by deepest first for consistency with {@link SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)})</li>
	 * <li>{@link #getPorts()}</li>
	 * <li>{@link #getSchemes()}</li>
	 * </ol>
	 * <p>
	 * When there is not more than one {@link #getPrefixes() prefix}, the first value returned
	 * will be the {@link #getPrimary() primary}.
	 * </p>
	 * <p>
	 * TODO: A more space-efficient implementation could generate these on-the-fly.
	 * Or should we just return the generated {@link Set}?
	 * Will depend on how his is used.
	 * </p>
	 * <p>
	 * <b>Implementation Note:</b><br>
	 * This currently generates the full set at the time of method invocation.
	 * This is not a performance-oriented implementation.  Please see
	 * {@link  PartialURLMap} for a fast way to index partial URLs.
	 * </p>
	 *
	 * @see  #getPrimary()
	 */
	@Override
	public Iterable<SinglePartialURL> getCombinations() {
		long combinations = SafeMath.multiply(
			(hosts        == null ? 1 : hosts.size()),
			(contextPaths == null ? 1 : contextPaths.size()),
			(prefixes     == null ? 1 : prefixes.size()),
			(ports        == null ? 1 : ports.size()),
			(schemes      == null ? 1 : schemes.size())
		);
		if(combinations > Integer.MAX_VALUE) throw new IllegalStateException("Too many combinations: " + combinations);
		Set<SinglePartialURL> results = AoCollections.newLinkedHashSet((int)combinations);

		Iterable<HostAddress> hostIter;
		if(hosts == null) hostIter = nullIterable();
		else hostIter = hosts.keySet();

		Iterable<Path> contextPathIter;
		if(contextPaths == null) contextPathIter = nullIterable();
		else contextPathIter = contextPaths;

		Iterable<Path> prefixIter;
		if(prefixes == null) prefixIter = nullIterable();
		else {
			SortedSet<Path> sortedPrefixes = new TreeSet<>(SinglePartialURL.prefixComparator);
			sortedPrefixes.addAll(prefixes);
			prefixIter = sortedPrefixes;
		}

		Iterable<Port> portIter;
		if(ports == null) portIter = nullIterable();
		else portIter = ports;

		Iterable<String> schemesIter;
		if(schemes == null) schemesIter = nullIterable();
		else schemesIter = schemes;

		for(HostAddress host : hostIter) {
			for(Path contextPath : contextPathIter) {
				for(Path prefix : prefixIter) {
					for(Port port : portIter) {
						for(String scheme : schemesIter) {
							SinglePartialURL single = valueOf(scheme, host, port, contextPath, prefix);
							// Use existing primary object when matches the result
							if(single.equals(primary)) {
								single = primary;
							}
							if(!results.add(single)) {
								throw new AssertionError("Unexpected duplicate partial URL combination: " + single);
							}
						}
					}
				}
			}
		}
		if(results.size() != combinations) throw new AssertionError("Unexpected number of combinations.  Expected " + combinations + ", got " + results.size());
		return results;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Implementation Note:</b><br>
	 * This selects the specifically matching fields from each set when field source is non-null.
	 * </p>
	 */
	@Override
	@SuppressWarnings("AssertWithSideEffects")
	public URL toURL(FieldSource fieldSource) throws MalformedURLException {
		String schemeStr;
		{
			if(schemes == null) {
				assert fieldSource != null;
				schemeStr = fieldSource.getScheme();
			} else {
				String sourceSchemeLower;
				if(fieldSource != null && schemes.contains(sourceSchemeLower = fieldSource.getScheme().toLowerCase(Locale.ROOT))) {
					schemeStr = sourceSchemeLower;
				} else {
					schemeStr = schemes.iterator().next();
				}
			}
		}
		int portNum;
		{
			if(ports == null) {
				assert fieldSource != null;
				portNum = fieldSource.getPort().getPort();
			} else {
				Port sourcePort;
				if(fieldSource != null && ports.contains(sourcePort = fieldSource.getPort())) {
					portNum = sourcePort.getPort();
				} else {
					portNum = ports.iterator().next().getPort();
				}
			}
		}
		if(
			// TODO: Could use URL#getDefaultPort() and moved this hard-coded check to HttpServletRequestFieldSource
			(HTTP.equalsIgnoreCase(schemeStr) && portNum == 80)
			|| (HTTPS.equalsIgnoreCase(schemeStr) && portNum == 443)
		) {
			portNum = -1;
		}
		String hostStr;
		{
			if(hosts == null) {
				assert fieldSource != null;
				hostStr = fieldSource.getHost().toBracketedString();
			} else {
				HostAddress canonical;
				if(fieldSource != null && (canonical = hosts.get(fieldSource.getHost())) != null) {
					hostStr = canonical.toBracketedString();
				} else {
					hostStr = hosts.keySet().iterator().next().toBracketedString();
				}
			}
		}
		Path contextPath;
		{
			if(contextPaths == null) {
				assert fieldSource != null;
				contextPath = fieldSource.getContextPath();
			} else {
				Path sourceContextPath;
				if(fieldSource != null && contextPaths.contains(sourceContextPath = fieldSource.getContextPath())) {
					contextPath = sourceContextPath;
				} else {
					contextPath = contextPaths.iterator().next();
				}
			}
		}
		String file;
		if(contextPath == Path.ROOT) {
			file = (prefixes == null) ? "" : prefixes.iterator().next().toString();
		} else {
			if(prefixes == null) {
				file = contextPath.toString();
			} else {
				file = contextPath.toString() + prefixes.iterator().next().toString();
			}
		}
		try {
			URL url = new URL(
				schemeStr,
				hostStr,
				portNum,
				file
			);
			SinglePartialURL match;
			assert !(
				fieldSource != null
				&& (prefixes == null || prefixes.size() == 1) // toURL prefix by original order, matches by deepest, but will be same order always when zero or one prefix
				&& (match = matches(fieldSource)) != null
				&& !match.toURL(fieldSource).equals(url)
			) : "matches().toURL() must be consistent with toURL() when fieldSource provided and less than two prefixes";
			return url;
		} catch(MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Gets the unmodifiable set of lower-case schemes (such as https/http/other) for this partial URL.
	 *
	 * @return  The schemes or {@code null} when {@link FieldSource#getScheme()} should be used.
	 *
	 * @see  #HTTP
	 * @see  #HTTPS
	 *
	 * @see  FieldSource#getScheme()
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Set<String> getSchemes() {
		return schemes;
	}

	/**
	 * Gets the unmodifiable set of IP addresses or hostnames for this partial URL.
	 *
	 * @return  The IP addresses/hostnames or {@code null} when {@link FieldSource#getHost()} should be used.
	 *
	 * @see  FieldSource#getHost()
	 */
	public Set<HostAddress> getHosts() {
		return hosts.keySet();
	}

	/**
	 * Gets the unmodifiable set of port numbers for this partial URL.
	 *
	 * @return  The ports or {@code null} when {@link FieldSource#getPort()} should be used.
	 *
	 * @see  FieldSource#getPort()
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Set<Port> getPorts() {
		return ports;
	}

	/**
	 * Gets the unmodifiable set of context paths for this partial URL, only ending in a slash (/) when is
	 * {@link Path#ROOT the root context}.
	 *
	 * @return  The context paths or {@code null} when the {@link FieldSource#getContextPath() context path of the field source} should be used.
	 *
	 * @see  FieldSource#getContextPath()
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Set<Path> getContextPaths() {
		return contextPaths;
	}

	/**
	 * Gets the unmodifiable set of prefixes of the path for this partial URL, always either {@code null} or ending in a slash (/).
	 * This is matched as a prefix of {@link  FieldSource#getPath()}.
	 *
	 * @see  FieldSource#getPath()
	 * @see  Path#SEPARATOR_CHAR
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Set<Path> getPrefixes() {
		return prefixes;
	}
}
