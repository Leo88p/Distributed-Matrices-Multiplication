import java.io.*;
import java.lang.reflect.Array;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import org.apache.batik.swing.JSVGCanvas;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
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
            System.err.println(e);
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
    	System.err.println("Error has occured"); 
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
	private JButton JBServersNumber = new JButton();
	private ImageIcon ImgAntOn;
	private ImageIcon ImgAntOnDis;
	private ImageIcon ImgAntOff;
	private ImageIcon ImgAntOffDis;
	private ImageIcon ImgStop;
	private ImageIcon ImgStart;
	private ImageIcon ImgEther;
	private ImageIcon ImgEtherDis;
	private int selectedNetworkInterface=0;
	private int selectedIPnumber = 0;
	private InterfaceAddress selectedIP;
	private ExplorerThread ExplorerThread;
	private ListenerThread ListenerThread;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		add(JTools, BorderLayout.NORTH);
		
		JPanel JStatusBar = new JPanel();
		JStatusBar.setLayout(new BorderLayout());
		JStatusBar.setBackground(new Color(230,230,230));
		JBServersNumber.setText("Количество активных серверов: " + Server.serverAddresses.size());
		JBServersNumber.setBorderPainted(false);
		JBServersNumber.setFocusPainted(false);
		JBServersNumber.setBackground(new Color(230,230,230));
		JBServersNumber.addMouseListener(new JBserversNumberMouseL());
		JStatusBar.add(JBServersNumber, BorderLayout.WEST);
		add(JStatusBar, BorderLayout.SOUTH);
		
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
/* class WindowMode {
  JRadioButton active, passive;
  ButtonGroup mode;
  Button OK;
  TextArea text;
  boolean started = false;
  FileDialog fd1 = new FileDialog(this);
  FileDialog fd2 = new FileDialog(this);
  TextField tf1;
  Button Matrix1;
  Button Matrix2;
  Button Mult;
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
  SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
  public Window() {
    setLayout(new FlowLayout());
    String str = "Выберите режим работы узла. \n" +
    "В пассивном режиме узел не сообщает о своём присутствии в сети и не учавствует в перемножении матриц \n";
    text = new TextArea(str, 3, 30, TextArea.SCROLLBARS_NONE);
    text.setEditable(false);
    text.setBackground(Color.white);
    new java.awt.Font(Font.MONOSPACED, Font.PLAIN, 6);
    mode = new CheckboxGroup();
    active = new Checkbox("активный", mode, true);
    passive = new Checkbox("пассивный", mode, false);
    OK = new Button("ОК");
    add(text);
    add(active);
    add(passive);
    add(OK);
    OK.addActionListener(this);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    setSize(300,150);
    setTitle("Распределённое умножение матриц");
  }
  public void actionPerformed(ActionEvent ae) {
    String name = ae.getActionCommand();
    if (name.equals("ОК")) {
      try {
        new ExplorerThread(Server.ExplorerPORT, Server.ListenerPORT, Server.serverAddresses, Server.Lifespan, mode.getSelectedCheckbox().getLabel().equals("активный"), this);
        new ListenerThread(Server.ListenerPORT, Server.serverAddresses, Server.Lifespan, this);
      } catch(IOException e) {
        System.err.println(e);
      }
      remove(text);
      remove(active);
      remove(passive);
      remove(OK);
      started = true;
      Matrix1 = new Button("Выбрать первую матрицу");
      Matrix2 = new Button("Выбрать вторую матрицу");
      Mult = new Button("Перемножить матрицы");
      Matrix1.setActionCommand("Matrix1");
      Matrix2.setActionCommand("Matrix2");
      Mult.setActionCommand("Mult");
      Matrix1.addActionListener(this);
      Matrix2.addActionListener(this);
      Mult.addActionListener(this);
      Mult.setEnabled(false);
      tf1 = new TextField("1");
      add(tf1);
      add(Matrix1);
      add(Matrix2);
      add(Mult);
      validate();
    } else if (name.equals("Matrix1")) {
      fd1 = new FileDialog(this);
      fd1.setDirectory("Matrices\\");
      fd1.setFile("*.dat");
      fd1.setVisible(true);
      Client.FileName1 = fd1.getDirectory()+fd1.getFile();
      if (fd1.getFile()!=null) {
        Matrix1.setLabel("Изменить первую матрицу");
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" File of the first matrix is " + fd1.getFile());
        } catch (IOException e) {
          System.err.println(e);
        }
      } else {
        Matrix1.setLabel("Выбрать первую матрицу");
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" No file of the first matrix");
        } catch (IOException e) {
          System.err.println(e);
        }
      }
      repaint();
    } else if (name.equals("Matrix2")) {
      fd2 = new FileDialog(this);
      fd2.setDirectory("Matrices\\");
      fd2.setFile("*.dat");
      fd2.setVisible(true);
      Client.FileName2 = fd2.getDirectory()+fd2.getFile();
      if (fd2.getFile()!=null) {
        Matrix2.setLabel("Изменить вторую матрицу");
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" File of the second matrix is " + fd2.getFile());
        } catch (IOException e) {
          System.err.println(e);
        }
      } else {
        Matrix2.setLabel("Выбрать вторую матрицу");
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" No file of the second matrix");
        } catch (IOException e) {
          System.err.println(e);
        }
      }
      repaint();
    } else if (name.equals("Mult")) {
      try {
    	Client.useNs = Integer.parseInt(tf1.getText());
        Client.main(null);
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }
  public void paint(Graphics g) {
    if (started) g.drawString("Число активных серверов: " + Server.serverAddresses.size(), 10, 130);
    if (Server.serverAddresses.size()>0 & fd1.getFile()!=null & fd2.getFile()!=null) {
      Mult.setEnabled(true);
    }
  }
} */
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
}
public class Server {
    static int PORT = 9999;
    static int ListenerPORT = 9998;
    static int ExplorerPORT = 9997;
    static List <serverAddress> serverAddresses = new ArrayList<serverAddress>();
    static List <Integer> Lifespan = new ArrayList<Integer>();
    static SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
    static SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
    public static void main(String[] args) throws IOException{
      String log = Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt";
      ServerSocket serverNode = new ServerSocket(PORT);
      try (FileWriter f = new FileWriter(log, true); 
      BufferedWriter b = new BufferedWriter(f); 
      PrintWriter p = new PrintWriter(b);) {
        p.println(timeForm.format(Calendar.getInstance().getTime())+" Program has started. "+serverNode);
      }
      SwingUtilities.invokeLater ( new Runnable() {
    	  public void run() {new MainWindow();}
      }  
      );
      while (true) {
        new MultiplicationThread(serverNode);
      }
    }
}