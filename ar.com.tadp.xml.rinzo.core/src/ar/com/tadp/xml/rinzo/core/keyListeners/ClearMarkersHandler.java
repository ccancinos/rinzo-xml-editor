package ar.com.tadp.xml.rinzo.core.keyListeners;

import org.eclipse.ui.IEditorActionDelegate;

import ar.com.tadp.xml.rinzo.core.actions.ClearMarkersAction;

/**
 * 
 * @author ccancinos
 */
public class ClearMarkersHandler extends ActionToHandlerAdapter {

	@Override
	protected IEditorActionDelegate createAction() {
		return new ClearMarkersAction();
	}

}
