package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

public class TCFColumnPresentationExpression implements IColumnPresentation {

    public static final String PRESENTATION_ID = "Expressions";
    
    /**
     * Presentation column IDs.
     */
    public static final String
        COL_TYPE = "Type",
        COL_NAME = "Name",
        COL_HEX_VALUE = "HexValue",
        COL_DEC_VALUE = "DecValue";

    private static String[] cols_all = {
        COL_TYPE,
        COL_NAME,
        COL_DEC_VALUE,
        COL_HEX_VALUE,
    };
    
    private static String[] headers  = {
        "Type",
        "Name",
        "Decimal",
        "Hex",
    };

    private static String[] cols_ini = {
        COL_NAME,
        COL_DEC_VALUE,
        COL_HEX_VALUE,
    };
    
    public void dispose() {
    }

    public String[] getAvailableColumns() {
        return cols_all;
    }

    public String getHeader(String id) {
        for (int i = 0; i < cols_all.length; i++) {
            if (id.equals(cols_all[i])) return headers[i];
        }
        return null;
    }

    public String getId() {
        return PRESENTATION_ID;
    }

    public ImageDescriptor getImageDescriptor(String id) {
        return null;
    }

    public String[] getInitialColumns() {
        return cols_ini;
    }

    public void init(IPresentationContext context) {
    }

    public boolean isOptional() {
        return false;
    }
}
