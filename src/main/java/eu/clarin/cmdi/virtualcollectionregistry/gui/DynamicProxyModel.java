package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.model.IModel;

/**
 * Abstract model that dynamically wraps another model. Can be used to share a
 * model with a component that changes its model during its life cycle (such as
 * a form that gets reused).
 *
 * @author twagoo
 * @param <T> type of model object
 */
@SuppressWarnings("serial")
public abstract class DynamicProxyModel<T> implements IModel<T> {

    /**
     * This is called from all methods in this class to obtain the wrapped
     * model; the result is never stored internally, and the model to wrap gets
     * evaluated on each call through this method.
     *
     * @return the model that needs to be wrapped
     */
    protected abstract IModel<T> getWrappedModel();

    @Override
    public T getObject() {
        return getWrappedModel().getObject();
    }

    @Override
    public void setObject(T object) {
        getWrappedModel().setObject(object);
    }

    @Override
    public void detach() {
        getWrappedModel().detach();
    }

}
