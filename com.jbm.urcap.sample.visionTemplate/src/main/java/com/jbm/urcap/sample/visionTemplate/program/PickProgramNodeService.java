package com.jbm.urcap.sample.visionTemplate.program;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class PickProgramNodeService implements SwingProgramNodeService<PickProgramNodeContribution, PickProgramNodeView>{

	@Override
	public String getId() {
		return "pickTemplateNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Vision Pick";
	}

	@Override
	public PickProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new PickProgramNodeView(apiProvider);
	}

	@Override
	public PickProgramNodeContribution createNode(ProgramAPIProvider apiProvider, PickProgramNodeView view,
			DataModel model, CreationContext context) {
		return new PickProgramNodeContribution(apiProvider, view, model, context);
	}

}
