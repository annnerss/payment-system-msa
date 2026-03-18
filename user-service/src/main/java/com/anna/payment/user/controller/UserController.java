package com.anna.payment.user.controller;

import com.anna.payment.user.entity.User;
import com.anna.payment.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User create(@RequestBody User user){
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable("id") Long id){
        return userService.getUser(id);
    }

}