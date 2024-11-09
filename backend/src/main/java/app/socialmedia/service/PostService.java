package app.socialmedia.service;

import app.socialmedia.dto.PostRequest;
import app.socialmedia.entity.Post;
import app.socialmedia.entity.User;
import app.socialmedia.repository.PostRepository;
import app.socialmedia.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PostService {

    private PostRepository postRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;


    public List<Post> getAllPosts(){
       return postRepository.findAll();
    }

    @Transactional
    public Post addPost(UserDetails userDetails, PostRequest postRequest) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        log.info("Adding new post for the user {}",user.getFirstName());
       postRequest.setUser(user);
       postRequest.setDate(new Date(System.currentTimeMillis()));
       return postRepository.save(modelMapper.map(postRequest,Post.class));
    }
}
