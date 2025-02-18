package org.repin.service;

import lombok.extern.slf4j.Slf4j;
import org.repin.domain.Game;
import org.repin.dto.ErrorResponse;
import org.repin.dto.GameInfoResponseDto;
import org.repin.dto.GameTurnRequestDto;
import org.repin.dto.NewGameRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class GameService {

    private final RedisTemplate<String, Game> redisTemplate;

    @Autowired
    GameService(RedisTemplate<String, Game> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public Game createNewGame(NewGameRequestDto requestDto){

        Game game = new Game();

        game.setGameId(generateUUID());
        game.setWidth(requestDto.getWidth());
        game.setHeight(requestDto.getHeight());
        game.setMinesCount(requestDto.getMines_count());
        game.setField(generateField(requestDto));
        game.setCompleted(false);
        game.setMinesField(generateMinesField(requestDto));

        //сохраняем игру в редисе для хранения состояния поля между запросами
        redisTemplate.opsForValue().set(game.getGameId().toString(), game);

        return game;
    }

    public GameInfoResponseDto getGameInfo(Game game){
        GameInfoResponseDto response = new GameInfoResponseDto();

        response.setGame_id(game.getGameId());
        response.setField(game.getField());
        response.setWidth(game.getWidth());
        response.setHeight(game.getHeight());
        response.setMinesCount(game.getMinesCount());
        response.setCompleted(game.isCompleted());

        return response;
    }

    private String[][] generateField(NewGameRequestDto dto){
        String[][] field = new String[dto.getHeight()][dto.getWidth()];
        for (String[] string : field) {
            Arrays.fill(string, " ");
        }
        return field;
    }

    //true на i, j-ой позиции значит, что там есть бомба, false - что нет
    private Boolean[][] generateMinesField(NewGameRequestDto dto){
        int minesCount = dto.getMines_count();
        int height = dto.getHeight();
        int width = dto.getWidth();

        Boolean[][] minesField = new Boolean[dto.getHeight()][dto.getWidth()];

        for (Boolean[] str : minesField) {
            Arrays.fill(str, false);
        }

        //Генерируем позиции мин
        Random random = new Random();
        while (minesCount > 0) {
            int position = random.nextInt(height * width);
            int row = position / width;
            int col = position % width;
            if (!minesField[row][col]) {
                minesField[row][col] = true;
                minesCount--;
            }
        }
        return minesField;
    }


    public Game makeTurn(Game game, GameTurnRequestDto turnRequest) {


        Boolean[][] minesField = game.getMinesField();
        String[][] userField = game.getField();
        int col = turnRequest.getCol();
        int row = turnRequest.getRow();

        // Попали на мину
        if (minesField[row][col]) {
            game.setCompleted(true);
            game.setField(openCells(game, "X")); // Открываем все ячейки с символом X (т.к. игрок проигал)

            redisTemplate.opsForValue().set(game.getGameId().toString(), game);
            return game;

        } else {
            int countMinesAround = getCountMinesAround(game, row, col);

            if (countMinesAround == 0) { //вокруг нет мин
                game.setField(openEmptyCells(game, row, col, new boolean[game.getHeight()][game.getWidth()])); //открываем пустые клетки
            } else {
                userField[row][col] = String.valueOf(countMinesAround); //вокруг есть мины, ставим их количество в ячейку
                game.setField(userField);
            }
        }

        //проверка на выигрыш
        if (isGameCompleted(game)) {
            game.setCompleted(true);
            game.setField(openCells(game, "M")); //открываем клетки с символом M
        }


        redisTemplate.opsForValue().set(game.getGameId().toString(), game);
        return game;
    }

    private String[][] openCells(Game game, String mineSymbol) {
        Boolean[][] minesField = game.getMinesField();
        String[][] userField = game.getField();

        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (minesField[i][j]) {
                    userField[i][j] = mineSymbol;
                } else {
                    userField[i][j] = String.valueOf(getCountMinesAround(game, i, j));
                }
            }
        }
        game.setField(userField);
        return userField;
    }


    //открываем клетки рекурсивно поиском в глубину
    String[][] openEmptyCells(Game game, int row, int col, boolean[][] visited){
        int width = game.getWidth();
        int height = game.getHeight();

        String[][] userField = game.getField();

        //проверка на границы поля
        if (row < 0 || row >= height || col < 0 || col >= width || visited[row][col]) {
            return userField;
        }

        visited[row][col] = true;

        int minesAround = getCountMinesAround(game, row, col);

        //если вокруг есть мины, ставим на текущую клетку их количество
        if(minesAround > 0){
            userField[row][col] = String.valueOf(minesAround);
            return userField;
        }

        //иначе ставим 0 и проверяем соседние клетки
        userField[row][col] = "0";

        //все смещения относительно текущей клетки
        int[] x = {-1, -1,  0,  1,  1,  1,  0, -1};
        int[] y = { 0,  1,  1,  1,  0, -1, -1, -1};


        for (int i = 0; i < 8; i++) {
            int newRow = row + y[i];
            int newCol = col + x[i];

            // Проверяем, является ли соседняя клетка пустой перед рекурсией
            if (newRow >= 0 && newRow < height && newCol >= 0 && newCol < width && !visited[newRow][newCol]) {
                openEmptyCells(game, newRow, newCol, visited);
            }
        }

        game.setField(userField);
        return userField;
    }


    private Integer getCountMinesAround(Game game, int row, int col){
        Boolean[][] minesField = game.getMinesField();

        //все возможные смещения относительно текущего элемента поля
        int[] x = {-1, -1,  0,  1,  1,  1,  0, -1};
        int[] y = { 0,  1,  1,  1,  0, -1, -1, -1};

        int count = 0;
        int newRow, newCol;
        for(int i = 0; i < 8; i++){
            newRow = row + x[i];
            newCol = col + y[i];

            //проверка чтобы не выйти за границы поля
            if ((newRow >= 0 && newRow < game.getHeight()) && (newCol >= 0 && newCol < game.getWidth())) {
                count += minesField[newRow][newCol] ? 1 : 0; //если попадаем на true (мина есть ) увеличиваем счётчик
            }
        }

        return count;
    }

    private boolean isGameCompleted(Game game) {
        Boolean[][] minesField = game.getMinesField();
        String[][] userField = game.getField();

        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                // Если это безопасная ячейка и она всё ещё закрыта (равна " ")
                if (!minesField[i][j] && userField[i][j].equals(" ")) {
                    return false;
                }
            }
        }
        return true;
    }


    public Game getGameById(UUID gameId) throws ErrorResponse {
        Game game = redisTemplate.opsForValue().get(gameId.toString());
        if (game == null) {
            throw new ErrorResponse("Игра с таким id не создана или устарела");
        }
        return game;
    }

    private UUID generateUUID(){
        return UUID.randomUUID();
    }

}
