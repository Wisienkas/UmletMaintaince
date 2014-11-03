package com.baselet.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.baselet.element.GridElement;
import com.baselet.elementnew.facet.common.HierarchyRelation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelateManager {

	private static RelateManager INSTANCE;

	public static RelateManager getInstance(){
		if(INSTANCE == null){
			INSTANCE = new RelateManager();
		}
		return INSTANCE;
	}

	private final HashMap<GridElement, Long> elementToIdMap; // To Find ID using element
	private final HashMap<Long, GridElement> idToElementMap; // To find Element using ID
	private final HashMap<Long, Relate> relationMap;

	private final AtomicLong idGenerator;

	private RelateManager(){
		elementToIdMap = new HashMap<GridElement, Long>();
		idToElementMap = new HashMap<Long, GridElement>();
		relationMap = new HashMap<Long, RelateManager.Relate>();
		idGenerator = new AtomicLong();
	}

	public void AddPair(GridElement parent, GridElement child){
		Long c_id = getIdByElement(child);
		Long p_id = getIdByElement(parent);

		if(relationMap.containsKey(c_id)){
			Relate c_rel = relationMap.get(c_id);
			if(c_rel.parent != null){
				removeChild(child);
			}
			c_rel.parent = p_id;
		}
	}

	public Optional<GridElement> getElementById(Long id){
		return Optional.ofNullable(idToElementMap.get(id));
	}

	public Long getIdByElement(GridElement element){
		if(elementToIdMap.containsKey(element)){
			return elementToIdMap.get(element);
		}else{
			return generateId(element);
		}
	}

	public boolean trySetId(GridElement element, Long id){
		if(idToElementMap.containsKey(id) && !element.equals(idToElementMap.get(id))){
			return false;
		}
		addIdToElement(element, id);
		return true;
	}

	private void addIdToElement(GridElement element, Long id) {
		idToElementMap.put(id, element);
		elementToIdMap.put(element, id);
		relationMap.put(id, new Relate(id));

		List<String> attributes = element.getPanelAttributesAsList();
		for(int i = 0; i < attributes.size(); i++) {
			if(attributes.get(i).startsWith(HierarchyRelation.KEY + "=")) {
				String json = attributes.get(i)
						.substring(attributes.get(i).lastIndexOf(HierarchyRelation.KEY + "="));
				try {
					relationMap.put(id, new ObjectMapper().readValue(json, Relate.class));
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Long generateId(GridElement element) {
		Long id;
		do{
			id = idGenerator.getAndIncrement();
		}while(idToElementMap.containsKey(id));

		addIdToElement(element, id);

		return id;
	}

	private void removeOldParent(GridElement child) {
		final Long c_id = elementToIdMap.get(child);
		Relate c_rel = relationMap.get(child);
		if(c_rel != null){
			Long op_id = c_rel.parent;
			if(relationMap.containsKey(op_id)) {
				relationMap.get(op_id).children.removeIf(new Predicate<Long>() {
					@Override
					public boolean test(Long t) {
						return t == c_id;
					}});
			}
		}
	}

	public void removeChild(GridElement child){
		removeOldParent(child);
	}

	public void removeAllChilds(GridElement parent){
		if(elementToIdMap.containsKey(parent)){
			removeAllChilds(elementToIdMap.get(parent));
		}
	}

	public void removeAllChilds(Long p_id){
		Relate p_rel = relationMap.get(p_id);
		p_rel.children.forEach(new Consumer<Long>() {
			@Override
			public void accept(Long c_id) {
				relationMap.get(c_id).parent = null;
			}});
		p_rel.children.clear();
	}

	public boolean hasChild(GridElement parent){
		return elementToIdMap.containsKey(parent) ? hasChild(elementToIdMap.get(parent)) : false;
	}

	public boolean hasChild(Long id){
		if(relationMap.containsKey(id)){
			return relationMap.get(id).children.contains(id);
		}
		return false;
	}

	public boolean hasParent(GridElement child){
		return elementToIdMap.containsKey(child) ? hasParent(elementToIdMap.get(child)) : false;
	}

	public boolean hasParent(Long id){
		return relationMap.containsKey(id) ? relationMap.get(id).parent != null : false;
	}

	public List<GridElement> getChildren(GridElement parent){
		if(elementToIdMap.containsKey(parent)){
			List<GridElement> result = new ArrayList<GridElement>();
			for(Long id : getChildrenIds(elementToIdMap.get(parent))) {
				result.add(idToElementMap.get(id));
			}
			return result;
		}else {
			return new ArrayList<GridElement>();
		}
	}

	public List<Long> getChildrenIds(Long id){
		return relationMap.get(id).children;
	}

	@SuppressWarnings("unchecked")
	public Optional<GridElement> getParent(GridElement child) {
		return (Optional<GridElement>) (elementToIdMap.containsKey(child) ?
				Optional.ofNullable(getParent(elementToIdMap.get(child))) : Optional.empty());
	}

	public Long getParent(Long id){
		return relationMap.containsKey(id) ? relationMap.get(id).parent : null;
	}

	/**
	 * @param child
	 * @param parent
	 * @return a map with 2 entries with json for change
	 */
	public Map<GridElement, String> getJSONForChange(GridElement child, GridElement parent){
		Map<GridElement, String> result = new HashMap<GridElement, String>();

		ObjectMapper ob = new ObjectMapper();

		try {
			Relate c_rel = new Relate(getRelationByElement(child));
			Relate p_rel = new Relate(getRelationByElement(parent));

			c_rel.parent = p_rel.id;
			if(!p_rel.children.contains(c_rel.id)) {
				p_rel.children.add(c_rel.id);
			}

			result.put(child, ob.writeValueAsString(c_rel));
			result.put(parent, ob.writeValueAsString(p_rel));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return result;
	}

	private Relate getRelationByElement(GridElement child) {
		Long id = getIdByElement(child);
		return relationMap.get(id);
	}

	// Relate={"id":4, child:[1,2,3] parent:6}
	public static class Relate{

		public Relate(Relate other){
			id = other.id;
			children = other.children;
			parent = other.parent;
		}

		public Relate(Long id) {
			this.id = id;
		}

		public Long id;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public List<Long> getChildren() {
			return children;
		}
		public void setChildren(List<Long> children) {
			this.children = children;
		}
		public Long getParent() {
			return parent;
		}
		public void setParent(Long parent) {
			this.parent = parent;
		}
		public List<Long> children = new ArrayList<Long>();
		public Long parent;

	}
}
