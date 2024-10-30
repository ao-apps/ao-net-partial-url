/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

import com.aoapps.collections.MinimalMap;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Maps {@link PartialURL partial URLs} to arbitrary values and provides fast lookups.
 */
@SuppressWarnings("AssertWithSideEffects")
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

  private final Map<
      HostAddress,
      Map<
          Path,
          MutablePair<
              Integer,
              Map<
                  String,
                  Map<
                      Port,
                      Map<
                          String,
                          ImmutableTriple<
                              PartialURL,
                              SinglePartialURL,
                              V
                          >
                      >
                  >
              >
          >
      >
      > index = new HashMap<>();

  /**
   * For sequential implementation used for assertions only.
   *
   * @see  #getSequential(com.aoapps.net.partialurl.FieldSource)
   */
  private final SortedMap<SinglePartialURL, ImmutablePair<PartialURL, V>> sequential = ASSERTIONS_ENABLED ? new TreeMap<>() : null;

  /**
   * Adds a new partial URL to this map while checking for conflicts.
   *
   * <p>TODO: Use {@link MinimalMap} in the index?</p>
   *
   * <p><b>Implementation Note:</b><br>
   * Currently, when an exception occurs, the index may be in a partial state.  Changes are not rolled-back.</p>
   *
   * @throws  IllegalStateException  If the partial URL conflicts with an existing entry.
   */
  public void put(PartialURL partialUrl, V value) throws IllegalStateException {
    writeLock.lock();
    try {
      for (SinglePartialURL singleUrl : partialUrl.getCombinations()) {
        Path prefix = singleUrl.getPrefix();
        @SuppressWarnings("deprecation")
        String prefixStr = Objects.toString(prefix, null);
        int slashCount = (prefixStr == null) ? 0 : StringUtils.countMatches(prefixStr, Path.SEPARATOR_CHAR);
        // host
        HostAddress host = singleUrl.getHost();
        Map<Path, MutablePair<Integer, Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>>>> hostIndex = index.get(host);
        if (hostIndex == null) {
          hostIndex = new HashMap<>();
          index.put(host, hostIndex);
        }
        // contextPath
        Path contextPath = singleUrl.getContextPath();
        MutablePair<Integer, Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>>> contextPathPair = hostIndex.get(contextPath);
        if (contextPathPair == null) {
          contextPathPair = MutablePair.of(slashCount, new HashMap<>());
          hostIndex.put(contextPath, contextPathPair);
        } else {
          // Store the maximum path depth for prefix-based match, only parse this far while inside "get"
          if (slashCount > contextPathPair.left) {
            contextPathPair.left = slashCount;
          }
        }
        Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>> contextPathIndex = contextPathPair.right;
        // prefix
        Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>> prefixIndex = contextPathIndex.get(prefixStr);
        if (prefixIndex == null) {
          prefixIndex = new HashMap<>();
          contextPathIndex.put(prefixStr, prefixIndex);
        }
        // port
        Port port = singleUrl.getPort();
        Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>> portIndex = prefixIndex.get(port);
        if (portIndex == null) {
          portIndex = new HashMap<>();
          prefixIndex.put(port, portIndex);
        }
        // scheme
        String scheme = singleUrl.getScheme();
        ImmutableTriple<PartialURL, SinglePartialURL, V> existing = portIndex.get(scheme);
        if (existing != null) {
          throw new IllegalStateException(
              "Partial URL already in index: partialUrl = " + partialUrl
                  + ", singleUrl = " + singleUrl
                  + ", existing = " + existing.getLeft());
        }
        portIndex.put(
            scheme,
            ImmutableTriple.of(partialUrl, singleUrl, value)
        );
        if (ASSERTIONS_ENABLED) {
          if (sequential.put(singleUrl, ImmutablePair.of(partialUrl, value)) != null) {
            throw new AssertionError("Duplicate singleUrl: " + singleUrl);
          }
        }
      }
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Indexed implementation of {@link #get(com.aoapps.net.partialurl.FieldSource)}.
   *
   * @see  #index
   * @see  #get(com.aoapps.net.partialurl.FieldSource)
   */
  private PartialURLMatch<V> getIndexed(FieldSource fieldSource) throws MalformedURLException {
    // Must be holding readLock already
    // TODO: CompletePartialURL (subclassing single) instead of toURL?
    // TODO: A sequential implementation for assertions, like in PathSpace?
    // TODO: Write tests
    HostAddress[] hostSearchOrder = new HostAddress[]{fieldSource.getHost(), null};
    Path[] contextPathSearchOrder = new Path[]{fieldSource.getContextPath(), null};
    Path path = fieldSource.getPath();
    String pathStr = (path == null) ? "" : path.toString();
    Port[] portSearchOrder = new Port[]{fieldSource.getPort(), null};
    String[] schemeSearchOrder = new String[]{fieldSource.getScheme().toLowerCase(Locale.ROOT), null};
    for (HostAddress host : hostSearchOrder) {
      Map<Path, MutablePair<Integer, Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>>>> hostIndex = index.get(host);
      if (hostIndex != null) {
        for (Path contextPath : contextPathSearchOrder) {
          MutablePair<Integer, Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>>> contextPathPair = hostIndex.get(contextPath);
          if (contextPathPair != null) {
            final int maxSlashCount = contextPathPair.left;
            Map<String, Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>>> contextPathIndex = contextPathPair.right;
            // TODO: Could store slash indexes in an array instead of searching back-and-forth
            // TODO: Could also store the resulting substrings, too
            // TODO: If doing this, track the maximum number of slashes anywhere in the index during put, and create the array this size
            // TODO: Might not be worth this, because this slash scan is only done four times maximum (host, null) x (contextPath, null)
            int slashCount = 0;
            int lastSlashPos = -1;
            // TODO: Track maximum path length in the index, and constrain that here, too, to avoid overhead for impossible-match lookups
            while (slashCount < maxSlashCount) {
              int slashPos = pathStr.indexOf(Path.SEPARATOR_CHAR, lastSlashPos + 1);
              if (slashPos == -1) {
                break;
              }
              lastSlashPos = slashPos;
              slashCount++;
            }
            while (slashCount >= 0) {
              String prefix;
              if (slashCount == 0) {
                // Looking for null key on slashCount 0
                assert lastSlashPos == -1;
                prefix = null;
              } else {
                // Get the current prefix of the lookup path
                assert lastSlashPos != -1;
                prefix = pathStr.substring(0, lastSlashPos + 1);
              }
              Map<Port, Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>>> prefixIndex = contextPathIndex.get(prefix);
              if (prefixIndex != null) {
                for (Port port : portSearchOrder) {
                  Map<String, ImmutableTriple<PartialURL, SinglePartialURL, V>> portIndex = prefixIndex.get(port);
                  if (portIndex != null) {
                    for (String scheme : schemeSearchOrder) {
                      ImmutableTriple<PartialURL, SinglePartialURL, V> match = portIndex.get(scheme);
                      if (match != null) {
                        assert Objects.equals(match.left.matches(fieldSource), match.middle) : "Get inconsistent with matches";
                        assert Objects.equals(match.middle.matches(fieldSource), match.middle) : "Get inconsistent with matches";
                        return new PartialURLMatch<>(
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
   * Sequential implementation of {@link #get(com.aoapps.net.partialurl.FieldSource)} used for assertions only.
   * Verifies that a sequential scan calling {@link SinglePartialURL#matches(com.aoapps.net.partialurl.FieldSource)}
   * yields the same result as the indexed lookup performed in {@link #get(com.aoapps.net.partialurl.FieldSource)}.
   *
   * @see  #sequential
   * @see  #get(com.aoapps.net.partialurl.FieldSource)
   */
  private PartialURLMatch<V> getSequential(FieldSource fieldSource) throws MalformedURLException {
    // Must be holding readLock already
    for (Map.Entry<SinglePartialURL, ImmutablePair<PartialURL, V>> entry : sequential.entrySet()) {
      SinglePartialURL singleUrl = entry.getKey();
      SinglePartialURL match = singleUrl.matches(fieldSource);
      if (match != null) {
        assert match == singleUrl;
        ImmutablePair<PartialURL, V> pair = entry.getValue();
        return new PartialURLMatch<>(
            pair.left,
            singleUrl,
            singleUrl.toURL(fieldSource),
            pair.right
        );
      }
    }
    return null;
  }

  /**
   * Gets the value associated with the given URL, returning the most specific match.
   *
   * <p>Ordering is consistent with:</p>
   *
   * <ul>
   *   <li>{@link PartialURL#matches(com.aoapps.net.partialurl.FieldSource)}</li>
   *   <li>{@link PartialURL#getCombinations()}</li>
   *   <li>{@link SinglePartialURL#compareTo(com.aoapps.net.partialurl.SinglePartialURL)}</li>
   * </ul>
   *
   * <p><b>Implementation Note:</b><br>
   * The maximum number of internal map lookups is: {@code (host, null) * (contextPath, null) * (maxSlashCount + 1) * (scheme, null) * (port, null)},
   * or {@code 2 * 2 * (maxSlashCount + 1) * 2 * 2}, or {@code 16 * (maxSlashCount + 1)}.  The actual number of map lookups
   * will typically be much less than this due to a sparsely populated index.</p>
   *
   * @return  The matching value or {@code null} of no match
   */
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
    if (ASSERTIONS_ENABLED && !Objects.equals(indexedMatch, sequentialMatch)) {
      throw new AssertionError("getIndexed is inconsistent with getSequential: indexedMatch = " + indexedMatch + ", sequentialMatch = " + sequentialMatch);
    }
    return indexedMatch;
  }
}
