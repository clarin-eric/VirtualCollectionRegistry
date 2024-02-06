/*
 * Copyright (C) 2016 CLARIN
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

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 *
 * @author wilelb
 */
public class HttpResponseValidator implements IValidator<String> {

    private StatusLine status;
    
    @Override
    public void validate(IValidatable<String> validatable) {
        try {
            if(!checkValidityOfUri(URI.create(validatable.getValue()))) {
                ValidationError error = new ValidationError();
                if(status == null) {
                    error.setMessage(String.format("There was an unkown issue when trying to connect to '%s'", validatable.getValue()));
                } else {                
                    error.setMessage(
                        String.format("'%s' received invalid HTTP response: HTTP %d %s", 
                        validatable.getValue(),
                        status.getStatusCode(),
                        status.getReasonPhrase()));
                }
                validatable.error(error);
            }
        } catch(UnknownHostException ex) {
            ValidationError error = new ValidationError();
            error.setMessage(String.format("Unkown host: '%s'", validatable.getValue()));
            validatable.error(error);
        } catch(IOException ex) {
            ValidationError error = new ValidationError();
            error.setMessage(String.format("There was an I/O issue when trying to connect to '%s': %s", validatable.getValue(), ex.getMessage()));
            validatable.error(error);
        } catch(IllegalArgumentException ex) {
            ValidationError error = new ValidationError();
            error.setMessage(String.format("Invalid URI: '%s'", validatable.getValue()));
            validatable.error(error);
        }
    }
    
    protected boolean checkValidityOfUri(URI uri) throws IOException {
        boolean result = false;
        DefaultHttpClient client = new DefaultHttpClient();
        HttpContext ctx = new BasicHttpContext();
        try {         
            HttpResponse response = client.execute(new HttpGet(uri), ctx);
            status = response.getStatusLine();
            result = status.getStatusCode() == 200;
        } finally {
            client.getConnectionManager().shutdown();
        }
        return result;
    }
    
}
