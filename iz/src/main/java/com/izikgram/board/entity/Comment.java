package com.izikgram.board.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private int comment_id;
    private int board_id;
    private String writer_id;
    private String comment_content;
    private LocalDateTime reg_date;

}
