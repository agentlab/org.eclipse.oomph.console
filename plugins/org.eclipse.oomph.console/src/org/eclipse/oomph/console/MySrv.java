/**
 *
 */
package org.eclipse.oomph.console;

import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Ivanov_AM
 *
 */
@Component(immediate = true)
public class MySrv {

	@Reference(policy = ReferencePolicy.STATIC)
	FrameworkAdmin fa;

	@Activate
	public void activate() {
		System.out.println("asdasd"); //$NON-NLS-1$
	}
}
