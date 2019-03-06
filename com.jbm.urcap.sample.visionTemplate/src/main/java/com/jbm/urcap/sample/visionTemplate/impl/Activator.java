package com.jbm.urcap.sample.visionTemplate.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.jbm.urcap.sample.visionTemplate.program.PickProgramNodeService;
import com.jbm.urcap.sample.visionTemplate.program.ScanProgramNodeService;
import com.jbm.urcap.sample.visionTemplate.templateNodes.ErrorProgramService;
import com.jbm.urcap.sample.visionTemplate.templateNodes.NoAvailablePartsProgramService;
import com.jbm.urcap.sample.visionTemplate.templateNodes.PartsFoundAndReachableProgramService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;

/**
 * Hello world activator for the OSGi bundle URCAPS contribution
 *
 */
public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		bundleContext.registerService(SwingProgramNodeService.class, new PickProgramNodeService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new ScanProgramNodeService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new PartsFoundAndReachableProgramService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new NoAvailablePartsProgramService(), null);
		bundleContext.registerService(SwingProgramNodeService.class, new ErrorProgramService(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}
}

