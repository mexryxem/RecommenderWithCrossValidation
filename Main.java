
public class Main {
	public static void main(String[] args) {
		Tester tester = new Tester();
		Recommender r = new Recommender(tester);
		tester.setRecommender(r);

		// ------------====== (1) LOAD DATA ======---------------------------------
		// Method #1:  for this to work your files must be in c:\\data
		tester.loadDataFromFile();
		
		// Method #2:  uncomment the line below and specify your own file paths.
		// tester.loadDataFromFile("c:\\your_user_file_path",
		// 						   "c:\\your_movie_file_path");
		
		// ------------------------------------------------------------------------
		
		
		// ------------====== (2) RUN TESTS ======---------------------------------
		// Note:  do not change the 5.  This controls how many test groups the validator 
		// separates the data into.
		
		// tester.DISPLAY = true;			// Display predictions you got wrong
		tester.threshold = 0;					// How far off does your predictor have to be to display
																	// a prediction as incorrect?
		
		double error = tester.crossValidate("c:\\data\\recommender\\allratings.txt", 5);
		
		// -------------------------------------------------------------------------
		
		System.out.println("Your error is: " + error);
		System.out.println("(Since it's error, smaller is better.)");
	}
}
