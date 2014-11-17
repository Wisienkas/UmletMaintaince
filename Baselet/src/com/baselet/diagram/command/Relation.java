package com.baselet.diagram.command;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;

public class Relation extends Command {

	private static final Logger log = Logger.getLogger(Relation.class);

	private final GridElement parent;
	private final GridElement child;
	private Optional<GridElement> oldParent;

	public Relation(GridElement parent, GridElement child) {
		this.parent = parent;
		this.child = child;
		oldParent = Optional.empty();
	}

	@Override
	public void execute(DiagramHandler handler) {
		super.execute(handler);
		oldParent = handler.getRelationManager().getParent(child);
		handler.getRelationManager().addPair(parent, child);
		String childJson = handler.getRelationManager().getJSON(child);
		String parentJson = handler.getRelationManager().getJSON(parent);
		child.setRelateSettings(childJson);
		parent.setRelateSettings(parentJson);
		log.info("Child: " + childJson);
		log.info("Parent: " + parentJson);
	}

	@Override
	public void undo(DiagramHandler handler) {
		super.undo(handler);
		handler.getRelationManager().removeChild(child, parent);
		oldParent.ifPresent(old -> handler.getRelationManager().addPair(old, this.child));
		String childJson = handler.getRelationManager().getJSON(child);
		String parentJson = handler.getRelationManager().getJSON(parent);
		log.info("Child: " + childJson);
		log.info("Parent: " + parentJson);
	}

}
