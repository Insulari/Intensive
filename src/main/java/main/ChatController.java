package main;

import main.model.Message;
import main.model.User;
import main.repos.MessageRepository;
import main.repos.UserRepository;
import main.response.AddMessageResponse;
import main.response.AuthResponse;
import main.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    @GetMapping(path = "/api/auth")
    public AuthResponse auth() {
        AuthResponse response = new AuthResponse();
        String sessionId = getSessionId();
        User user = userRepository.getBySessionId(sessionId);
        response.setResult(user != null);
        if (user != null) {
            response.setName(user.getName());
        }
        return response;
    }

    @GetMapping(path = "/api/users")
    public Map<String, List> getUsers() {
        Iterable<User> all = userRepository.findAll();
        final List<User> users = StreamSupport
                .stream(all.spliterator(), false)
                .collect(Collectors.toList());
        Map<String, List> getUsers = new HashMap<>();
        getUsers.put("users", users);
        return getUsers;
    }

    @PostMapping(path = "/api/users")
    public Map<String, Boolean> addUser(HttpServletRequest request) {
        String name = request.getParameter("name");
        String sessionId = getSessionId();
        User user = new User();
        user.setName(name);
        user.setRegTime(new Date());
        user.setSessionId(sessionId);
        userRepository.save(user);
        HashMap<String, Boolean> response = new HashMap<>();
        response.put("result", true);
        return response;
    }

    @PostMapping(path = "/api/messages")
    public AddMessageResponse addMessage(HttpServletRequest request) {
        String text = request.getParameter("text");

        String sessionId = getSessionId();
        User user = userRepository.getBySessionId(sessionId);

        Date time = new Date();
        Message message = new Message();
        message.setSendTime(time);
        message.setUser(user);
        message.setText(text);
        messageRepository.save(message);

        AddMessageResponse response = new AddMessageResponse();
        response.setResult(true);
        response.setTime(formatter.format(time));
        return response;
    }

    @GetMapping(path = "/api/messages")
    public Map<String, List> getMessages() {
        List<MessageResponse> messagesList =
                new ArrayList<>();
        Iterable<Message> messages = messageRepository.findAll();
        for (Message message : messages) {
            MessageResponse messageItem = new MessageResponse();
            messageItem.setName(message.getUser().getName());
            messageItem.setTime(
                    formatter.format(message.getSendTime())
            );
            messageItem.setText(message.getText());
            messagesList.add(messageItem);
        }
        Map<String, List> response = new HashMap<>();
        response.put("messages", messagesList);
        return response;
    }

    private String getSessionId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }
}
