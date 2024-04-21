import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DistributionTest {
	@ParameterizedTest
	@CsvSource(value = {
		    "1, dist/1A.dat, dist/1B.dat, dist/1C.dat",
		    "2, dist/1A.dat, dist/1B.dat, dist/2C.dat",
		    "4, dist/1A.dat, dist/1B.dat, dist/3C.dat",
		    "6, dist/1A.dat, dist/1B.dat, dist/4C.dat",
		    "7, dist/1A.dat, dist/1B.dat, dist/4C.dat",
		}, ignoreLeadingAndTrailingWhitespace = true)
	void Distribution(int serversNumber, String FileName1, String FileName2, String FileName3) throws IOException {
        int l, n, m;
        String[] M1, M2;
        List<String>Result = new ArrayList<String>();
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (Scanner sc = new Scanner(new File(classLoader.getResource(FileName1).getFile()))) {
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
        try (Scanner sc = new Scanner(new File(classLoader.getResource(FileName2).getFile()))) {
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
        try (Scanner sc = new Scanner(new File(classLoader.getResource(FileName3).getFile()))) {
        	for (int i=0; i<serversNumber && sc.hasNextLine(); ++i) {
        		Result.add(sc.nextLine());
        	}
        }
        assertArrayEquals(Result.toArray(), Client.Distribute(serversNumber, m, M1, M2));
	}
}