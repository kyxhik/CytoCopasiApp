package org.cytoscape.CytoCopasiApp.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import org.COPASI.*;
import org.cytoscape.CytoCopasiApp.AttributeUtil;
import org.cytoscape.CytoCopasiApp.CyActivator;
import org.cytoscape.CytoCopasiApp.MyCopasiPanel;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;



public class CopasiFileReaderTask extends AbstractTask implements CyNetworkReader {

    private final String fileName;
    private final InputStream stream;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkViewFactory viewFactory;
    private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
    
    VisualStyle visStyle;
    String styleName;
   
    
    //private final VisualMappingManager vmm;
    private VisualStyle visualStyle;
    //private final VisualStyleFactory visualStyleFactory;

    private LinkedList<CyNetwork> cyNetworks;
    
    private TaskMonitor taskMonitor;
    private File visFile = null;
    private File visFile2 = null;
    private String styleFileCopasi = "/home/people/hkaya/CytoscapeConfiguration/app-data/CytoCopasi/logs/cy3Copasi.xml";
    private String styleFileSbml = "/home/people/hkaya/CytoscapeConfiguration/app-data/CytoCopasi/logs/cy3sbml.xml";
    private Map<String, CyNode> id2Node;      // node dictionary
    private Boolean error = false;
    CyNode n;


    /**
     * Constructor
     */
    public CopasiFileReaderTask(InputStream stream, String fileName,
                          CyNetworkFactory networkFactory,
                          CyNetworkViewFactory viewFactory,
                          CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {

        this.stream = stream;
        this.fileName = fileName;
        this.networkFactory = networkFactory;
        this.viewFactory = viewFactory;
        this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
        
        //this.visualMappingManager = visualMappingManager;
       // this.visualStyleFactory = visualStyleFactory;
        // networks returned by the reader
        cyNetworks = new LinkedList<>();
        

		
    }
    
    public static void main(String[] args) {
    	System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
        
        
     }
  
    @Override
    public CyNetwork[] getNetworks() {
        return cyNetworks.toArray(new CyNetwork[cyNetworks.size()]);
    }
    
    

   
    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {
        // Create view
    	
	
        CyNetworkView view = viewFactory.createNetworkView(network);

	    styleName = "cy3Sbml";
			
       
       
      
        
     //   CyActivator.netMgr.destroyNetwork(network);
      
        // layout
        if (cyLayoutAlgorithmManager != null) {
            CyLayoutAlgorithm layout = cyLayoutAlgorithmManager.getLayout("hierarchical");
            if (layout == null) {
                layout = cyLayoutAlgorithmManager.getLayout(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
            }
            TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
            Task nextTask = itr.next();
            try {
                nextTask.run(taskMonitor);
            } catch (Exception e) {
                throw new RuntimeException("Could not finish layout", e);
            }
        }

        // finished
        return view;
    }
    
    
    public Boolean getError() {
        return error;
    }
    
   
    
    
    
    @Override
    public void cancel() {
    }
    
private static final int BUFFER_SIZE = 16384;
    
    public static String inputStream2String(InputStream source) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        try {
            char[] buffer = new char[BUFFER_SIZE];
            int charactersRead = reader.read(buffer, 0, buffer.length);
            while (charactersRead != -1) {
                writer.write(buffer, 0, charactersRead);
                charactersRead = reader.read(buffer, 0, buffer.length);
            }
        } finally {
            reader.close();
        }
        return writer.toString();
    }
    
    
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        this.taskMonitor = taskMonitor;
        try {
            if (taskMonitor != null) {
                taskMonitor.setTitle("copasi reader");
                taskMonitor.setProgress(0.0);
            }
            if (cancelled) {
                return;
            }
            ParsingReportGenerator.getInstance().appendLine("java.library.path: " + System.getProperty("java.library.path"));
            File nativeLib = CyActivator.getNativeLib();
            File nativeLibMac = CyActivator.getNativeLibMac();
            File nativeLibWindows = CyActivator.getNativeLibWindows();
            
            String dests = System.getProperty("java.library.path");
            String[] destsList = dests.split(":");
            for (int i = 0; i< destsList.length ; i++) {
            	if (destsList[i].contains("framework")) {
            		System.out.println(destsList[i]);
            		System.out.println(System.getProperty("os.name"));
            		File dest = new File(destsList[i]+ "/libCopasiJava.so");
            		File destMac = new File(destsList[i]+"/libCopasiJava.jnilib");
            		File destWindows = new File(destsList[i]+"\'CopasiJava.dll");
                    Files.copy(nativeLib.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(nativeLibMac.toPath(), destMac.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(nativeLibWindows.toPath(), destWindows.toPath(), StandardCopyOption.REPLACE_EXISTING);

            	}
            
            }
           String xml = inputStream2String(stream);
           id2Node = new HashMap<>();

           String modelName = new Scanner(CyActivator.getReportFile(1)).next();
           CDataModel dm  = CRootContainer.addDatamodel();
           //if (modelName.endsWith(".cps")) {
           dm.loadFromString(xml);
           //} else if (modelName.endsWith(".xml")) {
           //dm.importSBML(modelName);
     //   }
           
           CyNetwork network = readModelInNetwork(dm.getModel(), xml);
           addAllNetworks(network);
           
           
           CRootContainer.removeDatamodel(dm);
          
         
        }
        catch (Exception e) {
        	
        }
        
    }
        
    
    private CyNetwork readModelInNetwork(CModel model, String xml) {
    	
    	CyNetwork network = networkFactory.createNetwork();
    	
    	
    	readCore(network, model, xml);
    	
    	
    	if (taskMonitor !=null) {
    		taskMonitor.setProgress(0.4);
    	}
    	return network;
    }
    
    
    private void addAllNetworks(CyNetwork network){

        // root network
        CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();
        String name = getNetworkName(network);
  
        //String name = AttributeUtil.get(network, network, SBML.ATTR_ID, String.class);
        rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, String.format("%s", name));

        // all network
        network.getRow(network).set(CyNetwork.NAME, String.format("%s: %s", "ALL", name));


        // add the networks to the created networks
        cyNetworks.add(network);
    }
    
    private String getNetworkName(CyNetwork network) {
        // name of root network
        String name = network.getRow(network).get("id", String.class);
        if (name == null) {
            String[] tokens = fileName.split(File.separator);
            name = tokens[tokens.length - 1];
        }
        return name;
    }

    private static void setAttributes(CyNetwork network, CyIdentifiable n, CDataObject obj) {

    	AttributeUtil.set(network, n, "id", obj.getKey(), String.class);
    	AttributeUtil.set(network, n, "cn", obj.getCN().getString(), String.class);
    	AttributeUtil.set(network, n, "name", obj.getObjectName(), String.class);
    	AttributeUtil.set(network, n, "display name", obj.getObjectDisplayName(), String.class);    	
    	
    }
    
    private CyNode createNode(CyNetwork network, CDataObject obj, String type, String xml) 
    {
        CyNode n = network.addNode();
		
        // Set attributes
        if (xml.contains("http://www.copasi.org/static/schema")) {
        AttributeUtil.set(network, n, "type", type, String.class);     
        setAttributes(network, n, obj);
        } else{
        	AttributeUtil.set(network, n, "sbml type", type, String.class);   
			
            setAttributes(network, n, obj);
        }
        
        id2Node.put(obj.getCN().getString(), n);
    
        return n;
    }
     
     void readCore(CyNetwork network ,CModel model, String xml) {
         String modelName;
		try {
			modelName = new Scanner(CyActivator.getReportFile(1)).next();
		
        // SBMLDocument & Model //
        // Mark network as SBML
        AttributeUtil.set(network, network, "copasiNetwork", "copasi", String.class);
        AttributeUtil.set(network, network, "copasiVersion", CVersion.getVERSION().getVersion(), String.class);

        setAttributes(network, network, model);

        
        
     // Parameter //
    /*    for(int i = 0; i < model.getNumModelValues(); i++) {
			CModelValue parameter = model.getModelValue(i);       		
            CyNode n = createNode(network, parameter, "parameter", xml);
            AttributeUtil.set(network, n, "value", (double)parameter.getInitialValue(), Double.class);
            
            
        }*/
        
     // Species //
        for(int i = 0; i < model.getNumMetabs(); i++) {
			CMetab species = model.getMetabolite(i);       		
            CyNode n = createNode(network, species, "species", xml);

            // edge to compartment
            CCompartment comp = species.getCompartment();
            AttributeUtil.set(network, n, "compartment", comp.getObjectName(), String.class);
            
            if (xml.contains("http://www.copasi.org/static/schema")) {
            AttributeUtil.set(network, n, "initial concentration", species.getInitialConcentration(), Double.class);
            } else {
            AttributeUtil.set(network, n, "sbml initial concentration", species.getInitialConcentration(), Double.class);

            }
            int getMetabStatus = species.getStatus();
            switch (getMetabStatus) {
			case CModelEntity.Status_ASSIGNMENT :
	            AttributeUtil.set(network, n, "status", "Assignment", String.class);
				break;
			case CModelEntity.Status_FIXED :
	            AttributeUtil.set(network, n, "status", "Fixed", String.class);
				break;
			case CModelEntity.Status_ODE:
	            AttributeUtil.set(network, n, "status", "ODE", String.class);
				break;
			case CModelEntity.Status_REACTIONS:
	            AttributeUtil.set(network, n, "status", "Reactions", String.class);
				break;
			case CModelEntity.Status_TIME:
	            AttributeUtil.set(network, n, "status", "Time", String.class);
				break;
			}
            
        }
    
        for(int i = 0; i < model.getNumReactions(); i++) {
        	CReaction reaction = model.getReaction(i);
        	
        	if (reaction.isReversible()) {
            n = createNode(network, reaction, "reaction rev", xml);
            AttributeUtil.set(network, n, "reversible", reaction.isReversible(), Boolean.class);
        	} else if (!reaction.isReversible()) {
        	n = createNode(network, reaction, "reaction irrev", xml);
        	AttributeUtil.set(network, n, "reversible", reaction.isReversible(), Boolean.class);
        	}
            

    		CChemEq eqn = reaction.getChemEq();
    		
    		CFunction rateLaw = reaction.getFunction();
    		
    	
    		
    		//CyNode reactionNode = id2Node.get(eqn.toString());
    		AttributeUtil.set(network, n, "Chemical Equation", reaction.getReactionScheme() , String.class);
    		AttributeUtil.set(network, n, "Rate Law", rateLaw.getObjectName(), String.class);
    		AttributeUtil.set(network, n, "Rate Law Formula", rateLaw.getInfix(), String.class);
    		
    	
    		int numSubstrates = (int) eqn.getSubstrates().size();
    		int numProducts = (int)eqn.getProducts().size();
    		int numModifiers = (int) eqn.getModifiers().size();
    		int numParameters = (int) reaction.getParameters().size();
    		StringJoiner joiner = new StringJoiner(", ");
    		StringJoiner joiner2 = new StringJoiner(", ");
    		StringJoiner joiner3 = new StringJoiner(", ");
    		StringJoiner joiner4 = new StringJoiner(", ");
    		
    		StringJoiner units1 = new StringJoiner(", ");
    		
    		String[] substrates = new String[numSubstrates];
    		String[] unitsSub = new String[numSubstrates];
    		String[] products = new String[numProducts];
    		String[] modifiers = new String[numModifiers];
    		String[] parameters = new String[numParameters];
    		
    		// Reactants
            for (int j = 0; j < numSubstrates; j++) {
            	CChemEqElement el = eqn.getSubstrate(j);
            	
            	String cn = el.getMetabolite().getCN().getString();
                CyNode reactantNode = id2Node.get(cn);
                substrates[j] = eqn.getSubstrate(j).getMetabolite().getObjectName();
                unitsSub[j] = eqn.getSubstrate(j).getUnits().toString();
               
                joiner.add(substrates[j]);
                CyEdge edge = createEdge(network, reactantNode, n, "reaction");
              //  AttributeUtil.set(network, n, "Substrates", substrates, String.class);
                
                Double stoichiometry = el.getMultiplicity();
                AttributeUtil.set(network, edge, "stoichiometry", stoichiometry, Double.class);
                //AttributeUtil.set(network, edge, "Substrate"+"_"+(j+1), el.getMetabolite().getObjectName(), String.class);
            }
            String subStr = joiner.toString();
            String unitSubStr = unitsSub.toString();
            AttributeUtil.set(network, n, "substrates", subStr, String.class);
            AttributeUtil.set(network, n, "substrate units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
            
            
         // Products
            for (int j = 0; j < numProducts; j++) {
            	CChemEqElement el = eqn.getProduct(j);
            	String cn = el.getMetabolite().getCN().getString();
                CyNode reactantNode = id2Node.get(cn);
                //AttributeUtil.set(network, n, "Product"+"_"+(j+1), el.getMetabolite().getObjectName(), String.class);
            	products[j] = eqn.getProduct(j).getMetabolite().getObjectName();
            	joiner2.add(products[j]);
                CyEdge edge = createEdge(network, n, reactantNode, "reaction");

                Double stoichiometry = el.getMultiplicity();
                AttributeUtil.set(network, edge, "stoichiometry", stoichiometry, Double.class);
            }
            
            String subPro = joiner2.toString();
            AttributeUtil.set(network, n, "products", subPro, String.class);
            AttributeUtil.set(network, n, "product units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
            for (int j = 0; j< numModifiers; j++) {
            	CChemEqElement el = eqn.getModifier(j);
            	String cn = el.getMetabolite().getCN().getString();
                CyNode reactantNode = id2Node.get(cn);
                modifiers[j] = eqn.getModifier(j).getMetabolite().getObjectName();
                joiner3.add(modifiers[j]);
                CyEdge edge = createEdge(network, n, reactantNode, "reaction-inhibitor");
            }
            
            String subMod = joiner3.toString();
            AttributeUtil.set(network, n, "modifiers", subMod, String.class);
            AttributeUtil.set(network, n, "modifier units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
            
            for (int j = 0 ; j<numParameters; j++) {
                //AttributeUtil.set(network, n, "Product"+"_"+(j+1), el.getMetabolite().getObjectName(), String.class);
            	//products[j] = eqn.getProduct(j).getMetabolite().getObjectName();
            	parameters[j] = reaction.getParameters().getParameter(j).getObjectName();
            	joiner4.add(parameters[j]);
            }
            String subPar =  joiner4.toString();
            AttributeUtil.set(network, n, "parameters", subPar, String.class);
            	CCopasiParameterGroup parameterGroup = reaction.getParameters();
            	
            for (int j=0 ; j< parameterGroup.size(); j++) {
            	CCopasiParameter parameter = parameterGroup.getParameter(j);
            
            	AttributeUtil.set(network, n, parameter.getObjectName(), parameter.getDblValue(), Double.class);
            }
            //
           

    } 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

           
    
}
    
    
    private CyEdge createEdge(CyNetwork network, CyNode source, CyNode target, String interactionType) {
        CyEdge e = network.addEdge(source, target, true);
        AttributeUtil.set(network, e, "type", interactionType, String.class);
        return e;
    }
    
    public void setSBMLTable(CyNetwork network) {
        String modelName;
		try {
			modelName = new Scanner(CyActivator.getReportFile(1)).next();
			CDataModel dm = CRootContainer.addDatamodel();
			if (modelName.contains(".xml")) {
	    	try {
				dm.importSBML(modelName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	CModel model = dm.getModel();
	    	
	    	int nodeCount = network.getNodeCount();
	    for (int b = 0; b < nodeCount ; b++) {
	    	CyNode node = network.getNodeList().get(b);
	    	if (AttributeUtil.get(network, node, "sbml type", String.class).equals("reaction")) {
	    	
	        	CReaction reaction = model.getReaction(AttributeUtil.get(network, node, "name", String.class));
	        	
	        	
	        	if (reaction.isReversible()) {
		        AttributeUtil.set(network, node, "sbml type", "reaction rev", String.class);

	            AttributeUtil.set(network, node, "reversible", reaction.isReversible(), Boolean.class);
	        	} else if (!reaction.isReversible()) {
			        AttributeUtil.set(network, node, "sbml type", "reaction irrev", String.class);
	        	AttributeUtil.set(network, node, "reversible", reaction.isReversible(), Boolean.class);
	        	}
	        	
	        	CChemEq eqn = reaction.getChemEq();
	    		
	    		CFunction rateLaw = reaction.getFunction();
	    		
	    	
	    		
	    		//CyNode reactionNode = id2Node.get(eqn.toString());
	    		AttributeUtil.set(network, node, "Chemical Equation", reaction.getReactionScheme() , String.class);
	    		AttributeUtil.set(network, node, "Rate Law", rateLaw.getObjectName(), String.class);
	    		AttributeUtil.set(network, node, "Rate Law Formula", rateLaw.getInfix(), String.class);
	    		int numSubstrates = (int) eqn.getSubstrates().size();
	    		int numProducts = (int)eqn.getProducts().size();
	    		int numModifiers = (int) eqn.getModifiers().size();
	    		int numParameters = (int) reaction.getParameters().size();
	    		StringJoiner joiner = new StringJoiner(", ");
	    		StringJoiner joiner2 = new StringJoiner(", ");
	    		StringJoiner joiner3 = new StringJoiner(", ");
	    		StringJoiner joiner4 = new StringJoiner(", ");
	    		
	    		StringJoiner units1 = new StringJoiner(", ");
	    		
	    		String[] substrates = new String[numSubstrates];
	    		String[] unitsSub = new String[numSubstrates];
	    		String[] products = new String[numProducts];
	    		String[] modifiers = new String[numModifiers];
	    		String[] parameters = new String[numParameters];
	    		
	    		// Reactants
	            for (int j = 0; j < numSubstrates; j++) {
	            	CChemEqElement el = eqn.getSubstrate(j);
	            	
	            	String cn = el.getMetabolite().getCN().getString();
	               
	                substrates[j] = eqn.getSubstrate(j).getMetabolite().getObjectName();
	                unitsSub[j] = eqn.getSubstrate(j).getUnits().toString();
	               
	                joiner.add(substrates[j]);
	                
            }
	            String subStr = joiner.toString();
	            String unitSubStr = unitsSub.toString();
	            AttributeUtil.set(network, node, "substrates", subStr, String.class);
	            AttributeUtil.set(network, node, "substrate units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
	            
	            
	         // Products
	            for (int j = 0; j < numProducts; j++) {
	            	CChemEqElement el = eqn.getProduct(j);
	            	String cn = el.getMetabolite().getCN().getString();
	               
	                //AttributeUtil.set(network, n, "Product"+"_"+(j+1), el.getMetabolite().getObjectName(), String.class);
	            	products[j] = eqn.getProduct(j).getMetabolite().getObjectName();
	            	joiner2.add(products[j]);
	              }
	            
	            String subPro = joiner2.toString();
	            AttributeUtil.set(network, node, "products", subPro, String.class);
	            AttributeUtil.set(network, node, "product units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
	            for (int j = 0; j< numModifiers; j++) {
	            	CChemEqElement el = eqn.getModifier(j);
	            	String cn = el.getMetabolite().getCN().getString();
	               
	                modifiers[j] = eqn.getModifier(j).getMetabolite().getObjectName();
	                joiner3.add(modifiers[j]);
	            }
	            
	            String subMod = joiner3.toString();
	            AttributeUtil.set(network, node, "modifiers", subMod, String.class);
	            AttributeUtil.set(network, node, "modifier units", model.getQuantityUnit()+"/"+model.getVolumeUnit(), String.class);
	            
	            for (int j = 0 ; j<numParameters; j++) {
	                //AttributeUtil.set(network, n, "Product"+"_"+(j+1), el.getMetabolite().getObjectName(), String.class);
	            	//products[j] = eqn.getProduct(j).getMetabolite().getObjectName();
	            	parameters[j] = reaction.getParameters().getParameter(j).getObjectName();
	            	joiner4.add(parameters[j]);
	            }
	            String subPar =  joiner4.toString();
	            AttributeUtil.set(network, node, "parameters", subPar, String.class);
	            	CCopasiParameterGroup parameterGroup = reaction.getParameters();
	            	
	            for (int j=0 ; j< parameterGroup.size(); j++) {
	            	CCopasiParameter parameter = parameterGroup.getParameter(j);
	            
	            	AttributeUtil.set(network, node, parameter.getObjectName(), parameter.getDblValue(), Double.class);
	            }
	            //
	    	}
	    	}}
	    	
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	
    
}
}