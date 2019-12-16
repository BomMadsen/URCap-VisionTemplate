package com.jbm.urcap.sample.visionTemplate.program;

import com.jbm.urcap.sample.visionTemplate.templateNodes.ErrorProgramService;
import com.jbm.urcap.sample.visionTemplate.templateNodes.NoAvailablePartsProgramService;
import com.jbm.urcap.sample.visionTemplate.templateNodes.PartsFoundAndReachableProgramService;
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

public class PickProgramNodeContribution implements ProgramNodeContribution{

	private final ProgramAPIProvider apiProvider;
	private final PickProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	public PickProgramNodeContribution(ProgramAPIProvider apiProvider, PickProgramNodeView view,
			DataModel model, CreationContext context) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.undoRedoManager = apiProvider.getProgramAPI().getUndoRedoManager();
		
		if(context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			buildTemplateTree();
		}
	}
	
	private void buildTemplateTree() {
		final ProgramModel programModel = apiProvider.getProgramAPI().getProgramModel();
		final ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(this);
		undoRedoManager.recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				try {
					// Insert child template nodes
					root.addChild(nf.createURCapProgramNode(PartsFoundAndReachableProgramService.class));
					root.addChild(nf.createURCapProgramNode(NoAvailablePartsProgramService.class));
					root.addChild(nf.createURCapProgramNode(ErrorProgramService.class));
					
					// Lock the sequence of branches
					root.setChildSequenceLocked(true);
					
				} catch (TreeStructureException e) {
					// TODO: handle exception
				}
			}
		});
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
		return "Vision Pick";
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		
//		// We need to oay attention to, that the variable name may have changed
//		// In order to get the variable for the program, this must be done: 
//		String actualApproachVariableName = writer.getResolvedVariableName(getApproachWaypointVariable());
//		// Or we can just use this in an assignment:
//		writer.assign(getApproachWaypointVariable(), "p[0.1, 0.2, 0.3, 0, 3.1415, 0]");
//		writer.appendLine("popup(\"Actual approach name is: "+actualApproachVariableName+"\",\"Vision Message\", blocking=True)");
		
		writer.writeChildren();
	}
	

}
