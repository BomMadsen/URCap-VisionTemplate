package com.jbm.urcap.sample.visionTemplate.templateNodes;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class PartsFoundAndReachableProgramContribution implements ProgramNodeContribution{

	public PartsFoundAndReachableProgramContribution(ProgramAPIProvider apiProvider,
			PartsFoundAndReachableProgramView view, DataModel model, CreationContext context) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void openView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		return "Parts found and reachable";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.writeChildren();
	}

}
