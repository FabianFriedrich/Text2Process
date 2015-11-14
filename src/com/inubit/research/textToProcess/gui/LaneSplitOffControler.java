/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.MessageEndEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;

import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.textToProcess.Constants;

/**
 * @author ff
 *
 */
public class LaneSplitOffControler implements ActionListener, WorkbenchEditorListener{

	private Configuration f_config = Configuration.getInstance();
	
	private boolean f_createMsgEvt = false;
	private boolean f_createCommLinks = false;;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2942852754336955833L;
	private JMenuItem f_item = new JMenuItem("Seperate into Pool");
	private Workbench f_wb;

	private ArrayList<ProcessEdge> f_newEdges = new ArrayList<ProcessEdge>();
	
	private HashMap<ProcessNode,MessageIntermediateEvent> f_extraCommLinkNodeCache = new HashMap<ProcessNode, MessageIntermediateEvent>();
	

	private Map<ProcessNode, String> f_commLinks;
	
	/**
	 * 
	 */
	public LaneSplitOffControler(Workbench wb) {
		f_wb = wb;
		f_wb.addWorkbenchEditorListener(this);
		for(int i=0;i<f_wb.getNumOfProcessEditors();i++) {
			f_wb.getProcessEditor(i).addCustomContextMenuItem(Lane.class, f_item);
		}
		f_item.addActionListener(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		f_newEdges.clear();
		f_createMsgEvt = "1".equals(f_config.getProperty(Constants.CONF_LANESO_ALWAYS_CREATE_MSG_EVT));
		f_createCommLinks = "1".equals(f_config.getProperty(Constants.CONF_LANESO_CREATE_EXTRA_COMM_LINKS));
		//Splitting of selected Lane as a new Pool
		ProcessEditor _pe = f_wb.getSelectedProcessEditor();
		Lane _lane = (Lane)_pe.getSelectionHandler().getLastSelectedNode();
		BPMNModel _model = (BPMNModel) _pe.getModel();
		if(_lane != null) {
			List<ProcessNode> _containedNodes = _lane.getProcessNodesRecursivly();
			_model.removeNode(_lane);
			Pool _newPool = new Pool(_lane.getPos().x,_lane.getPos().y,_lane.getName());
			_model.addNode(_newPool);
			_model.moveToBack(_newPool);
			for(ProcessNode n:_containedNodes) {
				_newPool.addProcessNode(n);
			}
	
			for(ProcessEdge e:new ArrayList<ProcessEdge>(_model.getEdges())) {
				if(e instanceof SequenceFlow) {
					if(_containedNodes.contains(e.getSource()) != _containedNodes.contains(e.getTarget())) {
						transformToMessageFlow(_model,e);
					}
				}
			}
			if(f_createCommLinks && f_commLinks != null) {
				//now we add extra communication links if the user wished this
				for(ProcessNode p:f_commLinks.keySet()) {
					if(_containedNodes.contains(p)) {
						buildExtraCommLink(_model,p,getClusterByName(_model,f_commLinks.get(p)));
					}
				}
			}
			
			//TODO Test was sort Clusters
			ProcessUtils.sortTopologically(_model);
			layoutModel(_pe);	
		}
	}
	
	/**
	 * @param model 
	 * @param p
	 * @param clusterByName
	 */
	private void buildExtraCommLink(BPMNModel model, ProcessNode p, Cluster c) {
		ProcessNode _pred = findPredecessorInCluster(model, c, p);
		if(_pred == null) return;
		if(_pred instanceof EndEvent) {
			if(_pred instanceof MessageEndEvent) {
				try {
					_pred = ProcessUtils.refactorNode(model, _pred, MessageIntermediateEvent.class);
					_pred.setProperty(MessageIntermediateEvent.PROP_EVENT_SUBTYPE, MessageIntermediateEvent.EVENT_SUBTYPE_THROWING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//creating new event after _pred
		MessageIntermediateEvent _mie = null;
		if(f_extraCommLinkNodeCache.containsKey(_pred)) {
			_mie = f_extraCommLinkNodeCache.get(_pred);
		}else {
			_mie = new MessageIntermediateEvent();
			_mie.setProperty(MessageIntermediateEvent.PROP_EVENT_SUBTYPE, MessageIntermediateEvent.EVENT_SUBTYPE_CATCHING);
			c.addProcessNode(_mie);
			SequenceFlow _sqf = new SequenceFlow(_pred,_mie);
			model.addNode(_mie);
			model.addFlow(_sqf);
			f_extraCommLinkNodeCache.put(_pred, _mie);
			//changing flows (should be only 1)		
			int _sCount = 0;
			List<ProcessEdge> _succs = model.getOutgoingEdges(SequenceFlow.class, _pred);
			for(ProcessEdge sq :_succs) {
				if(sq.getTarget() != _mie) {
					sq.setSource(_mie);	
					_sCount++;
				}
			}
			//build an end event if necessary following our new message event
			if(_sCount == 0) {
				EndEvent _ee = new EndEvent();
				model.addNode(_ee);
				c.addProcessNode(_ee);
				SequenceFlow _sqf2 = new SequenceFlow(_mie,_ee);
				model.addFlow(_sqf2);
			}
		}			
		//connecting the nodes
		p.setStereotype(Task.TYPE_SEND);
		MessageFlow _msf = new MessageFlow(p,_mie);
		model.addFlow(_msf);
	}

	/**
	 * @param model 
	 * @param p
	 * @return
	 */
	private Cluster getClusterByName(BPMNModel model, String name) {
		for(ProcessNode n:model.getNodes()) {
			if(n instanceof Cluster && n.getText().equals(name)) {
				return (Cluster)n; 
			}
		}
		return null;
	}

	/**
	 * @param model
	 * @param e
	 */
	private void transformToMessageFlow(BPMNModel model, ProcessEdge e) {
		model.removeEdge(e);
		ProcessNode _source = e.getSource();
		ProcessNode _target = e.getTarget();
		ProcessNode _start = null;
		ProcessNode _end = null;
		
		//can we connect the source node to someone later on?
		Cluster _startLane = model.getClusterForNode(_source);
		List<ProcessNode> _connectMeFwd = findSuccessorsInCluster(model,_startLane,_target);	
		boolean _ee = _connectMeFwd.size() == 0; //if none was found, create end events (ee)

		_startLane = model.getClusterForNode(_target);
		ProcessNode _connectMeBwd = findPredecessorInCluster(model,_startLane,_source);	
		boolean _se = _connectMeBwd == null; //if after removing e no edge is left we need a start event
				
		if(f_createMsgEvt || !(_source instanceof Task) || ((Task)_source).getStereotype().length()>0 || _ee) {
			_start = _ee? new MessageEndEvent() : new MessageIntermediateEvent();
			if(!_ee) _start.setProperty(MessageIntermediateEvent.PROP_EVENT_SUBTYPE, MessageIntermediateEvent.EVENT_SUBTYPE_THROWING);
			model.addNode(_start);
			model.getClusterForNode(_source).addProcessNode(_start);
			_start.setPos(_source.getPos());
			SequenceFlow _startFlow = new SequenceFlow(_source,_start);
			model.addFlow(_startFlow);		
		}else {
			_source.setStereotype(Task.TYPE_SEND);
			_start = _source;			
		}
			
		if(f_createMsgEvt || !(_target instanceof Task)  || ((Task)_target).getStereotype().length()>0 || _se) {
			_end = _se ? new MessageStartEvent() : new MessageIntermediateEvent();
			model.addNode(_end);
			model.getClusterForNode(_target).addProcessNode(_end);
			_end.setPos(_target.getPos());
			SequenceFlow _endFlow = new SequenceFlow(_end,_target);
			//changing connection of all other incoming flows before adding new flow
			for(ProcessEdge inEdge:model.getPreceedingEdges(_target)){
				if(f_newEdges.contains(inEdge)) {
					inEdge.setTarget(_end);
				}
			}			
			model.addFlow(_endFlow);
		}else {
			_target.setStereotype(Task.TYPE_RECEIVE);
			_end = _target;
		}
		MessageFlow _msf = new MessageFlow(_start,_end);
		model.addFlow(_msf);	
		//keeping the process model connected
		if(_connectMeFwd.size() > 0) {
			if(_connectMeFwd.size() <= 1) {
				SequenceFlow _newSqf = new SequenceFlow(_start,_connectMeFwd.get(0));
				model.addFlow(_newSqf);
				f_newEdges.add(_newSqf);
			}else {
				Gateway _gate = new ExclusiveGateway();
				_gate.setPos(_start.getPos());
				model.addNode(_gate);
				model.getClusterForNode(_start).addProcessNode(_gate);
				SequenceFlow _newSqf = new SequenceFlow(_start,_gate);
				model.addFlow(_newSqf);
				f_newEdges.add(_newSqf);
				for(ProcessNode fwd:_connectMeFwd) {
					_newSqf = new SequenceFlow(_gate,fwd);
					model.addFlow(_newSqf);
					f_newEdges.add(_newSqf);
				}
			}
		}
//		if(_connectMeBwd != null) {
//			SequenceFlow _newSqf = new SequenceFlow(_connectMeBwd,_end);
//			model.addFlow(_newSqf);
//			f_newEdges.add(_newSqf);
//		}
	}

	/**
	 * @param model
	 * @param lane
	 * @param _source
	 * @return
	 */
	private ProcessNode findPredecessorInCluster(BPMNModel model, Cluster cl, ProcessNode source) {
		if(cl != null) {
			ArrayList<ProcessNode> _toCheck = new ArrayList<ProcessNode>();
			ArrayList<ProcessNode> _visited = new ArrayList<ProcessNode>();
			_toCheck.addAll(model.getPredecessors(source));
			for(int i=0;i<_toCheck.size();i++) {
				ProcessNode n = _toCheck.get(i);
				_visited.add(n);
				if(cl.getProcessNodes().contains(n)) {
					return n;
				}else {
					for(ProcessNode suc:model.getPredecessors(n)) {
						if(!_visited.contains(suc))
							_toCheck.add(suc);
					}
					
				}
			}
		}
		return null;
	}

	/**
	 * find the a successor of target that lies in the Cluster cl.
	 * It uses breadth-first search to find the closest node that fulfills this property.
	 * @param cl
	 * @param _target
	 * @return
	 */
	private List<ProcessNode> findSuccessorsInCluster(BPMNModel model,Cluster cl, ProcessNode target) {
		ArrayList<ProcessNode> _result = new ArrayList<ProcessNode>();
		ArrayList<ProcessNode> _toCheck = new ArrayList<ProcessNode>();
		ArrayList<ProcessNode> _visited = new ArrayList<ProcessNode>();
		_toCheck.addAll(model.getSuccessors(target));
		for(int i=0;i<_toCheck.size();i++) {
			ProcessNode n = _toCheck.get(i);
			_visited.add(n);
			if(cl.getProcessNodes().contains(n)) {
				if(!_result.contains(n)) {
					_result.add(n);
				}
			}else {
				for(ProcessNode suc:model.getSuccessors(n)) {
					if(!_visited.contains(suc))
						_toCheck.add(suc);
				}
			}
		}
		return _result;
	}

	private void layoutModel(ProcessEditor _result) {
		GridLayouter _layouter = new GridLayouter(Configuration.getProperties());
		try {
			LayoutingAnimator _anim = new LayoutingAnimator(_layouter);
			_anim.layoutModelWithAnimation(_result,null,0,0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void newEditorCreated(ProcessEditor editor) {
		editor.addCustomContextMenuItem(Lane.class, f_item);
	}

	@Override
	public void selectedProcessEditorChanged(ProcessEditor editor) {
		//we do not care
	}

	/**
	 * Adds links which should be kept in mind when splitting apart two lanes
	 * @param commLinks
	 */
	public void setCommLinks(Map<ProcessNode, String> commLinks) {
		f_commLinks = commLinks;
	}

}
