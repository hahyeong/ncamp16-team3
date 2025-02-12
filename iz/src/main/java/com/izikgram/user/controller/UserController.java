package com.izikgram.user.controller;

import com.izikgram.user.entity.User;
import com.izikgram.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String register() {
        return "/user/register";
    }

    @PostMapping("/register")
    public String register(User user) {
        timeSubstring(user);

        userService.register(user);
        return "redirect:/";
    }

    @GetMapping("/findId")
    public String findId() {
        return "/user/findId";
    }

    @PostMapping("/findId")
    public String findId(String name, Model model) {
        String id = userService.findId(name);
        model.addAttribute("member_id", id);
        return "/user/findIdResult";
    }

    @GetMapping("/findPw")
    public String findPw() {
        return "/user/findPw";
    }

    @PostMapping("/findPw")
    public String findPw(String member_id, Model model) {
        boolean object = userService.findPassword(member_id);
//        log.info("member_id: " + member_id + ", object: " + object);
        if (object) {
            model.addAttribute("member_id", member_id);
//            log.info("member_id: " + member_id);
            return "/user/findPwResult";
        } else {
            return "redirect:/user/findPw";
        }
    }

    @PostMapping("/resetPw")
    public String resetPw(String member_id, String password) {
//        log.info("member_id: " + member_id + ", password: " + password);
        userService.updatePassword(member_id, password);
        return "redirect:/";
    }

    @GetMapping("/update")
    public String update() {
        return "update";
    }

    @GetMapping("/checkPwd")
    public String checkPwd() {
        return "checkPwd";
    }

    private static void timeSubstring(User user) {
        String startTime = user.getStart_time();
        String endTime = user.getEnd_time();
        String lunchTime = user.getLunch_time();

        startTime = AmPm(startTime);
        endTime = AmPm(endTime);
        lunchTime = AmPm(lunchTime);

        user.setStart_time(startTime);
        user.setEnd_time(endTime);
        user.setLunch_time(lunchTime);
    }

    private static String AmPm(String time) {
        String meridian = time.substring(6);
//        log.debug("meridian: " + meridian);

        if (meridian.equals("PM")) {
            String result;
            int hour = Integer.parseInt(time.substring(0, 2));
            if (hour == 12) {
                time = time.substring(0, 5) + ":00";
                return time;
            }
//            log.debug("hour: " + hour);
            String stringHour = String.valueOf(hour + 12);
//            log.debug("stringHour: " + stringHour);

            String substring = time.substring(2);
            result = stringHour.concat(substring);
//            log.debug("result: " + result);

            time = result.substring(0, 5) + ":00";
//            log.debug("startTime: " + time);
        } else {
            time = time.substring(0, 5) + ":00";
//            log.debug("startTime: " + time);
        }
        return time;
    }
}
