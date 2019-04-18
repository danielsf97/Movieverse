/**
 * Licensee: mariana(Universidade do Minho)
 * License Type: Academic
 */
package ormsamples;

import org.orm.*;
public class CreateMovieverseData {
	public void createTestData() throws PersistentException {
		PersistentTransaction t = data.movieverse.MovieversePersistentManager.instance().getSession().beginTransaction();
		try {
			data.movieverse.MUser ldatamovieverseMUser = data.movieverse.MUserDAO.createMUser();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : friends, requestedFriendships, userMovies, achievements, feed, comments, receivedFriendships, friendsCount, ratingsCount, commentsCount, hoursCount, movieCount, gender, userCountry, favouriteGenre
			data.movieverse.MUserDAO.save(ldatamovieverseMUser);
			data.movieverse.Movie ldatamovieverseMovie = data.movieverse.MovieDAO.createMovie();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : media, relatedMovies, showtime, movieMembers, comments, companies, genres, userMovies, ratingCount, ratingSum, runtime
			data.movieverse.MovieDAO.save(ldatamovieverseMovie);
			data.movieverse.Genre ldatamovieverseGenre = data.movieverse.GenreDAO.createGenre();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : movies
			data.movieverse.GenreDAO.save(ldatamovieverseGenre);
			data.movieverse.Company ldatamovieverseCompany = data.movieverse.CompanyDAO.createCompany();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : movies, companyCountry
			data.movieverse.CompanyDAO.save(ldatamovieverseCompany);
			data.movieverse.Country ldatamovieverseCountry = data.movieverse.CountryDAO.createCountry();
			// Initialize the properties of the persistent object here
			data.movieverse.CountryDAO.save(ldatamovieverseCountry);
			data.movieverse.UserMovie ldatamovieverseUserMovie = data.movieverse.UserMovieDAO.createUserMovie();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : mUser, movie, visible, favourite, rating, status
			data.movieverse.UserMovieDAO.save(ldatamovieverseUserMovie);
			data.movieverse.Member ldatamovieverseMember = data.movieverse.MemberDAO.createMember();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : media, movieMembers, gender
			data.movieverse.MemberDAO.save(ldatamovieverseMember);
			data.movieverse.MovieMember ldatamovieverseMovieMember = data.movieverse.MovieMemberDAO.createMovieMember();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : movie, member
			data.movieverse.MovieMemberDAO.save(ldatamovieverseMovieMember);
			data.movieverse.Badge ldatamovieverseBadge = data.movieverse.BadgeDAO.createBadge();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : achievements
			data.movieverse.BadgeDAO.save(ldatamovieverseBadge);
			data.movieverse.Friendship ldatamovieverseFriendship = data.movieverse.FriendshipDAO.createFriendship();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : receivedMuser, requestedMuser, pending
			data.movieverse.FriendshipDAO.save(ldatamovieverseFriendship);
			data.movieverse.Comment ldatamovieverseComment = data.movieverse.CommentDAO.createComment();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : upvoter, likes, parent, commenter, movie
			data.movieverse.CommentDAO.save(ldatamovieverseComment);
			data.movieverse.Feed ldatamovieverseFeed = data.movieverse.FeedDAO.createFeed();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : idContent, type, user
			data.movieverse.FeedDAO.save(ldatamovieverseFeed);
			data.movieverse.Achievement ldatamovieverseAchievement = data.movieverse.AchievementDAO.createAchievement();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : mUser, badge
			data.movieverse.AchievementDAO.save(ldatamovieverseAchievement);
			data.movieverse.Theater ldatamovieverseTheater = data.movieverse.TheaterDAO.createTheater();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : showtime, country
			data.movieverse.TheaterDAO.save(ldatamovieverseTheater);
			data.movieverse.Showtime ldatamovieverseShowtime = data.movieverse.ShowtimeDAO.createShowtime();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : theater, movie
			data.movieverse.ShowtimeDAO.save(ldatamovieverseShowtime);
			data.movieverse.Media ldatamovieverseMedia = data.movieverse.MediaDAO.createMedia();
			// TODO Initialize the properties of the persistent object here, the following properties must be initialized before saving : idType
			data.movieverse.MediaDAO.save(ldatamovieverseMedia);
			t.commit();
		}
		catch (Exception e) {
			t.rollback();
		}
		
	}
	
	public static void main(String[] args) {
		try {
			CreateMovieverseData createMovieverseData = new CreateMovieverseData();
			try {
				createMovieverseData.createTestData();
			}
			finally {
				data.movieverse.MovieversePersistentManager.instance().disposePersistentManager();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
