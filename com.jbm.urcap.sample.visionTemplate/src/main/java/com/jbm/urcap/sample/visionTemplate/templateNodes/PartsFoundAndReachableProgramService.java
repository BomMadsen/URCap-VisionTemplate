package com.jbm.urcap.sample.visionTemplate.templateNodes;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class PartsFoundAndReachableProgramService implements SwingProgramNodeService<PartsFoundAndReachableProgramContribution, PartsFoundAndReachableProgramView>{

	@Override
	public String getId() {
		return "partsFoundAndReachableNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setUserInsertable(false);
		configuration.setChildrenAllowed(true);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Parts Found and Reachable";
	}

	@Override
	public PartsFoundAndReachableProgramView createView(ViewAPIProvider apiProvider) {
		return new PartsFoundAndReachableProgramView(apiProvider);
	}

	@Override
	public PartsFoundAndReachableProgramContribution createNode(ProgramAPIProvider apiProvider,
			PartsFoundAndReachableProgramView view, DataModel model, CreationContext context) {
		return new PartsFoundAndReachableProgramContribution(apiProvider, view, model, context);
	}

}
