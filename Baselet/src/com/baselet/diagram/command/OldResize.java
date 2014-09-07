package com.baselet.diagram.command;

import java.util.Vector;

import com.baselet.control.Main;
import com.baselet.control.SharedConstants;
import com.baselet.control.Utils;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.draw.geom.Point;
import com.baselet.diagram.draw.geom.PointDouble;
import com.baselet.diagram.draw.geom.Rectangle;
import com.baselet.element.GridElement;
import com.baselet.element.sticking.StickingPolygon;
import com.umlet.element.Relation;
import com.umlet.element.relation.RelationLinePoint;

/**
 * resizing has been merged with Move command and only remains for old grid elements which will not be migrated but removed from the code after some time
 */
@Deprecated
public class OldResize extends Command {
	private static int current_id = 0;

	private int id;
	private final int diffx, diffy, diffw, diffh;
	private Vector<RelationLinePoint> linepoints;
	private final Vector<OldMoveLinePoint> move_commands;
	private final GridElement entity;

	private int getDiffx() {
		return diffx * Main.getHandlerForElement(entity).getGridSize();
	}

	private int getDiffy() {
		return diffy * Main.getHandlerForElement(entity).getGridSize();
	}

	private int getDiffw() {
		return diffw * Main.getHandlerForElement(entity).getGridSize();
	}

	private int getDiffh() {
		return diffh * Main.getHandlerForElement(entity).getGridSize();
	}

	public OldResize(GridElement entity, int diffx, int diffy, int diffw, int diffh) {
		this(entity, diffx, diffy, diffw, diffh, null);
	}

	// resize for merge
	private OldResize(GridElement entity, int id, int diffx, int diffy, int diffw, int diffh,
			Vector<OldMoveLinePoint> move_commands, Vector<OldMoveLinePoint> move_commands2) {
		this.entity = entity;
		this.id = id;
		this.move_commands = move_commands;
		this.move_commands.addAll(move_commands2);
		this.diffx = diffx / Main.getHandlerForElement(entity).getGridSize();
		this.diffy = diffy / Main.getHandlerForElement(entity).getGridSize();
		this.diffw = diffw / Main.getHandlerForElement(entity).getGridSize();
		this.diffh = diffh / Main.getHandlerForElement(entity).getGridSize();
	}

	public OldResize(GridElement entity, int diffx, int diffy, int diffw, int diffh, OldResize first) {
		this.entity = entity;
		move_commands = new Vector<OldMoveLinePoint>();
		this.diffx = diffx / Main.getHandlerForElement(entity).getGridSize();
		this.diffy = diffy / Main.getHandlerForElement(entity).getGridSize();
		this.diffw = (diffw - diffx) / Main.getHandlerForElement(entity).getGridSize();
		this.diffh = (diffh - diffy) / Main.getHandlerForElement(entity).getGridSize();

		Rectangle entityRect = this.entity.getRectangle();
		StickingPolygon from = this.entity.generateStickingBorder(entityRect);

		// AB: FIXED: Use this.diffw/this.diffh instead of diffw/diffh as calculation base
		Rectangle newRect = new Rectangle(entityRect.x + diffx, entityRect.y + diffy, entityRect.width + getDiffw(), entityRect.height + getDiffh());
		StickingPolygon to = this.entity.generateStickingBorder(newRect);

		if (first != null) {
			id = first.id;
			linepoints = first.linepoints;
		}
		else {
			id = current_id;
			current_id++;
			linepoints = Utils.getStickingRelationLinePoints(Main.getHandlerForElement(this.entity), from);
		}

		PointDouble diff;
		Point p;
		Relation r;
		for (RelationLinePoint lp : linepoints) {
			r = lp.getRelation();
			p = r.getLinePoints().get(lp.getLinePointId());

			diff = from.getLine(lp.getStickingLineId()).diffToLine(to.getLine(lp.getStickingLineId()), p.x + r.getRectangle().x, p.y + r.getRectangle().y);

			DiagramHandler handler = Main.getHandlerForElement(entity);
			move_commands.add(new OldMoveLinePoint(lp.getRelation(), lp.getLinePointId(), handler.realignToGrid(diff.x), handler.realignToGrid(diff.y)));
		}

	}

	@Override
	public void execute(DiagramHandler handler) {
		super.execute(handler);

		entity.setLocationDifference(getDiffx(), getDiffy());
		entity.changeSize(getDiffw(), getDiffh());
		if (SharedConstants.stickingEnabled) {
			for (OldMoveLinePoint c : move_commands) {
				c.execute(handler);
			}
		}
	}

	@Override
	public void undo(DiagramHandler handler) {
		super.undo(handler);
		entity.setLocationDifference(-getDiffx(), -getDiffy());
		entity.changeSize(-getDiffw(), -getDiffh());
		for (OldMoveLinePoint c : move_commands) {
			c.undo(handler);
		}
		Main.getInstance().getDiagramHandler().getDrawPanel().updatePanelAndScrollbars();
	}

	@Override
	public boolean isMergeableTo(Command c) {
		if (!(c instanceof OldResize)) {
			return false;
		}
		OldResize r = (OldResize) c;
		if (id == r.id) {
			return true;
		}
		return false;
	}

	@Override
	public Command mergeTo(Command c) {
		OldResize tmp = (OldResize) c;
		return new OldResize(entity, Math.max(id, tmp.id), getDiffx() + tmp.getDiffx(), getDiffy() + tmp.getDiffy(),
				getDiffw() + tmp.getDiffw(), getDiffh() + tmp.getDiffh(), move_commands, tmp.move_commands);
	}
}
