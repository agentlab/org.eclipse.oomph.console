package org.eclipse.oomph.console.installer;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.oomph.console.configuration.NotFoundException;
import org.eclipse.oomph.console.configuration.ProductVersionSelector;
import org.eclipse.oomph.console.core.p2.AcceptCacheUsageConfirmer;
import org.eclipse.oomph.console.core.p2.P2ServiceUI;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.console.core.util.InstallationInitializer;
import org.eclipse.oomph.console.core.util.LaunchUtil;
import org.eclipse.oomph.console.core.util.UserAdjuster;
import org.eclipse.oomph.internal.setup.SetupPrompter;
import org.eclipse.oomph.p2.internal.core.CacheUsageConfirmer;
import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.Product;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.SetupTaskContext;
import org.eclipse.oomph.setup.Stream;
import org.eclipse.oomph.setup.Trigger;
import org.eclipse.oomph.setup.UnsignedPolicy;
import org.eclipse.oomph.setup.User;
import org.eclipse.oomph.setup.VariableTask;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.setup.internal.core.SetupContext;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.util.Confirmer;
import org.eclipse.oomph.util.OS;
import org.eclipse.oomph.util.UserCallback;

@SuppressWarnings("restriction")
public class ConsoleInstaller {

	private SetupContext context;
	private SetupTaskPerformer performer;
	private ResourceSet resourceSet;

	public ConsoleInstaller() {

	}

	public void run() throws Exception {
		init();
		URIConverter uriConverter = resourceSet.getURIConverter();
		SetupPrompter prompter = new SetupPrompter() {

			@Override
			public UserCallback getUserCallback() {
				return null;
			}

			@Override
			public String getValue(VariableTask variable) {
				return null;
			}

		    public OS getOS()
		    {
		      return OS.INSTANCE;
		    }

			@Override
			public boolean promptVariables(List<? extends SetupTaskContext> performers) {
				for (SetupTaskContext performer : performers) {
					List<VariableTask> unresolvedVariables = ((SetupTaskPerformer) performer).getUnresolvedVariables();
					for (VariableTask variable : unresolvedVariables) {
						String value = variable.getValue();
						if (!variable.getChoices().isEmpty()) {
							value = variable.getChoices().get(0).getValue();
						} else if (value == null) {
							variable.setValue(variable.getDefaultValue());
						}
					}
				}
				return true;
			}

			@Override
			public String getVMPath() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		UserAdjuster adjuster = new UserAdjuster();
		String installationFolder = Parameters.INSTALLATION_LOCATION;
		if (installationFolder != null) {
			adjuster.setPreferredInstallDir(context.getUser(), installationFolder);
		}
		performer = SetupTaskPerformer.create(uriConverter, prompter, Trigger.BOOTSTRAP, context, false);
		File installationLocation = performer.getInstallationLocation();
		if (installationLocation.exists()) {
			try {
				LaunchUtil.launchProduct(performer, false);
				return;
			} catch (IOException | NullPointerException e) {
				System.out.println("Old installation did probably not finish correctly...");
				System.out.println("Reinstalling");
			}
		}
		saveResources();
		performer.recordVariables(context.getInstallation(), context.getWorkspace(), context.getUser());

		performer.getUnresolvedVariables().clear();
		performer.put(UIServices.class, P2ServiceUI.SERVICE_UI);
		performer.put(CacheUsageConfirmer.class, new AcceptCacheUsageConfirmer());
		performer.put(ILicense.class, Confirmer.ACCEPT);
		performer.put(Certificate.class, Confirmer.ACCEPT);
		performer.setProgress(new ConsoleProgressLog());
		performer.setMirrors(false);
		try {
			performer.perform(new ConsoleProgressMonitor());
			LaunchUtil.launchProduct(performer, true);
		} catch (Exception e) {
			System.err.println("ABORTING: " + e.getMessage());
		}
	}

	private void saveResources() {
		Installation installation = performer.getInstallation();
		Resource installationResource = installation.eResource();
		installationResource.setURI(URI.createFileURI(
				new File(performer.getProductConfigurationLocation(), "org.eclipse.oomph.setup/installation.setup")
						.toString()));

		Workspace workspace = performer.getWorkspace();
		Resource workspaceResource = null;
		if (workspace != null) {
			workspaceResource = workspace.eResource();
			workspaceResource.setURI(URI.createFileURI(new File(performer.getWorkspaceLocation(),
					".metadata/.plugins/org.eclipse.oomph.setup/workspace.setup").toString()));
		}
		performer.savePasswords();
		try {
			if (installationResource != null) {
				installationResource.save(Collections.emptyMap());
			}
			if (workspaceResource != null) {
				workspaceResource.save(Collections.emptyMap());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void init() throws IOException {
		InstallationInitializer installationHelper = new InstallationInitializer();
		resourceSet = installationHelper.getResourceSet();
		try {
			initInstallation(Parameters.PRODUCT);
		} catch (NotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	private void initInstallation(String productIdentifer) throws NotFoundException {
		ProductVersionSelector selector = new ProductVersionSelector(resourceSet);

		Product product = selector.selectProduct(Parameters.PRODUCT);
		ProductVersion version = selector.selectProductVersion(product);
		List<Stream> streams = new ArrayList<>();
		if (!Parameters.STREAMS.isEmpty()) {
			streams = selector.selectStreams(Arrays.asList(Parameters.STREAMS.split(",")));
		}
		context = SetupContext.create(resourceSet, version);
		Installation installation = context.getInstallation();
		installation.setProductVersion(version);

		User user = context.getUser();
		user.setUnsignedPolicy(UnsignedPolicy.ACCEPT);

		context = SetupContext.create(installation, streams, user);
	}

}
