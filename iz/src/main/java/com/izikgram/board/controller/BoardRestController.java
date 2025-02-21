package com.izikgram.board.controller;


import com.izikgram.board.entity.Comment;
import com.izikgram.board.entity.CommentDto;
import com.izikgram.board.service.BoardService;
import com.izikgram.global.security.CustomUserDetails;
import com.izikgram.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/board")
public class BoardRestController {

    @Autowired
    private BoardService boardService;

    // 1. 화면에서 필요한 api가 뭔지
    // 2. api에서 필요한 기능이 뭔지
    // 3. 필요한 기능에서 필요한 쿼리가 뭔지

    // 좋아요/싫어요 토글
    @PostMapping("/{boardId}/{boardType}/update")
    public Map<String, Integer> updateLikeCount(@PathVariable("boardId") int boardId,
                                                @PathVariable("boardType") int boardType,
                                                @RequestParam boolean isLiked,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("요청 게시글 번호: {}", boardId);
        log.info("요청 종류: {}", isLiked);

        //유저 객체 처리해야함...
        User user = userDetails.getUser();
        String memberId = user.getMember_id();



        // isLiked(true: 좋아요 요청, false: 싫어요 요청)
        return boardService.updateLikeCount(boardId, memberId, isLiked,boardType);
    }


    // 댓글 수정 (게시판 타입에 따라 다른 테이블에서 수정)
    @PostMapping("/comment/update")
    public ResponseEntity<Map<String, Object>> updateComment(@RequestBody CommentDto commentDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 댓글 수정 처리 (서비스 레이어에서 실제 댓글 수정 처리)
            int result = boardService.updateComment(commentDto);
            if (result > 0) {
                response.put("success", true);
                response.put("message", "댓글이 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "댓글 수정에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
