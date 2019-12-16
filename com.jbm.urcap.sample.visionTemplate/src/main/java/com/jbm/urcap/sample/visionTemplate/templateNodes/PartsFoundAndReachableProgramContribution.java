package com.jbm.urcap.sample.visionTemplate.templateNodes;

import java.util.List;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.CreationContext.NodeCreationType;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.device.gripper.GripperDevice;
import com.ur.urcap.api.domain.device.gripper.GripperManager;
import com.ur.urcap.api.domain.program.ProgramModel;
import com.ur.urcap.api.domain.program.nodes.ProgramNode;
import com.ur.urcap.api.domain.program.nodes.ProgramNodeFactory;
import com.ur.urcap.api.domain.program.nodes.builtin.FolderNode;
import com.ur.urcap.api.domain.program.nodes.builtin.MoveNode;
import com.ur.urcap.api.domain.program.nodes.builtin.WaypointNode;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.BlendParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointMotionParameters;
import com.ur.urcap.api.domain.program.nodes.builtin.configurations.waypointnode.WaypointNodeConfig;
import com.ur.urcap.api.domain.program.nodes.contributable.device.gripper.GripperNode;
import com.ur.urcap.api.domain.program.nodes.contributable.device.gripper.GripperNodeFactory;
import com.ur.urcap.api.domain.program.nodes.contributable.device.gripper.configuration.GripActionConfig;
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

public class PartsFoundAndReachableProgramContribution implements ProgramNodeContribution{

	private final ProgramAPIProvider apiProvider;
	private final DataModel model;
	private final PartsFoundAndReachableProgramView view;
	private final UndoRedoManager undoRedoManager;
	
	private static final String BUILT_SUBTREE_KEY = "templateImplemented";
	private static final String VARIABLE_APPROACH_KEY = "approachWpt";
	private static final String VARIABLE_TARGET_KEY = "targetWpt";
	private static final String VARIABLE_EXIT_KEY = "exitWpt";
	
	public PartsFoundAndReachableProgramContribution(ProgramAPIProvider apiProvider,
			PartsFoundAndReachableProgramView view, DataModel model, CreationContext context) {
		this.apiProvider = apiProvider;
		this.model = model; 
		this.view = view;
		this.undoRedoManager = apiProvider.getProgramAPI().getUndoRedoManager();
		
		if(context.getNodeCreationType().equals(NodeCreationType.NEW)) {
			if (getGripperCount() == 0) {
				// No gripper drivers available, implement folder instead
				implementTemplateTree(null);
			}else if(getGripperCount() == 1) {
				// If there is one Gripper Driver available, we just build the tree
				implementTemplateTree(getGrippers().get(0));
			} else {
				// If more than 1 gripper is available, the user has to decide
				lockTreeWaitingForGripperSelection();
			}
		}
	}
	
	public void setSelectedGripper(GripperDevice selectedGripper) {
		System.out.println("Selected gripper: "+selectedGripper.getContributorInfo().getName());
		implementTemplateTree(selectedGripper);
		disableGripperSelection();
	}
	
	private void implementTemplateTree(final GripperDevice selectedGripper) {
		final ProgramModel programModel = apiProvider.getProgramAPI().getProgramModel();
		final ProgramNodeFactory nf = programModel.getProgramNodeFactory();
		final TreeNode root = programModel.getRootTreeNode(this);
		undoRedoManager.recordChanges(new UndoableChanges() {
			@Override
			public void executeChanges() {
				try {
					// Unlock child sequence
					root.setChildSequenceLocked(false);
					
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
					TreeNode approachMoveTreeNode = root.addChild(approachMoveJ);
					approachMoveTreeNode.addChild(approachWaypoint);
					
					TreeNode targetMoveTreeNode = root.addChild(targetMoveL);
					targetMoveTreeNode.addChild(targetWaypoint);
					
					root.addChild(createGripperNode(selectedGripper));
					
					TreeNode exitMoveTreeNode = root.addChild(exitMoveL);
					exitMoveTreeNode.addChild(exitWaypoint);
					
					storeImplementedSubtree(true);
				} catch (TreeStructureException e) {
					// TODO: handle exception
				}
			}
		});
	}
	
	private void lockTreeWaitingForGripperSelection() {
		final ProgramModel programModel = apiProvider.getProgramAPI().getProgramModel();
		final TreeNode root = programModel.getRootTreeNode(this);
		root.setChildSequenceLocked(true);
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
	
	private ProgramNode createGripperNode(GripperDevice selectedGripper) {
		if(selectedGripper!=null) {
			GripperNodeFactory gnf = apiProvider.getProgramAPI().getDeviceManager(GripperManager.class).getGripperProgramNodeFactory();
			GripperNode gripperNode = gnf.createGripperNode(selectedGripper);
			
			GripActionConfig gripConfig = gripperNode.createGripActionConfigBuilder().build();
			gripperNode.setConfig(gripConfig);
			
			return gripperNode;
		} else {
			// No gripper drivers available, insert a folder instead
			
			ProgramNodeFactory pnf = apiProvider.getProgramAPI().getProgramModel().getProgramNodeFactory();

			FolderNode gripperFolder = pnf.createFolderNode();
			gripperFolder.setName("Gripper Grip");
			
			return gripperFolder;
		}
	}
	
	private List<GripperDevice> getGrippers(){
		return apiProvider.getProgramAPI().getDeviceManager(GripperManager.class).getGrippers();
	}
	
	private int getGripperCount() {
		return getGrippers().size();
	}
	
	private boolean shouldSelectGripper() {
		return getGripperCount()>1;
	}
	
	private void showGripperSelection() {
		view.setGripperComboboxItems(getGrippers());
		view.showGripperSelection(true);
	}
	
	private void hideGripperSelection() {
		view.showGripperSelection(false);
	}
	
	private void enableGripperSelection() {
		view.enableGripperSelection(true);
	}
	
	private void disableGripperSelection() {
		view.enableGripperSelection(false);
	}
	
	@Override
	public void openView() {
		if(shouldSelectGripper()) {
			showGripperSelection();
		} else {
			hideGripperSelection();
		}
		
		if(!isSubtreeImplemented()) {
			enableGripperSelection();
		} else {
			disableGripperSelection();
		}
	}

	@Override
	public void closeView() {
		// Do nothing
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
		// We need to pay attention to, that the variable name may have changed
		// In order to get the variable for the program, this must be done: 
		String actualApproachVariableName = writer.getResolvedVariableName(getApproachWaypointVariable());
		// Or we can just use this in an assignment:
		writer.assign(getApproachWaypointVariable(), "p[0.1, 0.2, 0.3, 0, 3.1415, 0]");
		writer.appendLine("popup(\"Actual approach name is: "+actualApproachVariableName+"\",\"Vision Message\", blocking=True)");
		
		writer.writeChildren();
	}
	
	private boolean isSubtreeImplemented() {
		return model.get(BUILT_SUBTREE_KEY, false);
	}
	
	private Variable getApproachWaypointVariable() {
		return model.get(VARIABLE_APPROACH_KEY, createPoseVariable("Approach"));
	}
	
	// NOTE: These are already called inside an "UndoableChanges" scope
	private void storeImplementedSubtree(boolean implemented) {
		model.set(BUILT_SUBTREE_KEY, implemented);
	}
	
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
