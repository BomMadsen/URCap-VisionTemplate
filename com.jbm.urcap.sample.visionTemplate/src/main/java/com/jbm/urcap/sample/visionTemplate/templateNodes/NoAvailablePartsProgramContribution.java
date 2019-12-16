package com.jbm.urcap.sample.visionTemplate.templateNodes;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.nodes.builtin.CommentNode;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

public class NoAvailablePartsProgramContribution implements ProgramNodeContribution{

	private final ProgramAPIProvider apiProvider;
	private final UndoRedoManager undoRedoManager;
	
	private static final String CommentString = "Insert your no parts handling code here";
	
	public NoAvailablePartsProgramContribution(ProgramAPIProvider apiProvider,
			NoAvailablePartsProgramView view, DataModel model, CreationContext context) {
		this.apiProvider = apiProvider;
		this.undoRedoManager = apiProvider.getProgramAPI().getUndoRedoManager();
		
		if(context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			insertDescriptionComment();
		}
	}
	
	private void insertDescriptionComment() {
		final ProgramModel programModel = apiProvider.getProgramAPI().getProgramModel();
		final ProgramNodeFactory pnf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(this);
		undoRedoManager.recordChanges(new UndoableChanges() {
			
			@Override
			public void executeChanges() {
				try {
					CommentNode commentNode = pnf.createCommentNode();
					commentNode.setComment(CommentString);
					root.addChild(commentNode);
				} catch (TreeStructureException e) {
					// Do nothing
				}
			}
		});
	}
	
	@Override
	public void openView() {
		// Do nothing
	}

	@Override
	public void closeView() {
		// Do nothing
	}

	@Override
	public String getTitle() {
		return "No parts available";
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
