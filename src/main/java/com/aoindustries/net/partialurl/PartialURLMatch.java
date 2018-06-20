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

import java.net.URL;
import org.apache.commons.lang3.ObjectUtils;

/**
 * The result of a call to {@link PartialURLMap#get(com.aoindustries.net.partialurl.FieldSource)
 */
public class PartialURLMatch<V> {

	private final PartialURL partialURL;
	private final SinglePartialURL singleURL;
	private final URL url;
	private final V value;

	PartialURLMatch(
		PartialURL partialURL,
		SinglePartialURL singleURL,
		URL url,
		V value
	) {
		this.partialURL = partialURL;
		this.singleURL = singleURL;
		this.url = url;
		this.value = value;
	}

	@Override
	public String toString() {
		if(partialURL == singleURL) {
			return singleURL + " -> " + url;
		} else {
			return partialURL + " -> " + singleURL + " -> " + url;
		}
	}

	/**
	 * Two matches are equal when they have the same partialURL (by .equals),
	 * singleURL (by .equals), url (by .equals), and value (by identity).
	 */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PartialURLMatch<?>)) return false;
		PartialURLMatch<?> other = (PartialURLMatch<?>)o;
		return
			value == other.value
			&& partialURL.equals(other.partialURL)
			&& singleURL.equals(other.singleURL)
			&& url.equals(other.url)
		;
	}

	@Override
	@SuppressWarnings("deprecation") // Java 1.7: Do not suppress
	public int hashCode() {
		int hash = partialURL.hashCode();
		hash = hash * 31 + singleURL.hashCode();
		hash = hash * 31 + url.hashCode();
		hash = hash * 31 + ObjectUtils.hashCode(value);
		return hash;
	}

	/**
	 * Gets the partial URL that matched the lookup.
	 * This might be a {@link MultiPartialURL}.
	 */
	public PartialURL getPartialURL() {
		return partialURL;
	}

	/**
	 * Gets the single partial URL that matched the lookup.
	 * This will be the same object as {@link #getPartialURL()} when it is a
	 * {@link SinglePartialURL}.  Will be one of the {@link MultiPartialURL#getCombinations()}
	 * when is a {@link MultiPartialURL}.
	 */
	public SinglePartialURL getSingleURL() {
		return singleURL;
	}

	/**
	 * Gets the completed {@link URL}, with {@code null} fields selected from the {@link FieldSource} used in
	 * {@link PartialURLMap#get(com.aoindustries.net.partialurl.FieldSource)}.
	 *
	 * @implSpec this implementation uses {@link SinglePartialURL#toURL(com.aoindustries.net.partialurl.FieldSource)} on
	 *           {@link #getSingleURL()}.
	 *
	 * @see  #getSingleURL()
	 * @see  SinglePartialURL#toURL(com.aoindustries.net.partialurl.FieldSource)
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Gets the value associated with the partial URL.
	 */
	public V getValue() {
		return value;
	}
}
