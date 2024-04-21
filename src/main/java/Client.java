import java.io.*;
import java.net.*;
import java.time.*;
import java.util.Scanner;
class ClientThread extends Thread {
    private InetAddress serverIP;
    private String Request;
    private String Answer;
    private long elapsed;

    public ClientThread(InetAddress serverIP, String Request) {
        this.serverIP = serverIP;
        this.Request = Request;
        start();
    }
    public String getAnswer() {
        return Answer;
    }
    public String getTime() {
        return serverIP.getHostAddress() + ": " + Long.toString(elapsed);
    }
    @Override
    public void run() {
        try {
            Socket clientNode = new Socket(serverIP, Server.PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientNode.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientNode.getOutputStream()));
            Instant Start = Instant.now();
            out.write(Request);
            out.flush();
            Answer = in.readLine();
            out.close();
            Instant Finish = Instant.now();
            elapsed = Duration.between(Start, Finish).toMillis();
            in.close();
            clientNode.close(); 
        } catch (IOException e) {
            System.err.println("Socket has crashed");
        }
    }
}

public class Client {
    static String FileName1 = "Matrices/A.dat";
    static String FileName2 = "Matrices/B.dat";
    public static void main(String[] args) throws IOException{
        String FileNameOfResult = "Matrices/Result.txt";
        int l, n, m;
        String[] M1, M2;
        try (Scanner sc = new Scanner(new File(FileName1))) {
        	l = sc.nextInt();
        	n = sc.nextInt();
        	M1 = new String[l];
        	for (int i=0; i<l; ++i) {
        		M1[i]="";
        		for (int j=0; j<n; ++j) {
        			M1[i]+=sc.next() + " ";
        		}
        	}
        }
        try (Scanner sc = new Scanner(new File(FileName2))) {
        	n = sc.nextInt();
        	m = sc.nextInt();
        	M2 = new String[n];
        	for (int i=0; i<n; ++i) {
        		M2[i]="";
        		for (int j=0; j<m; ++j) {
        			M2[i]+=sc.next() + " ";
        		}
        	}
        }
        int[][] Result = new int[l][m];
        //InetAddress[] servers = {InetAddress.getByName("223.0.0.1"), InetAddress.getByName("223.0.0.2"), InetAddress.getByName("223.0.0.3"), InetAddress.getByName("223.0.0.4")};
        int serversNumber = 1;
        InetAddress[] servers = {InetAddress.getByName("223.0.0.1")};
        Socket clientNode;
        BufferedReader in;
        BufferedWriter out;
        try {
            clientNode = new Socket(InetAddress.getLocalHost(), Server.PORT);
            in = new BufferedReader(new InputStreamReader(clientNode.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientNode.getOutputStream()));
            out.write("GetData\n");
            out.flush();
            serversNumber = Integer.parseInt(in.readLine());
            servers = new InetAddress[serversNumber];
            for (int i=0; i<serversNumber; i++) {
                servers[i] = InetAddress.getByName(in.readLine());
            }
            out.close();
            in.close();
            clientNode.close();
        } catch (Exception e) {
            System.err.println("Could not receive data\n" + e);
        }
        int [] rowsNumber = new int[serversNumber];
        Instant start = Instant.now();
        String[] Request = Distribute(serversNumber, m, M1, M2);
        int cThr = Request.length;
        ClientThread[] Threads = new ClientThread[cThr];
        for (int i=0; i<cThr; ++i) {
        	Threads[i] = new ClientThread(servers[i], Request[i] + "\n");
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
        int currentResultRow = 0;
        for (int i=0; i <cThr; i++) {
            String[] AnswerNumbers = Threads[i].getAnswer().split(" ");
            int currentNumber = 0;
            for (int j=0; j<rowsNumber[i]; j++, currentResultRow++) {
                for (int k=0; k<m; k++,currentNumber++) {
                    Result[currentResultRow][k] = Integer.parseInt(AnswerNumbers[currentNumber]);
                }
            }
            Times[i] = Threads[i].getTime();
        }
        Instant finish = Instant.now();
        long elapsed = Duration.between(start, finish).toMillis();
        try (FileWriter f = new FileWriter("log.txt", true); 
        BufferedWriter b = new BufferedWriter(f); 
        PrintWriter p = new PrintWriter(b);) {
          p.println("Time: " + elapsed);
        } catch (IOException e) {
          System.err.println(e);
        }
        for (int i = 0; i<cThr; i++) {
            try (FileWriter f = new FileWriter("log.txt", true); 
            BufferedWriter b = new BufferedWriter(f); 
            PrintWriter p = new PrintWriter(b);) {
            p.println("Time of server " + Times[i]);
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
            try (FileWriter f = new FileWriter("log.txt", true); 
            BufferedWriter b = new BufferedWriter(f); 
            PrintWriter p = new PrintWriter(b);) {
            p.println("Writing is succeseful_0");
            } catch (IOException e) {
            System.err.println(e);
            }
        }  catch (Exception e) {
            System.err.println(e);
            return;
        }
    }
    static String[] Distribute(int serversNumber, int m, String[] M1, String[] M2) {
    	if (serversNumber > M1.length) {
    		serversNumber = M1.length;
    	}
        int remainder = M1.length % serversNumber;
        int currentRow = 0;
        int [] rowsNumber = new int[serversNumber];
        String [] Request = new String[serversNumber];
        String M2s = "";
        for (int i=0; i<M2.length; ++i) {
        	M2s+=M2[i];
        }
        for (int i=0; i<serversNumber; ++i) {
            rowsNumber[i] = M1.length  / serversNumber;
            if (remainder > 0) {
                ++rowsNumber[i];
                --remainder;
            }
            String MCurrent = "";
            for (int j = 0; j<rowsNumber[i]; j++, currentRow++) {
                MCurrent += M1[currentRow];
            }
            Request[i] = rowsNumber[i] + " " + M2.length + " " + MCurrent + M2.length + " " + m + " " + M2s;
        }
        return Request;
    }
}
