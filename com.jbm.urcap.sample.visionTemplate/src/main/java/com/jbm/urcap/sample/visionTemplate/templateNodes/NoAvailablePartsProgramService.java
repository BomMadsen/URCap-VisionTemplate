package com.jbm.urcap.sample.visionTemplate.templateNodes;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class NoAvailablePartsProgramService implements SwingProgramNodeService<NoAvailablePartsProgramContribution, NoAvailablePartsProgramView>{

	@Override
	public String getId() {
		return "noAvailablePartsNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setUserInsertable(false);
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "No Parts Available";
	}

	@Override
	public NoAvailablePartsProgramView createView(ViewAPIProvider apiProvider) {
		return new NoAvailablePartsProgramView(apiProvider);
	}

	@Override
	public NoAvailablePartsProgramContribution createNode(ProgramAPIProvider apiProvider,
			NoAvailablePartsProgramView view, DataModel model, CreationContext context) {
		return new NoAvailablePartsProgramContribution(apiProvider, view, model, context);
	}

}
