package edu.utexas.cgrex.automaton;

public class AutoEdge {

	// id for Edge class is a
	// SootMethod for call graph finite state machine
	// String for regular expression finite state machine
	protected Object id;

	protected boolean isDotEdge = false;
	
	//name displayed in the graph.
	protected String shortName = "";
	
	protected int weight = 1;
	
	protected int flow = 0;
	
	/**
	 * @return the flow
	 */
	public int getFlow() {
		return flow;
	}

	/**
	 * @param flow the flow to set
	 */
	public void setFlow(int flow) {
		this.flow = flow;
	}
	
	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * @param weight: the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	public AutoEdge(Object id) {
		this.id = id;
		this.isDotEdge = false;
		shortName = (String) id;
	}
	
	public AutoEdge(Object id, int weight, String shortName) {
		this.id = id;
		this.weight = weight;
		this.shortName = shortName;
	}
	
	public AutoEdge(Object id, boolean isDotEdge) {
		this.id = id;
		this.isDotEdge = isDotEdge;
	}

	public Object getId() {
		return id;
	}

	public boolean isDotEdge() {
		return isDotEdge;
	}
	
	public void setDotEdge() {
		isDotEdge = true;
	}
	
	public String getShortName() { 
		return shortName;
	}
	
	public void setShortName(String name) { 
		shortName = name; 
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof AutoEdge)
				&& (id.equals(((AutoEdge) other).getId()) ? true : false);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
