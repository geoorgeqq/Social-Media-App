package app.socialmedia.service;

import app.socialmedia.dto.UserRequestDto;
import app.socialmedia.dto.UserResponseDto;
import app.socialmedia.entity.User;
import app.socialmedia.exception.user.EmailAlreadyExistsException;
import app.socialmedia.exception.user.InvalidEmailException;
import app.socialmedia.repository.UserRepository;
import app.socialmedia.validation.Rules;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private ModelMapper modelMapper;
    private UserRepository userRepository;
    private Rules rules;

    public List<UserResponseDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> responseUsers = new ArrayList<>();
        for (User user : users) {
            responseUsers.add(modelMapper.map(user, UserResponseDto.class));
        }
        log.info("Found {} users", responseUsers.size());
        return responseUsers;
    }

    @Transactional
    public UserResponseDto addUser(UserRequestDto userDto) {
        log.info("Saving user with email: {}",userDto.getEmail());
        try {
            if(!rules.isValidEmail(userDto.getEmail())){
                throw new InvalidEmailException();
            }
            User user = userRepository.save(modelMapper.map(userDto, User.class));
            return modelMapper.map(user, UserResponseDto.class);
        } catch (DataIntegrityViolationException ex){
            throw new EmailAlreadyExistsException();
        }
    }
}
