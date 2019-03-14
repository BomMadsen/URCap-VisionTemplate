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
import com.ur.urcap.api.domain.program.nodes.builtin.CommentNode;
import com.ur.urcap.api.domain.program.nodes.builtin.FolderNode;
import com.ur.urcap.api.domain.program.nodes.builtin.MoveNode;
import com.ur.urcap.api.domain.program.nodes.builtin.WaypointNode;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.MoveJMoveNodeConfig;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.movenode.MoveLMoveNodeConfig;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.BlendParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointMotionParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointNodeConfig;
import com.ur.urcap.api.domain.program.structure.TreeNode;
import com.ur.urcap.api.domain.program.structure.TreeStructureException;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.PoseFactory;
import com.ur.urcap.api.domain.value.expression.ExpressionBuilder;
import com.ur.urcap.api.domain.value.expression.InvalidExpressionException;
import com.ur.urcap.api.domain.value.simple.Angle;
import com.ur.urcap.api.domain.value.simple.Length;
import com.ur.urcap.api.domain.variable.GlobalVariable;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableException;
import com.ur.urcap.api.domain.variable.VariableFactory;

public class PickProgramNodeContribution implements ProgramNodeContribution{

	private final ProgramAPIProvider apiProvider;
	private final PickProgramNodeView view;
	private final DataModel model;
	private final UndoRedoManager undoRedoManager;
	
	private static final String VARIABLE_APPROACH_KEY = "approachWpt";
	private static final String VARIABLE_TARGET_KEY = "targetWpt";
	private static final String VARIABLE_EXIT_KEY = "exitWpt";
	
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
					/*****
					 * This section is getting looooong, maybe split me up!
					 */
					
					// Build basic template
					root.addChild(nf.createURCapProgramNode(PartsFoundAndReachableProgramService.class));
					root.addChild(nf.createURCapProgramNode(NoAvailablePartsProgramService.class));
					root.addChild(nf.createURCapProgramNode(ErrorProgramService.class));
					
					// Lock the sequence of branches
					root.setChildSequenceLocked(true);
					
					// Build template for PartsFoundNode
					TreeNode partsFoundTreeNode = root.getChildren().get(0);
					
					// Build a MoveJ for approach
					MoveNode approachMoveJ = nf.createMoveNodeNoTemplate();
					approachMoveJ.setConfig(approachMoveJ.getConfigBuilders().createMoveJConfigBuilder().build());
					
					// Build approach waypoint
					WaypointNode approachWaypoint = nf.createWaypointNode();
					GlobalVariable approachVariable = createPoseVariable("Approach");
					storeApproachVariable(approachVariable); //Store the variable in the data model for later usage
					BlendParameters approachWaypointDefaultBlends = approachWaypoint.getConfigFactory().createNoBlendParameters();
					WaypointMotionParameters approachMotionParams = approachWaypoint.getConfigFactory().createSharedMotionParameters();
					WaypointNodeConfig approachConfig = approachWaypoint.getConfigFactory().createVariablePositionConfig(approachVariable, approachWaypointDefaultBlends, approachMotionParams);
					approachWaypoint.setConfig(approachConfig);
					
					// Build a MoveL for target
					MoveNode targetMoveL = nf.createMoveNodeNoTemplate();
					targetMoveL.setConfig(targetMoveL.getConfigBuilders().createMoveLConfigBuilder().build());
					
					// Build target waypoint
					WaypointNode targetWaypoint = nf.createWaypointNode();
					GlobalVariable targetVariable = createPoseVariable("Target");
					storeTargetVariable(targetVariable);
					BlendParameters targetWaypointDefaultBlends = targetWaypoint.getConfigFactory().createNoBlendParameters();
					WaypointMotionParameters targetMotionParams = targetWaypoint.getConfigFactory().createSharedMotionParameters();
					WaypointNodeConfig targetConfig = targetWaypoint.getConfigFactory().createVariablePositionConfig(targetVariable, targetWaypointDefaultBlends, targetMotionParams);
					targetWaypoint.setConfig(targetConfig);
					
					// Build a folder for gripper logic
					FolderNode gripperFolder = nf.createFolderNode();
					gripperFolder.setName("Gripper Grip");
					
					// Build a comment for the folder node
					CommentNode gripperComment = nf.createCommentNode();
					gripperComment.setComment("Insert gripper GRIP logic here");
					
					// Build a MoveL for exit
					MoveNode exitMoveL = nf.createMoveNodeNoTemplate();
					exitMoveL.setConfig(exitMoveL.getConfigBuilders().createMoveJConfigBuilder().build());
					
					// Build exit waypoint
					WaypointNode exitWaypoint = nf.createWaypointNode();
					GlobalVariable exitVariable = createPoseVariable("Exit");
					storeExitVariable(exitVariable);
					BlendParameters exitWaypointDefaultBlends = exitWaypoint.getConfigFactory().createNoBlendParameters();
					WaypointMotionParameters exitMotionParams = exitWaypoint.getConfigFactory().createSharedMotionParameters();
					WaypointNodeConfig exitConfig = exitWaypoint.getConfigFactory().createVariablePositionConfig(exitVariable, exitWaypointDefaultBlends, exitMotionParams);
					exitWaypoint.setConfig(exitConfig);
					
					// Now we have build all the components. 
					// Let's put them into a tree
					TreeNode approachMoveTreeNode = partsFoundTreeNode.addChild(approachMoveJ);
					approachMoveTreeNode.addChild(approachWaypoint);
					
					TreeNode targetMoveTreeNode = partsFoundTreeNode.addChild(targetMoveL);
					targetMoveTreeNode.addChild(targetWaypoint);
					
					partsFoundTreeNode.addChild(gripperFolder).addChild(gripperComment);
					
					TreeNode exitMoveTreeNode = partsFoundTreeNode.addChild(exitMoveL);
					exitMoveTreeNode.addChild(exitWaypoint);
					
					// Insert comment under NoAvailablePartsNode
					TreeNode noAvailablePartsTreeNode = root.getChildren().get(1);
					CommentNode noPartsCommentNode = nf.createCommentNode();
					noPartsCommentNode.setComment("Insert your no parts handling code here");
					noAvailablePartsTreeNode.addChild(noPartsCommentNode);
					
					// Insert comment under ErrorNode
					TreeNode errorTreeNode = root.getChildren().get(2);
					CommentNode errorCommentNode = nf.createCommentNode();
					errorCommentNode.setComment("Insert your error handling code here");
					errorTreeNode.addChild(errorCommentNode);
				} catch (TreeStructureException e) {
					// TODO: handle exception
				}
			}
		});
	}
	
	private GlobalVariable createPoseVariable(String suggestedName) {
		VariableFactory vf = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
		PoseFactory pf = apiProvider.getProgramAPI().getValueFactoryProvider().getPoseFactory();
		ExpressionBuilder eb = apiProvider.getProgramAPI().getValueFactoryProvider().createExpressionBuilder();
		
		// Create an empty pose, will be updated by program later
		Pose initialPose = pf.createPose(0, 0, 0, 0, 0, 0, Length.Unit.M, Angle.Unit.RAD);
		
		GlobalVariable var = null;
		try {
			var = vf.createGlobalVariable(suggestedName, eb.append(initialPose.toString()).build());
		} catch (VariableException e) {
			System.out.println("Exception creating pose variable...");
		} catch (InvalidExpressionException e) {
			System.out.println("Exception building pose...");
		}
		return var;
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
		
		// We need to oay attention to, that the variable name may have changed
		// In order to get the variable for the program, this must be done: 
		String actualApproachVariableName = writer.getResolvedVariableName(getApproachWaypointVariable());
		// Or we can just use this in an assignment:
		writer.assign(getApproachWaypointVariable(), "p[0.1, 0.2, 0.3, 0, 3.1415, 0]");
		writer.appendLine("popup(\"Actual approach name is: "+actualApproachVariableName+"\",\"Vision Message\", blocking=True)");
		
		writer.writeChildren();
	}
	
	private Variable getApproachWaypointVariable() {
		return model.get(VARIABLE_APPROACH_KEY, createPoseVariable("Approach"));
	}
	
	// NOTE: These are already called inside an "UndoableChanges" scope
	private void storeApproachVariable(Variable var) {
		model.set(VARIABLE_APPROACH_KEY, var);
	}
	
	private void storeTargetVariable(Variable var) {
		model.set(VARIABLE_TARGET_KEY, var);
	}
	
	private void storeExitVariable(Variable var) {
		model.set(VARIABLE_EXIT_KEY, var);
	}

}
