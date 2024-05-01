import java.io.*; 
import java.util.*;

public class Generator {
    public static void main(String[] args) {
        FileOutputStream f1;
        Random rnd = new Random();
        int l = 1000;
        int n = 1000;
        int m = 1000;
        String FileName1 = "Matrices/1000_1000_A.dat";
        String FileName2 = "Matrices/1000_1000_B.dat";
        try {
            f1 = new FileOutputStream(FileName1);
            System.out.println("FileStream is created");
        } catch (FileNotFoundException e) {
            System.out.println(e);
            return;
        }
        try {
            for (int i=0; i < Integer.toString(l).length(); i++) {
                f1.write(Integer.toString(l).charAt(i));
            }
            f1.write(' ');
            for (int i=0; i < Integer.toString(n).length(); i++) {
                f1.write(Integer.toString(n).charAt(i));
            }
            f1.write(' ');
            f1.write('\n');
            for (int y = 0; y < l; y++) {
                for (int x = 0; x < n; x++) {
                    String str = Integer.toString(rnd.nextInt() % 100);
                    for (int i=0; i < str.length(); i++) {
                        f1.write(str.charAt(i));
                    }
                    f1.write(' ');
                }
                f1.write('\n');
            }
            System.out.println("Writing is succeseful");
        } catch(IOException e) {
            System.out.println("Error while attempting writing");
        }
        try {
            f1.close();
        } catch(IOException e) {
            return;
        }
        try {
            f1 = new FileOutputStream(FileName2);
            System.out.println("FileStream is created");
        } catch (FileNotFoundException e) {
            System.out.println(e);
            return;
        }
        try {
            for (int i=0; i < Integer.toString(n).length(); i++) {
                f1.write(Integer.toString(n).charAt(i));
            }
            f1.write(' ');
            for (int i=0; i < Integer.toString(m).length(); i++) {
                f1.write(Integer.toString(m).charAt(i));
            }
            f1.write(' ');
            f1.write('\n');
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < m; x++) {
                    String str = Integer.toString(rnd.nextInt() % 100);
                    for (int i=0; i < str.length(); i++) {
                        f1.write(str.charAt(i));
                    }
                    f1.write(' ');
                }
                f1.write('\n');
            }
            System.out.println("Writing is succeseful");
        } catch(IOException e) {
            System.out.println("Error while attempting writing");
        }
        try {
            f1.close();
        } catch(IOException e) {
            return;
        }
    }
}