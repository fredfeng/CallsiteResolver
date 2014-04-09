package test.util;

import java.util.*;

// cgfsm is the slave fsm
public class cgfsm extends fsm {
	
	// the first state in return value is the state in refsm
	// the second state in return value is the state in cgfsm
	// i.e., 
	// ( masterState[first state], slaveState[second state] ) ----> true / false
	public Map<State, Map<State, Boolean>> annotate(Map<State, Set<Edge>> regExprOpts) {
		Map<State, Map<State, Boolean>> opts = new HashMap<State, Map<State, Boolean>>();
		for (State stateInReg : regExprOpts.keySet()) {
			Set<Edge> keyEdges = regExprOpts.get(stateInReg);
			opts.put(stateInReg, annotateOneMasterState(keyEdges));
		}
		return opts;
	}
	
	// state: each state in cgfsm
	// boolean: true -- this state is OK, false -- the subgraph of this state does not have the key edge
	protected Map<State, Boolean> annotateOneMasterState(Set<Edge> keyEdges) {
		Map<State, Boolean> opts = new HashMap<State, Boolean>();
		for (Object s : initStates) {
			State st = (State) s;
			annotateOneSlaveStateOneMasterState(st, keyEdges, opts);
		}
		return opts;
	}
	
	// for each master state in regular expr fsm
	// then for each slave state in call graph fsm
	// annotate the slave state with true/false
	// denoting whether the subgraph of the slave state has at least one key edge
	// that leads to the final state of regular expr fsm
	protected boolean annotateOneSlaveStateOneMasterState(State currSlaveState, 
			Set<Edge> keyEdges, Map<State, Boolean> opts) {
		// termination conditions
		// 1. if this slave state has been visited, return its boolean value
		if (opts.containsKey(currSlaveState)) 
			return opts.get(currSlaveState);
		// 2. if this slave state has only one (.*) edge, return true if it is fine
		/*
		if (currSlaveState.hasOnlyOneDotOutgoingEdge()) {
			if (keyEdges.contains(currSlaveState.getOnlyOneOutgoingEdge())) {
				opts.put(currSlaveState, true);
				return true;
			}
			return false;
		}
		*/
		// if this slave state has at least one outgoing edge (not cycle)
		// do the recursive case
		// we first check edges
		boolean edge_result = false, state_result = false;
		for (Object e : currSlaveState.outgoingStatesInv()) {
			Edge eg = (Edge) e;
			if (keyEdges.contains(eg)) {
				edge_result = true;
				break;
			} 
		}
		// if we can in advance annotate this currSlaveState to be true
		// we annotate it, which will be good if there is a cycle
		// but this does not help if there is a cycle and we cannot annotate true
		// in which case we still need to handle the graph with connected components
		// but generally, we do not need this code and it still works
		if (edge_result)
			opts.put(currSlaveState, true);
		// we check for the neighboring states of currSlaveState
		for (Object s : currSlaveState.outgoingStates()) {
			State st = (State) s;
			if (s.equals(currSlaveState))
				continue;
			state_result = state_result || annotateOneSlaveStateOneMasterState(st, keyEdges, opts);
		}
		// synthesize the result and annotate
		boolean result = edge_result || state_result;
		opts.put(currSlaveState, result);
		
		return result;
	}
}
