/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import com.aoapps.net.Protocol;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Obtains fields for {@link PartialURL} from a {@link URL}.
 * <p>
 * <b>Implementation Note:</b><br>
 * This implementation is not thread safe due to results caching.
 * </p>
 */
public class URLFieldSource implements FieldSource {

  private final URL url;

  // Cached results
  private HostAddress host;
  private Port port;
  private Path path;

  public URLFieldSource(URL url) {
    this.url = url;
  }

  /**
   * {@inheritDoc}
   *
   * @see  URL#getProtocol()
   */
  @Override
  public String getScheme() {
    return url.getProtocol();
  }

  /**
   * {@inheritDoc}
   *
   * @see  URL#getHost()
   */
  @Override
  public HostAddress getHost() throws MalformedURLException {
    if (host == null) {
      try {
        host = HostAddress.valueOf(url.getHost());
      } catch (ValidationException e) {
        MalformedURLException newErr = new MalformedURLException();
        newErr.initCause(e);
        throw newErr;
      }
    }
    return host;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <b>Implementation Note:</b><br>
   * The implementation assumes {@link Protocol#TCP}.
   * </p>
   *
   * @see  URL#getPort()
   * @see  URL#getDefaultPort()
   */
  @Override
  public Port getPort() throws MalformedURLException {
    if (port == null) {
      try {
        int urlPort = url.getPort();
        if (urlPort == -1) {
          urlPort = url.getDefaultPort();
          if (urlPort == -1) {
            throw new MalformedURLException("No default port for URL: " + url);
          }
        }
        port = Port.valueOf(urlPort, Protocol.TCP);
      } catch (ValidationException e) {
        MalformedURLException newErr = new MalformedURLException();
        newErr.initCause(e);
        throw newErr;
      }
    }
    return port;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <b>Implementation Note:</b><br>
   * This always returns {@link Path#ROOT}.
   * </p>
   */
  @Override
  public Path getContextPath() {
    return Path.ROOT;
  }

  /**
   * {@inheritDoc}
   *
   * @see  URL#getPath()
   */
  @Override
  public Path getPath() throws MalformedURLException {
    String urlPath = url.getPath();
    if (urlPath.isEmpty()) {
      return null;
    }
    if (path == null) {
      try {
        path = Path.valueOf(urlPath);
      } catch (ValidationException e) {
        MalformedURLException newErr = new MalformedURLException();
        newErr.initCause(e);
        throw newErr;
      }
    }
    return path;
  }
}
