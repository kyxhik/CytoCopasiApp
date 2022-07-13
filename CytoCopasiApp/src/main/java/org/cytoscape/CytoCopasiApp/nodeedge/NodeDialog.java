package org.cytoscape.CytoCopasiApp.nodeedge;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.COPASI.CCommonName;
import org.COPASI.CCopasiMessage;
import org.COPASI.CDataModel;
import org.COPASI.CDataObject;
import org.COPASI.CEvaluationTree;
import org.COPASI.CEvaluationTreeVector;
import org.COPASI.CEvaluationTreeVectorN;
import org.COPASI.CFunction;
import org.COPASI.CFunctionDB;
import org.COPASI.CFunctionVectorN;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelEntity;
import org.COPASI.COPASI;
import org.COPASI.CReaction;
import org.COPASI.CRootContainer;
import org.COPASI.ObjectStdVector;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.CytoCopasiApp.AttributeUtil;
import org.cytoscape.CytoCopasiApp.CopasiTree;
import org.cytoscape.CytoCopasiApp.CyActivator;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;
import org.cytoscape.CytoCopasiApp.actions.Optimize;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;


public class NodeDialog extends JDialog {
	private static final long serialVersionUID = 1498730989498413815L;
	  
	private boolean wasNewlyCreated;
	  
	private JTextField nameField;
	//private JTextArea formula;
	//private JTextField functionName;
	private JTree formulaTree;
	private JTable table;
	private CySwingApplication cySwingApplication;
	private FileUtil fileUtil;
	private ObjectStdVector changedObjects;
	private String[] parameterSplit;
	static JFrame frame = new JFrame("Specifics");
	private String compartment;
	private Double initConc;
	public NodeDialog(CyNetwork network, CyNode node) {
	    this(frame, network, node);
	  }
	
	 public NodeDialog(Window owner, final CyNetwork network, final CyNode node) {
		 super(owner, network.getRow((CyIdentifiable)node).isSet("canonicalName") ? ("Reactant " + (String)network.getRow((CyIdentifiable)node).get("canonicalName", String.class)) : "New reactant", Dialog.ModalityType.APPLICATION_MODAL);
		    String name;
		    setSize(new Dimension (400,600));
		    this.wasNewlyCreated = false;
		    this.nameField = null;
		    CyRow nodeAttributesRow = network.getRow((CyIdentifiable)node);
		    
		    changedObjects = new ObjectStdVector();
		    String modelName;
			try {
				modelName = new Scanner(CyActivator.getReportFile(1)).next();
				CDataModel dm = CRootContainer.addDatamodel();
				Image image = null;
		        URL url = null;
		        URL url2 = null;
				if (modelName.endsWith(".cps")) {
					String modelString = new Scanner(new File(modelName)).useDelimiter("\\Z").next();
					
			       dm.loadFromString(modelString);
			    } else if (modelName.endsWith(".xml")) {
			      dm.importSBML(modelName);
			    }
				CModel model = dm.getModel();
				
				Object type = nodeAttributesRow.get("type", Object.class);
			    Object typeSbml = nodeAttributesRow.get("sbml type", Object.class);
			
			    if (type == "species" || typeSbml == "species") {
		    	setTitle("Edit species");
			    } else if (type == "reaction rev" || type == "reaction irrev" || typeSbml == "reaction") {
		    	setTitle("Edit reaction");
			    }
		    
		    GridLayout grid = new GridLayout(6,1);
		    
		    
		    setLayout(grid);
		    
		    Object nodename = nodeAttributesRow.get("name", Object.class);
		    if (nodename != null) {
		    	name = nodename.toString();
		    } else {
		    	name = null;
		    }
		    
		    Box nameBox = Box.createHorizontalBox();
		   // this.nameField = new JTextField(name);
		   // int yCoordLeft = 0;
		   // add(new JLabel("Name"), new GridBagConstraints(0, yCoordLeft++, 1, 1, 0.5D, 0.0D, 
		     //       10, 2, new Insets(0, 0, 0, 0), 0, 0));
		    JLabel nameLabel = new JLabel("Name:");
		    JTextField theName = new JTextField(name);
		    JButton apply = new JButton("Apply");
	    	JButton cancel = new JButton("Cancel");
		    nameBox.add(nameLabel);
		    nameBox.add(theName);
		    add(nameBox);
		    
		    if (type == "species" || typeSbml == "species") {
		    	Box compartmentBox = Box.createHorizontalBox();
		    	JLabel compartmentLabel = new JLabel("Compartment:");
		    	
		    	if (modelName.endsWith(".cps")) {
		    	compartment = nodeAttributesRow.get("compartment", String.class);
		    	initConc = nodeAttributesRow.get("initial concentration", Double.class);
		    	} else if (modelName.endsWith(".xml")) {
		    	compartment = nodeAttributesRow.get("compartment", String.class);
		    	initConc = nodeAttributesRow.get("sbml initial concentration", Double.class);
		    	}
		    	JLabel theCompartment = new JLabel(compartment);
		   
		    	compartmentBox.add(compartmentLabel);
		    	compartmentBox.add(theCompartment);
		    	add (compartmentBox);
		    	
		    	Box structureBox = Box.createHorizontalBox();
		    	JLabel structureLabel = new JLabel("Structure: ");
		    	
		    	Box keggEntryBox = Box.createHorizontalBox();
		    	JLabel keggEntryLabel = new JLabel("Kegg Entry: ");
		    	if (compartment.equals("unknown") == true) {
		        
		        try {
		           url = new URL("https://rest.kegg.jp/get/"+name+"/image");
		           JLabel keggLab = new JLabel(name);
		           keggLab.setForeground(Color.BLUE.darker());
		           keggLab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		           keggLab.addMouseListener(new MouseAdapter(){
		        	   public void mouseClicked(MouseEvent e) {
		        		    String keggLink = "https://www.kegg.jp/entry/"+name;
		 		           URI keggURI = URI.create(keggLink);
				           try {
							Desktop.getDesktop().browse(keggURI);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		        		    
		        		    }
		           });
		           keggEntryBox.add(keggEntryLabel);
		           keggEntryBox.add(keggLab);
		           add(keggEntryBox);
		            image = ImageIO.read(url);
		        } catch (MalformedURLException ex) {
		            System.out.println("Malformed URL");
		        } catch (IOException iox) {
		            System.out.println("Can not load file");
		        }
		        
		        
		        JLabel label = new JLabel(new ImageIcon(image));
		        structureBox.add(structureLabel);
		        structureBox.add(label);
		    	add (structureBox, BorderLayout.CENTER);
		    	
		    	validate();
		    	repaint();
		    	
		    	}
		    	
		    	Box brendaBox = Box.createHorizontalBox();
		    	Box initConcBox = Box.createHorizontalBox();
		    	JLabel initConcLabel = new JLabel("Initial Concentration:");
		    	JTextField initConcField = new JTextField(5);
		    	
		    	
		    	initConcField.setText(initConc.toString());
		    	initConcBox.add(initConcLabel);
		    	initConcBox.add(initConcField);
		    	add(initConcBox);
		    	
		    	Box statusBox = Box.createHorizontalBox();
		    	JLabel statusLabel = new JLabel("Status");
		    	String[] statusOptions = {"Assignment","Fixed","ODE","Reactions", "Time"};
				JComboBox statusCombo = new JComboBox(statusOptions);
				statusBox.add(statusLabel);
				statusBox.add(statusCombo);
				add(statusBox);
				
				String getMetabStatus = nodeAttributesRow.get("status", String.class);
				switch (getMetabStatus) {
				case "Assignment" :
					statusCombo.setSelectedItem("Assignment");
					break;
				case "Fixed" :
					statusCombo.setSelectedItem("Fixed");
					break;
				case "ODE":
					statusCombo.setSelectedItem("ODE");
					break;
				case "Reactions":
				statusCombo.setSelectedItem("Reactions");
					break;
				case "Time":
					statusCombo.setSelectedItem("Time");
					break;
				}
				
		    	apply.addActionListener((ActionListener) new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						nodeAttributesRow.set("name", theName.getText());
						nodeAttributesRow.set("shared name", theName.getText());
						if (modelName.endsWith(".cps")) {
						nodeAttributesRow.set("display name", theName.getText());
						nodeAttributesRow.set("initial concentration", Double.parseDouble(initConcField.getText()));
						} else if (modelName.endsWith(".xml")) {
						nodeAttributesRow.set("sbml initial concentration", Double.parseDouble(initConcField.getText()));
						}
						for(int i = 0; i < model.getNumMetabs(); i++) {
								if (model.getMetabolite(i).getObjectName().equals(nodeAttributesRow.get("name", String.class))) {
								
								
								
								
								int createMetabStatus = 0;
								switch (statusCombo.getSelectedItem().toString()) {
								case "Assignment":
									createMetabStatus = CMetab.Status_ASSIGNMENT;
									nodeAttributesRow.set("status", "Assignment");
									break;
								case "Fixed":
									createMetabStatus = CMetab.Status_FIXED;
									nodeAttributesRow.set("status", "Fixed");
									break;
								case "ODE":
									createMetabStatus = CMetab.Status_ODE;
									nodeAttributesRow.set("status", "ODE");
									break;
								case "Reactions":
									createMetabStatus = CMetab.Status_REACTIONS;
									nodeAttributesRow.set("status", "Reactions");
									break;
								case "Time":
									createMetabStatus = CMetab.Status_TIME;
									break;
								}
								
								model.getMetabolite(i).compileIsInitialValueChangeAllowed();
								
								model.getMetabolite(i).setInitialConcentration(Double.parseDouble(initConcField.getText()));
								changedObjects.add(model.getMetabolite(i).getInitialConcentrationReference());
								model.updateInitialValues(changedObjects);
								model.compileIfNecessary();
								
								model.getMetabolite(i).setStatus(createMetabStatus);
								model.updateInitialValues(changedObjects);
								model.compileIfNecessary();
								
								
								
								
								if (modelName.endsWith(".cps")) {
									dm.saveModel(modelName, true);
								} else if (modelName.contains(".xml")) {
								
								
								try {
									
									dm.exportSBML(modelName, true);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								}
								
								

							}
						}
						
						dispose();
						
					}
		    		
		    	}
		    			);
		    	
		    	cancel.addActionListener((ActionListener) new ActionListener() {
		    		public void actionPerformed(ActionEvent e) {
		    			dispose();
		    		}
		    	}
		    			);
		    	Box buttonBox = Box.createHorizontalBox();
		    	buttonBox.add(apply);
		    	buttonBox.add(cancel);
		    	add(buttonBox);
		    } else if (type == "reaction rev" || type == "reaction irrev" || typeSbml == "reaction rev" || typeSbml == "reaction irrev") {
		    	//Reversible
		    	setSize(new Dimension (1225,350));
		    	Box reversibleBox = Box.createHorizontalBox();
		    	JLabel reverLabel = new JLabel ("Reversible: ");
		    	Boolean reversibleCheck = nodeAttributesRow.get("reversible", Boolean.class);
		    	JCheckBox revCheckBox = new JCheckBox();
		    	
		    	if (reversibleCheck == true) {
		    		
		    		revCheckBox.setSelected(true);
		    	} else {
		    		revCheckBox.setSelected(false);
		    	}
		    	
		    	reversibleBox.add(reverLabel);
		    	reversibleBox.add(revCheckBox);
		    	add(reversibleBox);
		    	
		    	// Chemical Equation
		    	Box chemEqBox = Box.createHorizontalBox();
		    	JLabel chemEqLabel = new JLabel("Reaction: ");
		    	JTextField chemEqField = new JTextField (10);
		    	String chemEq = nodeAttributesRow.get("Chemical Equation", String.class);
		    	chemEqField.setText(chemEq);
		    	chemEqBox.add(chemEqLabel);
		    	chemEqBox.add(chemEqField);
		    	add(chemEqBox);
		    	
		    	// RateLaw
		    	
		    	Box rateLawBox = Box.createHorizontalBox();
		    	JLabel rateLawLabel = new JLabel("Rate Law: ");
		    	JTextField formulaMainField = new JTextField(5);
		    	CFunctionDB functionDB = CRootContainer.getFunctionList();
		    	CFunctionVectorN allFunctions = functionDB.loadedFunctions();
		    	String [] functionList = new String[(int) allFunctions.size()];
		    	JTextField functionName = new JTextField(3);
				JTextArea formula = new JTextArea(5,1);
				String substrateData = nodeAttributesRow.get("substrates", String.class);
				String productData = nodeAttributesRow.get("products", String.class);
				String modifierData = nodeAttributesRow.get("modifiers", String.class);
				String units = nodeAttributesRow.get("substrate units", String.class);
				String parameters = nodeAttributesRow.get("parameters", String.class);
				
				String[] substrateSplit = substrateData.split(", ");
				String[] productSplit = productData.split(", ");
				String[] modifierSplit = modifierData.split(", ");
				String[] parameterSplit = parameters.split(", ");

		    	for (int a=0; a< allFunctions.size(); a++) {
		    		functionList[a] = allFunctions.get(a).getObjectName();
		    		
		    	}
		    	JComboBox rateLawCombo = new JComboBox(functionList);
		    	String rateLaw = nodeAttributesRow.get("Rate Law", String.class);
		    	String rateLawFormula = nodeAttributesRow.get("Rate Law Formula", String.class);
		    	rateLawCombo.setSelectedItem(rateLaw);
		    	
		    	
		    	
		    	JButton newRateLaw = new JButton("+");
		    	newRateLaw.addActionListener((ActionListener) new ActionListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JFrame newRateLawFrame = new JFrame("Add a new rate law");
						JPanel newRateLawPanel = new JPanel();
						newRateLawPanel.setPreferredSize(new Dimension(1000,750));
						newRateLawPanel.setLayout(new GridLayout(5,2));
						Box functionNameBox = Box.createHorizontalBox();
						JLabel functionNameLabel = new JLabel("Function: ");
						JTextField functionName = new JTextField(3);
						functionNameBox.add(functionNameLabel);
						functionNameBox.add(functionName);
						
						Box formulaBox = Box.createHorizontalBox();
						JLabel formulaLabel = new JLabel("Formula: ");
						JTextArea formula = new JTextArea(5,1);
						JButton formulaItem = new JButton("->");
						JButton commitButton = new JButton("commit");
						formulaItem.addActionListener( (ActionListener) new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								JPanel formulaItemPanel = new JPanel();
								formulaItemPanel.setPreferredSize(new Dimension(500,500));
								CopasiTree optimItems = new CopasiTree();
								DefaultMutableTreeNode optim = new DefaultMutableTreeNode("Parameter Items");
								String [] optCat =  {"Reactions", "Species"};
								try {
									optimItems.createNodes(optim, optCat);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								formulaTree = new JTree(optim);
							
								JButton plus = new JButton("+");
							       plus.addActionListener( new ActionListener() {
							    	   public void actionPerformed(ActionEvent e) {
							    		   formula.append("+");
							    		   
							    	   }
							       }
				  
							    		   );
							       formulaItemPanel.add(plus);

							      JButton minus = new JButton("-");
							        minus.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("-");
								    		 
								    	   }
								       }
								    		   );
							        formulaItemPanel.add(minus);
					  
							        JButton times = new JButton("*");
							        times.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("*");
								    		 
								    	   }
								       }
								    		   
								    		   );
							        formulaItemPanel.add(times);

							        JButton divide = new JButton("/");
							        divide.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }
								    		   
								    		   );
							        formulaItemPanel.add(divide);
							        
							        JButton power = new JButton("^");
							        power.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("^");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(power);
							        
							        JButton parantheses1 = new JButton("(");
							        parantheses1.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(parantheses1);
							        
							        JButton parantheses2 = new JButton("(");
							        parantheses2.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(parantheses2);
							        
							    formulaTree.addTreeSelectionListener(new TreeSelectionListener() {
									@SuppressWarnings("null")
									public void valueChanged(TreeSelectionEvent e) {
										DefaultMutableTreeNode node = (DefaultMutableTreeNode) formulaTree.getLastSelectedPathComponent();
									
										if (node == null)
											return;
							
											Object objNew = e.getNewLeadSelectionPath().getLastPathComponent();
											formula.append(objNew.toString());
											
									
									}
								}
										
										);
								JScrollPane treeView = new JScrollPane(formulaTree);
								treeView.setPreferredSize(new Dimension(100,300));
								formulaItemPanel.add(treeView);
								Object[] paroptions= {"OK","Cancel"};
								int parameterSelection = JOptionPane.showOptionDialog(null, formulaItemPanel, "Select Parameter",JOptionPane.PLAIN_MESSAGE, 1, null, paroptions, paroptions[0]);
							}
							
							
							
						});
						
						
						formulaBox.add(formulaLabel);
						formulaBox.add(formula);
						formulaBox.add(formulaItem);
						formulaBox.add(commitButton);
						
						
						
						Box functionTypeBox = Box.createHorizontalBox();
						JLabel functionTypeLabel = new JLabel("Function Type");
						JRadioButton revButton = new JRadioButton("reversible");
						JRadioButton irrevButton = new JRadioButton("irreversible");
						
						functionTypeBox.add(functionTypeLabel);
						functionTypeBox.add(revButton);
						functionTypeBox.add(irrevButton);
						
						newRateLawPanel.add(functionNameBox);
						newRateLawPanel.add(formulaBox);
						newRateLawPanel.add(functionTypeBox);
						
						newRateLawFrame.add(newRateLawPanel);
						newRateLawFrame.setSize(new Dimension(1000,750));
						
						Object[] rateLawAddOptions = {"Add", "Cancel"};
						int rateLawAddDialog = JOptionPane.showOptionDialog(owner, newRateLawPanel, "Add a new rate law", JOptionPane.PLAIN_MESSAGE, 1, null, rateLawAddOptions, rateLawAddOptions[0]);
						
						if (rateLawAddDialog == 0) {
							rateLawCombo.addItem(functionName.getText());
							rateLawCombo.setSelectedItem(functionName.getText());
							formulaMainField.setText(formula.getText());
							//CFunction newFunction = new CFunction(functionName.getText());
							CEvaluationTree newFunction = functionDB.createFunction(functionName.getText(), CEvaluationTree.UserDefined);
							newFunction.setInfix(formula.getText());
							
						}
					}
		    		
		    	});
		    	
				functionName.setText(rateLaw);
				
				formula.setText(rateLawFormula);

				rateLawCombo.addItemListener((ItemListener) new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent e) {
						// TODO Auto-generated method stub
						
						if (e.getStateChange() == ItemEvent.SELECTED) {
							if (functionName.getText() == rateLaw) {
						functionName.setText(e.getItem().toString());
						String newFunctionEdit = functionName.getText();
					        CEvaluationTree findFunc = functionDB.findFunction(newFunctionEdit);
					    //   if (formula.getText() == rateLawFormula) {
							formula.setText(findFunc.getInfix());
							} 
					      // }

						
					}
					}

					
				});
				
		    	JButton editRateLaw = new JButton("edit");
		    	editRateLaw.addActionListener((ActionListener) new ActionListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JFrame editRateLawFrame = new JFrame("Edit rate law");
						JPanel editRateLawPanel = new JPanel();
						functionName.setText(rateLawCombo.getSelectedItem().toString());
						String newFunctionEdit = functionName.getText();
					        CEvaluationTree findFunc = functionDB.findFunction(newFunctionEdit);
					    //   if (formula.getText() == rateLawFormula) {
							formula.setText(findFunc.getInfix());
						editRateLawPanel.setPreferredSize(new Dimension(1000,750));
						editRateLawPanel.setLayout(new GridLayout(5,2));
						Box functionNameBox = Box.createHorizontalBox();
						JLabel functionNameLabel = new JLabel("Function: ");
						
						functionNameBox.add(functionNameLabel);
						functionNameBox.add(functionName);
						
						
						
						Box formulaBox = Box.createHorizontalBox();
						JLabel formulaLabel = new JLabel("Formula: ");
						
						JButton formulaItem = new JButton("->");
						formulaBox.add(formulaLabel);
						formulaBox.add(formula);
						formulaBox.add(formulaItem);
						
						
						
						String description[] = {"Name", "Type", "Units"};
						
						String type[] = {"Substrate", "Product", "Modifier", "Parameter"};
						DefaultTableModel editRateLawModel = new DefaultTableModel();
						JTable rateLawTable = new JTable();
						rateLawTable.setModel(editRateLawModel);
						editRateLawModel.addColumn(description[0]);
						editRateLawModel.addColumn(description[1]);
						editRateLawModel.addColumn(description[2]);
						
						JComboBox typeCombo = new JComboBox(type);

						
						
						for (int i=0; i<substrateSplit.length; i++) {
							typeCombo.setSelectedIndex(0);
							editRateLawModel.addRow(new Object[] {substrateSplit[i], typeCombo.getSelectedItem(), units});
						}
						
						for (int i=0; i<productSplit.length; i++) {
							typeCombo.setSelectedIndex(1);
							editRateLawModel.addRow(new Object[] {productSplit[i], typeCombo.getSelectedItem(), units});
						}
						
						if (modifierSplit.length>0 && modifierSplit[0]!="") {
						for (int i=0; i<modifierSplit.length; i++) {
							typeCombo.setSelectedIndex(2);
							editRateLawModel.addRow(new Object[] {modifierSplit[i], typeCombo.getSelectedItem(), units});
						}
						
						}
						
						for (int i = 0 ; i<parameterSplit.length; i++) {
							Double paramVal= nodeAttributesRow.get(parameterSplit[i], Double.class);
							typeCombo.setSelectedIndex(3);
							editRateLawModel.addRow(new Object[] {parameterSplit[i], typeCombo.getSelectedItem(), paramVal});
						}
						
						rateLawTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));
						JScrollPane sp = new JScrollPane(rateLawTable);

						editRateLawPanel.add(functionNameBox);
						editRateLawPanel.add(formulaBox);
						editRateLawPanel.add(sp);
						formulaItem.addActionListener( (ActionListener) new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								JPanel formulaItemPanel = new JPanel();
								formulaItemPanel.setPreferredSize(new Dimension(500,500));
								CopasiTree optimItems = new CopasiTree();
								DefaultMutableTreeNode optim = new DefaultMutableTreeNode("Parameter Items");
								String [] optCat =  {"Reactions", "Species"};
								try {
									optimItems.createNodes(optim, optCat);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								formulaTree = new JTree(optim);
							
								JButton plus = new JButton("+");
							       plus.addActionListener( new ActionListener() {
							    	   public void actionPerformed(ActionEvent e) {
							    		   formula.append("+");
							    		   
							    	   }
							       }
				  
							    		   );
							       formulaItemPanel.add(plus);

							      JButton minus = new JButton("-");
							        minus.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("-");
								    		 
								    	   }
								       }
								    		   );
							        formulaItemPanel.add(minus);
					  
							        JButton times = new JButton("*");
							        times.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("*");
								    		 
								    	   }
								       }
								    		   
								    		   );
							        formulaItemPanel.add(times);

							        JButton divide = new JButton("/");
							        divide.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }
								    		   
								    		   );
							        formulaItemPanel.add(divide);
							        
							        JButton power = new JButton("^");
							        power.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("^");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(power);
							        
							        JButton parantheses1 = new JButton("(");
							        parantheses1.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(parantheses1);
							        
							        JButton parantheses2 = new JButton("(");
							        parantheses2.addActionListener(new ActionListener() {
								    	   public void actionPerformed(ActionEvent e) {
								    		   formula.append("/");
								    	   }
								       }   
								    		   );
							        
							        formulaItemPanel.add(parantheses2);
							    
							    
							    formulaTree.addTreeSelectionListener(new TreeSelectionListener() {
									@SuppressWarnings("null")
									public void valueChanged(TreeSelectionEvent e) {
										DefaultMutableTreeNode node = (DefaultMutableTreeNode) formulaTree.getLastSelectedPathComponent();
									
										if (node == null)
											return;
							
											Object objNew = e.getNewLeadSelectionPath().getLastPathComponent();
											formula.append(objNew.toString());
											
									}
								}
										
										);
								JScrollPane treeView = new JScrollPane(formulaTree);
								treeView.setPreferredSize(new Dimension(100,300));
								formulaItemPanel.add(treeView);
								
								Object[] paroptions= {"OK","Cancel"};
								int parameterSelection = JOptionPane.showOptionDialog(null, formulaItemPanel, "Select Parameter",JOptionPane.PLAIN_MESSAGE, 1, null, paroptions, paroptions[0]);
							}
							
							
							
						});
						
						
					

						editRateLawFrame.add(editRateLawPanel);
						editRateLawFrame.setSize(new Dimension(1000,750));
						
						Object[] rateLawAddOptions = {"Apply", "Cancel"};
						int rateLawEditDialog = JOptionPane.showOptionDialog(owner, editRateLawPanel, "Edit rate law", JOptionPane.PLAIN_MESSAGE, 1, null, rateLawAddOptions, rateLawAddOptions[0]);
						
						if (rateLawEditDialog == 0) {
							for (int i = 0 ; i<parameterSplit.length; i++) {
							
							nodeAttributesRow.set(parameterSplit[i], Double.parseDouble(rateLawTable.getValueAt(editRateLawModel.getRowCount()-parameterSplit.length+i, 2).toString()));
						}
						}
					
					}
		    		
		    	});
		    	rateLawBox.add(rateLawLabel);
		    	rateLawBox.add(rateLawCombo);
		    	rateLawBox.add(editRateLaw);
		    	rateLawBox.add(newRateLaw);
		    	add(rateLawBox);
		    	

		    	
		    	apply.addActionListener((ActionListener) new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						
						String finalFormula = rateLawCombo.getSelectedItem().toString();
						nodeAttributesRow.set("reversible", revCheckBox.isSelected());
						
						Boolean isRev = revCheckBox.isSelected();
						ParsingReportGenerator.getInstance().appendLine("reversible: " + isRev);
						if (modelName.contains(".cps")) {
							if (isRev == true) {
								nodeAttributesRow.set("type", "reaction rev");
							} else if (isRev == false) {
								nodeAttributesRow.set("type", "reaction irrev");
							}	
						} else if (modelName.contains(".xml")) {
							if (isRev == true) {
								nodeAttributesRow.set("sbml type", "reaction rev");
							} else if (isRev == false) {
								nodeAttributesRow.set("sbml type", "reaction irrev");
							}
						}
						
						
						nodeAttributesRow.set("Chemical Equation", chemEqField.getText());
						nodeAttributesRow.set("Rate Law", finalFormula);
						nodeAttributesRow.set("Rate Law Formula", functionDB.findFunction(finalFormula).getInfix());
					
							CReaction reaction = model.getReaction(nodeAttributesRow.get("name", String.class));
							
							//reaction.setReversible(revCheckBox.isSelected());
							
							long numPar = reaction.getParameters().size();
							
							for (int j = 0 ; j< numPar; j++) {
								if (reaction.getParameters().getParameter(j).getObjectName().equals(parameterSplit[j]));
								reaction.getParameters().getParameter(j).setDblValue(nodeAttributesRow.get(parameterSplit[j], Double.class));
								reaction.getParameters().getParameter(j).isEditable();
								changedObjects.add(reaction.getParameters().getParameter(j).getValueReference());
								model.compileIfNecessary();
								model.updateInitialValues(changedObjects);
								
								//ParsingReportGenerator.getInstance().appendLine("new parameter value: " + reaction.getParameters().getParameter(j).getObjectName() + ":" +  reaction.getParameters().getParameter(j).getDblValue());
							}
							model.updateInitialValues(changedObjects);
							
						if (modelName.contains(".cps")) {
							dm.saveModel(modelName, true);
						}else if (modelName.contains(".xml")) {
							try {
								dm.exportSBML(modelName, true);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
						dispose();
						
					}
		    		
		    	}
		    			);
		    	
		    	cancel.addActionListener((ActionListener) new ActionListener() {
		    		public void actionPerformed(ActionEvent e) {
		    			dispose();
		    		}
		    	}) ;
		    	
			    
			   add(apply);
			   add(cancel);
			   //updateModel(model, changedObjects);
		    }
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ParsingReportGenerator.getInstance().appendLine("error: " + CCopasiMessage.getAllMessageText());

			} 
		    
		  // double initConc = (double)nodeAttributesRow.get("initial concentration", double.class);
	 }
	 
	// public void updateModel(CModel model, ObjectStdVector changedObjects) {
	//	 model.updateInitialValues(changedObjects);
	// }
	 
	 public ObjectStdVector getChangedObjects() {
		 return changedObjects;
	 }
	 public void showYourself() {
		    setLocationRelativeTo(frame);
		    setVisible(true);
		    this.nameField.requestFocusInWindow();
		  }
		  
		  public void setCreatedNewNode() {
		    this.wasNewlyCreated = true;
		  }
		  
		  private static Thread networkViewUpdater = null;
		  
		  public static void tryNetworkViewUpdate() {
		    if (networkViewUpdater == null) {
		      networkViewUpdater = new Thread() {
		          final VisualMappingManager vmm = CyActivator.visualMappingManager;
		          
		          final CyNetworkView networkView = CyActivator.cyApplicationManager.getCurrentNetworkView();
		          final CyNetwork network = CyActivator.cyApplicationManager.getCurrentNetwork();
		          
		          final VisualStyle visualStyle = this.vmm.getCurrentVisualStyle();
		          
		          public void run() {
		            while (true) {
		              boolean doUpdate = true;
		              try {
		                Thread.sleep(200L);
		              } catch (InterruptedException ex) {
		                doUpdate = false;
		              } 
		              if (doUpdate) {
		                this.visualStyle.apply(this.networkView);
		                this.networkView.updateView();
		              } 
		              synchronized (this) {
		                try {
		                  wait();
		                } catch (InterruptedException interruptedException) {}
		              } 
		            } 
		          }
		        };
		      networkViewUpdater.start();
		    } 
		    synchronized (networkViewUpdater) {
		      networkViewUpdater.notify();
		    } 
		  }
		  
		  public static void dontUpdateNetworkView() {
		    if (networkViewUpdater != null && 
		      networkViewUpdater.getState().equals(Thread.State.TIMED_WAITING))
		      networkViewUpdater.interrupt(); 
		  }
}
