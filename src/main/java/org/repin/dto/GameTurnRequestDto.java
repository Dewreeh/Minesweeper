package org.repin.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GameTurnRequestDto {
    UUID game_id;
    int col;
    int row;
}
