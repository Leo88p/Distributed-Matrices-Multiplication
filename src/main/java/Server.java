import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.time.*;

class ExplorerThread extends Thread {
  private DatagramSocket ExplorerSocket;
  private int LPORT;
  private InetAddress Broadcast;
  private List<InetAddress> serverAddresses;
  private List<Integer> Lifespan;
  private boolean active;
  private Window window;
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
  SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
  public ExplorerThread(int PORT, int LPORT, List<InetAddress> serverAddresses, List<Integer> Lifespan, boolean active, Window window) throws IOException {
    this.LPORT = LPORT;
    this.serverAddresses = serverAddresses;
    this.Lifespan = Lifespan;
    this.active = active;
    this.window = window;
    Broadcast = InetAddress.getByName("255.255.255.255");
    ExplorerSocket = new DatagramSocket(PORT);
    ExplorerSocket.setBroadcast(true);
    start();
  }
  @Override
  public void run() {
    while (true) {
      boolean deleted = false;
      for (int i=0; i < Lifespan.size(); i++) {
        Lifespan.set(i, Lifespan.get(i) - 1);
        if (Lifespan.get(i) == 0) {
          try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println(timeForm.format(Calendar.getInstance().getTime())+" "+serverAddresses.get(i) + " Doesn't answer for too long");
          } catch (IOException e) {
            System.err.println(e);
          }
          serverAddresses.set(i, Broadcast);
          deleted = true;
        }
      }
      Lifespan.removeIf(n->(n == 0));
      serverAddresses.removeIf(n->(n.equals(Broadcast)));
      window.repaint();
      if (deleted) {
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" Servers number is " + serverAddresses.size());
        } catch (IOException e) {
          System.err.println(e);
        }
      }
      if (active) {
        String Message = "Hello";
        byte[] buffer = Message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, Broadcast, LPORT);
        try {
          ExplorerSocket.send(packet);
        } catch (IOException e) {
          System.err.println("Failed to send message");
        }
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e){
        Thread.currentThread().interrupt();
        System.err.println(e);
      }
    }
  }
}

class ListenerThread extends Thread {
  private DatagramSocket ListenerSocket;
  private byte[] buffer;
  private DatagramPacket packet;
  private List<InetAddress> serverAddresses;
  private List<Integer> Lifespan;
  private Window window;
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
  SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
  ListenerThread (int PORT, List<InetAddress> serverAddresses, List<Integer> Lifespan, Window window) throws IOException {
    this.serverAddresses = serverAddresses;
    this.Lifespan = Lifespan;
    this.window = window;
    ListenerSocket = new DatagramSocket(PORT);
    buffer = new byte[5];
    packet = new DatagramPacket(buffer, 5);
    start();
  }
  @Override
  public void run() {
    while(true) {
      try {
        ListenerSocket.receive(packet);
        InetAddress received = packet.getAddress();
        if (!serverAddresses.contains(received)) {
          serverAddresses.add(received);
          Lifespan.add(10);
          window.repaint();
          try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println(timeForm.format(Calendar.getInstance().getTime())+" Got hello from " + received);
            p.println(timeForm.format(Calendar.getInstance().getTime())+" Servers number is " + serverAddresses.size());
          } catch (IOException e) {
            System.err.println(e);
          }
        } else {
          Lifespan.set(serverAddresses.indexOf(received), 10);
        }
      } catch (IOException e){
        System.err.println("Failed to recive data");
      }
    }
  }
}

class MultiplicationThread extends Thread {
  private Socket clientNode;
  List<InetAddress> serverAddresses;
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
  SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
  public MultiplicationThread(ServerSocket serverNode, List<InetAddress> serverAddresses) {
    this.serverAddresses = serverAddresses;
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
class Window extends Frame implements ActionListener {
  private static final long serialVersionUID = -71397683808689083L;
  Checkbox active, passive;
  Button OK;
  CheckboxGroup mode;
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
}
public class Server {
    static int PORT = 9999;
    static int ListenerPORT = 9998;
    static int ExplorerPORT = 9997;
    static List <InetAddress> serverAddresses = new ArrayList<InetAddress>();
    static List <Integer> Lifespan = new ArrayList<Integer>();
    public static void main(String[] args) throws IOException{
      SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
      SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
      String log = Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt";
      ServerSocket serverNode = new ServerSocket(PORT);
      try (FileWriter f = new FileWriter(log, true); 
      BufferedWriter b = new BufferedWriter(f); 
      PrintWriter p = new PrintWriter(b);) {
        p.println(timeForm.format(Calendar.getInstance().getTime())+" Program has started. "+serverNode);
      }
      Window Window = new Window();
      Window.setVisible(true);
      while (true) {
        new MultiplicationThread(serverNode, serverAddresses);
      }
    }
}