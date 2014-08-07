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
import org.junit.Before;

/**
 *
 * @author twagoo
 */
public class ReferenceValidatorTest {

    private ReferenceValidator instance;

    @Before
    public void setUp() {
        instance = new ReferenceValidator();
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateUrl() {
        IValidatable<String> validatable = new Validatable<>("http://www.clarin.eu");
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateHdl() {
        IValidatable<String> validatable = new Validatable<>("hdl:1234/abcd-EF-5678");
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateDoi() {
        IValidatable<String> validatable = new Validatable<>("doi:10.1000/182");
        instance.validate(validatable);
        assertTrue(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateIllegal() {
        IValidatable<String> validatable = new Validatable<>("not a legal URL or handle");
        instance.validate(validatable);
        assertFalse(validatable.isValid());
    }

    /**
     * Test of onValidate method, of class ReferenceValidator.
     */
    @Test
    public void testOnValidateIllegalHdl() {
        IValidatable<String> validatable = new Validatable<>("hdl:12a4/abcd-EF-5678");
        instance.validate(validatable);
        assertFalse(validatable.isValid());
    }

}
