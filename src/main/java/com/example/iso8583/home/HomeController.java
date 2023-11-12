package com.example.iso8583.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class HomeController {
  @GetMapping(value = {"/", "/home"})
  String home() {
    return "Welcome!";
  }
}
