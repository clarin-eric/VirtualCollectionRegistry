/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import java.util.regex.Pattern;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.UrlValidator;

/**
 * String validator that checks whether the value is a valid handle with 'hdl'
 * scheme; if not, it passed the value on to an instance of {@link UrlValidator}
 * (configured not to accept fragments, see {@link UrlValidator#NO_FRAGMENTS})
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class ReferenceValidator extends AbstractValidator<String> {

    private static final String HANDLE_SPECIFIC_PART_PATTERN = "[0-9\\.]+\\/.+$";
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^(hdl|doi):" + HANDLE_SPECIFIC_PART_PATTERN);
    private static final Pattern HANDLE_RESOLVER_PATTERN = Pattern.compile("^http://(hdl\\.handle\\.net|dx\\.doi\\.org|)/" + HANDLE_SPECIFIC_PART_PATTERN);
    private final IValidator<String> urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        // first check if it is a valid handle
        if (!HANDLE_PATTERN.matcher(validatable.getValue()).matches()) {
            // check if it is a valid URL
            urlValidator.validate(validatable);
            if (!validatable.isValid()) {
                validatable.error(new ValidationError().setMessage(String.format("'%s' is not a valid handle", validatable.getValue())));
            }
        }
    }

    public boolean validate(String value) {
        final Validatable<String> validatable = new Validatable<>(value);
        validate(validatable);
        return validatable.isValid();
    }

    /**
     *
     * @param uri
     * @return true IFF the expression is a URI consisting of a valid handle
     * pattern preceded by a handle scheme expression (hdl: or doi:) OR one of
     * the accepted handle resolver base URL's (http://hdl.handle.net or
     * http://dx.doi.org)
     */
    public static boolean isPid(CharSequence uri) {
        return HANDLE_PATTERN.matcher(uri).matches()
                || HANDLE_RESOLVER_PATTERN.matcher(uri).matches();
    }

}
