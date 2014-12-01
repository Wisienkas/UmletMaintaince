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
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.baselet.element.GridElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelateManager {

	private final Logger log = Logger.getLogger(RelateManager.class);

	private final HashMap<GridElement, Long> elementToIdMap; // To Find ID using element
	private final HashMap<Long, GridElement> idToElementMap; // To find Element using ID
	private final HashMap<Long, Relate> relationMap;
	private final AtomicLong idGenerator;
	private final ObjectMapper ob;

	public RelateManager(List<GridElement> collectionOfElements) {
		elementToIdMap = new HashMap<GridElement, Long>();
		idToElementMap = new HashMap<Long, GridElement>();
		relationMap = new HashMap<Long, Relate>();
		ob = new ObjectMapper();
		idGenerator = new AtomicLong();
		addExistingElements(collectionOfElements);
	}

	private void addExistingElements(List<GridElement> collectionOfElements) {
		collectionOfElements.stream()
				.forEach(gridElement -> {
					addByJson(gridElement);
				});
	}

	private void addByJson(GridElement element) {
		if (element.getRelateSettings() != null && !element.getRelateSettings().isEmpty()) {
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
		} else {
			createRelation(element);
		}
	}

	public void addPair(GridElement parent, GridElement child) {
		getIdByElement(child);
		getIdByElement(parent);
		replaceParent(parent, child);
	}

	private void replaceParent(GridElement parent, GridElement child) {
		// Child relate object
		removeChild(child, parent);
		addParent(child, parent);
	}

	private void addParent(GridElement child, GridElement parent) {
		getRelationByElement(child).setOptParent(Optional.ofNullable(getIdByElement(parent)));
		getRelationByElement(parent).addChild(getIdByElement(child));
	}

	private void removeParent(Optional<Relate> childRelate) {
		childRelate.ifPresent(child -> {
			child.getOptParent().ifPresent(parent -> {
				if (getRelationMap().containsKey(child.getOptParent().get())) {
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
		if (exists(element)) {
			return getElementToId().get(element);
		}
		else {
			Long id = generateId();
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
		while (getIdToElement().containsKey(getGenerator().incrementAndGet())) {
			;
		}
		return getGenerator().get();
	}

	private AtomicLong getGenerator() {
		return idGenerator;
	}

	private HashMap<Long, GridElement> getIdToElement() {
		return idToElementMap;
	}

	public void removeChild(GridElement child, GridElement parent) {
		getRelationByElement(parent).removeChild(getIdByElement(child));
		getRelationByElement(child).setOptParent(Optional.empty());
	}

	public void removeAllChilds(GridElement parent) {
		getRelationByElement(parent)
			.getChildren()
			.stream()
			.map(id -> getElementById(id))
			.filter(optElement -> optElement.isPresent())
			.map(optElement -> optElement.get())
			.collect(Collectors.toList())
			.forEach(child -> {
				removeChild(child, parent);
			});
	}

	public boolean hasChild(GridElement parent) {
		Relate relParent = getRelationByElement(parent);
		return !relParent.getChildren().isEmpty();
	}

	public boolean hasChild(Long id) {
		if (relationMap.containsKey(id)) {
			return relationMap.get(id).children.contains(id);
		}
		return false;
	}

	public boolean hasParent(GridElement child) {
		Relate rel = getRelationByElement(child);
		return getRelationByElement(child).getOptParent().isPresent();
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
		Optional<Long> parentAsId = getRelationByElement(child).getOptParent();

		return parentAsId.isPresent() ? getElementById(parentAsId.get()) : Optional.empty();
	}

	public Optional<Long> getParent(Long childId) {
		return getRelationMap()
				.getOrDefault(childId, new Relate(childId))
				.getOptParent();
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
		return getRelationMap().get(getIdByElement(element));
	}

	// Relate={"id":4, child:[1,2,3] parent:6}
	public static class Relate {

		public Relate(Relate other) {
			id = other.id;
			children = other.children;
			parent = other.parent;
		}

		public void addChild(Long childId) {
			children.add(childId);
		}

		public void removeChild(Long childId) {
			children.remove(childId);
		}

		public Relate(Long id) {
			this.id = id;
			parent = null;
			children = new HashSet<>();
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
			return parent;
		}

		public Set<Long> children = new HashSet<Long>();

		public Long parent;

	}
}
