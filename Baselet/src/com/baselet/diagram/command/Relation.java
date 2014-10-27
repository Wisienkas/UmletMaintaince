package com.baselet.diagram.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;

public class Relation extends ChangeElementSetting {

	private static final Logger log = Logger.getLogger(Relation.class);

	private final GridElement parent;
	private final GridElement child;
	private GridElement oldParent;

	public Relation(GridElement parent, GridElement child) {
		super("Relate", getMap(parent, child));
		this.parent = parent;
		this.child = child;
	}

	private static Map<GridElement, String> getMap(GridElement parent, GridElement child) {
		Map<GridElement, String> args = new HashMap<GridElement, String>();
		log.info(child.getRectangle());
		args.put(parent, "child:" + child.getRectangle());
		args.put(child, "parent:" + parent.getRectangle());

		return args;
	}

	@Override
	public void execute(DiagramHandler handler) {

		parent.setProperty("child", child.getRectangle());
		for(String attribute : child.getPanelAttributesAsList()) {
			if(attribute.startsWith("parent")){
				oldParent = findOldParent(attribute);
			}
		}
		child.setProperty("parent", parent.getRectangle());
		super.execute(handler);
	}

	private GridElement findOldParent(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undo(DiagramHandler handler) {



		super.undo(handler);
	}

	@Override
	public boolean isMergeableTo(Command c) {
		// TODO Auto-generated method stub
		return super.isMergeableTo(c);
	}

	@Override
	public Command mergeTo(Command c) {
		// TODO Auto-generated method stub
		return super.mergeTo(c);
	}

	@Override
	public void redo(DiagramHandler handler) {
		// TODO Auto-generated method stub
		super.redo(handler);
	}

	@Override
	public boolean isChangingDiagram() {
		// TODO Auto-generated method stub
		return super.isChangingDiagram();
	}

}
