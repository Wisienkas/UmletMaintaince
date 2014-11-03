package com.baselet.diagram.command;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.baselet.control.RelateManager;
import com.baselet.diagram.DiagramHandler;
import com.baselet.element.GridElement;

public class Relation extends ChangeElementSetting {

	private static final Logger log = Logger.getLogger(Relation.class);

	private final GridElement parent;
	private final GridElement child;
	private Optional<GridElement> oldParent;

	public Relation(GridElement parent, GridElement child) {
		super("Relate", getIds(parent, child));
		this.parent = parent;
		this.child = child;
		oldParent = Optional.empty();
	}

	private static Map<GridElement, String> getIds(GridElement parent, GridElement child) {
		return RelateManager.getInstance().getJSONForChange(child, parent);
	}

	@Override
	public void execute(DiagramHandler handler) {
		oldParent = RelateManager.getInstance().getParent(child);
		RelateManager.getInstance().AddPair(parent, child);
		super.execute(handler);
	}

	@Override
	public void undo(DiagramHandler handler) {
		oldParent.ifPresent(new Consumer<GridElement>() {
			@Override
			public void accept(GridElement ge) {
				RelateManager.getInstance().AddPair(ge, child);
		}});
		super.undo(handler);
	}

}
