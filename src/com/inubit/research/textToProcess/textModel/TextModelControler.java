/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.textModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.ProcessUtils;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.textToProcess.SentenceWordID;
import com.inubit.research.textToProcess.TextToProcess;
import com.inubit.research.textToProcess.transform.AnalyzedSentence;
import com.inubit.research.textToProcess.transform.DummyAction;
import com.inubit.research.textToProcess.transform.TextAnalyzer;
import com.inubit.research.textToProcess.transform.TextModelBuilder;
import com.inubit.research.textToProcess.worldModel.Action;
import com.inubit.research.textToProcess.worldModel.Actor;
import com.inubit.research.textToProcess.worldModel.ExtractedObject;
import com.inubit.research.textToProcess.worldModel.Resource;
import com.inubit.research.textToProcess.worldModel.SpecifiedElement;
import com.inubit.research.textToProcess.worldModel.Specifier;
import com.inubit.research.textToProcess.worldModel.Specifier.SpecifierType;

/**
 * @author ff
 *
 */
public class TextModelControler extends ProcessUtils implements ProcessEditorListener, ProcessModelListener  {

	
	private TextAnalyzer f_analyzer;
	private TextModel f_model;
	private TextModelBuilder f_builder;
	
	private ArrayList<ProcessNode> f_highlightCache = new ArrayList<ProcessNode>();
	private TextEdge f_edge;
	private TextToProcess f_parent;
	private TextToProcess f_processor;
	private boolean f_showRefs = true;
	private boolean f_showLinks = true;
	
	
	public void setTextToprocess(TextToProcess parent) {
		f_parent = parent;	
	}
	
	@Override
	public void modelChanged(ProcessModel m) {
		
	}

	@Override
	public void processNodeEditingFinished(ProcessNode o) {
	}

	@Override
	public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
	}

	@Override
	public void processObjectClicked(ProcessObject o) {
		resetColors();
		if(o instanceof ProcessNode) {
			if(o instanceof WordNode) {
				SpecifiedElement _element = getElement((ProcessNode)o);
				if(_element != null) {
					highlightAll();
					if(f_parent != null) {
						f_parent.textElementClicked(_element);
					}
				}
			}else if(o instanceof SentenceNode) {
				SentenceNode _sn = (SentenceNode) o;
				highlightComponents(_sn);
			}			
		}
		
		
	}

	/**
	 * 
	 */
	private void highlightAll() {
		for(Actor ac:f_analyzer.getWorld().getActors()) {
			highlightElement(f_builder.getSentenceNode(ac.getOrigin()), getColorActor(0), ac,false);
		}
		for(Resource ac:f_analyzer.getWorld().getResources()) {
			highlightElement(f_builder.getSentenceNode(ac.getOrigin()), getColorObject(0), ac,false);
		}
		for(Action ac:f_analyzer.getWorld().getActions()) {
			if(!(ac instanceof DummyAction)) {
				highlightAction(f_builder.getSentenceNode(ac.getOrigin()), getColorAction(0), ac,false);
			}
		}
	}

	/**
	 * 
	 */
	private void resetColors() {
		for(ProcessNode pn:f_highlightCache) {
			pn.setBackground(Color.WHITE);
		}
		f_highlightCache.clear();
	}

	/**
	 * @param _sn
	 */
	private void highlightComponents(SentenceNode _sn) {
		AnalyzedSentence _sentence = f_analyzer.getAnalyzedSentence(_sn.getIndex());
		List<Action> _actions = _sentence.getExtractedActions();
		for(int i=0;i<_actions.size();i++) {
			Action _a =_actions.get(i);
			highlightAction(_sn, getColorAction(i), _a,true);
			highlightElement(_sn, getColorActor(i), _a.getActorFrom(),true);
			Color _cObj = _a.getObject() instanceof Actor ? getColorActor(i) : getColorObject(i);
			highlightElement(_sn, _cObj, _a.getObject(),true);
		}
	}

	private void highlightElement(SentenceNode _sn, Color c, ExtractedObject a,boolean highlightDependants) {
		//highlighting Element
		if(a != null) {
			if(highlightDependants) {
				ProcessNode _pn = getElement(_sn,a.getDeterminer(),a.getWordIndex()-1);
				if(_pn != null) {
					_pn.setBackground(c);
					f_highlightCache.add(_pn);
				}			
			}
			highlightSpecifiedElement(_sn, c, a,highlightDependants);
		}
	}
	
	public void highlightAction (Action a) {
		resetColors();
		SentenceNode _sn = f_builder.getSentenceNode(a.getOrigin());
		highlightAction(_sn, getColorAction(0), a, true );
	}
	
	private void highlightAction(SentenceNode _sn, Color c, Action a,boolean highlightDependants) {
		//highlighting Element
		if(a != null) {
			if(highlightDependants) {
				ProcessNode _pn = getElement(_sn,a.getAux(),a.getWordIndex()-1);
				if(_pn != null) {
					_pn.setBackground(c);
					f_highlightCache.add(_pn);
				}	
				_pn = getElement(_sn,a.getCop(),a.getWordIndex()-1);
				if(_pn != null) {
					_pn.setBackground(c);
					f_highlightCache.add(_pn);
				}
				_pn = getElement(_sn,a.getPrt(),a.getWordIndex()-1);
				if(_pn != null) {
					_pn.setBackground(c);
					f_highlightCache.add(_pn);
				}
				_pn = getElement(_sn,a.getMod(),a.getModPos());
				if(_pn != null) {
					_pn.setBackground(c);
					f_highlightCache.add(_pn);
				}
				if(a.getXcomp() != null) {
					highlightAction(_sn, getColorAction(4), a.getXcomp(),highlightDependants);
				}
			}
			highlightSpecifiedElement(_sn, c, a,highlightDependants);
		}
	}
	

	private void highlightSpecifiedElement(SentenceNode _sn, Color c, SpecifiedElement a,boolean highlightDependants) {
		ProcessNode _pn;
		
		_pn = _sn.getProcessNodes().get(a.getWordIndex()-1);
		_pn.setBackground(c);
		f_highlightCache.add(_pn);			
		
		if(highlightDependants) {
			List<Specifier> _hglt = new ArrayList<Specifier>(a.getSpecifiers());
			_hglt.removeAll(a.getSpecifiers(SpecifierType.SBAR)); //do not highlight those
			for(Specifier sp:_hglt) {
				if(sp.getWordIndex() >= _sn.getProcessNodes().size()) {
					System.out.println("error");
				}
				for(String str:sp.getName().split(" ")) {
					_pn = getElement(_sn,str,sp.getWordIndex()-1);
					if(_pn == null) {
						System.err.println("error! Could not find node for: "+str);
					}else {
						_pn.setBackground(c);
						f_highlightCache.add(_pn);
					}
				}
			}
		}
	}

	/**
	 * searches for an object with the given string in the given sentence
	 * index is a startPosition
	 * @param _sn
	 * @param determiner
	 * @param wordIndex
	 * @return
	 */
	private ProcessNode getElement(SentenceNode sn, String name,
			int index) {
		if(name != null) {

			name = name.replaceAll(",", "");
			name = name.replaceAll("\\(", "");
			name = name.replaceAll("\\)", "");
			name = name.replaceAll("\\$", "");
			name = name.replaceAll("\\/", "\\\\\\/");
			List<ProcessNode> _nodes = sn.getProcessNodes();
			for(int i=0;i<_nodes.size()-1;i++) {
				int idx = index-i;
				if(idx >= 0) {
					//backwards
					ProcessNode pn = sn.getProcessNodes().get(idx);
					if(pn.getText().equalsIgnoreCase(name)) {
						return pn;
					}
				}
				idx = index+i;
				if(idx < _nodes.size() && idx >= 0) {
					//forward
					ProcessNode pn = _nodes.get(idx);
					if(pn.getText().equalsIgnoreCase(name)) {
						return pn;
					}
				}
			}
		}
		return null;
	}

	/**
	 * produces greenish pastel colors
	 * @param i
	 * @return
	 */
	public static Color getColorActor(int i) {
		int val1 = Math.min(255, 225+i*8);
		int val2 = Math.min(255,168+i*22);
		return new Color(val1,val1,val2);
	}
	
	/**
	 * produces greenish pastel colors
	 * @param i
	 * @return
	 */
	public static Color getColorObject(int i) {
		int val1 = Math.min(255, 144+i*14);
		int val2 = Math.min(255, 255);
		return new Color(val1,val1,val2);
	}
	
	/**
	 * produces greenish pastel colors
	 * @param i
	 * @return
	 */
	public static Color getColorAction(int i) {
		int val1 = Math.min(255, 187+i*9);
		int val2 = Math.min(255, 125+i*17);
		return new Color(255,val1,val2);
	}

	/**
	 * @param o
	 * @return
	 */
	private SpecifiedElement getElement(ProcessNode o) {
		Cluster _cluster = f_model.getClusterForNode(o);
		if(_cluster instanceof SentenceNode) {
			SentenceNode _sn = (SentenceNode) _cluster;
			int index = _sn.getProcessNodes().indexOf(o)+1;
			AnalyzedSentence _origin = f_analyzer.getAnalyzedSentence(_sn.getIndex());
			for(Action ac:_origin.getExtractedActions()) {
				//checking the Action
				SpecifiedElement _result = checkContainmentOfIndex(index, ac);
				if(_result != null) {
					return _result;
				}
				//checking the xcomp
				_result = checkContainmentOfIndex(index, ac.getXcomp());
				if(_result != null) {
					return _result;
				}
				//checking actor
				_result = checkContainmentOfIndex(index, ac.getActorFrom());
				if(_result != null) {
					return _result;
				}
				//checking object
				_result = checkContainmentOfIndex(index, ac.getObject());
				if(_result != null) {
					return _result;
				}
			}
		}
		return null;
	}

	private SpecifiedElement checkContainmentOfIndex(int index, SpecifiedElement elem) {
		if(elem != null) {
			if(elem.getWordIndex() == index) {
				return elem;
			}
			for(Specifier sp:elem.getSpecifiers()) {
				if(sp.getType() != SpecifierType.SBAR) {
					if(sp.getWordIndex() == index) {
						if(sp.getObject() == null) {
							return elem;
						}else {
							return sp.getObject();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void processObjectDoubleClicked(ProcessObject o) {
	}

	@Override
	public void processObjectDragged(Dragable o, int oldX, int oldY) {
		System.out.println(o);
	}

	/**
	 * @param pgui 
	 * @param world
	 * @param _model
	 */
	public void setModels(TextToProcess processor,TextAnalyzer analyzer, TextModelBuilder builder, TextModel model) {
		f_analyzer = analyzer;
		f_builder = builder;
		f_model = model;
		f_model.setUtils(this);
		f_model.addListener(this);		
		f_processor = processor;
		setShowLinks(model, f_showLinks);
		setShowReferences(model, f_showRefs);
	}

	@Override
	public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
		SpecifiedElement _spec = getElement(source);
		if(_spec instanceof ExtractedObject) {
			ExtractedObject _obj = (ExtractedObject) _spec;
			if(_obj.needsResolve()) {
				_spec = getElement(target);
				if(_spec instanceof ExtractedObject) {
					f_edge = new TextEdge();
					f_edge.setColor(TextModelBuilder.COLOR_REFERENCE_EDGES);
					f_edge.setAlpha(TextModelBuilder.DEFAULT_EDGE_ALPHA);
					f_edge.setSource(source);
					f_edge.setTarget(target);
					return f_edge;
				}
			}
		}
		return null;
	}

	@Override
	public List<ProcessLayouter> getLayouters() {
		return new ArrayList<ProcessLayouter>(0);
	}

	@Override
	public void processEdgeAdded(ProcessEdge edge) {
		addReferenceToTextAnalyzer(edge);		
		//only possibility, an edge was added by our reference repointing
		if(f_edge != null) {
			for(ProcessEdge e:new ArrayList<ProcessEdge>(f_model.getEdges())) {
				if(e != f_edge) {
					if(e.getSource().equals(f_edge.getSource())) {
						f_model.removeEdge(e);
					}
				}
			}
		}
		f_processor.analyzeText(true); //rebuild process model
	}

	/**
	 * @param edge
	 */
	private void addReferenceToTextAnalyzer(ProcessEdge edge) {
		SpecifiedElement _from = getElement(edge.getSource());
		SpecifiedElement _to = getElement(edge.getTarget());
		f_processor.addManualReferenceResolution(new SentenceWordID(_from),new SentenceWordID(_to));
	}

	@Override
	public void processEdgeRemoved(ProcessEdge edge) {
	}

	@Override
	public void processNodeAdded(ProcessNode newNode) {
	}

	@Override
	public void processNodeRemoved(ProcessNode remNode) {
	}

	@Override
	public void processObjectPropertyChange(ProcessObject obj, String name,
			String oldValue, String newValue) {
	}
	
	public void setShowLinks(ProcessModel model, boolean selected) {
		f_showLinks = selected;
    	for(ProcessEdge edge:model.getEdges()){
            if(edge instanceof TextLinkEdge){
                edge.setAlpha(selected ? TextModelBuilder.DEFAULT_EDGE_ALPHA: 0.0f);
            }
        }
    }

    public void setShowReferences(ProcessModel model, boolean selected) {
    	f_showRefs = selected;
    	for(ProcessEdge edge:model.getEdges()){
           if(!(edge instanceof TextLinkEdge)){
               edge.setAlpha(selected ? TextModelBuilder.DEFAULT_EDGE_ALPHA: 0.0f);
           }
    	}
    }


}
