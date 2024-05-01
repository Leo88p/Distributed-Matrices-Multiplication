import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MultiplicationTest {
	@ParameterizedTest
	@CsvSource(value = {
		    "mult/1A.dat, mult/1B.dat, mult/1C.dat",
		    "mult/2A.dat, mult/2B.dat, mult/2C.dat",
		    "mult/3A.dat, mult/3B.dat, mult/3C.dat",
		    "mult/4A.dat, mult/4B.dat, mult/4C.dat"
		}, ignoreLeadingAndTrailingWhitespace = true)
    void Multiplication(String FileNameA, String FileNameB, String FileNameC) throws IOException{
    	int[][] M1, M2;
    	ClassLoader classLoader = this.getClass().getClassLoader();
    	try (Scanner sc = new Scanner(new File(classLoader.getResource(FileNameA).getFile()))) {
        	int l = sc.nextInt();
        	int m = sc.nextInt();
        	M1 = new int[l][m];
        	for (int i=0; i<l; ++i) {
        		for (int j=0; j<m; ++j) {
        			M1[i][j] = sc.nextInt();
        		}
        	}
    	}
    	try (Scanner sc = new Scanner(new File(classLoader.getResource(FileNameB).getFile()))) {
        	int l = sc.nextInt();
        	int m = sc.nextInt();
        	M2 = new int[l][m];
        	for (int i=0; i<l; ++i) {
        		for (int j=0; j<m; ++j) {
        			M2[i][j] = sc.nextInt();
        		}
        	}
    	}
    	int[][] Result;
    	try (Scanner sc = new Scanner(new File(classLoader.getResource(FileNameC).getFile()))) {
        	int l = sc.nextInt();
        	int m = sc.nextInt();
        	Result = new int[l][m];
        	for (int i=0; i<l; ++i) {
        		for (int j=0; j<m; ++j) {
        			Result[i][j] = sc.nextInt();
        		}
        	}
    	}
    	assertArrayEquals(Result, MultiplicationThread.Multiply(M1, M2));
    }
}
