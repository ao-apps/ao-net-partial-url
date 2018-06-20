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
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Maps {@link PartialURL partial URLs} to arbitrary values and provides fast lookups.
 */
public class PartialURLMap<V> {

	// TODO: Java 1.8: StampedLock since not needing reentrant
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	private final Map<HostAddress,Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>> index = new HashMap<HostAddress,Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>>();

	/**
	 * Adds a new partial URL to this map while checking for conflicts.
	 *
	 * @implNote  Currently, when an exception occurs, the index may be in a partial state.  Changes are not rolled-back.
	 *
	 * @throws  IllegalStateException  If the partial URL conflicts with an existing entry.
	 */
	public void put(PartialURL partialURL, V value) throws IllegalStateException {
		writeLock.lock();
		try {
			for(SinglePartialURL single : partialURL.getCombinations()) {
				Path prefix = single.getPrefix();
				@SuppressWarnings("deprecation")
				String prefixStr = ObjectUtils.toString(prefix, null); // TODO: Java 1.7: Use Objects
				int slashCount = (prefixStr == null) ? 0 : StringUtils.countMatches(prefixStr, Path.SEPARATOR_CHAR);
				// host
				HostAddress host = single.getHost();
				Map<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>> hostIndex = index.get(host);
				if(hostIndex == null) {
					hostIndex = new HashMap<Path,MutablePair<Integer,Map<String,Map<Port,Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>>>>>();
					index.put(host, hostIndex);
				}
				// contextPath
				Path contextPath = single.getContextPath();
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
				Port port = single.getPort();
				Map<String,ImmutableTriple<PartialURL,SinglePartialURL,V>> portIndex = prefixIndex.get(port);
				if(portIndex == null) {
					portIndex = new HashMap<String,ImmutableTriple<PartialURL,SinglePartialURL,V>>();
					prefixIndex.put(port, portIndex);
				}
				// schemeLower
				String schemeLower = single.getSchemeLower();
				ImmutableTriple<PartialURL,SinglePartialURL,V> existing = portIndex.get(schemeLower);
				if(existing != null) {
					throw new IllegalStateException(
						"Partial URL already in index: partialURL = " + partialURL
							+ ", single = " + single
							+ ", existing = " + existing.getLeft());
				}
				portIndex.put(
					schemeLower,
					ImmutableTriple.of(partialURL, single, value)
				);
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Gets the value associated with the given URL, returning the most specific match.
	 * <p>
	 * Ordering is consistent with:
	 * </p>
	 * <ul>
	 *   <li>{@link SinglePartialURL#compareTo(com.aoindustries.net.partialurl.SinglePartialURL)}</li>
	 *   <li>{@link PartialURL#getCombinations()}</li>
	 * </ul>
	 *
	 * @return  The matching value or {@code null} of no match
	 *
	 * @implNote  The maximum number of internal map lookups is: {@code (host, null) * (contextPath, null) * (maxSlashCount + 1) * (schemeLower, null) * (port, null)},
	 *            or {@code 2 * 2 * (maxSlashCount + 1) * 2 * 2}, or {@code 16 * (maxSlashCount + 1)}.  The actual number of map lookups
	 *            will typically be much less than this due to a sparsely populated index.
	 */
	public PartialURLMatch<V> get(FieldSource fieldSource) throws MalformedURLException {
		// TODO: CompletePartialURL (subclassing single) instead of toURL?
		// TODO: A sequential implementation for assertions, like in PathSpace?
		// TODO: Write tests
		HostAddress[] hostSearchOrder = new HostAddress[] {fieldSource.getHost(), null};
		Path[] contextPathSearchOrder = new Path[] {fieldSource.getContextPath(), null};
		Path path = fieldSource.getPath();
		String pathStr = (path == null) ? "" : path.toString();
		Port[] portSearchOrder = new Port[] {fieldSource.getPort(), null}; // TODO: Deal with -1 port here or disallow it from FieldSource
		String[] schemeLowerSearchOrder = new String[] {fieldSource.getScheme().toLowerCase(Locale.ROOT), null};
		// Note: readLock is releases once a match is found, but before toURL, so that all accesses to fieldSource are without holding the readLock
		boolean unlocked = false;
		readLock.lock();
		try {
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
											for(String schemeLower : schemeLowerSearchOrder) {
												ImmutableTriple<PartialURL,SinglePartialURL,V> match = portIndex.get(schemeLower);
												if(match != null) {
													readLock.unlock();
													unlocked = true;
													assert match.left.matches(fieldSource) : "Get inconsistent with matches";
													assert match.middle.matches(fieldSource) : "Get inconsistent with matches";
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
								lastSlashPos = pathStr.lastIndexOf(Path.SEPARATOR_CHAR, slashCount - 1);
								slashCount--;
							}
						}
					}
				}
			}
		} finally {
			if(!unlocked) readLock.unlock();
		}
		return null;
	}
}
