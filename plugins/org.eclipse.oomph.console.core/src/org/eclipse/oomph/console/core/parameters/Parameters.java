package org.eclipse.oomph.console.core.parameters;

public class Parameters {
	public static final String OOMPH_PRODUCT_ID = "oomph.product.id";
	public static final String OOMPH_VERSION_ID = "oomph.product.version";
	private static final String OOMPH_STREAMS_ID = "oomph.streams.id";
	private static final String INSTALLER_VERBOSE_ID = "oomph.installer.verbose";
	private static final String INSTALLATION_LOCATION_ID = "oomph.installation.location";

	public static final String PRODUCT = System.getProperty(OOMPH_PRODUCT_ID) == null ? "Not selected"
			: System.getProperty(OOMPH_PRODUCT_ID);
	public static final String VERSION = System.getProperty(OOMPH_VERSION_ID) == null ? "Not selected"
			: System.getProperty(OOMPH_VERSION_ID);
	public static final String STREAMS = System.getProperty(OOMPH_STREAMS_ID) == null ? ""
			: System.getProperty(OOMPH_STREAMS_ID);
	public static final boolean VERBOSE = System.getProperty(INSTALLER_VERBOSE_ID) == null ? false
			: Boolean.parseBoolean(System.getProperty(INSTALLER_VERBOSE_ID));
	public static final String INSTALLATION_LOCATION = System.getProperty(INSTALLATION_LOCATION_ID);
}
