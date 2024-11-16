package app.socialmedia.controller;

import app.socialmedia.dto.FriendRequestDto;
import app.socialmedia.entity.Friendship;
import app.socialmedia.entity.Post;
import app.socialmedia.entity.User;
import app.socialmedia.service.FriendshipService;
import app.socialmedia.service.PostService;
import app.socialmedia.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/friends")
@AllArgsConstructor
public class FriendController {

    private final UserService userService;
    private final PostService postService;
    private final FriendshipService friendshipService;

    @GetMapping
    public List<Friendship> getFriendRequests(@AuthenticationPrincipal UserDetails user) {
        return friendshipService.getFriends(user);
    }

    @PostMapping
    public User addFriend(@AuthenticationPrincipal UserDetails userDetails, @RequestBody FriendRequestDto friendRequestDto) {
        return friendshipService.addFriend(userDetails,friendRequestDto);
    }

    @GetMapping("/posts")
    public List<Post> getPosts(@AuthenticationPrincipal UserDetails user) {
        return postService.getFriendsPosts(user);
    }
}
