import java.util.Arrays;
import java.util.Scanner;

public class InteractiveDataQuery {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter("\n");

		Tester tester = new Tester();
		tester.loadDataFromFile();
		
		tester.runInteractiveMode();
	}
}
