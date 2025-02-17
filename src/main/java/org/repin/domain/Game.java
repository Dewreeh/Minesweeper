package org.repin.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class Game {
    private UUID gameId;
    private int width;
    private int height;
    private int minesCount;
    private boolean completed;
    private String[][] field;
    private Boolean[][] minesField;
}
