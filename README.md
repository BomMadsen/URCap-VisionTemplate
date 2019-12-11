# URCap-VisionTemplate
Sample to demonstrate building a template program, i.e. for vision systems or similar

The sample consist of two different user-insertable program nodes; 

* Vision Pick: 
A template program node (parent node), that builds a sub-tree of relevant nodes to help the user. 
The node contains a number of branches, that happen based on the feedback from the vision sensor. 
If any Gripper Drivers are installed, this will be inserted in the appropriate place in the tree, and configured to a Grip-action.
* Vision Scan: 
A singular program node (leaf node), that could be used by the user to trigger a vision sensor to scan. 
I.e. if the vision processing time is long, the user could trigger this upon completing a pick.

The built program template can be seen below: 

![Template Program](/Pictures/visionTemplateOverview.png)

Take a closer look at the [PickProgramNodeContribution](/com.jbm.urcap.sample.visionTemplate/src/main/java/com/jbm/urcap/sample/visionTemplate/program/PickProgramNodeContribution.java) where all the template magic happens. 
