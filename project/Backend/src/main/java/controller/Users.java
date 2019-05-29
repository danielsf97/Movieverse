package controller;

import business.MovieManager;
import business.UsersManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RestController
public class Users {

    @Autowired
    UsersManager usersManager;

    @Autowired
    MovieManager movieManager;

    @RequestMapping(method = GET, value = "/profile")
    public ResponseEntity<Object> profile(@RequestHeader(value = "Authorization") String t,
                                          @RequestParam(value = "username", required = false) String username) {
        String token = t.split(" ")[1];

        try {
            return Util.ok(usersManager.profileInfo(token, username));
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = GET, value = "/feed")
    public ResponseEntity<Object> feed(@RequestHeader(value = "Authorization") String t,
                                       @RequestParam(value = "username", required = false) String username) {
        String token = t.split(" ")[1];

        try {
            return Util.ok(usersManager.feedInfo(token, username));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Util.badRequest("");
        }
    }

    @RequestMapping(method = GET, value = "/avatar")
    public ResponseEntity<Object> getAvatar(@RequestHeader(value = "Authorization") String t) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.getAvatar(token));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = "/avatar")
    public ResponseEntity<Object> setAvatar(@RequestHeader(value = "Authorization") String t,
                                         @RequestParam(value = "image") MultipartFile file) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.newAvatar(token, file));
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = PUT, value = "/genre")
    public ResponseEntity<Object> genre(@RequestHeader(value = "Authorization") String t,
                                         @RequestBody Map body) {
        String token = t.split(" ")[1];
        try {
            usersManager.newGenre(token, (String) body.get("genre"));
            return Util.ok("");
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = GET, value = "/friend-requests")
    public ResponseEntity<Object> genre(@RequestHeader(value = "Authorization") String t,
                                        @RequestParam(value = "type") String type) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.getFriendRequests(token, type));
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = POST, value = "/friend-request")
    public ResponseEntity<Object> friendRequest(@RequestHeader(value = "Authorization") String t,
                                                @RequestBody Map body) {
        String token = t.split(" ")[1];
        String username = (String) body.get("username");
        try {
            usersManager.newFriendRequest(token, username);
            return Util.ok("");
        }
        catch (Exception e) {
            e.printStackTrace();
            return Util.badRequest(e.getMessage());
        }
    }

    //Processes a friend request
    @RequestMapping(method = PUT, value = "/process-request")
    public ResponseEntity<Object> process(@RequestHeader(value = "Authorization") String t,
                                          @RequestBody Map body) {
        String token = t.split(" ")[1];
        String username = (String) body.get("username");
        boolean decision = (boolean) body.get("decision");

        try {
            usersManager.processRequest(token, username, decision);
            return Util.ok("");
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    //Cancels a sent request
    @RequestMapping(method = PUT, value = "/cancel-request")
    public ResponseEntity<Object> cancelRequest(@RequestHeader(value = "Authorization") String t,
                                                @RequestBody Map body) {
        String token = t.split(" ")[1];
        String username = (String) body.get("username");

        try {
            usersManager.cancelRequest(token, username);
            return Util.ok("");
        }
        catch (Exception e) {
            return Util.badRequest(e.getMessage());
        }
    }

    @RequestMapping(method = GET, value = "/users-search")
    public ResponseEntity<Object> search(@RequestHeader(value = "Authorization") String t,
                                         @RequestParam(value = "name") String name) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.search(token, name));
        }
        catch (Exception e) {
            return Util.badRequest("");
        }
    }

    @RequestMapping(method = GET, value = "profile/{movieListType}")
    public ResponseEntity<Object> userMovieList(@PathVariable("movieListType") String type,
                                                @RequestHeader(value = "Authorization") String t,
                                                @RequestParam(value = "begin") int begin,
                                                @RequestParam(value = "limit") int limit) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.movieList(token, type, begin, limit));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Util.badRequest("");
        }
    }

    @RequestMapping(method = GET, value = "profile/friends")
    public ResponseEntity<Object> userMovieList(@RequestHeader(value = "Authorization") String t,
                                                @RequestParam(value = "begin") int begin,
                                                @RequestParam(value = "limit") int limit) {
        String token = t.split(" ")[1];
        try {
            return Util.ok(usersManager.friendsList(token, begin, limit));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Util.badRequest("");
        }
    }
}
