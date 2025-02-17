package org.repin.service;

import org.repin.domain.Game;
import org.repin.dto.ErrorResponse;
import org.repin.dto.GameTurnRequestDto;
import org.repin.dto.NewGameRequestDto;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    public ErrorResponse validateNewGameRequest(NewGameRequestDto requestDto) {
        if (requestDto.getHeight() < 2 || requestDto.getHeight() > 30) {
            return new ErrorResponse("Высота поля должна быть не менее 2 и не более 30");
        }
        if (requestDto.getWidth() < 2 || requestDto.getWidth() > 30) {
            return new ErrorResponse("Ширина поля должна быть не менее 2 и не более 30");
        }
        if (requestDto.getMines_count() < 1 || requestDto.getMines_count() > requestDto.getHeight() * requestDto.getWidth() - 1) {
            return new ErrorResponse("Количество мин должно быть не менее 1 и не более " + (requestDto.getHeight() * requestDto.getWidth() - 1));
        }
        return null;
    }

    public ErrorResponse validateGameState(Game game, GameTurnRequestDto turnRequest) {
        if (game.isCompleted()) {
            return new ErrorResponse("Игра уже закончена.");
        }
        String[][] userField = game.getField();
        int col = turnRequest.getCol();
        int row = turnRequest.getRow();

        if (!userField[row][col].equals(" ")) {
            return new ErrorResponse("Ячейка уже была выбрана ранее.");
        }

        return null;
    }
}
