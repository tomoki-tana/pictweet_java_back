package in.tech_camp.pictweet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.repository.UserRepository;
import in.tech_camp.pictweet.service.UserService;
import in.tech_camp.pictweet.validation.ValidationOrder;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

  private final UserRepository userRepository;

  private final UserService userService;

  @PostMapping("/")
  public ResponseEntity<?> createUser(@RequestBody @Validated(ValidationOrder.class) UserForm userForm, BindingResult result) {
    userForm.validatePasswordConfirmation(result);
    if (userRepository.existsByEmail(userForm.getEmail())) {
      result.rejectValue("email", "null", "Email already exists");
    }

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      return ResponseEntity.badRequest().body(Map.of("messages", errorMessages));
    }

    UserEntity userEntity = new UserEntity();
    userEntity.setNickname(userForm.getNickname());
    userEntity.setEmail(userForm.getEmail());
    userEntity.setPassword(userForm.getPassword());

    try {
      userService.createUserWithEncryptedPassword(userEntity);
      return ResponseEntity.ok().body(Map.of(
        "id", userEntity.getId(),
        "nickname", userEntity.getNickname()
    ));
    } catch (Exception e) {
      System.out.println("エラー：" + e);
       return ResponseEntity.internalServerError().body(Map.of("messages", List.of("Internal Server Error")));
    }
  }

  @GetMapping("/users/login")
  public String showLogin(){
      return "users/login";
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserEntity> showMypage(@PathVariable("userId") Integer userId) {
    UserEntity user = userRepository.findById(userId);
    List<TweetEntity> tweets = user.getTweets();
    return ResponseEntity.ok().body(user);
  }
}
