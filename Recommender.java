import java.util.ArrayList;
import java.util.HashMap;

public class Recommender {
	Tester tester;
	
	public Recommender(Tester t) {
		tester = t;
	}
	
	// This is automatically run before any tests.  You may use this method to do
	// any calculations you need to support your predictRating method.

	// NOTE:  to avoid cheating you must re-set any variables you use here
	// before you calculate their new values.
	public void init() {
		
	}

	public double predictRating(Person person, Movie movie) {
		//find weights based on similarity
		// find k people to be in neighborhood
		ArrayList<Rating> personRatings = person.getRatings();
		ArrayList<Person> allPeople = tester.getAllPeople();
		ArrayList<Person> neighborhood = new ArrayList<Person>();
		ArrayList<Double> neighborhoodWeights = new ArrayList<Double>(); //index corresponds with person in neighborhood
		
		//loop through p1 ratings
		for(int i = 0; i < personRatings.size(); i ++){
			//get rating
			Rating r1 = personRatings.get(i);
			
			//loop through all people
			for(int j = 0; j < allPeople.size(); j ++){
				Person p2 = allPeople.get(i);
				if(p2.hasWatchedMovie(movie)){
					//Rating r2 = p2.getRatingForMovie();
				}
			}
		}
		
		//predict from weighted average
		for(int i = 0; i < neighborhood.size(); i ++){
			
		}
		return 2;
	}
}