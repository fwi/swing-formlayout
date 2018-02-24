package com.github.fwi.swing.formlayout.builder;

import java.awt.Container;

import com.github.fwi.swing.formlayout.AbstractBox;
import com.github.fwi.swing.formlayout.FormGraphics;

/**
 * Helper class for building a form, see alse {@link BaseFormBuilder}.
 * <br>This is an endpoint for the generic fluent api design, try NOT to extend this class (that will break the fluent api hierarchy).
 * Instead, extend {@link ComponentFormBuilder} similar to how {@link ComponentFormBuilder} extends {@link BaseFormBuilder}
 * and create a separate (custom) endpoint for that class (similar to how this class is an endpoint for {@link ComponentFormBuilder}. 
 * <br>This will allow for the greatest flexibility: new methods can be added to all classes in the hierarchy 
 * and these can be re-used anywhere in the class hierarchy.  
 */
public class SimpleFormBuilder extends ComponentFormBuilder<SimpleFormBuilder> {

	public SimpleFormBuilder(Container container) {
		this(null, container);
	}

	public SimpleFormBuilder(AbstractBox container) {
		this(container == null ? null : container.getFormGraphics(), container);
	}

	public SimpleFormBuilder(FormGraphics formGraphics, Container container) {
		super(formGraphics, container);
	}

}
