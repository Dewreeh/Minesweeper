package org.repin.dto;

import lombok.Data;

@Data
public class NewGameRequestDto {
    private int width;
    private int height;
    private int mines_count;
}
