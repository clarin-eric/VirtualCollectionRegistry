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
package eu.clarin.cmdi.virtualcollectionregistry.core.validation;

import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidLink;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.validator.UrlValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * String validator that checks whether the value is a valid handle with 'hdl'
 * scheme; if not, it passed the value on to an instance of {@link UrlValidator}
 * (configured not to accept fragments, see {@link UrlValidator#NO_FRAGMENTS})
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class ReferenceValidator implements IValidator<String> {
    private final IValidator<String> urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);
    private final IValidator httpResponseValidator = new HttpResponseValidator();
    
    public boolean validate(String value) {
        final Validatable<String> validatable = new Validatable<>(value);
        validate(validatable);
        return validatable.isValid();
    }
    
    @Override
    public void validate(IValidatable<String> ivalidatable) {
        if(PidLink.isSupportedPersistentIdentifier(ivalidatable.getValue())) {
            if(PidLink.isActionableSupportedPersistentIdentifier(ivalidatable.getValue())) {
                httpResponseValidator.validate(ivalidatable);
            }
        } else {
            urlValidator.validate(ivalidatable);
            if (!ivalidatable.isValid()) {
                String val = ivalidatable.toString();
                if(!val.startsWith("http") || val.startsWith("https") || val.startsWith("hdl") || val.startsWith("doi") || val.startsWith("urn:nbn")) {
                    ivalidatable.error(new ValidationError().setMessage(String.format("is not a valid PID or URL. Incorrect scheme (not http:, https:, hdl:, doi: or urn:nbn:)", val)));
                } else {
                    ivalidatable.error(new ValidationError().setMessage(String.format("is not a valid PID or URL", val)));
                }
            } else {
                httpResponseValidator.validate(ivalidatable);
            }
        }
    }
}
