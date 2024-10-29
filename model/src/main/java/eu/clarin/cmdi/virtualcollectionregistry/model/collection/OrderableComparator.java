package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import jakarta.validation.constraints.NotNull;

public class OrderableComparator {

    public static int compare(@NotNull Orderable a, @NotNull Orderable b) {
        if(a == null || b == null) {
            return 0;
        }
        Long thisDisplayOrder = a.getDisplayOrder();
        if(thisDisplayOrder == null) return 0;
        Long otherDisplayOrder = b.getDisplayOrder();
        if(otherDisplayOrder == null) return 0;
        return thisDisplayOrder.compareTo(otherDisplayOrder);
    }

}
