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
 * 
 * 
 * 
 * http://10.199.71.103:8080        https://still-beyond-18845.herokuapp.com       https://minaj-snake-2017.herokuapp.com/
 * 
 * 
 */

package com.battlesnake;

import com.battlesnake.data.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {
    
    private static List<Move> ALL_MOVES = Arrays.asList(Move.values());

    @RequestMapping(value="/start", method=RequestMethod.POST, produces="application/json")
    public StartResponse start(@RequestBody StartRequest request) {
        return new StartResponse()
                .setName("Coachwhip-G")
                .setColor("#FF3497")
                .setHeadUrl("http://vignette1.wikia.nocookie.net/nintendo/images/6/61/Bowser_Icon.png/revision/latest?cb=20120820000805&path-prefix=en")
                .setHeadType(HeadType.SHADES)
                .setTailType(TailType.PIXEL)
                .setTaunt("Hello world!");
    }

    @RequestMapping(value="/move", method=RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        MoveResponse moveResponse = new MoveResponse();
        
        Snake mySnake = findOurSnake(request); // kind of handy to have our snake at this level
        List<Move> towardsFoodMoves = moveTowardsFood(request, mySnake.getCoords());
        // List<Move> keepGoingMoves = keepGoing(request, mySnake.getCoords());
        List<Move> keepGoingMoves = guardFood(request, mySnake);
        
        // evade
        towardsFoodMoves = evade(request, mySnake, towardsFoodMoves);
        keepGoingMoves = evade(request, mySnake, keepGoingMoves);
        List<Move> anythingMoves = evade(request, mySnake, ALL_MOVES);
        
        Move selectedMove = null;
        if (towardsFoodMoves != null && !towardsFoodMoves.isEmpty() && mySnake.getHealth() < 60) {
            selectedMove = towardsFoodMoves.get(0);
        } else if (!keepGoingMoves.isEmpty()){
            selectedMove = keepGoingMoves.get(0);
        } else {
            selectedMove = anythingMoves.get(0);
        }
        return moveResponse.setMove(selectedMove);
    }

    private List<Move> evade(MoveRequest request, Snake mySnake, List<Move> options) {
        Point head = mySnake.getCoords().get(0);
        int[][] field = new int[request.getWidth()][request.getHeight()];
        // Makr the field
        for (Snake s: request.getSnakes()) {
            for (Point p: s.getCoords()) {
                field[p.x][p.y] = 1;
            }
            if (!s.getId().equals(mySnake.getId())) {
                Point h = s.getCoords().get(0);
                if (h.leftOf().isValid(request.getWidth(), request.getHeight()))
                    field[h.leftOf().x][h.leftOf().y] = 2;
                if (h.rightOf().isValid(request.getWidth(), request.getHeight()))
                    field[h.rightOf().x][h.rightOf().y] = 2;
                if (h.upOf().isValid(request.getWidth(), request.getHeight()))
                    field[h.upOf().x][h.upOf().y] = 2;
                if (h.downOf().isValid(request.getWidth(), request.getHeight()))
                    field[h.downOf().x][h.downOf().y] = 2;
            }
        }
        // Vet the options
        List<Move> vetted = new ArrayList<Move>(options.size());
        for (Move m: options) {
            Point target = head.move(m);
            if (target.isValid(request.getWidth(), request.getHeight()) && field[target.x][target.y] == 0 && hasPathToEdge(deepCopy(field), target)) {
                vetted.add(m);
            }
        }
        return vetted;
    }
    
    private int[][] deepCopy(int[][] matrix) {
        return java.util.Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());
    }
    
    private boolean hasPathToEdge(int[][] field, Point target) {
        boolean hasLeft = true;
        for (int x = target.x; x >= 0; x--) {
            if (field[x][target.y] != 0) {
                hasLeft = false;
                break;
            }
        }
        boolean hasRight = true;
        for (int x = target.x; x < field.length; x++) {
            if (field[x][target.y] != 0) {
                hasRight = false;
                break;
            }
        }
        boolean hasUp = true;
        for (int y = target.y; y >= 0; y--) {
            if (field[target.x][y] != 0) {
                hasUp = false;
                break;
            }
        }
        boolean hasDown = true;
        for (int y = target.y; y < field[0].length; y++) {
            if (field[target.x][y] != 0) {
                hasDown = false;
                break;
            }
        }
        return hasLeft || hasRight || hasUp || hasDown;
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
                if (head.y <= 1) {
                    if (head.x <= 1) {
                        moves.add(Move.RIGHT);
                    } else {
                        moves.add(Move.LEFT);
                    }
                } else {
                    moves.add(Move.UP);
                }
            } else {
                // down
                if (head.y >= request.getHeight() - 2) {
                    if (head.x <= 1) {
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
                if (head.x <= 1) {
                    if (head.y <= 1) {
                        moves.add(Move.DOWN);
                    } else {
                        moves.add(Move.UP);
                    }
                } else {
                    moves.add(Move.LEFT);
                }
            } else {
                // right
                if (head.x >= request.getWidth() - 2) {
                    if (head.y <= 1) {
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

        if (firstFoodLocation[0] < mySnakeHead.x) {
            towardsFoodMoves.add(Move.LEFT);
        }

        if (firstFoodLocation[0] > mySnakeHead.x) {
            towardsFoodMoves.add(Move.RIGHT);
        }

        if (firstFoodLocation[1] < mySnakeHead.y) {
            towardsFoodMoves.add(Move.UP);
        }

        if (firstFoodLocation[1] > mySnakeHead.y) {
            towardsFoodMoves.add(Move.DOWN);
        }

        return towardsFoodMoves;
    }
    
    private boolean someBody(List<Point> mySnake, Point target) {
        for (int i = 1; i < mySnake.size(); i++) {
            if (mySnake.get(i).theSame(target)) {
                return true;
            }
        }
        return false;
    }
    
    private List<Move> guardFood(MoveRequest request, Snake mySnake) {
        List<Point> coords = mySnake.getCoords();
        if (coords.size() < 6 || mySnake.getHealth() < 75) {
            return keepGoing(request, coords);
        }
        double distance = ((double)coords.size() - 2) / 8;
        ArrayList<Move> moves = new ArrayList<>();
        Point head = coords.get(0);
        Point neck = coords.get(1);
        int[] firstFoodLocation = request.getFood()[request.getFood().length - 1];
        if (firstFoodLocation[0] < distance || firstFoodLocation[1] < distance || 
                firstFoodLocation[0] > request.getWidth() - 1  - distance ||
                firstFoodLocation[1] > request.getHeight() - 1 - distance) {
            return keepGoing(request, coords);
        }
        double distanceLeft = penalizeLow(distance, head.leftOf().distanceTo(firstFoodLocation)) + (neck.theSame(head.leftOf()) ? 100 : 0);
        double distanceRight = penalizeLow(distance, head.rightOf().distanceTo(firstFoodLocation)) + (neck.theSame(head.rightOf()) ? 100 : 0);;
        double distanceUp = penalizeLow(distance, head.upOf().distanceTo(firstFoodLocation)) + (neck.theSame(head.upOf()) ? 100 : 0);;
        double distanceDown = penalizeLow(distance, head.downOf().distanceTo(firstFoodLocation)) + (neck.theSame(head.downOf()) ? 100 : 0);;
        
        if (distanceLeft <= distanceRight && distanceLeft <= distanceUp && distanceLeft <= distanceDown) {
            moves.add(Move.LEFT);
        }
        if (distanceRight <= distanceLeft && distanceRight <= distanceUp && distanceRight <= distanceDown) {
            moves.add(Move.RIGHT);
        }
        if (distanceUp <= distanceLeft && distanceUp <= distanceRight && distanceUp <= distanceDown) {
            moves.add(Move.UP);
        }
        if (distanceDown <= distanceLeft && distanceDown <= distanceRight && distanceDown <= distanceUp) {
            moves.add(Move.DOWN);
        }
        return moves;
    }

    private double penalizeLow(double distance, double computed) {
        return computed > distance ? computed : 100;
    }
}
