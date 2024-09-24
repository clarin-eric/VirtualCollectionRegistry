/*
 * Copyright 2018 computing center garching of the max planck society.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mpg.aai.shhaa;

import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class HttpHeaderUtils {

    private static Logger log = LoggerFactory.getLogger(HttpHeaderUtils.class);
    
    public static String decodeHeaderValue(String name, String value) {
        if (value == null) {
            return null;
        }

        try {
            return new String(value.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error(String.format("Failed to decode header [%s] value [%s] as UTF-8. Error=%s.", name, value, ex.getMessage()));
            log.debug("Stacktrace:", ex);
        }
        return value;
    }
}
