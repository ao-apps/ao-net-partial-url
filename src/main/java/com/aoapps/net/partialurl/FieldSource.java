/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2020, 2021  AO Industries, Inc.
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

import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import java.net.MalformedURLException;

/**
 * A source providing the incomplete fields of {@link PartialURL} and used in
 * matching.
 */
public interface FieldSource {

	/**
	 * Gets the scheme (such as https/http/other) for this URL.
	 *
	 * @throws MalformedURLException  When unable to obtain the scheme or the obtained scheme is invalid
	 *
	 * @see  SinglePartialURL#getScheme()
	 */
	String getScheme() throws MalformedURLException;

	/**
	 * Gets the IP address or hostname for this URL.
	 *
	 * @throws MalformedURLException  When unable to obtain the host or the obtained host is invalid
	 *
	 * @see  SinglePartialURL#getHost()
	 */
	HostAddress getHost() throws MalformedURLException;

	/**
	 * Gets the port number for this URL.
	 *
	 * @throws MalformedURLException  When unable to obtain the port or the obtained port is invalid
	 *
	 * @see  SinglePartialURL#getPort()
	 */
	Port getPort() throws MalformedURLException;

	/**
	 * Gets the context path for this URL, only ending in a slash (/) when is
	 * {@link Path#ROOT the root context}.
	 *
	 * @throws MalformedURLException  When unable to obtain the context path or the obtained context path is invalid
	 *
	 * @see  SinglePartialURL#getContextPath()
	 */
	Path getContextPath() throws MalformedURLException;

	/**
	 * Gets the path for this URL or {@code null} when the path does not exist or is empty.
	 *
	 * @throws MalformedURLException  When unable to obtain the path or the obtained path is invalid
	 *
	 * @see  SinglePartialURL#getPrefix()
	 */
	Path getPath() throws MalformedURLException;
}
