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

import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A {@link PartialURL} that may contain at most one values for each field matched.
 * All fields are optional.
 */
public class SinglePartialURL extends PartialURL implements Comparable<SinglePartialURL> {

  private final String scheme;
  private final HostAddress host;
  private final Port port;
  private final Path contextPath;
  private final Path prefix;

  /**
   * Creates a new single-partial-URL.
   *
   * @see  #valueOf(java.lang.String, com.aoapps.net.HostAddress, com.aoapps.net.Port, com.aoapps.net.Path, com.aoapps.net.Path)
   */
  SinglePartialURL(String scheme, HostAddress host, Port port, Path contextPath, Path prefix) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    if (contextPath != null && contextPath != Path.ROOT) {
      String contextPathStr = contextPath.toString();
      if (contextPathStr.endsWith(Path.SEPARATOR_STRING)) {
        throw new IllegalArgumentException("Non-root context path may not end in slash (" + Path.SEPARATOR_CHAR + "): " + contextPath);
      }
    }
    this.contextPath = contextPath;
    if (prefix != null && !prefix.toString().endsWith(Path.SEPARATOR_STRING)) {
      throw new IllegalArgumentException("Prefix does not end in slash (" + Path.SEPARATOR_CHAR + "): " + prefix);
    }
    this.prefix = prefix;
  }

  @Override
  public String toString() {
    String hostStr = host == null ? WILDCARD_STRING : host.toBracketedString();
    int toStringLen =
        (
            (scheme == null) ? 0 : (
                scheme.length()
                    + 1// ':'
            )
        )
            + 2 // "//"
            + hostStr.length();
    String portStr;
    if (port == null) {
      portStr = WILDCARD_STRING;
    } else {
      int portNum = port.getPort();
      if (
          (HTTP.equals(scheme) && portNum == 80)
              || (HTTPS.equals(scheme) && portNum == 443)
      ) {
        portStr = null;
      } else {
        portStr = Integer.toString(portNum);
      }
    }
    if (portStr != null) {
      toStringLen +=
          1 // ':'
              + portStr.length();
    }
    String contextPathStr;
    if (contextPath != null) {
      contextPathStr = (contextPath == Path.ROOT) ? "" : contextPath.toString();
    } else {
      contextPathStr = NULL_CONTEXT_PATH;
    }
    toStringLen += contextPathStr.length();
    String prefixStr = (prefix == null) ? NULL_PREFIX : prefix.toString();
    toStringLen += prefixStr.length();
    StringBuilder toString = new StringBuilder(toStringLen);
    if (scheme != null) {
      toString.append(scheme).append(':');
    }
    toString.append("//").append(hostStr);
    if (portStr != null) {
      toString.append(':').append(portStr);
    }
    toString.append(contextPathStr).append(prefixStr);
    assert toStringLen == toString.length() : "toStringLen != toString.length(): " +  toStringLen + " != " + toString.length();
    return toString.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SinglePartialURL)) {
      return false;
    }
    SinglePartialURL other = (SinglePartialURL) obj;
    return
        Objects.equals(scheme, other.scheme)
            && Objects.equals(host, other.host)
            && Objects.equals(port, other.port)
            && Objects.equals(contextPath, other.contextPath)
            && Objects.equals(prefix, other.prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scheme,
        host,
        port,
        contextPath,
        prefix
    );
  }

  /**
   * Sorts nulls after non-nulls, deeper prefixes first, then by {@link Path#compareTo(com.aoapps.net.Path)}.
   */
  // TODO: Move to Path class as a "depth-first comparator"?
  static final Comparator<Path> prefixComparator = (prefix1, prefix2) -> {
    if (prefix1 == null) {
      if (prefix2 == null) {
        return 0;
      } else {
        return 1; // Nulls after
      }
    } else {
      if (prefix2 == null) {
        return -1; // Nulls after
      }
      String prefix1Str = prefix1.toString();
      String prefix2Str = prefix2.toString();
      // Deeper prefixes first
      if (prefix1Str.equals(prefix2Str)) {
        return 0;
      }
      if (prefix1Str.startsWith(prefix2Str)) {
        return -1;
      }
      if (prefix2Str.startsWith(prefix1Str)) {
        return 1;
      }
      return prefix1.compareTo(prefix2);
    }
  };

  /**
   * {@inheritDoc}
   *
   * <p>Ordering is consistent with:</p>
   *
   * <ul>
   *   <li>{@link PartialURL#matches(com.aoapps.net.partialurl.FieldSource)}</li>
   *   <li>{@link PartialURL#getCombinations()}</li>
   *   <li>{@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}</li>
   * </ul>
   *
   * @see  PartialURL#getCombinations()
   */
  @Override
  public int compareTo(SinglePartialURL other) {
    int diff = ObjectUtils.compare(host, other.host, true);
    if (diff != 0) {
      return diff;
    }
    diff = ObjectUtils.compare(contextPath, other.contextPath, true);
    if (diff != 0) {
      return diff;
    }
    diff = prefixComparator.compare(prefix, other.prefix);
    if (diff != 0) {
      return diff;
    }
    diff = ObjectUtils.compare(port, other.port, true);
    if (diff != 0) {
      return diff;
    }
    return ObjectUtils.compare(scheme, other.scheme, true);
  }

  /**
   * {@inheritDoc}
   *
   * @return  {@code this} when matches, or {@code null} when does not match.
   */
  @Override
  public SinglePartialURL matches(FieldSource fieldSource) throws MalformedURLException {
    Path fieldPath;
    return
        (scheme == null || scheme.equals(fieldSource.getScheme().toLowerCase(Locale.ROOT)))
            && (host == null || host.equals(fieldSource.getHost()))
            && (port == null || port.equals(fieldSource.getPort()))
            && (contextPath == null || contextPath.equals(fieldSource.getContextPath()))
            && (
            prefix == null
                || (
                (fieldPath = fieldSource.getPath()) != null
                    && fieldPath.toString().startsWith(prefix.toString())
            )
        )
            ? this
            : null;
  }

  @Override
  public boolean isComplete() {
    return
        scheme != null
            && host != null
            && port != null
            && contextPath != null;
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Implementation Note:</b><br>
   * a partial URL is its own primary</p>
   *
   * @return  {@code this} because it is already a single partial URL
   */
  @Override
  public SinglePartialURL getPrimary() {
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Implementation Note:</b><br>
   * iterates over {@code this} only</p>
   */
  @Override
  public Iterable<SinglePartialURL> getCombinations() {
    return Collections.singleton(this);
  }

  @Override
  public URL toURL(FieldSource fieldSource) throws MalformedURLException {
    String schemeStr = (scheme == null) ? fieldSource.getScheme() : scheme;
    int portNum = ((port == null) ? fieldSource.getPort() : port).getPort();
    if (
        // TODO: Could use URL#getDefaultPort() and moved this hard-coded check to HttpServletRequestFieldSource
        (HTTP.equalsIgnoreCase(schemeStr) && portNum == 80)
            || (HTTPS.equalsIgnoreCase(schemeStr) && portNum == 443)
    ) {
      portNum = -1;
    }
    String hostStr = ((host == null) ? fieldSource.getHost() : host).toBracketedString();
    String file;
    if (contextPath == null) {
      Path contextPathStr = fieldSource.getContextPath();
      if (contextPathStr == Path.ROOT) {
        file = (prefix == null) ? "" : prefix.toString();
      } else {
        if (prefix == null) {
          file = contextPathStr.toString();
        } else {
          file = contextPathStr + prefix.toString();
        }
      }
    } else if (contextPath == Path.ROOT) {
      file = (prefix == null) ? "" : prefix.toString();
    } else {
      if (prefix == null) {
        file = contextPath.toString();
      } else {
        file = contextPath.toString() + prefix.toString();
      }
    }
    try {
      return new URL(
          schemeStr,
          hostStr,
          portNum,
          file
      );
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Gets the lower-case scheme (such as https/http/other) for this partial URL.
   *
   * @return  The scheme or {@code null} when {@link FieldSource#getScheme()} should be used.
   *
   * @see  #HTTP
   * @see  #HTTPS
   *
   * @see  FieldSource#getScheme()
   */
  public String getScheme() {
    return scheme;
  }

  /**
   * Gets the IP address or hostname for this partial URL.
   *
   * @return  The IP address/hostname or {@code null} when {@link FieldSource#getHost()} should be used.
   *
   * @see  FieldSource#getHost()
   */
  public HostAddress getHost() {
    return host;
  }

  /**
   * Gets the port number for this partial URL.
   *
   * @return  The port or {@code null} when {@link FieldSource#getPort()} should be used.
   *
   * @see  FieldSource#getPort()
   */
  public Port getPort() {
    return port;
  }

  /**
   * Gets the context path for this partial URL, only ending in a slash (/) when is
   * {@link Path#ROOT the root context}.
   *
   * @return  The context path or {@code null} when the {@link FieldSource#getContextPath() context path of the field source} should be used.
   *
   * @see  FieldSource#getContextPath()
   */
  public Path getContextPath() {
    return contextPath;
  }

  /**
   * Gets the prefix of the path for this partial URL, always either {@code null} or ending in a slash (/).
   * This is matched as a prefix of {@link  FieldSource#getPath()}.
   *
   * @see  FieldSource#getPath()
   * @see  Path#SEPARATOR_CHAR
   */
  public Path getPrefix() {
    return prefix;
  }
}
