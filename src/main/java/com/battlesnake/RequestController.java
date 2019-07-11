/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlesnake;

import com.battlesnake.data.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {

    @RequestMapping(value="/start", method=RequestMethod.POST, produces="application/json")
    public StartResponse start(@RequestBody StartRequest request) {
        return new StartResponse()
                .setName("Coachwhip")
                .setColor("#FF3497")
                .setHeadUrl("http://vignette1.wikia.nocookie.net/nintendo/images/6/61/Bowser_Icon.png/revision/latest?cb=20120820000805&path-prefix=en")
                .setHeadType(HeadType.DEAD)
                .setTailType(TailType.PIXEL)
                .setTaunt("Hello world!");
    }

    @RequestMapping(value="/move", method=RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        MoveResponse moveResponse = new MoveResponse();
        
        Snake mySnake = findOurSnake(request); // kind of handy to have our snake at this level
        List<Move> towardsFoodMoves = moveTowardsFood(request, mySnake.getCoords());
        List<Move> keepGoingMoves = keepGoing(request, mySnake.getCoords());
        
        Move selectedMove = null;
        
        if (towardsFoodMoves != null && !towardsFoodMoves.isEmpty() && mySnake.getHealth() < 50) {
            selectedMove = towardsFoodMoves.get(0);
        } else {
            selectedMove = keepGoingMoves.get(0);
        }
        // evade
        selectedMove = evade(request, mySnake, selectedMove);
        
        return moveResponse.setMove(selectedMove);
    }

    private Move evade(MoveRequest request, Snake mySnake, Move selectedMove) {
        Point head = mySnake.getCoords().get(0);
        for (int i = 0; i < 4; i++) {
            Point target = head.move(selectedMove);
            boolean hit = false;
            for (Snake s: request.getSnakes()) {
                if(!notBody(s.getCoords(), target)) {
                    hit = true;
                    selectedMove = clockwise(selectedMove);
                    break;
                }
            }
            if (!hit) {
                break;
            }
        }
        return selectedMove;
    }

    private Move clockwise(Move selectedMove) {
        switch (selectedMove) {
        case DOWN:
            return Move.LEFT;
        case LEFT:
            return Move.UP;
        case UP:
            return Move.RIGHT;
        default:
            return Move.DOWN;
            
        }
    }

    private List<Move> keepGoing(MoveRequest request, List<Point> coords) {
        ArrayList<Move> moves = new ArrayList<>();
        if (coords.size() < 2) {
            moves.add(Move.LEFT);
        }
        Point head = coords.get(0);
        Point neck = coords.get(1);
        if (head.x == neck.x) {
            if (head.y < neck.y) {
                // up
                if (head.y == 0) {
                    if (head.x == 0) {
                        moves.add(Move.RIGHT);
                    } else {
                        moves.add(Move.LEFT);
                    }
                } else {
                    moves.add(Move.UP);
                }
            } else {
                // down
                if (head.y == request.getHeight() - 1) {
                    if (head.x == 0) {
                        moves.add(Move.RIGHT);
                    } else {
                        moves.add(Move.LEFT);
                    }
                } else {
                    moves.add(Move.DOWN);
                }
            }
        } else {
            if (head.x < neck.x) {
                // left
                if (head.x == 0) {
                    if (head.y == 0) {
                        moves.add(Move.DOWN);
                    } else {
                        moves.add(Move.UP);
                    }
                } else {
                    moves.add(Move.LEFT);
                }
            } else {
                // right
                if (head.x == request.getWidth() - 1) {
                    if (head.y == 0) {
                        moves.add(Move.DOWN);
                    } else {
                        moves.add(Move.UP);
                    }
                } else {
                    moves.add(Move.RIGHT);
                }
            }
        }
        return moves;
    }

    @RequestMapping(value="/end", method=RequestMethod.POST)
    public Object end() {
        // No response required
        Map<String, Object> responseObject = new HashMap<String, Object>();
        return responseObject;
    }

    /*
     *  Go through the snakes and find your team's snake
     *  
     *  @param  request The MoveRequest from the server
     *  @return         Your team's snake
     */
    private Snake findOurSnake(MoveRequest request) {
        String myUuid = request.getYou();
        List<Snake> snakes = request.getSnakes();
        return snakes.stream().filter(thisSnake -> thisSnake.getId().equals(myUuid)).findFirst().orElse(null);
    }


    /*
     *  Simple algorithm to find food
     *  
     *  @param  request The MoveRequest from the server
     *  @param  request An integer array with the X,Y coordinates of your snake's head
     *  @return         A Move that gets you closer to food
     */    
    public ArrayList<Move> moveTowardsFood(MoveRequest request, List<Point> mySnake) {
        Point mySnakeHead = mySnake.get(0);
        ArrayList<Move> towardsFoodMoves = new ArrayList<>();

        int[] firstFoodLocation = request.getFood()[request.getFood().length - 1];

        if (firstFoodLocation[0] < mySnakeHead.x && notBody(mySnake, mySnakeHead.leftOf())) {
            towardsFoodMoves.add(Move.LEFT);
        }

        if (firstFoodLocation[0] > mySnakeHead.x && notBody(mySnake, mySnakeHead.rightOf())) {
            towardsFoodMoves.add(Move.RIGHT);
        }

        if (firstFoodLocation[1] < mySnakeHead.y && notBody(mySnake, mySnakeHead.upOf())) {
            towardsFoodMoves.add(Move.UP);
        }

        if (firstFoodLocation[1] > mySnakeHead.y && notBody(mySnake, mySnakeHead.downOf())) {
            towardsFoodMoves.add(Move.DOWN);
        }

        return towardsFoodMoves;
    }
    
    private boolean notBody(List<Point> mySnake, Point target) {
        for (int i = 1; i < mySnake.size(); i++) {
            if (mySnake.get(i).theSame(target)) {
                return false;
            }
        }
        return true;
    }
}
