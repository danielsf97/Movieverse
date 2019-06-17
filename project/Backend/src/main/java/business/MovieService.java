package business;

import data.ElasticSearch;
import data.RedisCache;
import data.daos.*;
import data.entities.*;
import data.entities.Genre;
import data.entities.Movie;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private ElasticSearch elasticSearch;

    @Autowired
    private MovieDAO movieDAO;

    @Autowired
    private UserMovieDAO userMovieDAO;

    @Autowired
    private MUserDAO mUserDAO;

    @Autowired
    private GenreDAO genreDAO;

    @Autowired
    private ShowtimeDAO showtimeDAO;

    @Autowired
    private TheaterDAO theaterDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private AchievementDAO achievementDAO;

    @Autowired
    private MediaDAO mediaDAO;

    @Autowired
    private Util util;

    @Autowired
    private FeedDAO feedDAO;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private UserService userService;


    public Map<String, Object> get(Integer id) throws Exception {
        Movie m = movieDAO.loadEntityEager(id);

        var rating = 0.0;

        try {
            rating = (double) m.getRatingSum()/m.getRatingCount();
            rating = Math.round(rating * 10) / 10.0;
        }
        catch (Exception e) {
            rating = 0.0;
        }

        Integer auxI;
        String auxS;
        Map<String, Object> result = new HashMap<>();

        result.put("tmdb", m.getTmdb());
        result.put("name", m.getName());
        result.put("poster", m.getPoster());
        result.put("backdrop", m.getBackdrop());
        result.put("plot", m.getPlot());
        result.put("imdb", m.getImdb());
        result.put("release", m.getRelease());
        result.put("runtime", m.getRuntime());
        result.put("rating", rating);
        result.put("isShowing", showtimeDAO.isShowing(id));

        if ((auxI = m.getBudget()) != null) {
            result.put("budget", auxI);
        }
        if ((auxS = m.getLanguage()) != null) {
            result.put("language", auxS);
        }
        if ((auxS = m.getTagline()) != null) {
            result.put("tagline", auxS);
        }

        var genres = new ArrayList<String>();
        for(Object g : m.getGenres()) {
            Genre genre = (Genre) g;
            genres.add(genre.getName());
        }

        result.put("genres", genres);

        var companies = new ArrayList<Map<String, Object>>();
        for(Object c : m.getCompanies()) {
            Company company = (Company) c;
            var h = new HashMap<String, Object>();
            h.put("id", company.getId());
            h.put("name", company.getName());
            companies.add(h);
        }

        result.put("companies", companies);

        var groupedMedia = mediaDAO.getMovieMedia(id);

        if(groupedMedia.get('b')!=null)
            result.put("backdrops", ((List<String>) groupedMedia.get('b'))
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList()));

        if(groupedMedia.get('v')!=null)
            result.put("videos", ((List<String>) groupedMedia.get('v'))
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList()));

        if(groupedMedia.get('p')!=null)
            result.put("posters", ((List<String>) groupedMedia.get('p'))
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList()));

        return result;
    }


    public Map<String, Object> getShort(Integer id) throws Exception {
        Movie m = movieDAO.findById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("name", m.getName());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(m.getRelease());
        int year = calendar.get(Calendar.YEAR);
        result.put("year", year);

        return result;
    }


    public HashMap<Object, Object> getMovieMeInfo(String token, Integer movieId) throws Exception {

        var user = mUserDAO.getSimpleUserByToken(token);
        var userMovie = new UserMovie();

        try {
            userMovie = userMovieDAO.loadEntity("muserid=" + user.getId() + " and movieid=" + movieId);
        }
        catch(Exception e) {
            if(user != null) {
                var result = new HashMap<>();
                result.put("watched", false);
                return result;
            }
            else throw new IOException();
        }

        var result = new HashMap<>();

        if(userMovie.getStatus()) {
            result.put("watched", true);
            result.put("dateWatched", userMovie.getDateWatched());
            Integer r;
            if((r = userMovie.getRating()) != null) {
                result.put("isRated", true);
                result.put("rating", r);
            }
            if(userMovie.getFavourite()) {
                result.put("favourite", true);
                result.put("dateFavourited", userMovie.getDateFavourite());
            }
        }
        else {
            result.put("watchlist", !userMovie.getStatus());
        }

        return result;
    }


    public boolean patchMovieMeInfo(String token, Integer movieId, Map<String, Object> updates) throws Exception {
        var user = mUserDAO.getSimpleUserByToken(token);
        var userMovie = new UserMovie();
        var movie = movieDAO.findById(movieId);
        try {
            try {
                userMovie = userMovieDAO.loadEntity("muserid=" + user.getId() + " and movieid=" + movieId);
            } catch (Exception e) {
                userMovie.setMovie(movie);
                userMovie.setmUser(user);
            }
            if (updates.containsKey("watched")) {
                var watched = (boolean) updates.get("watched");
                var feedEntryType = 1;

                if (watched) {
                    // If movie was marked as watched
                    var dateWatched = new Date();

                    if (updates.containsKey("favourited")) {
                        // If movie was also marked as favourite

                        userMovie.setFavourite(true);
                        userMovie.setDateFavourite(new Date());

                        addFeedEntry(2, user, movie.getORMID(), dateWatched);
                        updateFirstFavouriteAchievement(user);

                    }
                    if (updates.containsKey("rating")) {
                        // If movie was rated

                        feedEntryType = 0;

                        int rating = (int) updates.get("rating");
                        userMovie.setRating(rating);
                    }
                    userMovie.setStatus(true);
                    userMovie.setDateWatched(dateWatched);
                    userMovieDAO.merge(userMovie);
                    userMovieDAO.flush();
                    addFeedEntry(feedEntryType, user, movie.getORMID(), dateWatched);
                    updateMovieHoursAchievements(user, movie.getRuntime());

                } else {
                    // If movie was marked as unwatched

                    if (updates.containsKey("addedToWatchlist")) {
                        // If movie was also added to watchlist

                        userMovie.setStatus(false);
                        userMovie.setDateWatched(null);
                        userMovie.setFavourite(false);
                        userMovie.setDateFavourite(null);
                        userMovie.setRating(null);

                        userMovieDAO.merge(userMovie);
                        userMovieDAO.flush();

                    } else {
                        // If movie was only marked as unwatched
                        userMovieDAO.removeEntity("muserid=" + user.getId() + " and movieid=" + movieId);

                    }

                    feedDAO.removeEntity("muserid=" + user.getId() + "and idcontent=" + movieId);
                }
            } else if (updates.containsKey("addedToWatchlist")) {
                boolean addedToWatchlist = (boolean) updates.get("addedToWatchlist");

                if (addedToWatchlist) {
                    // If movie was added to watchlist

                    userMovie.setStatus(false);

                    userMovieDAO.merge(userMovie);
                    userMovieDAO.flush();
                } else {
                    // If movie was removed from watchlist

                    userMovieDAO.removeEntity("muserid=" + user.getId() + " and movieid=" + movieId);
                }
            } else if (updates.containsKey("favourited")) {
                boolean favourite = (boolean) updates.get("favourited");

                if (favourite) {
                    // If movie was added to favourites

                    var dateFavourited = new Date();

                    userMovie.setFavourite(true);
                    userMovie.setDateFavourite(dateFavourited);

                    userMovieDAO.merge(userMovie);
                    userMovieDAO.flush();

                    addFeedEntry(2, user, movie.getORMID(), dateFavourited);
                    updateFirstFavouriteAchievement(user);
                } else {
                    // If movie was removed from favourites

                    userMovie.setFavourite(false);
                    userMovie.setDateFavourite(null);
                    userMovie.setRating(null);

                    userMovieDAO.merge(userMovie);
                    userMovieDAO.flush();
                    feedDAO.removeEntity("muserid=" + user.getId() + "and idcontent=" + movieId + " and type=2");

                }
            } else if (updates.containsKey("rating")) {

                // If movie was rated

                Integer rating = (Integer) updates.get("rating");

                userMovie.setRating(rating);
                userMovieDAO.merge(userMovie);
                userMovieDAO.flush();

                if (rating != null) {
                    Feed f = feedDAO.getFeedWithType(movie.getTmdb(), 1);

                    if (f != null)
                        updateFeed(f, 0, new Date());

                    updateFirstRatingAchievement(user);
                } else {
                    Feed f = feedDAO.getFeedWithType(movie.getTmdb(), 0);

                    updateFeed(f, 1, null);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        userService.clearUsersCache(user.getUsername());
        return true;
    }


    private Map<Integer, String> movieHoursAchievements = Map.of(
        10, "10 movie hours",
        50, "50 movie hours",
        100, "100 movie hours",
        500, "500 movie hours",
        1000, "1000 movie hours"
    );


    public void updateMovieHoursAchievements(MUser u, int movieRuntime) {
        int hoursB = u.getMinutesCount() / 60;
        int hoursA = (u.getMinutesCount() + movieRuntime) / 60;
        for(Map.Entry<Integer, String> m: movieHoursAchievements.entrySet()){
            int key = m.getKey();
            if (key > hoursB && key<= hoursA){
                userService.addAchievement(u, m.getValue());
                break;
            }
        }
    }


    private void updateFirstFavouriteAchievement(MUser u) {
        if (u.getFavouriteCount() == 0)
            userService.addAchievement(u, "First favourite movie");
    }


    private void updateFirstRatingAchievement(MUser u) {
        if (u.getRatingsCount() == 0)
            userService.addAchievement(u, "First rating");
    }


    private void updateFirstCommentAchievement(MUser u) {
        if (u.getCommentsCount() == 0)
            userService.addAchievement(u, "First comment");
    }


    private Map<Integer, String> likesAchievements = Map.of(
            1, "1 like single comment",
            10, "10 likes single comment",
            50, "50 likes single comment",
            100, "100 likes single comment",
            500, "500 likes single comment",
            1000, "1000 likes single comment"
    );

    private List<Integer> likesNumber = new ArrayList<>(likesAchievements.keySet()).stream().sorted().collect(Collectors.toList());

    private void updateLikesAchievements(MUser u, int likes) {
        int likesB = likes - 1;
        int likesA = likes;

        int nrLikeBadges = achievementDAO.getMaxLikeBadge(u.getId()) - 1;
        int maxLikeBadge = nrLikeBadges > -1? likesNumber.get(nrLikeBadges) : 0 ;

        if (likesA < maxLikeBadge) return;

        for(Map.Entry<Integer, String> m: likesAchievements.entrySet()){
            int key = m.getKey();
            if (key > likesB && key<= likesA){
                userService.addAchievement(u, m.getValue());
                break;
            }
        }
    }


    private void addFeedEntry(int feedEntryType, MUser user, int contentId, Date date) {
        Feed f = new Feed();

        f.setType(feedEntryType);
        f.setUser(user);
        f.setIdContent(contentId);
        f.setTimestamp(new Timestamp(date.getTime()));

        feedDAO.persist(f);
    }


    private void updateFeed(Feed f, int feedEntryType, Date date) {
        f.setType(feedEntryType);

        if(date != null) {
            f.setTimestamp(new Timestamp(date.getTime()));
        }

        feedDAO.merge(f);
        feedDAO.flush();
    }


    public Map postComment(Integer movieId, String token, Map<String, Object> content) throws Exception {

        var user = mUserDAO.getSimpleUserByToken(token);
        var movie = movieDAO.loadEntity("tmdb=" + movieId);

        var comment = new Comment();

        comment.setCommenter(user);

        comment.setContent((String) content.get("message"));

        var dateCommented = new Date();

        comment.setTimestamp(new Timestamp(dateCommented.getTime()));
        comment.setLikes(0);
        comment.setMovie(movie);

        commentDAO.persist(comment);
        commentDAO.flush();

        var map = new HashMap<String, Object>();
        map.put("id", comment.getId());
        map.put("userId", user.getId());
        map.put("date", util.formatDate(dateCommented));
        map.put("content", comment.getContent());
        map.put("likes", comment.getLikes());
        map.put("username", user.getUsername());
        map.put("userAvatar", user.getAvatar() != null ? user.getAvatar() : user.getGender() + ".svg");

        addFeedEntry(3, user, comment.getId(), dateCommented);
        updateFirstCommentAchievement(user);

        return map;
    }


    public Object getMovieComments(Integer movieId, int page, String token) throws Exception {

        List comments;

        if(token != null) {
            var user = mUserDAO.getSimpleUserByToken(token);
            comments = commentDAO.getCommentsMovieWithUserLikes(movieId, page * 10, 10, user.getId());
        }
        else {
            comments = commentDAO.getCommentsMovie(movieId, page * 10, 10);
        }

        boolean moreComments = !(comments.size() < 10);

        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("moreComments", moreComments);

        return result;
    }


    public Map<String, Object>  getMedia(int movieId) {
        HashMap<String, Object> result = new HashMap<>();
        Map media = mediaDAO.getMovieMedia(movieId);
        result.put("videos", media.get('v'));
        result.put("backdrops", media.get('b'));
        result.put("posters", media.get('p'));
        return result;
    }


    @Transactional
    public boolean likeAComment(Integer id, String token) throws Exception {

        var user = mUserDAO.getSimpleUserByToken(token);
        var comment = commentDAO.findById(id);

        comment.addUpvoter(user);

        commentDAO.merge(comment);
        commentDAO.flush();

        updateLikesAchievements(user, comment.getLikes());

        return true;
    }


    @Transactional
    public boolean dislikeComment(Integer id, String token) throws Exception {
        var user = mUserDAO.getSimpleUserByToken(token);
        var comment = commentDAO.findById(id);

        comment.removeUpvoter(user);

        commentDAO.merge(comment);
        commentDAO.flush();

        return true;

    }


    @Transactional
    public Object getCommentReplies(Integer commentId, int page, String token) throws Exception {
        List replies;

        if(token != null) {
            var user = mUserDAO.getSimpleUserByToken(token);
            replies = commentDAO.getRepliesCommentWithUserLikes(commentId, page * 2, 2, user.getId());
        }
        else {
            replies = commentDAO.getRepliesComment(commentId, page * 2, 2);
        }

        boolean moreReplies = !(replies.size() < 2);

        Map<String, Object> result = new HashMap<>();
        result.put("replies", replies);
        result.put("moreReplies", moreReplies);

        return result;
    }


    @Transactional
    public Object replyToComment(int id, String token, Map<String, Object> content) throws Exception {
        var user = mUserDAO.getSimpleUserByToken(token);
        var parentComment = commentDAO.findById(id);

        var comment = new Comment();

        comment.setCommenter(user);

        comment.setContent((String) content.get("message"));

        var dateCommented = new Date();

        comment.setTimestamp(new Timestamp(dateCommented.getTime()));
        comment.setLikes(0);
        comment.setParent(id);
        comment.setMovie(parentComment.getMovie());

        commentDAO.persist(comment);
        commentDAO.flush();

        var map = new HashMap<String, Object>();

        map.put("id", comment.getId());
        map.put("userId", user.getId());
        map.put("date", util.formatDate(dateCommented));
        map.put("content", comment.getContent());
        map.put("likes", comment.getLikes());
        map.put("username", user.getUsername());
        map.put("userAvatar", user.getAvatar() != null ? user.getAvatar() : user.getGender() + ".svg");
        map.put("isLiked", false);

        updateFirstCommentAchievement(user);

        return map;
    }


    public List search(String title, String sort, String genre) throws IOException {
        if (title.equals("") && sort == null && genre == null)
            return null;

        var search = new SearchRequest("movieverse_movies");
        var builder = new SearchSourceBuilder();

        if (title.equals("") && genre == null)
            builder.query(QueryBuilders.matchAllQuery());

        else {
            var boolQuery = QueryBuilders.boolQuery();
            for (var t : title.split("\\s"))
                boolQuery.should(QueryBuilders.fuzzyQuery("name", t));
            if (genre != null) {
                for (var g: genre.split(","))
                    boolQuery.must(QueryBuilders.matchQuery("genre", g));
            }
            boolQuery.should(QueryBuilders.existsQuery("poster"));
            builder.query(boolQuery);
        }

        if (sort != null) {
            if (sort.equals("dateAsc"))
                builder.sort("release", SortOrder.ASC);
            else if (sort.equals("dateDesc"))
                builder.sort("release", SortOrder.DESC);
            else
                builder.sort("rating", SortOrder.DESC);
        }

        builder.size(30);
        if (genre == null || genre.split(",").length != 1)
            builder.minScore(1.001f);

        search.source(builder);
        var response = elasticSearch.search(search);

        var result = new ArrayList<>();
        for (var r: response.getHits()) {
            var m = r.getSourceAsMap();
            m.put("id", r.getId());
            result.add(m);
        }
        return result;
    }


    public Object showtimes(int theater) {
        String cachedShowtimes = redisCache.get("showtimes_" + theater);

        if (cachedShowtimes != null)
            return cachedShowtimes;

        List showtimes = showtimeDAO.getShowtimes(theater);

        String json = util.toJson(showtimes);
        redisCache.set("showtimes_" + theater, json);
        redisCache.set("showtimes_" + theater + "_date", Long.toString(util.unixTimeSeconds() + 3600)); //1 hour

        return showtimes;
    }


    public int estimatedCount() {
        return movieDAO.estimatedSize();
    }


    public int estimatedNumberOfComments() {
        return commentDAO.estimatedSize();
    }


    public List latestMovies(int begin, int limit) {
        return movieDAO.getLatestMovies(begin, limit);
    }


    public Object movieSearchPage() {
        String cachedInfo = redisCache.get("movieSearchPageInfo");

        if (cachedInfo != null)
            return cachedInfo;

        Map m = new HashMap();
        m.put("latest", movieDAO.getLatestMovies(0, 30));
        m.put("popular", movieDAO.getPopularMovies(0, 30));
        m.put("upcoming", movieDAO.getUpcomingMovies(0, 30));

        String json = util.toJson(m);
        redisCache.set("movieSearchPageInfo", json);
        redisCache.set("movieSearchPageInfo_date", Long.toString(util.unixTimeSeconds() + 3600)); //1 hour

        return m;
    }


    public List<Integer> theatersIds() {
        return theaterDAO.findAll()
                .stream()
                .map(Theater::getId)
                .collect(Collectors.toList());
    }

    public List randomUpcomingMovies(int limit) {
        return movieDAO.getRandomUpcomingMovies(limit);
    }

}
