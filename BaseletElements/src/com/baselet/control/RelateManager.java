package com.baselet.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.DrawPanel;
import com.baselet.element.GridElement;
import com.baselet.elementnew.facet.common.HierarchyRelation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelateManager {

	private final Logger log = Logger.getLogger(RelateManager.class);
	
	private HashMap<GridElement, Long> elementToIdMap; // To Find ID using element
	private HashMap<Long, GridElement> idToElementMap; // To find Element using ID
	private HashMap<Long, Relate> relationMap;
	private AtomicLong idGenerator;
	private ObjectMapper ob;

	public RelateManager(DrawPanel drawPanel) {
		elementToIdMap = new HashMap<GridElement, Long>();
		idToElementMap = new HashMap<Long, GridElement>();
		relationMap = new HashMap<Long, Relate>();
		ob = new ObjectMapper();
		addExistingElements(drawPanel);
		idGenerator = new AtomicLong();
	}

	private void addExistingElements(DrawPanel drawPanel) {
		log.info("Counting:_" + drawPanel.getGridElements().size() + " Elements!");
		drawPanel.getGridElements()
			.stream()
			.forEach(gridElement -> {
				addByJson(gridElement);
			});
	}

	private void addByJson(GridElement element) {
		if(!element.getRelateSettings().isEmpty()){
			try {
				Relate relate = ob.readValue(element.getRelateSettings(), Relate.class);
				getElementToId().put(element, relate.getId());
				getIdToElement().put(relate.id, element);
				getRelationMap().put(relate.getId(), relate);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addPair(GridElement parent, GridElement child) {
		replaceParent(parent, child);
	}

	private void replaceParent(GridElement parent, GridElement child) {
		// Child relate object
		removeParent(child);
		addParent(child, parent);
	}
	
	private void addParent(GridElement child, GridElement parent) {
		getRelationByElement(child).setOptParent(Optional.ofNullable(getIdByElement(parent)));
		getRelationByElement(parent).addChild(getIdByElement(child));
	}

	private void removeParent(GridElement child) {
		removeParent(Optional.ofNullable(getRelationByElement(child)));
	}
	
	private void removeParent(Optional<Relate> childRelate) {
		childRelate.ifPresent(child -> {
			child.getOptParent().ifPresent(parent -> {
				if(getRelationMap().containsKey(child.getOptParent().get())) {
					getRelationMap().get(child.getOptParent().get()).removeChild(child.getId());
				}
			});
			child.setOptParent(Optional.empty());
		});
	}

	private HashMap<GridElement, Long> getElementToId() {
		return elementToIdMap;
	}

	private HashMap<Long, Relate> getRelationMap() {
		return relationMap;
	}

	public Optional<GridElement> getElementById(Long id) {
		return Optional.ofNullable(idToElementMap.get(id));
	}

	/** 
	 * Return the ID associated with the element
	 * If the element have no ID, an ID is being made for that element
	 * @param element
	 * @return ID
	 */
	public Long getIdByElement(GridElement element) {
		return getElementToId().putIfAbsent(element, createRelation(element));
	}

	private Long createRelation(GridElement element) {
		if(exists(element)){
			return getElementToId().get(element);
		} else {
			Long id = generateId();
			log.info("Found id: " + id + "\tFor: " + element.getPanelAttributes());
			getElementToId().putIfAbsent(element, id);
			getIdToElement().putIfAbsent(id, element);
			getRelationMap().putIfAbsent(id, new Relate(id));
			return id;
		}
	}

	private boolean exists(GridElement element) {
		return getElementToId().containsKey(element);
	}

	private Long generateId() {
		while(getIdToElement().containsKey(getGenerator().incrementAndGet()));
		return getGenerator().get();
	}

	private AtomicLong getGenerator() {
		return idGenerator;
	}

	private HashMap<Long, GridElement> getIdToElement() {
		return idToElementMap;
	}

	public void removeChild(GridElement child, GridElement parent) {
		removeParent(child);
	}

	public void removeAllChilds(GridElement parent) {
		getElementToId().computeIfPresent(parent, (e, i) -> {
			getRelationMap().computeIfPresent(i, (id, rel) -> {
				rel.children.forEach((c_id) -> {
					removeParent(getElementById(c_id).get());
				});
				// Of precaution
				rel.getChildren().clear();
				return rel;
			});
			return i;
		});
	}

	public boolean hasChild(GridElement parent) {
//		getRelationByElement(parent)
		return elementToIdMap.containsKey(parent) ? hasChild(elementToIdMap.get(parent)) : false;
	}

	public boolean hasChild(Long id) {
		if (relationMap.containsKey(id)) {
			return relationMap.get(id).children.contains(id);
		}
		return false;
	}

	public boolean hasParent(GridElement child) {
		return elementToIdMap.containsKey(child) ? hasParent(elementToIdMap.get(child)) : false;
	}

	public boolean hasParent(Long id) {
		return relationMap.containsKey(id);
	}

	public List<GridElement> getChildren(GridElement parent) {
		if (elementToIdMap.containsKey(parent)) {
			List<GridElement> result = new ArrayList<GridElement>();
			for (Long id : getChildrenIds(elementToIdMap.get(parent))) {
				result.add(idToElementMap.get(id));
			}
			return result;
		}
		else {
			return new ArrayList<GridElement>();
		}
	}

	public Set<Long> getChildrenIds(Long id) {
		return relationMap.get(id).children;
	}

	@SuppressWarnings("unchecked")
	public Optional<GridElement> getParent(GridElement child) {
		Optional result = Optional.empty();
		getRelationByElement(child).getOptParent().ifPresent(parent -> {
			result = Optional.ofNullable(getElementById(parent));
		});
		
		return (Optional<GridElement>) (elementToIdMap.containsKey(child) ?
				Optional.ofNullable(getParent(elementToIdMap.get(child))) : Optional.empty());
	}

	public Optional<Long> getParent(Long childId) {
		return getRelationMap()
				.getOrDefault(childId, new Relate(childId))
				.getOptParent();
	}

	/**
	 * @param child
	 * @param parent
	 * @return a map with 2 entries with json for change
	 */
	public Map<GridElement, String> getJSONForChange(GridElement child, GridElement parent) {
		Map<GridElement, String> result = new HashMap<GridElement, String>();

		try {
			result.put(child, ob.writeValueAsString(getRelationByElement(child)));			
			result.put(parent, ob.writeValueAsString(getRelationByElement(parent)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return result;
	}

	public String getJSON(GridElement element) {
		try {
			return ob.writeValueAsString(getRelationByElement(element));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "Error at Id: " + getIdByElement(element);
	}

	private Relate getRelationByElement(GridElement element) {
		return getRelationMap().putIfAbsent(getIdByElement(element), new Relate(getIdByElement(element)));
	}

	// Relate={"id":4, child:[1,2,3] parent:6}
	public static class Relate {

		public Relate(Relate other) {
			id = other.id;
			children = other.children;
			parent = other.parent;
		}

		public void addChild(Long childId) {
			this.children.add(childId);
		}
		
		public void removeChild(Long childId){
			this.children.remove(childId);
		}

		public Relate(Long id) {
			this.id = id;
			this.parent = null;
			this.children = new HashSet<>();
		}

		public Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Set<Long> getChildren() {
			return children;
		}

		public void setChildren(Set<Long> children) {
			this.children = children;
		}

		@JsonIgnore
		public Optional<Long> getOptParent() {
			return Optional.ofNullable(parent);
		}

		@JsonIgnore
		public void setOptParent(Optional<Long> parent) {
			this.parent = parent.orElse(null);
		}
		
		public void setParent(Long parent) {
			this.parent = parent;
		}
		
		public Long getParent() {
			return this.parent;
		}

		public Set<Long> children = new HashSet<Long>();
		
		
		public Long parent;
		
	}

	public boolean containsElement(GridElement element) {
		return getElementToId().containsKey(element);
	}
}
