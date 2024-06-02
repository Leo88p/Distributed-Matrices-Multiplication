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
            in.close();
            out.close();
            clientNode.close();
        } catch (IOException | ClassNotFoundException e) {
        	try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+Server.formatter.format(Calendar.getInstance().getTime())+".txt", true); 
    	            BufferedWriter b = new BufferedWriter(f); 
    	            PrintWriter p = new PrintWriter(b);) {
    			p.println(Server.timeForm.format(Calendar.getInstance().getTime())+"\t"+e);
    		} catch (IOException e2) {
    			System.err.println(e2);
    		}
        }
    }
}

public class Client {
    static int[][] M1;
    static int[][] M2;
    static int[][] Result;
    static int useNs = -1;
    public static void main(String[] args) throws IOException{
        InetAddress[] servers =  new InetAddress[Server.serverAddresses.size()];
        for (int i=0; i<servers.length; ++i) {
        	servers[i] = Server.serverAddresses.get(i).getAddress();
        }
        int serversNumber = servers.length;
        int n = M1.length;
        int m = M2[0].length;
        Result = new int[n][m];
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
        SimpleDateFormat timeForm=new SimpleDateFormat("HH:mm:ss"); 
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
                try (FileWriter f = new FileWriter(Files.createDirectories(Paths.get("logs")).toAbsolutePath().toString()+"/log-"+formatter.format(Calendar.getInstance().getTime())+".txt", true); 
        	            BufferedWriter b = new BufferedWriter(f); 
        	            PrintWriter p = new PrintWriter(b);) {
        			p.println(timeForm.format(Calendar.getInstance().getTime())+"\t"+e);
        		} catch (IOException e2) {
        			System.err.println(e2);
        		}
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
