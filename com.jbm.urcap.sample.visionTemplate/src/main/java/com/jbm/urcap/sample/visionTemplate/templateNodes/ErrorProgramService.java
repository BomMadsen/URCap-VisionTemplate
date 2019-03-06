package com.jbm.urcap.sample.visionTemplate.templateNodes;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class ErrorProgramService implements SwingProgramNodeService<ErrorProgramContribution, ErrorProgramView>{

	@Override
	public String getId() {
		return "errorNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setUserInsertable(false);
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Error";
	}

	@Override
	public ErrorProgramView createView(ViewAPIProvider apiProvider) {
		return new ErrorProgramView(apiProvider);
	}

	@Override
	public ErrorProgramContribution createNode(ProgramAPIProvider apiProvider,
			ErrorProgramView view, DataModel model, CreationContext context) {
		return new ErrorProgramContribution(apiProvider, view, model, context);
	}

}
