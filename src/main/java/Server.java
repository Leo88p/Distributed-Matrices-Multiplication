import java.io.*;
import java.net.*;
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
          try (FileWriter f = new FileWriter("log.txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println(serverAddresses.get(i) + " Doesn't answer for too long");
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
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("Servers number is " + serverAddresses.size());
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
          Lifespan.add(5);
          window.repaint();
          try (FileWriter f = new FileWriter("log.txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println("Got hello from " + received);
            p.println("Servers number is " + serverAddresses.size());
          } catch (IOException e) {
            System.err.println(e);
          }
        } else {
          Lifespan.set(serverAddresses.indexOf(received), 5);
        }
      } catch (IOException e){
        System.err.println("Failed to recive data");
      }
    }
  }
}

class NewThread extends Thread {
  private Socket clientNode;
  private BufferedReader in;
  private BufferedWriter out;
  List<InetAddress> serverAddresses;
  public NewThread(ServerSocket serverNode, List<InetAddress> serverAddresses) {
    this.serverAddresses = serverAddresses;
    try {
      clientNode = serverNode.accept();
      in = new BufferedReader(new InputStreamReader(clientNode.getInputStream()));
      out = new BufferedWriter(new OutputStreamWriter(clientNode.getOutputStream()));
    } catch (IOException e) {
      System.err.println("Failed to create sockets");
    } finally {
      start();
    }
  }
  @Override
  public void run() {
    try {
      try (FileWriter f = new FileWriter("log.txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println("Connection accepted: "+ clientNode);
          } catch (IOException e) {
            System.err.println(e);
          }
      String[] M = in.readLine().split(" ");
      if (M[0].equals("GetData")) {
        try (FileWriter f = new FileWriter("log.txt", true); 
          BufferedWriter b = new BufferedWriter(f); 
          PrintWriter p = new PrintWriter(b);) {
            p.println("Request to give data");
          } catch (IOException e) {
            System.err.println(e);
          }
        out.write(Integer.toString(serverAddresses.size()) + '\n');
        out.flush();
        for (InetAddress server: serverAddresses) {
          out.write(server.getHostAddress() + '\n');
          out.flush();
        }
      } else {
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("ReadLine() is succesfull: ");
        } catch (IOException e) {
          System.err.println(e);
        }
        Instant Start = Instant.now();
        int l = Integer.parseInt(M[0]);
        int n = Integer.parseInt(M[1]);
        int m = Integer.parseInt(M[l*n+3]);
        int[][] M1 = new int[l][n];
        for (int i=0; i < l; i++) {
          for (int j=0; j<n; j++) {
            M1[i][j]=Integer.parseInt(M[2+i*n+j]);
          }
        }
        int[][] M2 = new int[n][m];
        for (int i=0; i < n; i++) {
          for (int j=0; j<m; j++) {
            M2[i][j]=Integer.parseInt(M[l*n+4+i*m+j]);
          }
        }
        String newM=Multiply(M1, M2);
        Instant Finish = Instant.now();
        long multiplication = Duration.between(Start, Finish).toMillis();
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("Multiplication time on this server: " + Long.toString(multiplication));
        } catch (IOException e) {
          System.err.println(e);
        }
        out.write(newM + "\n");
        out.flush();
      }
    } catch(Exception e) {
      System.err.println("Error has occured");
      try {
        out.write("Error\n");
        out.flush();
      } catch (IOException err) {
        System.err.println("Failed to report error");
      }
    } finally {
      try {
        in.close();
        out.close();
        clientNode.close();
      } catch (IOException e) {
        System.err.println("Failed to close");
      }
    }
  }
  public static String Multiply (int[][] M1, int[][] M2) {
  	String Result ="";
    for (int i=0; i<M1.length; i++) {
        for (int j=0; j<M2[0].length; j++) {
          int temp = 0;
          for (int k=0; k<M2.length; k++) {
            temp+=M1[i][k]*M2[k][j];
          }
          Result+=temp+" ";
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
  Button Matrix1;
  Button Matrix2;
  Button Mult;
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
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("File of the first matrix is " + fd1.getFile());
        } catch (IOException e) {
          System.err.println(e);
        }
      } else {
        Matrix1.setLabel("Выбрать первую матрицу");
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("No file of the first matrix");
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
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("File of the second matrix is " + fd2.getFile());
        } catch (IOException e) {
          System.err.println(e);
        }
      } else {
        Matrix2.setLabel("Выбрать вторую матрицу");
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("No file of the second matrix");
        } catch (IOException e) {
          System.err.println(e);
        }
      }
      repaint();
    } else if (name.equals("Mult")) {
      try {
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
      ServerSocket serverNode = new ServerSocket(PORT);
      try (FileWriter f = new FileWriter("log.txt", true); 
      BufferedWriter b = new BufferedWriter(f); 
      PrintWriter p = new PrintWriter(b);) {
        p.println(serverNode);
      }
      Window Window = new Window();
      Window.setVisible(true);
      while (true) {
        new NewThread(serverNode, serverAddresses);
      }
    }
}