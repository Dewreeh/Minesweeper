package org.repin.controller;

import lombok.extern.slf4j.Slf4j;
import org.repin.domain.Game;
import org.repin.dto.ErrorResponse;
import org.repin.dto.GameTurnRequestDto;
import org.repin.dto.NewGameRequestDto;
import org.repin.service.GameService;
import org.repin.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@Slf4j
public class Controller {

    GameService gameService;
    ValidationService validationService;

    @Autowired
    Controller(GameService gameService,
               ValidationService validationService){
        this.gameService = gameService;
        this.validationService = validationService;
    }

    @PostMapping("/new")
    ResponseEntity<Object> createNewGame(@RequestBody NewGameRequestDto requestDto){
        ErrorResponse errorResponse = validationService.validateNewGameRequest(requestDto);
        if (errorResponse != null) {
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Game newGame = gameService.createNewGame(requestDto);
        return ResponseEntity.ok().body(gameService.getGameInfo(newGame));
    }


    @PostMapping("/turn")
    ResponseEntity<Object> makeTurn(@RequestBody GameTurnRequestDto turnRequest){
        Game game;
        try {
            game = gameService.getGameById(turnRequest.getGame_id());
        } catch (ErrorResponse e){
            return ResponseEntity.badRequest().body(e);
        }

        //Вызываем проверку на то, не сделан ли ход ранее
        ErrorResponse validationError = validationService.validateGameState(game, turnRequest);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        game = gameService.makeTurn(game, turnRequest);

        return ResponseEntity.ok().body(gameService.getGameInfo(game));
    }
}
