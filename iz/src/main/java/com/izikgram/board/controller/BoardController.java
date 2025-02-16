package com.izikgram.board.controller;

import com.izikgram.board.entity.Board;
import com.izikgram.board.service.BoardService;
import com.izikgram.user.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Controller
@RequestMapping("/board")
public class  BoardController {


    @Autowired
    private BoardService boardService;

    //자유,하소연 게시판 리스트
    @GetMapping("/{board_type}")
    public String boardCommunity(@PathVariable("board_type") int board_type, Model model){

        System.out.println("board_type: " + board_type);  // 확인용 로그
        if (board_type != 1 && board_type != 2) {
            model.addAttribute("error", "유효하지 않은 게시판 타입입니다.");
            return "redirect:/";
        }

        String board_name = boardService.findBoardName(board_type);
        List<Board> listBoard  = boardService.findBoardList(board_type);

        model.addAttribute("board_name", board_name);
        model.addAttribute("listBoard", listBoard);
        model.addAttribute("board_type", board_type);

        return "/board/community";
    }

    //자유,하소연 작성하기 페이지 이동
    @GetMapping("/postForm")
    public String postForm(@RequestParam("board_type")int board_type,
                           HttpSession session,
                           Model model){

        if (board_type != 1 && board_type != 2) {
            // 사용하게 된다면..
            model.addAttribute("error", "유효하지 않은 게시판 타입입니다.");
            return "redirect:/";
        }

        String board_name = boardService.findBoardName(board_type);

        model.addAttribute("board_name", board_name);
        model.addAttribute("board_type", board_type);
//        model.addAttribute("writer_id", writer_id);

        return "/board/postForm";
    }

    // 글작성
    @PostMapping("/{board_type}/post")
    public String insertPost(@PathVariable("board_type") int board_type,
                             HttpSession session,
                             @RequestParam("title") String title,
                             @RequestParam("content") String content) {

        // board_type 유효성 검사
        if (board_type != 1 && board_type != 2) {
            return "redirect:/";
        }

        //member_id 가져오기..
        User user = (User)session.getAttribute("user");
        String writer_id = user.getMember_id();

        // 게시글 삽입
        boardService.insertPost(board_type, writer_id, title, content);

        return "redirect:/board/" + board_type;
    }


    //인기게시판
    @GetMapping("/hot")
    public String hotCommunity(){
        return "/board/popularityCommunity";
    }

    //자유,하소연 상세보기
    @GetMapping("/{board_type}/{board_id}")
    public String postDetail(@PathVariable("board_type") int board_type,
                             @PathVariable("board_id") int board_id,
                             HttpSession session,
                             Model model ){

        Board board  = boardService.selectDetail(board_id,board_type);

        //세션에서 User 객체 가져오기
        User user = (User)session.getAttribute("user");
        String writer_id = user.getMember_id();

        model.addAttribute("board", board);
        model.addAttribute("member_id", writer_id);

        return "/board/postDetail";
    }

    //자유,하소연 게시글 수정하기페이지 이동
    @GetMapping("/update/{board_type}/{board_id}")
    public String updatePost(@PathVariable("board_type") int board_type,
                             @PathVariable("board_id") int board_id, Model model){

        Board board = boardService.selectDetail(board_id,board_type);
        model.addAttribute("board", board);

        return "/board/postFormModify";
    }

    //자유, 하소연 게시글 수정하기
    @PostMapping("/{board_type}/{board_id}")
    public String modifyPost(@PathVariable("board_type") int board_type,
                             @PathVariable("board_id") int board_id,
                             @RequestParam("title") String title,
                             @RequestParam("content") String content,
                             HttpSession session,
                             Model model){

        User user = (User)session.getAttribute("user");
        String member_id = user.getMember_id();

        Board board = boardService.selectDetail(board_id, board_type);
        String writer_id = board.getWriter_id();

        if(member_id != null && member_id.equals(writer_id)){
            // 서버에서 조회한 writer_id와 세션에서 조회한 member_id가 같으면 수정
            boolean result = boardService.modifyBoard(board_id, title, content, board_type);
            return "redirect:/board/" + board_type; // 성공시 게시판으로 이동
        } else {
            model.addAttribute("errorMessage", "수정에 실패했습니다.");
            return "redirect:/"; // 실패시 그냥 일단 로그인페이지
        }
    }



}
