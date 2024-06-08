import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BaseMultiResolutionImage;
import java.time.*;

class MultiplicationThread extends Thread {
  private Socket clientNode;
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
  SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
  public MultiplicationThread(ServerSocket serverNode) {
    try {
      clientNode = serverNode.accept();
    } catch (IOException e) {
      System.err.println("Failed to create sockets");
    } finally {
    	start();
    }
  }
  @Override
  public void run() {
    try {
    	ObjectOutputStream out = new ObjectOutputStream(clientNode.getOutputStream());
    	ObjectInputStream in = new ObjectInputStream(clientNode.getInputStream());
    	try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println(timeForm.format(Calendar.getInstance().getTime())+" Connection accepted: "+ clientNode);
    	} catch (IOException e) {
    		try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
    	            BufferedWriter b = new BufferedWriter(f); 
    	            PrintWriter p = new PrintWriter(b);) {
    			p.println(timeForm.format(Calendar.getInstance().getTime())+"\t"+e);
    		} catch (IOException e2) {
    			System.err.println(e2);
    		}
    	}
    	int[][] M1 = (int[][])in.readObject();
    	int[][] M2 = (int[][])in.readObject();
	    try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
	    BufferedWriter b = new BufferedWriter(f); 
	    PrintWriter p = new PrintWriter(b);) {
	      p.println(timeForm.format(Calendar.getInstance().getTime())+" ReadLine() is succesfull: ");
	    } catch (IOException e) {
	      System.err.println(e);
	    }
	    Instant Start = Instant.now();  
	    int[][] newM=Multiply(M1, M2);
	    Instant Finish = Instant.now();
	    long multiplication = Duration.between(Start, Finish).toMillis();
	    try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
	    BufferedWriter b = new BufferedWriter(f); 
	    PrintWriter p = new PrintWriter(b);) {
	      p.println(timeForm.format(Calendar.getInstance().getTime())+" Multiplication time on this server: " + Long.toString(multiplication));
	    } catch (IOException e) {
	      System.err.println(e);
	    }
	    out.writeObject(newM);
	    out.writeLong(multiplication);
	    out.close();
	    in.close();
	    clientNode.close();
    } catch(Exception e) {
    	try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
	            BufferedWriter b = new BufferedWriter(f); 
	            PrintWriter p = new PrintWriter(b);) {
			p.println(timeForm.format(Calendar.getInstance().getTime())+"\t"+e);
		} catch (IOException e2) {
			System.err.println(e2);
		}
    	}
  }
  public static int[][] Multiply (int[][] M1, int[][] M2) {
	  	int[][] Result =new int[M1.length][M2[0].length];
	    for (int i=0; i<M1.length; ++i) {
	        for (int j=0; j<M2[0].length; ++j) {
	          Result[i][j]=0;
	          for (int k=0; k<M2.length; ++k) {
	            Result[i][j]+=M1[i][k]*M2[k][j];
	          }
	        }
	      }
	  	return Result;
  }
}
class MainWindow extends JFrame {
	private static final long serialVersionUID = 5975871377366251408L;
	private boolean Active = true;
	private boolean Running = false;
	private JButton JBMode = new JButton();
	private JButton JBRun = new JButton();
	private JButton JBEther = new JButton();
	private JButton JBFolder = new JButton();
	private JButton JBServersNumber = new JButton();
	private JButton JBMult = new JButton();
	private JButton JBEqual = new JButton();
	private JFileChooser JFChooser = new JFileChooser();
	private JLabel JLTextCurrent = new JLabel();
	private DefaultListModel<Matrix> matrixListModel = new DefaultListModel<Matrix>();
	private JTable JTMatrix = new JTable();
	private JLMatricesSelectionL ListSelectionL = new JLMatricesSelectionL();
	private ImageIcon ImgAntOn;
	private ImageIcon ImgAntOnDis;
	private ImageIcon ImgAntOff;
	private ImageIcon ImgAntOffDis;
	private ImageIcon ImgStop;
	private ImageIcon ImgStart;
	private ImageIcon ImgEther;
	private ImageIcon ImgEtherDis;
	private ImageIcon ImgFolder;
	private JList<Matrix> JLMatrices;
	private int selectedNetworkInterface=0;
	private int selectedIPnumber = 0;
	private InterfaceAddress selectedIP;
	private ExplorerThread ExplorerThread;
	private ListenerThread ListenerThread;
	private Matrix M1;
	private Matrix M2;
	private boolean M1_selected=false;
	private boolean useAll = true;
	private int serversUsing = 1;
	public MainWindow() {
		super("Распределённое умножение матриц");
		try {
			ImgAntOn = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("antenna_on_100.svg")), 
					ImageIO.read(getClass().getResource("antenna_on_125.svg"))));
			ImgAntOnDis = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("antenna_on_disabled_100.svg")), 
					ImageIO.read(getClass().getResource("antenna_on_disabled_125.svg"))));
			ImgAntOff = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("antenna_off_100.svg")), 
					ImageIO.read(getClass().getResource("antenna_off_125.svg"))));
			ImgAntOffDis = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("antenna_off_disabled_100.svg")), 
					ImageIO.read(getClass().getResource("antenna_off_disabled_125.svg"))));
			ImgStop = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("stop_100.svg")), 
					ImageIO.read(getClass().getResource("stop_125.svg"))));
			ImgStart = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("start_100.svg")), 
					ImageIO.read(getClass().getResource("start_125.svg"))));
			ImgStart = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("start_100.svg")), 
					ImageIO.read(getClass().getResource("start_125.svg"))));
			ImgEther = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("ethernet_100.svg")), 
					ImageIO.read(getClass().getResource("ethernet_125.svg"))));
			ImgEtherDis = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("ethernet_disabled_100.svg")), 
					ImageIO.read(getClass().getResource("ethernet_disabled_125.svg"))));
			ImgFolder = new ImageIcon(new BaseMultiResolutionImage(ImageIO.read(getClass().getResource("folder_100.svg")), 
					ImageIO.read(getClass().getResource("folder_125.svg"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    JFChooser.setCurrentDirectory(new File("Matrices\\"));
	    JFChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".dat","dat");
		JFChooser.setFileFilter(filter);
		JFChooser.setDialogTitle("Открыть");
	    
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		JToolBar JTools = new JToolBar();
		JTools.setFloatable(false);
		JTools.addSeparator();
		JBMode.setIcon(ImgAntOn);
		JBMode.setDisabledIcon(ImgAntOnDis);
		JBMode.setFocusPainted(false); 
		JBMode.setToolTipText("Изменить режим работы узла");
		JBMode.addActionListener(new JBModeActionL());
		JTools.add(JBMode);
		JTools.addSeparator();
		
		JBEther.setIcon(ImgEther);
		JBEther.setDisabledIcon(ImgEtherDis);
		JBEther.setFocusPainted(false); 
		JBEther.setToolTipText("Изменить адаптер");
		JBEther.addActionListener(new JBEtherActionL());
		JTools.add(JBEther);
		JTools.addSeparator();
		
		JBRun.setIcon(ImgStart);
		JBRun.setFocusPainted(false); 
		JBRun.setToolTipText("Запустить сервер");
		JBRun.addActionListener(new JBRunActionL());
		JTools.add(JBRun);
		JTools.addSeparator();
		
		JBFolder.setIcon(ImgFolder);
		JBFolder.setFocusPainted(false); 
		JBFolder.setToolTipText("Открыть матрицу из файла");
		JBFolder.addActionListener(new JBFolderActionL());
		JTools.add(JBFolder);
		JTools.addSeparator();
		
		add(JTools, BorderLayout.NORTH);
		
		JPanel JStatusBar = new JPanel();
		JStatusBar.setLayout(new BorderLayout());
		JStatusBar.setBackground(new Color(230,230,230));
		JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
		JBServersNumber.setBorderPainted(false);
		JBServersNumber.setFocusPainted(false);
		JBServersNumber.setBackground(new Color(230,230,230));
		JBServersNumber.addMouseListener(new JBserversNumberMouseL());
		JBServersNumber.addActionListener(new JBserversNumberActionL());
		JStatusBar.add(JBServersNumber, BorderLayout.WEST);
		add(JStatusBar, BorderLayout.SOUTH);
		
		JPanel JPMain = new JPanel();
		MigLayout layout = new MigLayout ("align left center", "[10%][35%][10%][35%][10%]", "[23%][3%][50%][24%]");
		JPMain.setLayout(layout);
		JLMatrices = new JList<Matrix>(matrixListModel);
		JLMatrices.setCellRenderer(new MatricesRenderer());
		JLMatrices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JLMatrices.addListSelectionListener(ListSelectionL);
		JLabel JLText = new JLabel("Список матриц:");
		JLText.setHorizontalAlignment(SwingConstants.CENTER);
		JLText.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
		JPMain.add(JLText, "cell 1 1, grow");
		JPMain.add(new JScrollPane(JLMatrices), "cell 1 2, grow");
		
		JPanel JPButtons = new JPanel();
		MigLayout JPBlayout = new MigLayout("align left center", "[]", "[11%][11%][11%][11%][11%][11%][33%]");
		JPButtons.setLayout(JPBlayout);
		JBMult.setText("×");
		JBMult.setToolTipText("Необходимо выбрать матрицу");
		JBMult.setEnabled(false);
		JBMult.setFocusPainted(false);
		JBMult.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
		JBMult.addActionListener(new JBMultActionL());
		JPButtons.add(JBMult, "cell 0 1, dock center");
		
		JBEqual.setText("=");
		JBEqual.setToolTipText("Необходимо выбрать две матрицы");
		JBEqual.setEnabled(false);
		JBEqual.setFocusPainted(false);
		JBEqual.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
		JBEqual.addActionListener(new JBEqualActionL());
		JPButtons.add(JBEqual, "cell 0 4, dock center");
		
		JPMain.add(JPButtons, "cell 2 2, grow");
		
		JLTextCurrent.setText("Матрица не выбрана");
		JLTextCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		JLTextCurrent.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
		JPMain.add(JLTextCurrent, "cell 3 1, grow");
		JTMatrix.setTableHeader(null);
		JTMatrix.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		JTMatrix.setDefaultRenderer(Object.class, centerRenderer);
		JTMatrix.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
		JPMain.add(new JScrollPane(JTMatrix), "cell 3 2, grow");
		add(JPMain, BorderLayout.CENTER);
		
		setVisible(true);
		setMinimumSize(new Dimension(500,500));
	    SwingUtilities.invokeLater ( new Runnable() {
	    	public void run() {
	    		new DialogMode();
				if (selectedIP == null) {
					new DialogIP();
				}
	    	}
	    });
	}
	class MatricesRenderer extends JLabel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = 1706458350489650840L;
		
		public MatricesRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Matrix M = (Matrix)value;
			setFont(new Font("Sans-Serif", Font.PLAIN, 16));
			setText("  "+M.getName()+" ("+M.getRowNumber()+"×"+M.getColumnNumber()+")"); 
			 if (isSelected) {
		            setBackground(list.getSelectionBackground());
		            setForeground(list.getSelectionForeground());
	        } else {
		            setBackground(list.getBackground());
		            setForeground(list.getForeground());
		    }
			return this;
		}
	}
	class JLMatricesSelectionL implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			Matrix current = ((JList<Matrix>)e.getSource()).getSelectedValue();
			JTMatrix.setModel(new AbstractTableModel() {
				private static final long serialVersionUID = 1L;
				@Override
	            public int getRowCount() {
	                return current.getRowNumber();
	            }

	            @Override
	            public int getColumnCount() {
	                return current.getColumnNumber();
	            }
	            @Override
	            public Object getValueAt(int rowIndex, int columnIndex) {
	                return current.getData()[rowIndex][columnIndex];
	            }
			});	
			JLTextCurrent.setText("Выбрана матрица: "+current.getName());
			JBMult.setEnabled(true);
			if (!M1_selected) {
				JBMult.setToolTipText("Выбрать вторую матрицу");
			} else {
				JBEqual.setEnabled(true);
				JBEqual.setToolTipText("Перемножить матрицы");
			}
		}
	}
	class JBserversNumberActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
		    SwingUtilities.invokeLater ( new Runnable() {
		    	public void run() {new DialogServers();}
		    }
		    );
		}
	}
	class DialogServers extends JDialog {
		private static final long serialVersionUID = 6234740693862183001L;
		private JComboBox<String> JCNet = new JComboBox<String>();
		public DialogServers() {
			super(MainWindow.this, "Список серверов");
			setResizable(false); 
			setModal(true);
			
			JPanel JPDialog = new JPanel();
			JPDialog.setLayout(new BoxLayout(JPDialog, BoxLayout.Y_AXIS));
			JPanel JPBar = new JPanel();
			JPBar.setOpaque(false);
			JPBar.setLayout(new BoxLayout(JPBar, BoxLayout.X_AXIS));
        	DefaultListModel<serverAddress> listModel = new DefaultListModel<>();
        	for (int i=0; i<Server.serverAddresses.size(); ++i) {
        		listModel.addElement(Server.serverAddresses.get(i));
        	}
        	JList<serverAddress> ServerList = new JList<serverAddress>(listModel);
        	ServerList.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
        	ServerList.setBorder(BorderFactory.createLineBorder(Color.black));
        	JPBar.add(ServerList);
        	JPBar.setAlignmentX(CENTER_ALIGNMENT);
        	JPDialog.add(Box.createVerticalStrut(10));
        	JPDialog.add(JPBar);
        	JPDialog.add(Box.createVerticalStrut(10));
        	
        	JPanel JPChoise = new JPanel();
        	JPChoise.setLayout(new BoxLayout(JPChoise, BoxLayout.X_AXIS));
        	JPChoise.add(Box.createHorizontalStrut(10));
        	JLabel Choise = new JLabel("Использовать: ");
        	Choise.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
        	JPChoise.add(Choise);
        	JCNet = new JComboBox<String>();
        	for (int i=1; i<=Server.serverAddresses.size(); ++i) {
        		JCNet.addItem(Integer.toString(i));
        	}
        	JCNet.addItem("Все");
        	if (useAll) {
        		JCNet.setSelectedItem("Все");
        	} else {
        		JCNet.setSelectedItem(serversUsing);
        	}
        	JPChoise.add(JCNet);
        	JPChoise.add(Box.createHorizontalStrut(10));
        	JPDialog.add(JPChoise);
        	JPDialog.add(Box.createVerticalStrut(10));
        	
        	JPanel JPOk = new JPanel();
			JPOk.setLayout(new BoxLayout(JPOk, BoxLayout.X_AXIS));
			JButton JBOk = new JButton("OK");
			JBOk.setAlignmentX(CENTER_ALIGNMENT);
			JBOk.addActionListener(new JBOkActionL());
			JPOk.add(JBOk);
			JPDialog.add(JPOk);
			JPDialog.add(Box.createVerticalStrut(10));
			
			add(JPDialog);
			pack();
			setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
			setVisible(true);
		}
		class JBOkActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				if (JCNet.getSelectedItem().equals("Все")) {
					useAll = true;
				} else {
					useAll = false;
					serversUsing=Integer.parseInt((String)JCNet.getSelectedItem());
				}
				dispose();
			}
		}
	}
	class JBserversNumberMouseL implements MouseListener {
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {
			((JButton)e.getSource()).setBackground(new Color(220,220,220));
		}
		public void mouseExited(MouseEvent e) {
			((JButton)e.getSource()).setBackground(new Color(230,230,230));
		}
	}
	class JBMultActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if (!M1_selected) {
				DefaultListModel<Matrix> newListModel = new DefaultListModel<Matrix>();
				M1 = JLMatrices.getSelectedValue();
				for (int i=0; i<matrixListModel.size(); ++i) {
					if (M1.getColumnNumber()==matrixListModel.getElementAt(i).getRowNumber()) {
						newListModel.addElement(matrixListModel.getElementAt(i));
					}
				}
				JLMatrices.removeListSelectionListener(ListSelectionL);
	        	JLMatrices.setModel(newListModel);
	        	JLMatrices.addListSelectionListener(ListSelectionL);
				JTMatrix.setModel(new DefaultTableModel());
				JLTextCurrent.setText("Матрица не выбрана");
				JBMult.setToolTipText("Отменить выбор");
				JBMult.setText("-");
				M1_selected = true;
			} else {
				JLMatrices.removeListSelectionListener(ListSelectionL);
	        	JLMatrices.setModel(matrixListModel);
	        	JLMatrices.addListSelectionListener(ListSelectionL);
				JTMatrix.setModel(new DefaultTableModel());
				JLTextCurrent.setText("Матрица не выбрана");
				JBMult.setToolTipText("Выбрать вторую матрицу");
				JBMult.setText("×");
				JBMult.setEnabled(false);
				M1_selected = false;
				JBEqual.setToolTipText("Необходимо выбрать две матрицы");
				JBEqual.setEnabled(false);
			}
		}
	}
	class JBEqualActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			M2 = JLMatrices.getSelectedValue();
			JLMatrices.removeListSelectionListener(ListSelectionL);
        	JLMatrices.setModel(matrixListModel);
        	JLMatrices.addListSelectionListener(ListSelectionL);
			JTMatrix.setModel(new DefaultTableModel());
			JLTextCurrent.setText("Матрица не выбрана");
			JBMult.setToolTipText("Выбрать вторую матрицу");
			JBMult.setText("×");
			JBMult.setEnabled(false);
			M1_selected = false;
			JBEqual.setToolTipText("Необходимо выбрать две матрицы");
			JBEqual.setEnabled(false);
		    SwingUtilities.invokeLater ( new Runnable() {
		    	public void run() {new DialogMultiply();}
		    }
		    );
		}
	}
	class JBEtherActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
		    SwingUtilities.invokeLater ( new Runnable() {
		    	public void run() {new DialogIP();}
		    }
		    );
		}
	}
	class JBModeActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
		    SwingUtilities.invokeLater ( new Runnable() {
		    	public void run() {new DialogMode();
		    	}
		    }
		    );
		}
	}
	class JBRunActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
		    if (Running) {
				JBRun.setIcon(ImgStart);
				JBRun.setToolTipText("Запустить сервер");
				ExplorerThread.stopThread();
			    ListenerThread.stopThread();
			    Server.serverAddresses.clear();
			    JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
			    
		    } else {
				JBRun.setIcon(ImgStop);
				JBRun.setToolTipText("Остановить сервер");
				try {
			        ExplorerThread = new ExplorerThread();
			        ListenerThread = new ListenerThread();
			      } catch(IOException e) {
			        System.err.println(e);
			    }
		    }
		    Running = !Running;
		    JBMode.setEnabled(!JBMode.isEnabled());
		    JBEther.setEnabled(!JBEther.isEnabled());
		}
	}
	class JBFolderActionL implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			int returnVal = JFChooser.showOpenDialog(MainWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	SwingUtilities.invokeLater ( new Runnable() {
			    	public void run() {new DialogProgress();
			    	}
			    }
			    );
			}
		}
		class DialogProgress extends JDialog {
			private static final long serialVersionUID = 1L;
			private JProgressBar progressBar = new JProgressBar(0,1);
			DialogProgress() {
				super(MainWindow.this, "Идёт загрузка файла...");
				setResizable(false); 
				setModal(true);
				JPanel JPDialog = new JPanel();
				JPDialog.setBackground(Color.WHITE);
				JPDialog.setLayout(new BoxLayout(JPDialog, BoxLayout.X_AXIS));
				JPanel JPBar = new JPanel();
				JPBar.setOpaque(false);
				JPBar.setLayout(new BoxLayout(JPBar, BoxLayout.Y_AXIS));
				progressBar.setValue(0);
	        	progressBar.setStringPainted(true);
	        	progressBar.setFont(new Font("Sans-Serif", Font.BOLD, 13));
	        	progressBar.setForeground(new Color(0,200,0));
	        	JPBar.add(Box.createVerticalStrut(10));
	        	JPBar.add(progressBar);
	        	JPBar.add(Box.createVerticalStrut(10));
	        	JPDialog.add(Box.createHorizontalStrut(10));
	        	JPDialog.add(JPBar);
	        	JPDialog.add(Box.createHorizontalStrut(10));
	        	add(JPDialog);
				pack();
				setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
				new LoadingThread();
				setVisible(true);
		        
			}
			class LoadingThread extends Thread {
				public LoadingThread() {
					start();
				}
				@Override
			    public void run() {
					try (Scanner sc = new Scanner(JFChooser.getSelectedFile())) {
			        	int l = sc.nextInt();
			        	int n = sc.nextInt();
			        	int totalLength = l*n;
			        	progressBar.setMaximum(totalLength);
			            int readLength = 0;
			        	int[][] M = new int[l][n];
			        	for (int i=0; i<l; ++i) {
			        		for (int j=0; j<n; ++j) {
			        			M[i][j]=sc.nextInt();
			        			readLength+=1;
			        			progressBar.setValue(readLength);
			        		}
			        	}
			        	matrixListModel.addElement(new Matrix(JFChooser.getSelectedFile().getName().replaceFirst("[.][^.]+$", ""), M));
			        	JLMatrices.removeListSelectionListener(ListSelectionL);
			        	JLMatrices.setModel(matrixListModel);
			        	JLMatrices.addListSelectionListener(ListSelectionL);
			        }
			        catch (FileNotFoundException e) {
						System.out.println(e);
					} 
					DialogProgress.this.dispose();
				}
			}
		}
	}
	class DialogMultiply extends JDialog {
		private static final long serialVersionUID = 2808843896374862021L;
		private JPanel JPMain = new JPanel();
		private JTextField ResultName = new JTextField();
		public DialogMultiply() {
			super(MainWindow.this, "Перемножить матрицы");
			setResizable(false); 
			setModal(true);
			
			JPMain.setLayout(new BoxLayout(JPMain, BoxLayout.Y_AXIS));
			JPMain.setBackground(Color.WHITE);
			
			JLabel JLmessageM1 = new JLabel("Первая матрица: "+M1.getName());
			JLmessageM1.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
			JLmessageM1.setForeground(new Color(60,90,170));
			JPanel JPmessageM1 = new JPanel();
			JPmessageM1.add(Box.createHorizontalStrut(10));
			JPmessageM1.add(JLmessageM1);
			JPmessageM1.add(Box.createHorizontalStrut(10));
			JPmessageM1.setOpaque(false);
			JPMain.add(Box.createVerticalStrut(5));
			JPMain.add(JPmessageM1);
			JPMain.add(Box.createVerticalStrut(5));
			
			JLabel JLmessageM2 = new JLabel("Вторая матрица: "+M2.getName());
			JLmessageM2.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
			JLmessageM2.setForeground(new Color(60,90,170));
			JPanel JPmessageM2 = new JPanel();
			JPmessageM2.add(Box.createHorizontalStrut(10));
			JPmessageM2.add(JLmessageM2);
			JPmessageM2.add(Box.createHorizontalStrut(10));
			JPmessageM2.setOpaque(false);
			JPMain.add(JPmessageM2);
			JPMain.add(Box.createVerticalStrut(5));
			
			JPanel JPM3 = new JPanel();
			JPM3.setLayout(new BoxLayout(JPM3, BoxLayout.X_AXIS));
			JPM3.setOpaque(false);
			JPM3.add(Box.createHorizontalStrut(10));
			JLabel JLM3 = new JLabel("Результирующая матрица: ");
			JLM3.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
			JLM3.setForeground(new Color(60,90,170));
			JPM3.add(JLM3);
			JPM3.add(Box.createHorizontalStrut(10));
			ResultName = new JTextField("R_"+M1.getRowNumber()+"_"+M2.getColumnNumber());
			JPM3.add(ResultName);
			JPM3.add(Box.createHorizontalStrut(10));
			JPMain.add(JPM3);
			JPMain.add(Box.createVerticalStrut(5));
			
			JPanel JPOk = new JPanel();
			JPOk.setLayout(new BoxLayout(JPOk, BoxLayout.Y_AXIS));
			JPOk.add(Box.createVerticalStrut(10));
			JButton JBOk = new JButton("OK");
			JBOk.setAlignmentX(CENTER_ALIGNMENT);
			JBOk.addActionListener(new JBOkActionL());
			JPOk.add(JBOk);
			JPOk.add(Box.createVerticalStrut(10));
			JPMain.add(JPOk);
			
			add(JPMain);
			
			pack();
			setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
			setVisible(true);
		}
		class JBOkActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				new WaitingThread();
			}
		}
		class WaitingThread extends Thread {
			public WaitingThread() {
				start();
			}
			@Override
		    public void run() {
				DialogMultiply.this.remove(JPMain);
				JPanel JPDialog = new JPanel();
				JPDialog.setBackground(Color.WHITE);
				JPDialog.setLayout(new BoxLayout(JPDialog, BoxLayout.X_AXIS));
				JPanel JPBar = new JPanel();
				JPBar.setOpaque(false);
				JPBar.setLayout(new BoxLayout(JPBar, BoxLayout.Y_AXIS));
	        	JPBar.add(Box.createVerticalStrut(10));
	        	JLabel Label = new JLabel("Идёт процесс перемножения");
	        	Label.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
	        	Label.setForeground(new Color(60,90,170));
	        	JPBar.add(Label);
	        	JPBar.add(Box.createVerticalStrut(10));
	        	JPDialog.add(Box.createHorizontalStrut(10));
	        	JPDialog.add(JPBar);
	        	JPDialog.add(Box.createHorizontalStrut(10));
	        	add(JPDialog);
				pack();
				Client.M1 = M1.getData();
				Client.M2 = M2.getData();
				if (useAll) {
					Client.useNs = Server.serverAddresses.size();
				} else {
					Client.useNs = serversUsing;
				}
				try {
					Client.main(null);
				} catch (IOException e) {
					System.out.println(e);
				}
				matrixListModel.addElement(new Matrix(ResultName.getText(), Client.Result));
	        	JLMatrices.removeListSelectionListener(ListSelectionL);
	        	JLMatrices.setModel(matrixListModel);
	        	JLMatrices.addListSelectionListener(ListSelectionL);
				DialogMultiply.this.dispose();
			}
		}
	}
	class DialogIP extends JDialog {
		private static final long serialVersionUID = -4826144205570259432L;
		private ArrayList<String> NetList = new ArrayList<String>();
		private ArrayList<ArrayList<String>> addrList = new ArrayList<ArrayList<String>>();
		private ArrayList<ArrayList<InterfaceAddress>> Addresses = new ArrayList<ArrayList<InterfaceAddress>>();
		private JComboBox<String> JCNet = new JComboBox<String>();
		private JComboBox<String> JCIP = new JComboBox<>();
		public DialogIP() {
			super(MainWindow.this, "Выбор адаптера");
			setResizable(false); 
			setModal(true);
			
			try {
				for(Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces(); eni.hasMoreElements(); ) {
					NetworkInterface ifc = eni.nextElement();
					if(ifc.isUp() && !ifc.isLoopback()) {
						NetList.add(ifc.getDisplayName());
						addrList.add(new ArrayList<String>());
						Addresses.add(new ArrayList<InterfaceAddress>());
						for(InterfaceAddress ena : ifc.getInterfaceAddresses()) {
							if ( ena.getAddress() instanceof Inet4Address) {
								addrList.get(addrList.size()-1).add(ena.getAddress().getHostAddress()+"/"+ena.getNetworkPrefixLength());
								Addresses.get(addrList.size()-1).add(ena);
							}
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
			JPanel JPDialog = new JPanel();
			JPDialog.setLayout(new BoxLayout(JPDialog, BoxLayout.Y_AXIS));
			JPDialog.setBackground(Color.WHITE);
			
			JPDialog.add(Box.createVerticalStrut(5));
			JPanel JPText = new JPanel();
			JPText.setOpaque(false);
			JLabel JLText = new JLabel("Выберите адаптер и IP-адрес.");
			JLText.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
			JLText.setForeground(new Color(60,90,170));
			JPText.add(JLText);
			JPDialog.add(JPText);
			JPDialog.add(Box.createVerticalStrut(5));
			
			JPanel JPMain = new JPanel();
			JPMain.setLayout(new BoxLayout(JPMain, BoxLayout.X_AXIS));
			JPMain.setOpaque(false);
			
			JPMain.add(Box.createHorizontalStrut(10));
			JPanel JPLabels = new JPanel();
			JPLabels.setLayout(new BoxLayout(JPLabels, BoxLayout.Y_AXIS));
			JPLabels.setOpaque(false);
			JLabel JLNet = new JLabel("Адаптер:");
			JPLabels.add(JLNet);
			JPLabels.add(Box.createVerticalStrut(10));
			JLabel JLIP = new JLabel("IP-адрес:");
			JPLabels.add(JLIP);
			JPMain.add(JPLabels);
			JPMain.add(Box.createHorizontalStrut(5));
			
			JPanel JPCombox = new JPanel();
			JPCombox.setLayout(new BoxLayout(JPCombox, BoxLayout.Y_AXIS));
			JPCombox.setOpaque(false);
			JCNet = new JComboBox<>(NetList.toArray(new String[0]));
			JCNet.setSelectedIndex(selectedNetworkInterface);
			JCNet.addActionListener(new JCNetActionL());
			JPCombox.add(JCNet);
			JPCombox.add(Box.createVerticalStrut(5));
			JCIP = new JComboBox<>(addrList.get(selectedNetworkInterface).toArray(new String[0]));
			JCIP.setSelectedIndex(selectedIPnumber);
			selectedIP = Addresses.get(selectedNetworkInterface).get(selectedIPnumber);
			JCIP.addActionListener(new JCIPActionL());
			JPCombox.add(JCIP, BorderLayout.EAST);
			JPMain.add(JPCombox);
			JPMain.add(Box.createHorizontalStrut(10));
			JPDialog.add(JPMain);
			JPDialog.add(Box.createVerticalStrut(10));
			
			JPanel JPOk = new JPanel();
			JPOk.setLayout(new BoxLayout(JPOk, BoxLayout.Y_AXIS));
			JPOk.add(Box.createVerticalStrut(10));
			JButton JBOk = new JButton("OK");
			JBOk.setAlignmentX(CENTER_ALIGNMENT);
			JBOk.addActionListener(new JBOkActionL());
			JPOk.add(JBOk);
			JPOk.add(Box.createVerticalStrut(10));
			JPDialog.add(JPOk);
			
			add(JPDialog);
			
			pack();
			setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
			setVisible(true);
		}
		class JCNetActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				selectedNetworkInterface=JCNet.getSelectedIndex();
				selectedIPnumber = 0;
				selectedIP=Addresses.get(selectedNetworkInterface).get(0);
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(addrList.get(selectedNetworkInterface).toArray(new String[0]));
				JCIP.setModel(model);
			}
		}
		class JCIPActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				selectedIPnumber=JCIP.getSelectedIndex();
				selectedIP=Addresses.get(selectedNetworkInterface).get(selectedIPnumber);
			}
		}
		class JBOkActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		}
	}
	class DialogMode extends JDialog {
		private static final long serialVersionUID = 1823515078827607563L;
		private ButtonGroup BGmode;
		public DialogMode() {
			super(MainWindow.this, "Режим работы узла");
			setResizable(false); 
			setModal(true);
			
			JPanel JPMain = new JPanel();
			JPMain.setLayout(new BoxLayout(JPMain, BoxLayout.Y_AXIS));
			JPMain.setBackground(Color.WHITE);
			
			JTextArea JLmessage = new JTextArea("Выберите режим работы узла. \nВ пассивном режиме узел не сообщает о своём присутствии в сети и не учавствует в перемножении матриц.", 4, 25);
			JLmessage.setLineWrap(true);
			JLmessage.setWrapStyleWord(true);
			JLmessage.setFont(new Font("Sans-Serif", Font.PLAIN, 13));
			JLmessage.setForeground(new Color(60,90,170));
			JPanel JPmessage = new JPanel();
			JPmessage.add(Box.createHorizontalStrut(10));
			JPmessage.add(JLmessage);
			JPmessage.add(Box.createHorizontalStrut(10));
			JPmessage.setOpaque(false);
			JPMain.add(Box.createVerticalStrut(5));
			JPMain.add(JPmessage);
			JPMain.add(Box.createVerticalStrut(5));
			
			JRadioButton JRBactive = new JRadioButton("активный");
			JRBactive.setActionCommand("активный");
			JRBactive.setOpaque(false);
			JRadioButton JRBpassive = new JRadioButton("пассивный");
			JRBpassive.setActionCommand("пассивный");
			JRBpassive.setOpaque(false);
			if (Active)
				JRBactive.setSelected(true);
			else 
				JRBpassive.setSelected(true);
			BGmode = new ButtonGroup();
			BGmode.add(JRBactive);
			BGmode.add(JRBpassive);
			JPanel JPGroup = new JPanel();
			JPGroup.setLayout(new BoxLayout(JPGroup, BoxLayout.X_AXIS));
			JPGroup.add(JRBactive);
			JPGroup.add(JRBpassive);
			JPGroup.setAlignmentX(CENTER_ALIGNMENT);
			JPGroup.setOpaque(false);
			JPMain.add(JPGroup);
			JPMain.add(Box.createVerticalStrut(5));
			
			JPanel JPOk = new JPanel();
			JPOk.setLayout(new BoxLayout(JPOk, BoxLayout.Y_AXIS));
			JPOk.add(Box.createVerticalStrut(10));
			JButton JBOk = new JButton("OK");
			JBOk.setAlignmentX(CENTER_ALIGNMENT);
			JBOk.addActionListener(new JBOkActionL());
			JPOk.add(JBOk);
			JPOk.add(Box.createVerticalStrut(10));
			JPMain.add(JPOk);
			
			add(JPMain);
			
			pack();
			setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
			setVisible(true);
		}
		class JBOkActionL implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				Active = BGmode.getSelection().getActionCommand().equals("активный");
				JBMode.setIcon(Active?ImgAntOn:ImgAntOff);
				JBMode.setDisabledIcon(Active?ImgAntOnDis:ImgAntOffDis);
				dispose();
			}
		}
	}
	class ExplorerThread extends Thread {
		  private DatagramSocket ExplorerSocket;
		  private boolean stopFlag;
		  public ExplorerThread() throws IOException {
		    ExplorerSocket = new DatagramSocket(Server.ExplorerPORT, selectedIP.getAddress());
		    ExplorerSocket.setBroadcast(true);
		    stopFlag = false;
		    start();
		  }
		  synchronized void stopThread() {
			  stopFlag = true;
		  }
		  @Override
		  public void run() {
		    while (!stopFlag) {
		      boolean deleted = false;
		      for (serverAddress i : Server.serverAddresses) {
		        i.setLifespan(i.getLifespan()-1);
		        if (i.getLifespan() == 0) {
		          try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+Server.formatter.format(Calendar.getInstance().getTime())+".txt", true); 
		          BufferedWriter b = new BufferedWriter(f); 
		          PrintWriter p = new PrintWriter(b);) {
		            p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" "+i.getAddress() + " Doesn't answer for too long");
		          } catch (IOException e) {
		            System.err.println(e);
		          }
		          deleted = true;
		        }
		      }
		      Server.serverAddresses.removeIf(n->(n.getLifespan() == 0));
		      JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
		      if (deleted) {
		        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+Server.formatter.format(Calendar.getInstance().getTime())+".txt", true); 
		        BufferedWriter b = new BufferedWriter(f); 
		        PrintWriter p = new PrintWriter(b);) {
		          p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" Servers number is " + Server.serverAddresses.size());
		        } catch (IOException e) {
		          System.err.println(e);
		        }
		      }
		      if (Active) {
		        String Message = "Hello";
		        byte[] buffer = Message.getBytes(StandardCharsets.UTF_8);
		        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, selectedIP.getBroadcast(), Server.ListenerPORT);
		        try {
		          ExplorerSocket.send(packet);
		        } catch (IOException e) {
		          System.err.println("Failed to send message");
		        }
		      }
		      try {
		    	  if (!stopFlag) {
		    		  Thread.sleep(1000);
		    	  }
		      } catch (InterruptedException e){
		        Thread.currentThread().interrupt();
		        System.err.println(e);
		      }
		    }
		    String EndMessage = "Break";
	        byte[] buffer = EndMessage.getBytes(StandardCharsets.UTF_8);
	        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, selectedIP.getBroadcast(), Server.ListenerPORT);
	        try {
	          ExplorerSocket.send(packet);
	        } catch (IOException e) {
	          System.err.println("Failed to send message");
	        }
		    ExplorerSocket.close();
		  }
		}
	class ListenerThread extends Thread {
		  private DatagramSocket ListenerSocket;
		  private byte[] buffer;
		  private DatagramPacket packet;
		  private boolean stopFlag;
		  ListenerThread () throws IOException {
		    ListenerSocket = new DatagramSocket(Server.ListenerPORT, selectedIP.getAddress());
		    ListenerSocket.setSoTimeout(2000);
		    buffer = new byte[5];
		    packet = new DatagramPacket(buffer, 5);
		    stopFlag = false;
		    start();
		  }
		  synchronized void stopThread() {
			  stopFlag = true;
		  }
		  @Override
		  public void run() {
		    while(!stopFlag) {
		      try {
		        ListenerSocket.receive(packet);
		        String message = new String(packet.getData(), StandardCharsets.UTF_8);
		        InetAddress received = packet.getAddress();
		        if (message.equals("Break")) {
		        	if (Server.serverAddresses.contains(new serverAddress(received))) {
		        		Server.serverAddresses.remove(new serverAddress(received));
		        		JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
		        	}
		        	try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+Server.formatter.format(Calendar.getInstance().getTime())+".txt", true); 
				              BufferedWriter b = new BufferedWriter(f); 
				              PrintWriter p = new PrintWriter(b);) {
				        	  	  p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" Server send break signal: " + received);
				        	  	  p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" Servers number is " + (Server.serverAddresses.size()));
				          } catch (IOException e) {
				        	  System.err.println(e);
				    }
		        }
		        else if (message.equals("Hello")&&!Server.serverAddresses.contains(new serverAddress(received))) {
		        	Server.serverAddresses.add(new serverAddress(received));
		        	JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
		          try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+Server.formatter.format(Calendar.getInstance().getTime())+".txt", true); 
		              BufferedWriter b = new BufferedWriter(f); 
		              PrintWriter p = new PrintWriter(b);) {
		        	  	  p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" Got hello from " + received);
		        	  	  p.println(Server.timeForm.format(Calendar.getInstance().getTime())+" Servers number is " + Server.serverAddresses.size());
		          } catch (IOException e) {
		        	  System.err.println(e);
		          }
		        } else {
		        	Server.serverAddresses.get(Server.serverAddresses.indexOf(new serverAddress(received))).setLifespan(10);
		        }
		      } catch (IOException e){
		    	  System.err.println(e);
		      	}
		    }
		    ListenerSocket.close();
		  }
		}
}
class serverAddress {
	private int Lifespan;
	private InetAddress Address;
	public serverAddress(InetAddress Address) {
		this.Address = Address;
		this.Lifespan = 10;
	}
	public serverAddress(InetAddress Address, int Lifespan) {
		this.Address = Address;
		this.Lifespan = Lifespan;
	}
	public int getLifespan() {
		return Lifespan;
	}
	public InetAddress getAddress() {
		return Address;
	}
	public void setLifespan(int Lifespan) {
		this.Lifespan = Lifespan;
	}
	public void setAddress(InetAddress Address) {
		this.Address = Address;
	}
	@Override
	public int hashCode() {
		return Objects.hash(Address);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		serverAddress other = (serverAddress) obj;
		return Objects.equals(Address, other.Address);
	}
	@Override
	public String toString() {
		return Address.getHostAddress();
	}
}
class Matrix {
	private int[][] data;
	private int columnNumber;
	private int rowNumber;
	private String name;
	public Matrix(String name, int[][] data) {
		this.data = data;
		this.name = name;
		rowNumber = data.length;
		columnNumber = data[0].length;
	}
	public int[][] getData() {
		return data;
	}
	public int getColumnNumber() {
		return columnNumber;
	}
	public int getRowNumber() {
		return rowNumber;
	}
	public String getName() {
		return name;
	}
}
public class Server {
    static int PORT = 9999;
    static int ListenerPORT = 9998;
    static int ExplorerPORT = 9997;
    static List <serverAddress> serverAddresses = new ArrayList<serverAddress>();
    static SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
    static SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
    public static void main(String[] args) throws IOException{
		UIManager.put("FileChooser.openButtonText", "Открыть");
		UIManager.put("FileChooser.openButtonToolTipText", "Открыть выбранный файл");
	    UIManager.put("FileChooser.cancelButtonText", "Отмена");
	    UIManager.put("FileChooser.cancelButtonToolTipText", "Прервать диалог выбора файла");
	    UIManager.put("FileChooser.lookInLabelText", "Смотреть в");
	    UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
	    UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла");
	    UIManager.put("FileChooser.upFolderToolTipText", "На один уровень вверх");
	    UIManager.put("FileChooser.newFolderToolTipText", "Создание новой папки");
	    UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
	    UIManager.put("FileChooser.createButtonText", "Обновить");
	    UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблица");
	    UIManager.put("FileChooser.homeFolderToolTipText", "Рабочий стол");
	    UIManager.put("FileChooser.fileNameHeaderText", "Имя");
	    UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
	    UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
	    UIManager.put("FileChooser.fileDateHeaderText", "Изменён");
	    String log = Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt";
	    ServerSocket serverNode = new ServerSocket(PORT);
	    try (FileWriter f = new FileWriter(log, true); 
	    		BufferedWriter b = new BufferedWriter(f); 
	    		PrintWriter p = new PrintWriter(b);) {
	    	p.println(timeForm.format(Calendar.getInstance().getTime())+" Program has started. "+serverNode);
	    }
	    SwingUtilities.invokeLater ( new Runnable() {
	    	public void run() {new MainWindow();}
	    });
	    while (true) {
	    	new MultiplicationThread(serverNode);
	    }
    }
}