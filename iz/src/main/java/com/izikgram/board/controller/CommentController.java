package com.izikgram.board.controller;


import com.izikgram.board.entity.CommentDto;
import com.izikgram.board.service.BoardService;
import com.izikgram.global.security.CustomUserDetails;
import com.izikgram.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/board/")
public class CommentController {

    @Autowired
    private BoardService boardService;

    //댓글 작성
    @PostMapping("/{board_id}/comment")
    public ResponseEntity<Map<String, Object>> writeComment(
            @PathVariable("board_id") int boardId,
            @RequestParam("comment_content") String commentContent,
            @RequestParam("board_type") int boardType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userDetails.getUser();
            String memberId = user.getMember_id();

            if (boardType == 1) {
                boardService.addComment01(boardId, memberId, commentContent);
            } else if (boardType == 2) {
                boardService.addComment02(boardId, memberId, commentContent);
            }

            CommentDto newComment = boardService.getLastComment(boardId, boardType); // 새 댓글 조회 메소드 추가

            response.put("nickname", newComment.getNickname());
            response.put("comment_content", newComment.getComment_content());
            response.put("reg_date", newComment.getReg_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.put("comment_id", newComment.getComment_id());
            response.put("writer_id", newComment.getWriter_id());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "댓글 저장에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 댓글 삭제
    @DeleteMapping("/deleteComment")
    public ResponseEntity<Map<String, String>> deleteComment(
            @RequestParam("commentId") int commentId,
            @RequestParam("boardId") int boardId,
            @RequestParam("boardType") int boardType,
            @RequestParam("writerId") String writerId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, String> response = new HashMap<>();

        try {
            String memberId = userDetails.getUser().getMember_id();
            boolean isDeleted = false;

            if (memberId != null && memberId.equals(writerId)) {
                if (boardType == 1) {
                    isDeleted = boardService.deleteComment01(commentId, boardId);
                } else if (boardType == 2) {
                    isDeleted = boardService.deleteComment02(commentId, boardId);
                }
            }

            if (isDeleted) {
                response.put("message", "댓글이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "댓글 삭제에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("message", "댓글 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //  댓글 수정
    @PostMapping("/comment/update")
    public ResponseEntity<Map<String, Object>> updateComment(@RequestBody CommentDto commentDto) {
        Map<String, Object> response = new HashMap<>();

        try {
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