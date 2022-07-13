package org.cytoscape.CytoCopasiApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringJoiner;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.COPASI.CChemEq;
import org.COPASI.CCompartment;
import org.COPASI.CCopasiMessage;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiParameterGroup;
import org.COPASI.CDataModel;
import org.COPASI.CDataObject;
import org.COPASI.CEvaluationTree;
import org.COPASI.CFunction;
import org.COPASI.CFunctionDB;
import org.COPASI.CFunctionParameter;
import org.COPASI.CFunctionParameters;
import org.COPASI.CFunctionStdVector;
import org.COPASI.CFunctionVectorN;
import org.COPASI.CIssue;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelEntity;
import org.COPASI.COPASI;
import org.COPASI.COPASIConstants;
import org.COPASI.CReaction;
import org.COPASI.CRootContainer;
import org.COPASI.ObjectStdVector;
import org.apache.commons.lang3.StringUtils;

import org.cytoscape.CytoCopasiApp.Query.ECFinder;
import org.cytoscape.CytoCopasiApp.Query.QueryResultSplitter;
import org.cytoscape.CytoCopasiApp.Query.SoapClient;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;
import org.cytoscape.CytoCopasiApp.actions.CreateNewModelAction;
import org.cytoscape.CytoCopasiApp.actions.ImportAction;
import org.cytoscape.CytoCopasiApp.actions.KeggWebLoadAction;
import org.cytoscape.CytoCopasiApp.actions.Optimize;
import org.cytoscape.CytoCopasiApp.actions.ParameterScan;
import org.cytoscape.CytoCopasiApp.actions.SaveLayoutAction;
import org.cytoscape.CytoCopasiApp.actions.TimeCourseSimulationTask;
import org.cytoscape.CytoCopasiApp.newmodel.NewReaction;
import org.cytoscape.CytoCopasiApp.newmodel.NewSpecies;
import org.cytoscape.CytoCopasiApp.newmodel.ParameterOverview;
import org.cytoscape.CytoCopasiApp.actions.SaveLayoutAction.SaveTask;
import org.cytoscape.CytoCopasiApp.actions.SteadyStateTask;
import org.cytoscape.CytoCopasiApp.tasks.CopasiFileReaderTask;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.LinkAction;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.HyperlinkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import stoichiometry.Compound;
import stoichiometry.Element;
import stoichiometry.Expression;


public class MyCopasiPanel extends JPanel implements CytoPanelComponent {
	private CyNetworkFactory networkFactory;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkViewFactory networkViewFactory;
	private LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	private SynchronousTaskManager synchronousTaskManager;
	private FileUtil fileUtil;
	String[] reactantList;
	String[] productList;
	String[] reactantProduct;
	JComboBox rateLawCombo;
	JComboBox typeCombo; 
	JButton brendaButton;
	JButton preview;
	 Box paramVerBox;
     Box paramOverallBox; 
     Box newModelBox;
     Box newModelActionBox;
     JLabel newModelPanelLabel;
     ParameterOverview overview;
	//CDataModel dataModel;
	//CModel model;
	CFunctionParameters variables;
	CMetab newMetab;
	CDataObject object;
	ObjectStdVector changedObjects;
	CReaction reaction;
	CCopasiParameterGroup parameterGroup ;
	
	DefaultTableModel newRateLawModel;
	JTable rateLawTable;
	
	DefaultTableModel ecNoModel;
	JTable ecNoTable;
	
	JScrollPane sp;
	JScrollPane sp3 ;
	
	String[] metabSplit ;
	String[] coefficients;
	String[] metabolites;
	String modifier;
	String modStr;
	String[] paramValues;
	String newFormula;
	String myPath;
	
	VisualStyle visStyle;
	private File visFile = null;
	
	String[] resultColumns;
	Object [][] results;
	
	File tempFile;
	File myFile;
	
	LinkedList<CyNode> reactantsNodes;
	LinkedList<CyNode> productsNodes;
	CyNetwork copasiNetwork;
	CySwingApplication cySwingApplication;
	FileWriter f2 ;
	
	int copasiInt;
	
	JLabel[] paramLabels; 
    JTextField[] paramVals;
	public MyCopasiPanel(CySwingApplication cySwingApplication, FileUtil fileUtil, LoadNetworkFileTaskFactory loadNetworkFileTaskFactory, @SuppressWarnings("rawtypes") SynchronousTaskManager synchronousTaskManager) {
		this.cySwingApplication = cySwingApplication;
		this.fileUtil = fileUtil;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.synchronousTaskManager = synchronousTaskManager;
		setVisible(true);
		validate();
		repaint();
		buildUI();
	}
	
	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
	
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		// TODO Auto-generated method stub
		 return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "CytoCopasi";
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void buildUI() {
		setLayout(new GridLayout(12,1));
		JButton newModel = new JButton("New Copasi Model");
		JButton importModel = new JButton("Import Model");
		JButton importKegg = new JButton("Import KEGG Pathway");
		JButton timeCourseButton = new JButton("Run Time Course Simulation");
		JButton steadyState = new JButton("Steady State Calculation");
		JButton optimize = new JButton("Optimization");
		JButton parameterScan = new JButton("Parameter Scan");
		
		add(newModel);
		add(importModel);
		add(importKegg);
		add(timeCourseButton);
		add(steadyState);
		add(optimize);
		add(parameterScan);
		validate();
		repaint();
		importModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				ImportAction importAction = new ImportAction(cySwingApplication, fileUtil, loadNetworkFileTaskFactory, synchronousTaskManager);
				importAction.actionPerformed(e);	
				/*preview = new JButton("Parameter Overview");
				add(preview);*/
			}
			
			
		});
		
		importKegg.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				KeggWebLoadAction keggLoad = new KeggWebLoadAction();
				keggLoad.actionPerformed(e);
			}
			
		});
		newModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				CDataModel dataModel = CRootContainer.addDatamodel();
				CModel model = dataModel.getModel();
				JFrame modelFrame = new JFrame("Model Creation");
				JPanel modelPanel = new JPanel();
				modelPanel.setPreferredSize(new Dimension(800,200));
				modelPanel.setLayout(new GridLayout(4,1));
				
				Box modelNameBox = Box.createHorizontalBox();
				JLabel modelNameLabel = new JLabel("Name");
				JTextField modelNetworkName = new JTextField(5);
				modelNameBox.add(modelNameLabel);
				modelNameBox.add(modelNetworkName);
				
				Box modelUnitsBox = Box.createHorizontalBox();
				
				JLabel timeUnitLabel = new JLabel("Time Unit");
				String timeUnits[] = {"as","fs","ps","ns","µs","ms","s","min","h","d"};
				JComboBox timeUnitCombo = new JComboBox(timeUnits);
				
				JLabel volumeUnitLabel = new JLabel("Volume Unit");
				String volumeUnits[] = {"1","am³","fm³","pm³","nm³","µm³","um³","mm³","m³","km³","al","fl","pl","nl","µl","ul","ml","l","kl"};
				JComboBox volumeUnitCombo = new JComboBox(volumeUnits);
				
				JLabel quantityUnitLabel = new JLabel("Quantity Unit");
				String quantityUnit[] = {"amol", "fmol", "pmol", "nmol", "µmol","umol", "mmol", "mol", "kmol"};
				JComboBox quantityUnitCombo = new JComboBox(quantityUnit);
				
				Box compartmentBox = Box.createHorizontalBox();
				JLabel compartmentLabel = new JLabel("compartment");
				JTextField compartment = new JTextField(5);
				JLabel compVolLabel = new JLabel("volume");
				JTextField volume = new JTextField(5);
				compartmentBox.add(compartmentLabel);
				compartmentBox.add(compartment);
				compartmentBox.add(compVolLabel);
				compartmentBox.add(volume);
				
				modelUnitsBox.add(timeUnitLabel);
				modelUnitsBox.add(timeUnitCombo);
				modelUnitsBox.add(volumeUnitLabel);
				modelUnitsBox.add(volumeUnitCombo);
				modelUnitsBox.add(quantityUnitLabel);
				modelUnitsBox.add(quantityUnitCombo);
				
				Box commentBox = Box.createHorizontalBox();
				JLabel commentLabel = new JLabel("Comments");
				JTextArea comments = new JTextArea(5,1);
				commentBox.add(commentLabel);
				commentBox.add(comments);
				
				modelPanel.add(modelNameBox);
				modelPanel.add(modelUnitsBox);
				modelPanel.add(compartmentBox);
				modelPanel.add(commentBox);
				modelPanel.validate();
				modelPanel.repaint();
				modelFrame.add(modelPanel);
				Object[] modelCreationOptions = {"Create", "Cancel"};
				int modelCreationDialog = JOptionPane.showOptionDialog(modelFrame, modelPanel, "Create New Model", JOptionPane.PLAIN_MESSAGE, 1, null, modelCreationOptions, modelCreationOptions[0]);
				
				if (modelCreationDialog == 0) {
					remove(newModel);
					if (newModelBox!=null) {
						remove(newModelBox);
						remove(newModelActionBox);
						remove(newModelPanelLabel);
					}
					overview = new ParameterOverview();

					CreateNewModelAction newNetwork = new CreateNewModelAction();
					copasiNetwork = newNetwork.createNetwork();
					newNetwork.applyVisStyle();
					model.setTimeUnit(timeUnitCombo.getSelectedItem().toString());
					model.setVolumeUnit(volumeUnitCombo.getSelectedItem().toString());
					model.setQuantityUnit(quantityUnitCombo.getSelectedItem().toString());
				    changedObjects=new ObjectStdVector();
				    CCompartment myCompartment = model.createCompartment(compartment.getText(), Double.parseDouble(volume.getText()));
				    object = myCompartment.getInitialValueReference();
				    changedObjects.add(object);
				    newModelPanelLabel = new JLabel("New COPASI Model");
				    Font newModelFont = new Font("Calibri", Font.BOLD, 16);
				    newModelPanelLabel.setFont(newModelFont);
				    newModelPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
				    
				    JButton newSpecies = new JButton("Add Species");
					JButton newReaction = new JButton("Add Reaction");
					JButton resetButton = new JButton("Reset Model");
					JButton saveModel = new JButton("Save Model");
					JButton exportModel = new JButton("Export Model as SBML");
					JButton removeReaction = new JButton("Remove Reaction");
					preview = new JButton("Parameter Overview");
					newModelBox = Box.createHorizontalBox();					
					newModelActionBox = Box.createHorizontalBox();
					//newModelBox = Box.createVerticalBox();
					//add(newModel);
					newModelBox.add(newSpecies);
					newModelBox.add(newReaction);
					newModelBox.add(preview);
					
					newModelActionBox.add(saveModel);
					newModelActionBox.add(exportModel);
					newModelActionBox.add(resetButton);
					
					add(newModelPanelLabel);
					add(newModelBox);
					add(newModelActionBox);
					//add(newModelBox);
					validate();
					repaint();
					
					
					newSpecies.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							
							NewSpecies newSpecies = new NewSpecies();
							newSpecies.createNewSpecies(quantityUnitCombo, volumeUnitCombo, compartment, dataModel, model, copasiNetwork, newNetwork, object, changedObjects);
							model.updateInitialValues(changedObjects);
						}
						
					} );
					
					
					newReaction.addActionListener(new ActionListener() {
						
					//	String chemEqString; 
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							
							NewReaction newReaction = new NewReaction();
							newReaction.createNewReaction(dataModel, model, compartment, quantityUnitCombo, timeUnitCombo, copasiNetwork, newNetwork, object, changedObjects);
							model.updateInitialValues(changedObjects);
							if (!(model.getNumReactions()==0)) {
								newModelBox.remove(removeReaction);
							}
							newModelBox.add(removeReaction);
							validate();
							repaint();
							removeReaction.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									// TODO Auto-generated method stub
									NewReaction newReaction = new NewReaction();
									newReaction.removeReaction(dataModel, model, copasiNetwork);
								}
								
							});
						}
					
				});
					
					
					
					preview.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							//ParameterOverview overview = new ParameterOverview();
							overview.parameterOverview(dataModel, model);
						}
						
					});
					resetButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							
							JLabel resetModelWarn = new JLabel("Your changes will be lost if you have not saved your current model. Continue?");
							JFrame resetModelWarnFrame = new JFrame();
							resetModelWarnFrame.add(resetModelWarn);
							//Object[] newModelWarningOp = {"OK"};
							
							int resetModelWarningDialog = JOptionPane.showConfirmDialog(resetModelWarnFrame, resetModelWarn, "Warning", JOptionPane.DEFAULT_OPTION, 0);
							if (resetModelWarningDialog == 0) {
							for (int i=0; i< model.getNumReactions(); i++) {
								
								reaction = model.getReaction(i);
								model.removeReaction(model.getReaction(i).getKey());
								reaction=null;
							}
							model.removeCompartment(myCompartment);
							
							CRootContainer.removeDatamodel(dataModel);
							newNetwork.resetNetwork();
							
							remove(newModelPanelLabel);
							remove(newModelBox);
							remove(newModelActionBox);
							
							remove(importModel);
							remove(timeCourseButton);
							remove(steadyState);
							remove(optimize);
							remove(parameterScan);
							
							add(newModel);
							add(importModel);
							add(timeCourseButton);
							add(steadyState);
							add(optimize);
							add(parameterScan);
							
							validate();
							repaint();
							if (tempFile !=null) {
							tempFile.delete();
							}
						}
						}
					});
					saveModel.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							
							
							
							Component frame = CyActivator.cytoscapeDesktopService.getJFrame();
							HashSet<FileChooserFilter> filters = new HashSet<>();
							
							FileChooserFilter filter = new FileChooserFilter(".cps", "cps");
							filters.add(filter);
						   // FileUtil fileUtil = fileUtil;
						    
						    File xmlFile = CyActivator.fileUtil.getFile(frame, "Save File", FileUtil.SAVE, filters);
						    
						    final SaveTask task = new SaveTask(xmlFile.getAbsolutePath());
						    CyActivator.taskManager.execute(new TaskIterator(task));	
					}
						
						class SaveTask extends AbstractTask {
							
							private String filePath;
							private TaskMonitor taskMonitor;
							
							public SaveTask(String filePath) {
								this.filePath = filePath;
								super.cancelled = false;
							}

							@Override
							public void run(TaskMonitor taskMonitor) throws Exception {
								try {
								//	myFile.delete();
									myFile = new File(CyActivator.getReportFile(1).getAbsolutePath());
									
									model.compileIfNecessary();

								   
								    model.updateInitialValues(changedObjects);
								    taskMonitor.setTitle("Saving File");
									taskMonitor.setProgress(0.4);
									
									dataModel.saveModel(filePath ,true);
									
									try {
				    					f2 = new FileWriter(myFile, false);
				    					f2.write(filePath);
				    					f2.close();
					
				    				} catch (Exception e1) {
				    					// TODO Auto-generated catch block
				    					e1.printStackTrace();
						            taskMonitor.setStatusMessage("Saved Copasi Model to " + filePath + ".cps");
								
								} 
								}finally {
									System.gc();
								}
							
							}
						}
						
						
						
					});
				exportModel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						Component frame = CyActivator.cytoscapeDesktopService.getJFrame();
						HashSet<FileChooserFilter> filters = new HashSet<>();
						
						FileChooserFilter filter = new FileChooserFilter(".xml", "xml");
						filters.add(filter);
					   // FileUtil fileUtil = fileUtil;
					    
					    File xmlFile = CyActivator.fileUtil.getFile(frame, "Save File", FileUtil.SAVE, filters);
					    
					    final SaveTask task = new SaveTask(xmlFile.getAbsolutePath());
					    CyActivator.taskManager.execute(new TaskIterator(task));	
				}
					
					class SaveTask extends AbstractTask {
						
						private String filePath;
						private TaskMonitor taskMonitor;
						
						public SaveTask(String filePath) {
							this.filePath = filePath;
							super.cancelled = false;
						}

						@Override
						public void run(TaskMonitor taskMonitor) throws Exception {
							try {
								//myFile.delete();
								myFile = new File(CyActivator.getReportFile(1).getAbsolutePath());
								
								model.compileIfNecessary();

							   
							    model.updateInitialValues(changedObjects);
							    taskMonitor.setTitle("Saving File");
								taskMonitor.setProgress(0.4);
								
								//dataModel.saveModel(filePath ,true);
								dataModel.exportSBML(filePath, true);
								try {
			    					f2 = new FileWriter(myFile, false);
			    					f2.write(filePath);
			    					f2.close();
				
			    				} catch (Exception e1) {
			    					// TODO Auto-generated catch block
			    					e1.printStackTrace();
					            taskMonitor.setStatusMessage("Saved Copasi Model to " + filePath + ".xml");
							
							} 
							}finally {
								System.gc();
							}
						
						}
					}
					
				});
				
				modelFrame.dispose();
			}else if (modelCreationDialog == 1) {
				modelFrame.dispose();
				CRootContainer.removeDatamodel(dataModel);
				
			}
				
			} 
			
				
		});
		
		timeCourseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				TimeCourseSimulationTask timeCourse = new TimeCourseSimulationTask(cySwingApplication, fileUtil);
				timeCourse.actionPerformed(e);
				
			}
			
		});
		
		
		steadyState.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				SteadyStateTask steadyStateTask = new SteadyStateTask(cySwingApplication, fileUtil);
				steadyStateTask.actionPerformed(e);
			}
			
		});
		
		optimize.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Optimize optimizeTask = new Optimize(cySwingApplication, fileUtil);
				optimizeTask.actionPerformed(e);
			}
			
		});
		
		parameterScan.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ParameterScan parameterScanTask = new ParameterScan(cySwingApplication, fileUtil);
				parameterScanTask.actionPerformed(e);
			}
			
		});
		
	}
	
	private static boolean isMinusOrDigit(char c) {
	    return c == '-' || ( c >= 0 && c<=9 ); 
	}
}