package org.repin.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GameInfoResponseDto {
    private UUID game_id;
    private int width;
    private int height;
    private int minesCount;
    private boolean completed;
    private String[][] field;
}
