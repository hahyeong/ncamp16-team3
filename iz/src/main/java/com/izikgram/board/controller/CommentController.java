package com.izikgram.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board/{boardIdx}")
public class CommentController {
    @PostMapping("/comment")
    public ResponseEntity<?> writeComment(@PathVariable("boardIdx") Integer boardIdx) {
        //...
        // Comment comment = commentService.newComment();
        //return ResponseEntity.ok().body(comment);
        return ResponseEntity.ok().body("게시글 성공");
    }
}
