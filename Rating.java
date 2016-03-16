public class Rating {
	Person person;
	Movie movie;
	int rating;
	
	public Rating(Person p, Movie movie, int rating) {
		this.person = p;
		this.movie = movie;
		this.rating = rating;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person p) {
		this.person = p;
	}
	
	public Movie getMovie() {
		return movie;
	}
	
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	
	public int getRating() {
		return rating;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public String toString() {
		return "person: " + person.getId() + " movie: " + movie.getId() + " rating: " + rating + "\n";
	}
}
