package com.jbm.urcap.sample.visionTemplate.program;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class ScanProgramNodeService implements SwingProgramNodeService<ScanProgramNodeContribution, ScanProgramNodeView>{

	@Override
	public String getId() {
		return "scanNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
	}

	@Override
	public String getTitle(Locale locale) {
		return "Vision Scan";
	}

	@Override
	public ScanProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new ScanProgramNodeView(apiProvider);
	}

	@Override
	public ScanProgramNodeContribution createNode(ProgramAPIProvider apiProvider, ScanProgramNodeView view,
			DataModel model, CreationContext context) {
		return new ScanProgramNodeContribution(apiProvider, view, model, context);
	}

}
