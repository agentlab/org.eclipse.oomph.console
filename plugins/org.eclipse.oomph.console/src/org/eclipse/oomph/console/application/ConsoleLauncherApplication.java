package org.eclipse.oomph.console.application;

import java.util.concurrent.TimeUnit;

import org.eclipse.oomph.console.core.application.AbstractLauncherApplication;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.console.installer.ConsoleInstaller;

public class ConsoleLauncherApplication extends AbstractLauncherApplication {
	@Override
	public void run() {
		ConsoleInstaller installer = new ConsoleInstaller();
		long startTime = System.currentTimeMillis();
		try {
			installer.run();
		} catch (Exception e) {
			if (Parameters.VERBOSE) {
				e.printStackTrace();
			} else {
				System.out.println("Installation failed");
				System.err.println(e.getMessage());
			}
		} finally {
			long endTime = System.currentTimeMillis();
			long millis = endTime - startTime;
			String startDuration = String.format("%02d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
					TimeUnit.MILLISECONDS.toSeconds(millis)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
			System.out.println("Duration: " + startDuration);
		}
	}
}
