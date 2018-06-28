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
import com.aoindustries.util.MinimalMap;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Maps {@link PartialURL partial URLs} to arbitrary values and provides fast lookups.
 */
public class PartialURLMap<V> {

	private static final boolean ASSERTIONS_ENABLED;
	static {
		boolean assertsEnabled = false;
		assert (assertsEnabled = true); // Intentional side effects
		ASSERTIONS_ENABLED = assertsEnabled;
	}

	// Java 1.8: StampedLock since not needing reentrant
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	private final Map<HostAddress,Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>> index = new HashMap<HostAddress,Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>>();

	/**
	 * For sequential implementation used for assertions only.
	 *
	 * @see  #getSequential(com.aoindustries.net.partialurl.FieldSource)
	 */
	private final SortedMap<SinglePartialURL,ImmutablePair<PartialURL,V>> sequential = ASSERTIONS_ENABLED ? new TreeMap<SinglePartialURL,ImmutablePair<PartialURL,V>>() : null;

	/**
	 * Adds a new partial URL to this map while checking for conflicts.
	 * <p>
	 * TODO: Use {@link MinimalMap} in the index?
	 * </p>
	 *
	 * @implNote  Currently, when an exception occurs, the index may be in a partial state.  Changes are not rolled-back.
	 *
	 * @throws  IllegalStateException  If the partial URL conflicts with an existing entry.
	 */
	public void put(PartialURL partialURL, V value) throws IllegalStateException {
		writeLock.lock();
		try {
			for(SinglePartialURL singleURL : partialURL.getCombinations()) {
				Path prefix = singleURL.getPrefix();
				@SuppressWarnings("deprecation")
				String prefixStr = ObjectUtils.toString(prefix, null); // Java 1.7: Use Objects
				int slashCount = (prefixStr == null) ? 0 : StringUtils.countMatches(prefixStr, Path.SEPARATOR_CHAR);
				// host
				HostAddress host = singleURL.getHost();
				Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>> hostIndex = index.get(host);
				if(hostIndex == null) {
					hostIndex = new HashMap<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>();
					index.put(host, hostIndex);
				}
				// contextPath
				Path contextPath = singleURL.getContextPath();
				MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>> contextPathPair = hostIndex.get(contextPath);
				if(contextPathPair == null) {
					contextPathPair = MutablePair.of(
						slashCount,
						(Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>)new HashMap<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>()
					);
					hostIndex.put(contextPath, contextPathPair);
				} else {
					// Store the maximum path depth for prefix-based match, only parse this far while inside "get"
					if(slashCount > contextPathPair.left) contextPathPair.left = slashCount;
				}
				Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>> contextPathIndex = contextPathPair.right;
				// prefix
				Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>> prefixIndex = contextPathIndex.get(prefixStr);
				if(prefixIndex == null) {
					prefixIndex = new HashMap<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>();
					contextPathIndex.put(prefixStr, prefixIndex);
				}
				// port
				Port port = singleURL.getPort();
				Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>> portIndex = prefixIndex.get(port);
				if(portIndex == null) {
					portIndex = new HashMap<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>();
					prefixIndex.put(port, portIndex);
				}
				// scheme
				String scheme = singleURL.getScheme();
				ImmutableTriple<PartialURL,SinglePartialURL,V> existing = portIndex.get(scheme);
				if(existing != null) {
					throw new IllegalStateException(
						"Partial URL already in index: partialURL = " + partialURL
							+ ", singleURL = " + singleURL
							+ ", existing = " + existing.getLeft());
				}
				portIndex.put(
					scheme,
					ImmutableTriple.of(partialURL, singleURL, value)
				);
				if(ASSERTIONS_ENABLED) {
					if(sequential.put(singleURL, ImmutablePair.of(partialURL, value)) != null) throw new AssertionError("Duplicate singleURL: " + singleURL);
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Indexed implementation of {@link #get(com.aoindustries.net.partialurl.FieldSource)}.
	 *
	 * @see  #index
	 * @see  #get(com.aoindustries.net.partialurl.FieldSource)
	 */
	@SuppressWarnings("deprecation") // Java 1.7: No longer suppress
	private PartialURLMatch<V> getIndexed(FieldSource fieldSource) throws MalformedURLException {
		// Must be holding readLock already
		// TODO: CompletePartialURL (subclassing single) instead of toURL?
		// TODO: A sequential implementation for assertions, like in PathSpace?
		// TODO: Write tests
		HostAddress[] hostSearchOrder = new HostAddress[] {fieldSource.getHost(), null};
		Path[] contextPathSearchOrder = new Path[] {fieldSource.getContextPath(), null};
		Path path = fieldSource.getPath();
		String pathStr = (path == null) ? "" : path.toString();
		Port[] portSearchOrder = new Port[] {fieldSource.getPort(), null};
		String[] schemeSearchOrder = new String[] {fieldSource.getScheme().toLowerCase(Locale.ROOT), null};
		for(HostAddress host : hostSearchOrder) {
			Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>> hostIndex = index.get(host);
			if(hostIndex != null) {
				for(Path contextPath : contextPathSearchOrder) {
					MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>> contextPathPair = hostIndex.get(contextPath);
					if(contextPathPair != null) {
						final int maxSlashCount = contextPathPair.left;
						Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>> contextPathIndex = contextPathPair.right;
						// TODO: Could store slash indexes in an array instead of searching back-and-forth
						// TODO: Could also store the resulting substrings, too
						// TODO: If doing this, track the maximum number of slashes anywhere in the index during put, and create the array this size
						// TODO: Might not be worth this, because this slash scan is only done four times maximum (host, null) x (contextPath, null)
						int slashCount = 0;
						int lastSlashPos = -1;
						// TODO: Track maximum path length in the index, and constrain that here, too, to avoid overhead for impossible-match lookups
						while(slashCount < maxSlashCount) {
							int slashPos = pathStr.indexOf(Path.SEPARATOR_CHAR, lastSlashPos + 1);
							if(slashPos == -1) break;
							lastSlashPos = slashPos;
							slashCount++;
						}
						while(slashCount >= 0) {
							String prefix;
							if(slashCount == 0) {
								// Looking for null key on slashCount 0
								assert lastSlashPos == -1;
								prefix = null;
							} else {
								// Get the current prefix of the lookup path
								assert lastSlashPos != -1;
								prefix = pathStr.substring(0, lastSlashPos + 1);
							}
							Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>> prefixIndex = contextPathIndex.get(prefix);
							if(prefixIndex != null) {
								for(Port port : portSearchOrder) {
									Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>> portIndex = prefixIndex.get(port);
									if(portIndex != null) {
										for(String scheme : schemeSearchOrder) {
											ImmutableTriple<PartialURL,SinglePartialURL,V> match = portIndex.get(scheme);
											if(match != null) {
												assert ObjectUtils.equals(match.left.matches(fieldSource), match.middle) : "Get inconsistent with matches";
												assert ObjectUtils.equals(match.middle.matches(fieldSource), match.middle) : "Get inconsistent with matches";
												return new PartialURLMatch<V>(
													match.left,
													match.middle,
													match.middle.toURL(fieldSource),
													match.right
												);
											}
										}
									}
								}
							}
							// Work up the path one slash at a time
							lastSlashPos = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, lastSlashPos - 1);
							slashCount--;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Sequential implementation of {@link #get(com.aoindustries.net.partialurl.FieldSource)} used for assertions only.
	 * Verifies that a sequential scan calling {@link SinglePartialURL#matches(com.aoindustries.net.partialurl.FieldSource)}
	 * yields the same result as the indexed lookup performed in {@link #get(com.aoindustries.net.partialurl.FieldSource)}.
	 *
	 * @see  #sequential
	 * @see  #get(com.aoindustries.net.partialurl.FieldSource)
	 */
	private PartialURLMatch<V> getSequential(FieldSource fieldSource) throws MalformedURLException {
		// Must be holding readLock already
		for(Map.Entry<SinglePartialURL,ImmutablePair<PartialURL,V>> entry : sequential.entrySet()) {
			SinglePartialURL singleURL = entry.getKey();
			SinglePartialURL match = singleURL.matches(fieldSource);
			if(match != null) {
				assert match == singleURL;
				ImmutablePair<PartialURL,V> pair = entry.getValue();
				return new PartialURLMatch<V>(
					pair.left,
					singleURL,
					singleURL.toURL(fieldSource),
					pair.right
				);
			}
		}
		return null;
	}

	/**
	 * Gets the value associated with the given URL, returning the most specific match.
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link PartialURL#matches(com.aoindustries.net.partialurl.FieldSource)}</li>
	 *   <li>{@link PartialURL#getCombinations()}</li>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)}</li>
	 * </ul>
	 *
	 * @return  The matching value or {@code null} of no match
	 *
	 * @implNote  The maximum number of internal map lookups is: {@code (host, null) * (contextPath, null) * (maxSlashCount + 1) * (scheme, null) * (port, null)},
	 *            or {@code 2 * 2 * (maxSlashCount + 1) * 2 * 2}, or {@code 16 * (maxSlashCount + 1)}.  The actual number of map lookups
	 *            will typically be much less than this due to a sparsely populated index.
	 */
	@SuppressWarnings("deprecation") // Java 1.7: No longer suppress
	public PartialURLMatch<V> get(FieldSource fieldSource) throws MalformedURLException {
		PartialURLMatch<V> indexedMatch;
		PartialURLMatch<V> sequentialMatch;
		readLock.lock();
		try {
			indexedMatch = getIndexed(fieldSource);
			sequentialMatch = ASSERTIONS_ENABLED ? getSequential(fieldSource) : null;
		} finally {
			readLock.unlock();
		}
		if(ASSERTIONS_ENABLED && !ObjectUtils.equals(indexedMatch, sequentialMatch)) {
			throw new AssertionError("getIndexed is inconsistent with getSequential: indexedMatch = " + indexedMatch + ", sequentialMatch = " + sequentialMatch);
		}
		return indexedMatch;
	}
}
