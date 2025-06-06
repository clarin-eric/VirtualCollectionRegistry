/*
 * Copyright (C) 2020 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.rest;

/**
 *
 * @author wilelb
 */
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriInfo;

import jakarta.annotation.Priority;

import org.glassfish.jersey.message.internal.LanguageTag;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.uri.UriComponent;

/**
 * A URI-based content negotiation filter mapping a dot-declared suffix in
 * URI to media type that is the value of the <code>Accept</code> header
 * or a language that is the value of the <code>Accept-Language</code> header.
 * <p>
 * This filter may be used when the acceptable media type and acceptable
 * language need to be declared in the URI.
 * <p>
 * This class may be extended to declare the mappings and the extending class,
 * <code>foo.MyUriConnegFilter</code> say, can be registered as a container request
 * filter.
 * <p>
 * If a suffix of "atom" is registered with a media type of
 * "application/atom+xml" then a GET request of:
 * <pre>GET /resource.atom</pre>
 * <p>is transformed to:</p>
 * <pre>GET /resource
 * Accept: application/atom+xml</pre>
 * Any existing "Accept" header value will be replaced.
 * <p>
 * If a suffix of "english" is registered with a language of
 * "en" then a GET request of:
 * <pre>GET /resource.english</pre>
 * <p>is transformed to:</p>
 * <pre>GET /resource
 * Accept-Language: en</pre>
 * Any existing "Accept-Language"header  value will be replaced.
 * <p/>
 * The media type mappings are processed before the language type mappings.
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class CustomUriConnegFilter implements ContainerRequestFilter {

    protected final Map<String, MediaType> mediaTypeMappings;
    protected final Map<String, String> languageMappings;
    
    /**
     * Create a filter that reads the configuration (media type and language mappings)
     * from the provided {@link ResourceConfig} instance.
     * This constructor will be called by the Jersey runtime when the filter
     * class is returned from {@link jakarta.ws.rs.core.Application#getClasses()}.
     * The {@link ResourceConfig} instance will get auto-injected.
     *
     * @param rc ResourceConfig instance that holds the configuration for the filter.
     */
    public CustomUriConnegFilter(@Context final Configuration rc) {
        this(extractMediaTypeMappings(rc.getProperty(ServerProperties.MEDIA_TYPE_MAPPINGS)),
                extractLanguageMappings(rc.getProperty(ServerProperties.LANGUAGE_MAPPINGS)));
    }

    /**
     * Create a filter with suffix to media type mappings and suffix to
     * language mappings.
     *
     * @param mediaTypeMappings the suffix to media type mappings.
     * @param languageMappings  the suffix to language mappings.
     */
    public CustomUriConnegFilter(Map<String, MediaType> mediaTypeMappings, Map<String, String> languageMappings) {
        if (mediaTypeMappings == null) {
            mediaTypeMappings = Collections.emptyMap();
        }

        if (languageMappings == null) {
            languageMappings = Collections.emptyMap();
        }

        this.mediaTypeMappings = mediaTypeMappings;
        this.languageMappings = languageMappings;
    }

    @Override
    public void filter(final ContainerRequestContext rc) throws IOException {
        final UriInfo uriInfo = rc.getUriInfo();

        // Quick check for a '.' character
        String path = uriInfo.getRequestUri().getRawPath();
        if (path.indexOf('.') == -1) {
            return;
        }

        final List<PathSegment> l = uriInfo.getPathSegments(false);
        if (l.isEmpty()) {
            return;
        }

        // Get the last non-empty path segment
        PathSegment segment = null;
        for (int i = l.size() - 1; i >= 0; i--) {
            segment = l.get(i);
            if (segment.getPath().length() > 0) {
                break;
            }
        }
        if (segment == null) {
            return;
        }

        final int length = path.length();

        // Get the suffixes
        final String[] suffixes = segment.getPath().split("\\.");

        for (int i = suffixes.length - 1; i >= 1; i--) {
            final String suffix = suffixes[i];
            if (suffix.length() == 0) {
                continue;
            }

            final MediaType accept = mediaTypeMappings.get(suffix);

            if (accept != null) {
                rc.getHeaders().putSingle(HttpHeaders.ACCEPT, accept.toString());

                final int index = path.lastIndexOf('.' + suffix);
                path = new StringBuilder(path).delete(index, index + suffix.length() + 1).toString();
                suffixes[i] = "";
                break;
            }
        }

        for (int i = suffixes.length - 1; i >= 1; i--) {
            final String suffix = suffixes[i];
            if (suffix.length() == 0) {
                continue;
            }

            final String acceptLanguage = languageMappings.get(suffix);
            if (acceptLanguage != null) {
                rc.getHeaders().putSingle(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage);

                final int index = path.lastIndexOf('.' + suffix);
                path = new StringBuilder(path).delete(index, index + suffix.length() + 1).toString();
                suffixes[i] = "";
                break;
            }
        }

        if (length != path.length()) {
            rc.setRequestUri(uriInfo.getRequestUriBuilder().replacePath(path).build());
        }
    }

    private static interface TypeParser<T> {

        public T valueOf(String s);
    }

    private static Map<String, MediaType> extractMediaTypeMappings(final Object mappings) {
        // parse and validate mediaTypeMappings set through MEDIA_TYPE_MAPPINGS property
        return parseAndValidateMappings(ServerProperties.MEDIA_TYPE_MAPPINGS, mappings, new TypeParser<MediaType>() {
            public MediaType valueOf(final String value) {
                return MediaType.valueOf(value);
            }
        });
    }

    private static Map<String, String> extractLanguageMappings(final Object mappings) {
        // parse and validate languageMappings set through LANGUAGE_MAPPINGS property
        return parseAndValidateMappings(ServerProperties.LANGUAGE_MAPPINGS, mappings, new TypeParser<String>() {
            public String valueOf(final String value) {
                return LanguageTag.valueOf(value).toString();
            }
        });
    }

    private static <T> Map<String, T> parseAndValidateMappings(final String property,
                                                               final Object mappings,
                                                               final TypeParser<T> parser) {
        if (mappings == null) {
            return Collections.emptyMap();
        }

        if (mappings instanceof Map) {
            return (Map<String, T>) mappings;
        }

        final HashMap<String, T> mappingsMap = new HashMap<>();

        if (mappings instanceof String) {
            parseMappings(property, (String) mappings, mappingsMap, parser);
        } else if (mappings instanceof String[]) {
            final String[] mappingsArray = (String[]) mappings;
            for (final String aMappingsArray : mappingsArray) {
                parseMappings(property, aMappingsArray, mappingsMap, parser);
            }
        } else {
            throw new IllegalArgumentException(LocalizationMessages.INVALID_MAPPING_TYPE(property));
        }

        encodeKeys(mappingsMap);

        return mappingsMap;
    }

    private static <T> void parseMappings(final String property, final String mappings,
                                          final Map<String, T> mappingsMap, final TypeParser<T> parser) {
        if (mappings == null) {
            return;
        }

        final String[] records = mappings.split(",");

        for (final String record : records) {
            final String[] mapping = record.split(":");
            if (mapping.length != 2) {
                throw new IllegalArgumentException(LocalizationMessages.INVALID_MAPPING_FORMAT(property, mappings));
            }

            final String trimmedSegment = mapping[0].trim();
            final String trimmedValue = mapping[1].trim();

            if (trimmedSegment.length() == 0) {
                throw new IllegalArgumentException(LocalizationMessages.INVALID_MAPPING_KEY_EMPTY(property, record));
            }
            if (trimmedValue.length() == 0) {
                throw new IllegalArgumentException(LocalizationMessages.INVALID_MAPPING_VALUE_EMPTY(property, record));
            }

            mappingsMap.put(trimmedSegment, parser.valueOf(trimmedValue));
        }
    }

    private static <T> void encodeKeys(final Map<String, T> map) {
        final Map<String, T> tempMap = new HashMap<>();
        for (final Map.Entry<String, T> entry : map.entrySet()) {
            tempMap.put(UriComponent.contextualEncode(entry.getKey(), UriComponent.Type.PATH_SEGMENT), entry.getValue());
        }
        map.clear();
        map.putAll(tempMap);
    }
}
