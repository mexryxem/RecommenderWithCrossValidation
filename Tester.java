import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Tester {
	private Recommender recommender;
	public boolean DISPLAY = false;
	public double threshold = 2;

	private double mse;

	private final String usersfile = "c:\\data\\recommender\\users.txt";
	private final String itemsfile = "c:\\data\\recommender\\items.txt";

	private ArrayList<Rating> allRatings, trainingRatings;
	private HashMap<Integer, Person> people_ids;
	private HashMap<Integer, Movie> movie_ids;
	private boolean loadedRatings = false;
	private String[] tagList = { "unknown", "Action", "Adventure", "Animation",
			"Children's", "Comedy", "Crime", "Documentary", "Drama", "Fantasy",
			"Film-Noir", "Horror", "Musical", "Mystery", "Romance", "Sci-Fi",
			"Thriller", "War", "Western", "", "", "", "", "", "", "" };

	public Tester() {
		mse = 0;
		allRatings = new ArrayList<Rating>();
		trainingRatings = new ArrayList<Rating>();
		people_ids = new HashMap<Integer, Person>();
		movie_ids = new HashMap<Integer, Movie>();
	}

	public void loadDataFromFile() {
		loadDataFromFile(usersfile, itemsfile);
	}

	public void loadDataFromFile(String usersfile, String itemsfile) {
		// Load people
		Scanner scanner;
		int counter = 0;

		try {
			scanner = new Scanner(new FileReader(usersfile));
			scanner.useDelimiter("\n");

			String line;
			while (scanner.hasNext()) {
				line = scanner.next();
				String[] args = line.split("\\|");
				addPerson(args[0], args[1], args[2], args[3], args[4]);
				counter++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + usersfile);
		}

		System.out.println("Loaded " + counter + " people.");
		counter = 0;

		// Load items
		try {
			scanner = new Scanner(new FileReader(itemsfile));
			scanner.useDelimiter("\n");

			String line;
			while (scanner.hasNext()) {
				line = scanner.next();
				String[] args = line.split("\\|");

				ArrayList<String> tags = new ArrayList<String>();
				for (int i = 3; i < args.length; i++) {
					if (args[i].equals("1")) {
						tags.add(tagList[i - 5]);
					}
				}

				int movie_id = Integer.parseInt(args[0]);
				String movie_name = args[1];
				
				System.out.println("Movie name: " + movie_name);
				
				String release_date = args[2];

				Movie movie = new Movie(movie_id, movie_name, release_date, tags);
				addMovie(movie);

				counter++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + usersfile);
		}

		System.out.println("Loaded " + counter + " movies.");
	}

	public void loadAllRatings(String ratingsFile, boolean putRatingsInMoviesAndPeople) {
		allRatings.clear();

		// Load all ratings
		Scanner scanner;
		try {
			scanner = new Scanner(new FileReader(ratingsFile));
			scanner.useDelimiter("\n");

			System.out.println("Loading all ratings...");

			String line;
			while (scanner.hasNext()) {
				line = scanner.next();
				String[] arg = line.split("\\t");
				int person_id = Integer.parseInt(arg[0]);
				int movie_id = Integer.parseInt(arg[1]);
				int real_rating = Integer.parseInt(arg[2]);

				Person person = getPerson(person_id);
				Movie movie = getMovie(movie_id);

				if (person == null || movie == null)
					System.out.println("ERROR:  null movie or person");

				Rating new_rating = new Rating(person, movie, real_rating);

				allRatings.add(new_rating);
				
				if (putRatingsInMoviesAndPeople) {
					person.addRating(new_rating);
					movie.addRating(new_rating);
				}
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Test data file not found at " + ratingsFile);
		}
	}

	public double crossValidate(String testdatafile, int numGroups) {
		if (loadedRatings)
			System.out.println("Warning:  Don't manually load ratings.");
		resetTrainingData();

		// Load all ratings
		loadAllRatings(testdatafile, false);

		// Randomize the list
		Collections.shuffle(allRatings);

		// create the groups
		int sizeOfGroup = allRatings.size() / numGroups;
		ArrayList<ArrayList<Rating>> groups = new ArrayList<ArrayList<Rating>>();
		for (int currentGroup = 0; currentGroup < numGroups; currentGroup++) {
			ArrayList<Rating> group = new ArrayList<Rating>();
			for (int offsetInGroup = 0; offsetInGroup < sizeOfGroup; offsetInGroup++) {
				group.add(allRatings.get(sizeOfGroup * currentGroup + offsetInGroup));
			}
			groups.add(group);
		}

		double mse = 0;
		for (int i = 0; i < groups.size(); i++) {
			// make training set out of all groups that aren't i
			ArrayList<Rating> training = new ArrayList<Rating>();
			for (int j = 0; j < groups.size(); j++) {
				if (i != j)
					training.addAll(groups.get(j));
			}

			System.out.println("Running group " + i + " with " + groups.get(i).size()
					+ " ratings.");

			double error = runValidation(training, groups.get(i));
			System.out.println("Group " + i + " mean squared error is " + error);
			mse += error;
		}

		return mse / groups.size();
	}

	private void clearAllMovieRatings() {
		for (Movie movie : getAllMovies())
			movie.clearRatings();
	}

	private void clearAllPersonRatings() {
		for (Person person : getAllPeople())
			person.clearRatings();
	}

	public double runValidation(ArrayList<Rating> training, ArrayList<Rating> test) {

		double mse = 0;

		resetTrainingData();
		for (Rating rating : training) {
			addTrainingRating(rating); // this adds rating to training data and
																	// adds ratings to Movie and Person objects
		}

		recommender.init();

		if (training.size() == 0)
			System.out.println("ERROR: Training list is empty");

		for (Rating rating : test) {
			double prediction = recommender.predictRating(rating.getPerson(),
					rating.getMovie());

			if (DISPLAY) {
				if (Math.abs(prediction - rating.getRating()) > threshold) {
					System.out.println("For person " + rating.getPerson().getId()
							+ " and movie " + rating.getMovie().getId()
							+ " your prediction: " + prediction + " actual: "
							+ rating.getRating());
				}
			}

			mse += (rating.getRating() - prediction)
					* (rating.getRating() - prediction);
		}
		if (test.size() == 0)
			System.out.println("ERROR: Testing list is empty");

		return mse / test.size();
	}

	// *************************************************************************************

	public Person getPerson(int id) {
		return people_ids.get(id);
	}

	public Movie getMovie(int id) {
		return (Movie) movie_ids.get(id);
	}

	public ArrayList<Person> getAllPeople() {
		return new ArrayList<Person>(people_ids.values());
	}

	public ArrayList<Movie> getAllMovies() {
		return new ArrayList<Movie>(movie_ids.values());
	}

	public ArrayList<Rating> getTrainingRatings() {
		return this.trainingRatings;
	}

	// Accepts a String array containing: user_id, age, gender, occupation,
	// zip-code
	private void addPerson(String... args) {
		int id, age, gender; // 0 = male, 1 = female

		id = Integer.parseInt(args[0]);
		age = Integer.parseInt(args[1]);
		gender = (args[2]).equals("M") ? 0 : 1;
		Person person = new Person(id, age, gender, args[3], args[4]);
		people_ids.put(id, person);
	}

	// accepts a String array containing movie_id, movie name, and release date
	private void addMovie(String... args) {
		int movie_id;
		String movie_name, release_date;
		ArrayList<String> tags;

		movie_id = Integer.parseInt(args[0]);
		movie_name = args[1];
		release_date = args[2];

		Movie movie = new Movie(movie_id, movie_name, release_date);
		movie_ids.put(movie_id, movie);
	}

	private void addMovie(Movie m) {
		movie_ids.put(m.getId(), m);
	}

	private void addTrainingRating(Rating r) {
		Person person = r.getPerson();
		Movie movie = r.getMovie();

		// add the rating to the user and movie.
		person.addRating(r);
		movie.addRating(r);

		this.trainingRatings.add(r);
	}

	// accepts a String array containing person_id, movie_id, and rating (0 to
	// 5)
	private void addTrainingRating(String... args) {
		int person_id, movie_id, rating;
		person_id = Integer.parseInt(args[0]);
		movie_id = Integer.parseInt(args[1]);
		rating = Integer.parseInt(args[2]);

		Person person = people_ids.get(person_id);
		Movie movie = movie_ids.get(movie_id);

		if (person == null || movie == null)
			return;
		Rating r = new Rating(person, movie, rating);

		addTrainingRating(r);
	}

	private void resetTrainingData() {
		trainingRatings.clear();
		for (Movie movie : getAllMovies()) {
			movie.clearRatings();
		}
		for (Person person : getAllPeople()) {
			person.clearRatings();
		}
	}

	public void setRecommender(Recommender r) {
		this.recommender = r;
	}

	public void runInteractiveMode() {
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter("\n");

    System.out.println("loading all ratings...");
		loadAllRatings("c:\\data\\recommender\\allratings.txt", true);
		
		System.out.println("Welcome to the interactive data display!");
		System.out.println("----------------------------------------");
		System.out.println("You can use the following commands: ");
		System.out.println("'quit'\t\tQuits this program");
		System.out
				.println("'user ###'\t\tDisplays information about a specific user");
		System.out
				.println("'movie ###'\t\tDisplays information about a specific movie");
		System.out.println();

		String choice;
		do {
			System.out.print("Enter your command> ");
			choice = scanner.next();

			choice = cleanInput(choice);

			int num;
			if (choice.startsWith("user")) {
				num = parseOutNumber(choice.substring(4).trim());
				System.out.println("Displaying information for person " + num);

				Person u = getPerson(num);
				System.out.println(u);
			} else if (choice.startsWith("movie")) {
				num = parseOutNumber(choice.substring(5).trim());
				System.out.println("Displaying information for movie " + num);

				Movie m = getMovie(num);
				System.out.println(m);

			} else if (choice.startsWith("quit") || choice.startsWith("exit")) {
				System.exit(0);
			}
		} while (!choice.equals("quit"));
	}

	// returns the first number from choice
	private static int parseOutNumber(String choice) {
		try {
			return Integer.parseInt(choice);
		} catch (Exception E) {
		}

		return -1;
	}

	/*
	 * Cleans the user input by removing leading and trailing whitespace and
	 * changing everything to lowercase.
	 */
	private static String cleanInput(String choice) {
		choice = choice.trim();
		choice = choice.toLowerCase();
		return choice;
	}

}