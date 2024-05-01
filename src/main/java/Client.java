import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Scanner;
class ClientThread extends Thread {
    private InetAddress serverIP;
    private int[][] M1;
    private int[][] M2;
    private int[][] Answer;
    private long transfer;
    private long multiplication;
    private long elapsed;

    public ClientThread(InetAddress serverIP, int[][] M1, int[][] M2) {
        this.serverIP = serverIP;
        this.M1 = M1;
        this.M2 = M2;
        start();
    }
    public int[][] getAnswer() {
        return Answer;
    }
    public String getTime() {
        return serverIP.getHostAddress() + ": " + Long.toString(elapsed);
    }
    public String getTransferTime() {
        return serverIP.getHostAddress() + ": " + Long.toString(transfer);
    }
    public String getMultiplicationTime() {
        return serverIP.getHostAddress() + ": " + Long.toString(multiplication);
    }
    @Override
    public void run() {
    	
        try {
        	Socket clientNode = new Socket(serverIP, Server.PORT);
    		ObjectInputStream in = new ObjectInputStream(clientNode.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientNode.getOutputStream());
        	out.flush();
            Instant Start = Instant.now();
            out.writeObject(M1);
            out.writeObject(M2);
            Answer = (int[][])in.readObject();
            Instant Finish = Instant.now();
            multiplication = in.readLong();
            elapsed = Duration.between(Start, Finish).toMillis();
            transfer = elapsed-multiplication;
            clientNode.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket has crashed");
        }
    }
}

public class Client {
    static String FileName1 = "Matrices/A.dat";
    static String FileName2 = "Matrices/B.dat";
    static int useNs = -1;
    public static void main(String[] args) throws IOException{
        String FileNameOfResult = "Matrices/Result.txt";
        InetAddress[] servers =  Server.serverAddresses.toArray(new InetAddress[0]);
        int serversNumber = servers.length;
        int l, n, m;
        int[][] M1, M2;
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
        SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
        try (Scanner sc = new Scanner(new File(FileName1))) {
        	l = sc.nextInt();
        	n = sc.nextInt();
        	M1 = new int[l][n];
        	for (int i=0; i<l; ++i) {
        		for (int j=0; j<n; ++j) {
        			M1[i][j]=sc.nextInt();
        		}
        	}
        }
        try (Scanner sc = new Scanner(new File(FileName2))) {
        	n = sc.nextInt();
        	m = sc.nextInt();
        	M2 = new int[n][m];
        	for (int i=0; i<n; ++i) {
        		for (int j=0; j<m; ++j) {
        			M2[i][j]=sc.nextInt();
        		}
        	}
        }
        int[][] Result = new int[l][m];
        //InetAddress[] servers = {InetAddress.getByName("223.0.0.1"), InetAddress.getByName("223.0.0.2"), InetAddress.getByName("223.0.0.3"), InetAddress.getByName("223.0.0.4")};
        //int serversNumber = 1;
        //InetAddress[] servers = {InetAddress.getByName("223.0.0.1")};
        if (useNs!=-1) {
        	serversNumber = useNs;
        }
        int [] rowsNumber = new int[serversNumber];
        Instant start = Instant.now();
        int[][][] Request = Distribute(serversNumber, M1, rowsNumber);
        int cThr = Request.length;
        ClientThread[] Threads = new ClientThread[cThr];
        for (int i=0; i<cThr; ++i) {
        	Threads[i] = new ClientThread(servers[i], Request[i], M2);
        }
        for (int i = 0; i<cThr; i++) {
            try {
                Threads[i].join();
            } catch (InterruptedException e) {
                Threads[i].interrupt();
                System.err.println(e);
            }
        }
        String Times[] = new String[cThr];
        String TrTimes[] = new String[cThr];
        String MTimes[] = new String[cThr];
        int currentResultRow = 0;
        for (int i=0; i <cThr; i++) {
            int[][] AnswerNumbers = Threads[i].getAnswer();
            for (int j=0; j<rowsNumber[i]; ++j, ++currentResultRow) {
                for (int k=0; k<m; k++) {
                    Result[currentResultRow][k] = AnswerNumbers[j][k];
                }
            }
            Times[i] = Threads[i].getTime();
            TrTimes[i] = Threads[i].getTransferTime();
            MTimes[i] = Threads[i].getMultiplicationTime();
        }
        Instant finish = Instant.now();
        long elapsed = Duration.between(start, finish).toMillis();
        try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println(timeForm.format(Calendar.getInstance().getTime())+" Time: " + elapsed);
        } catch (IOException e) {
          System.err.println(e);
        }
        for (int i = 0; i<cThr; i++) {
            try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
            BufferedWriter b = new BufferedWriter(f); 
            PrintWriter p = new PrintWriter(b);) {
            p.println(timeForm.format(Calendar.getInstance().getTime())+"\tTime of server " + Times[i]);
            p.println(timeForm.format(Calendar.getInstance().getTime())+"\t\tTransportation time " + TrTimes[i]);
            p.println(timeForm.format(Calendar.getInstance().getTime())+"\t\tMultiplication time " + MTimes[i]);
            } catch (IOException e) {
            System.err.println(e);
            }
        }
        try (FileOutputStream f2 = new FileOutputStream(FileNameOfResult)) {
            for (int i=0; i < Integer.toString(l).length(); i++) {
                f2.write(Integer.toString(l).charAt(i));
            }
            f2.write(' ');
            for (int i=0; i < Integer.toString(m).length(); i++) {
                f2.write(Integer.toString(m).charAt(i));
            }
            f2.write(' ');
            f2.write('\n');
            for (int y = 0; y < l; y++) {
                for (int x = 0; x < m; x++) {
                    String str = Integer.toString(Result[y][x]);
                    for (int i=0; i < str.length(); i++) {
                        f2.write(str.charAt(i));
                    }
                    f2.write(' ');
                }
                f2.write('\n');
            }
            try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
            BufferedWriter b = new BufferedWriter(f); 
            PrintWriter p = new PrintWriter(b);) {
            p.println(timeForm.format(Calendar.getInstance().getTime())+" Writing is succeseful");
            } catch (IOException e) {
            System.err.println(e);
            }
        }  catch (Exception e) {
            System.err.println(e);
            return;
        }
    }
    static int[][][] Distribute(int serversNumber, int[][] M, int[] rowsNumber) {
    	if (serversNumber > M.length) {
    		serversNumber = M.length;
    	}
        int remainder = M.length % serversNumber;
        int currentRow = 0;
        int[][][] Request = new int[serversNumber][][];
        for (int i=0; i<serversNumber; ++i) {
            rowsNumber[i] = M.length  / serversNumber;
            if (remainder > 0) {
                ++rowsNumber[i];
                --remainder;
            }
            Request[i] = new int[rowsNumber[i]][];
            for (int j=0; j<rowsNumber[i]; ++j, ++currentRow) {
            	Request[i][j] = M[currentRow];
            }
        }
        return Request;
    }
}
