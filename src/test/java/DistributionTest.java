import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DistributionTest {
	@ParameterizedTest
	@CsvSource(value = {
		    "1, dist/1A.dat, dist/1C.dat",
		    "2, dist/1A.dat, dist/2C.dat",
		    "4, dist/1A.dat, dist/3C.dat",
		    "6, dist/1A.dat, dist/4C.dat",
		    "7, dist/1A.dat, dist/4C.dat",
		}, ignoreLeadingAndTrailingWhitespace = true)
	void Distribution(int serversNumber, String FileName, String FileResult) throws IOException {
        int l, n, m;
        int[] rowsNumber = new int[serversNumber];
        int[][] M;
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (Scanner sc = new Scanner(new File(classLoader.getResource(FileName).getFile()))) {
        	l = sc.nextInt();
        	n = sc.nextInt();
        	M = new int[l][n];
        	for (int i=0; i<l; ++i) {
        		for (int j=0; j<n; ++j) {
        			M[i][j]=sc.nextInt();
        		}
        	}
        }
        int[][][] Result;
        try (Scanner sc = new Scanner(new File(classLoader.getResource(FileResult).getFile()))) {
        	m = sc.nextInt();
        	Result = new int[m][][];
        	for (int i=0; i<m; ++i) {
            	l = sc.nextInt();
            	n = sc.nextInt();
            	Result[i] = new int[l][n];
            	for (int j=0; j<l; ++j) {
            		for (int k=0; k<n; ++k) {
            			Result[i][j][k]=sc.nextInt();
            		}
            	}
        	}
        }
        assertArrayEquals(Result, Client.Distribute(serversNumber, M, rowsNumber));
	}
}