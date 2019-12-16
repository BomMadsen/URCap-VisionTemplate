package com.jbm.urcap.sample.visionTemplate.templateNodes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.device.gripper.GripperDevice;

public class PartsFoundAndReachableProgramView implements SwingProgramNodeView<PartsFoundAndReachableProgramContribution> {
	
	private JComboBox<Object> gripperSelectionCombobox;
	private Box gripperSelectionView;
	
	public PartsFoundAndReachableProgramView(ViewAPIProvider apiProvider) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void buildUI(JPanel panel, ContributionProvider<PartsFoundAndReachableProgramContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		gripperSelectionView = createGripperSelectionComboBox("Select gripper:", provider);
		panel.add(gripperSelectionView);
	}
	
	public void showGripperSelection(boolean show) {
		gripperSelectionView.setVisible(show);
	}
	
	public void enableGripperSelection(boolean enable) {
		gripperSelectionCombobox.setEnabled(enable);
	}
	
	public void setGripperComboboxItems(List<GripperDevice> grippers) {
		gripperSelectionCombobox.removeAllItems();
		gripperSelectionCombobox.addItem("<- select ->");
		for (GripperDevice gripperDevice : grippers) {
			gripperSelectionCombobox.addItem(gripperDevice);
		}
	}

	private Box createGripperSelectionComboBox(String description, final ContributionProvider<PartsFoundAndReachableProgramContribution> provider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(150, 40));
		descriptionLabel.setMaximumSize(descriptionLabel.getPreferredSize());
		
		gripperSelectionCombobox = new JComboBox<Object>();
		gripperSelectionCombobox.setPreferredSize(new Dimension(300, 30));
		gripperSelectionCombobox.setMaximumSize(gripperSelectionCombobox.getPreferredSize());
		
		gripperSelectionCombobox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof GripperDevice) {
					provider.get().setSelectedGripper((GripperDevice) e.getItem());
				}
			}
		});
		
		box.add(descriptionLabel);
		box.add(gripperSelectionCombobox);
		
		return box;
	}
	
}
