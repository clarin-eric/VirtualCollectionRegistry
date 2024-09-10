/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.debug;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adding to an Ajax component:
 * 
 * //...
 * new AjaxButton("ajaxButton"){
 *	@Override
 *	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
 *	  super.updateAjaxAttributes(attributes);
 *	  attributes.getAjaxCallListeners().add(new DisableComponentListener(form));
 *	}
 * }
 * //...
 *  
 * @author wilelb
 */
public class AjaxLogger extends AjaxCallListener {

    private final static Logger logger = LoggerFactory.getLogger(AjaxLogger.class);

    @Override
    public AjaxCallListener onInit(CharSequence init) {
        logger.info("onInit: {}", init.toString());
        return super.onInit(init);
    }

    @Override
    public AjaxCallListener onBefore(CharSequence before) {
        logger.info("onBefore: {}", before.toString());
        return super.onBefore(before);
    }

    @Override
    public AjaxCallListener onAfter(CharSequence after) {
        logger.info("onAfter: {}", after.toString());
        return super.onAfter(after);
    }

    @Override
    public AjaxCallListener onPrecondition(CharSequence precondition) {
        logger.info("onPrecondition: {}", precondition.toString());
        return super.onPrecondition(precondition);
    }   
    
    @Override
    public AjaxCallListener onBeforeSend(CharSequence beforeSend) {
        logger.info("onBeforeSend: {}", beforeSend.toString());
        return super.onBeforeSend(beforeSend); 
    }

    @Override
    public AjaxCallListener onComplete(CharSequence complete) {
        logger.info("onComplete: {}", complete.toString());
        return super.onComplete(complete); 
    }

    @Override
    public AjaxCallListener onDone(CharSequence done) {
        logger.info("onDone: {}", done.toString());
        return super.onDone(done); 
    }

    @Override
    public AjaxCallListener onFailure(CharSequence failure) {
        logger.info("onFailure: {}", failure.toString());
        return super.onFailure(failure); 
    }

    @Override
    public AjaxCallListener onSuccess(CharSequence success) {
        logger.info("onSuccess: {}", success.toString());
        return super.onSuccess(success); 
    }
    
}
