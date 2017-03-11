package org.eclipse.oomph.console.core.util;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.oomph.setup.AttributeRule;
import org.eclipse.oomph.setup.SetupFactory;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.User;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;

@SuppressWarnings("restriction")
public class UserAdjuster {
	private static final URI INSTALLATION_LOCATION_ATTRIBUTE_URI = SetupTaskPerformer
			.getAttributeURI(SetupPackage.Literals.INSTALLATION_TASK__LOCATION);

	private EList<AttributeRule> attributeRules;

	private String oldValue;

	public void setPreferredInstallDir(User user, String installDir) {
		attributeRules = user.getAttributeRules();
		for (AttributeRule attributeRule : attributeRules) {
			if (INSTALLATION_LOCATION_ATTRIBUTE_URI.equals(attributeRule.getAttributeURI())) {
				oldValue = attributeRule.getValue();
				attributeRule.setValue(installDir);
				return;
			}
		}

		AttributeRule attributeRule = SetupFactory.eINSTANCE.createAttributeRule();
		attributeRule.setAttributeURI(INSTALLATION_LOCATION_ATTRIBUTE_URI);
		attributeRule.setValue(installDir);
		attributeRules.add(attributeRule);
	}

	public void undo() {
		for (Iterator<AttributeRule> it = attributeRules.iterator(); it.hasNext();) {
			AttributeRule attributeRule = it.next();
			if (INSTALLATION_LOCATION_ATTRIBUTE_URI.equals(attributeRule.getAttributeURI())) {
				if (oldValue == null) {
					it.remove();
				} else {
					attributeRule.setValue(oldValue);
				}

				return;
			}
		}
	}
}
