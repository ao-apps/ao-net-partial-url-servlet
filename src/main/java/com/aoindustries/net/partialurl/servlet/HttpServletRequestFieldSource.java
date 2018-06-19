/*
 * ao-net-partial-url-servlet - Matches and resolves partial URLs in a Servlet environment.
 * Copyright (C) 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-net-partial-url-servlet.
 *
 * ao-net-partial-url-servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-net-partial-url-servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-net-partial-url-servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.net.partialurl.servlet;

import com.aoindustries.net.HostAddress;
import com.aoindustries.net.Path;
import com.aoindustries.net.Port;
import com.aoindustries.net.Protocol;
import com.aoindustries.net.partialurl.FieldSource;
import com.aoindustries.net.partialurl.PartialURL;
import com.aoindustries.net.partialurl.URLFieldSource;
import com.aoindustries.validation.ValidationException;
import java.net.MalformedURLException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Obtains fields for {@link PartialURL} from a {@link HttpServletRequest}.
 *
 * @implSpec  This implementation is not thread safe due to results caching.
 *
 * @see  URLFieldSource
 */
public class HttpServletRequestFieldSource implements FieldSource {

	private final HttpServletRequest request;

	// Cached results
	private String scheme;
	private HostAddress host;
	private Port port;
	private Path contextPath;
	private Path path;

	public HttpServletRequestFieldSource(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  ServletRequest#getScheme()
	 */
	@Override
	public String getScheme() {
		if(scheme == null) scheme = request.getScheme();
		return scheme;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  ServletRequest#getServerName()
	 */
	@Override
	public HostAddress getHost() throws MalformedURLException {
		if(host == null) {
			try {
				host = HostAddress.valueOf(request.getServerName());
			} catch(ValidationException e) {
				MalformedURLException newErr = new MalformedURLException();
				newErr.initCause(e);
				throw newErr;
			}
		}
		return host;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implNote  The implementation assumes {@link Protocol#TCP}.
	 *
	 * @see  ServletRequest#getServerPort()
	 */
	@Override
	public Port getPort() throws MalformedURLException {
		if(port == null) {
			try {
				port = Port.valueOf(request.getServerPort(), Protocol.TCP);
			} catch(ValidationException e) {
				MalformedURLException newErr = new MalformedURLException();
				newErr.initCause(e);
				throw newErr;
			}
		}
		return port;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  HttpServletRequest#getContextPath()
	 */
	@Override
	public Path getContextPath() throws MalformedURLException {
		if(contextPath == null) {
			try {
				String cp = request.getContextPath();
				contextPath = cp.isEmpty() ? Path.ROOT : Path.valueOf(cp);
			} catch(ValidationException e) {
				MalformedURLException newErr = new MalformedURLException();
				newErr.initCause(e);
				throw newErr;
			}
		}
		return contextPath;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  HttpServletRequest#getServletPath()
	 * @see  HttpServletRequest#getPathInfo()
	 */
	@Override
	public Path getPath() throws MalformedURLException {
		if(path == null) {
			try {
				String servletPath = request.getServletPath(); // Might be empty string ""
				String pathInfo = request.getPathInfo(); // Might be null, but never empty
				if(servletPath.isEmpty()) {
					path = Path.valueOf(pathInfo);
				} else {
					if(pathInfo == null) {
						path = Path.valueOf(servletPath);
					} else {
						path = Path.valueOf(servletPath + pathInfo);
					}
				}
			} catch(ValidationException e) {
				MalformedURLException newErr = new MalformedURLException();
				newErr.initCause(e);
				throw newErr;
			}
		}
		return path;
	}
}
