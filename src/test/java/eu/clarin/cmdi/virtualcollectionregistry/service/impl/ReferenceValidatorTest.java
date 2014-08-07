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

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.Validatable;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class ReferenceValidatorTest {

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateUrl() {
        System.out.println("onValidate");
        IValidatable<String> validatable = new Validatable<>("http://www.clarin.eu");
        ReferenceValidator instance = new ReferenceValidator();
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateHdl() {
        System.out.println("onValidate");
        IValidatable<String> validatable = new Validatable<>("hdl:1234/abcd-EF-5678");
        ReferenceValidator instance = new ReferenceValidator();
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateDoi() {
        System.out.println("onValidate");
        IValidatable<String> validatable = new Validatable<>("doi:10.1000/182");
        ReferenceValidator instance = new ReferenceValidator();
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateIllegal() {
        System.out.println("onValidate");
        IValidatable<String> validatable = new Validatable<>("not a legal URL or handle");
        ReferenceValidator instance = new ReferenceValidator();
        instance.validate(validatable);
        assertFalse(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateIllegalHdl() {
        System.out.println("onValidate");
        IValidatable<String> validatable = new Validatable<>("hdl:12a4/abcd-EF-5678");
        ReferenceValidator instance = new ReferenceValidator();
        instance.validate(validatable);
        assertFalse(validatable.isValid());
    }

}
