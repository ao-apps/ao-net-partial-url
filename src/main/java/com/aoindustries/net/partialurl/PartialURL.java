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

import com.aoindustries.net.Path;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A {@link PartialURL} matches and completes URLs.
 * All fields are optional.
 * <p>
 * This is not a general-purpose representation of a URL.  It only contains the
 * fields specifically used for matching a request to a virtual host (TODO: Links to projects).  For
 * an instance of {@link URL}, see {@link #toURL(com.aoindustries.net.partialurl.FieldSource)}.
 * </p>
 */
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

	protected PartialURL() {
	}

	@Override
	abstract public String toString();

	@Override
	abstract public boolean equals(Object obj);

	@Override
	abstract public int hashCode();

	/**
	 * Checks if this partial URL is complete (has no {@code null} fields other than prefix).
	 * A complete URL may be converted to a {@link URL} without any
	 * {@link FieldSource field source} provided.
	 *
	 * @see  #toURL(com.aoindustries.net.partialurl.FieldSource)
	 */
	abstract public boolean isComplete();

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
