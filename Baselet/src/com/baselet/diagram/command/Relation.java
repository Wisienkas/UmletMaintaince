package com.baselet.diagram.command;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;

public class Relation extends Command {

	private static final Logger log = Logger.getLogger(Relation.class);

	private Optional<GridElement> parent;
	private GridElement child;
	private Optional<GridElement> oldParent;

	public Relation(Optional<GridElement> parent, GridElement child) {
		this.parent = parent;
		this.child = child;
		oldParent = Optional.empty();
	}

	@Override
	public void execute(DiagramHandler handler) {
		super.execute(handler);
		oldParent = handler.getRelationManager().getParent(child);
		oldParent.ifPresent(old -> {
			handler.getRelationManager().removeChild(child, old);
			old.setRelateSettings(handler.getRelationManager().getJSON(old));
		});
		parent.ifPresent(par -> {
			handler.getRelationManager().addPair(par, child);
			par.setRelateSettings(handler.getRelationManager().getJSON(par));
		});
		String childJson = handler.getRelationManager().getJSON(child);
		child.setRelateSettings(childJson);
	}

	@Override
	public void undo(DiagramHandler handler) {
		super.undo(handler);
		parent.ifPresent(par -> {
			handler.getRelationManager().removeChild(child, par);
			par.setRelateSettings(handler.getRelationManager().getJSON(par));
		});
		oldParent.ifPresent(old -> {
			handler.getRelationManager().addPair(old, this.child);
			old.setRelateSettings(handler.getRelationManager().getJSON(old));
		});
		String childJson = handler.getRelationManager().getJSON(child);
		child.setRelateSettings(childJson);
	}

}
