import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;
import java.util.List;

class SmartGuy {

    private final int SWITCH_ROUND = 50;
    private final int MAX_DEPTH = 6;
    private final int END_DEPTH = 14;
    private int maxDepth;

    public Socket s;
    public BufferedReader sin;
    public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int them;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;

    int validMoves[] = new int[64];
    int numValidMoves;


    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public SmartGuy(int _me, String host) {
        maxDepth = MAX_DEPTH;
        me = _me;
        if(me == 1)
        {
            them = 2;
        } else
        {
            them = 1;
        }
        initClient(host);

        int myMove;

        while (true) {
//            System.out.println("Read");
            readMessage();
            if(round == SWITCH_ROUND)
            {
                System.out.println("NOT MURDER!!!!!");
                maxDepth = END_DEPTH;
            }

            if (turn == me) {
//                System.out.println("Move");

//                myMove = new StateNode(state, true).pickMove(0, Integer.MIN_VALUE, Integer.MAX_VALUE);

                myMove = move();
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly

                String sel = myMove / 8 + "\n" + myMove % 8;

//                System.out.println("Selection: " + myMove / 8 + ", " + myMove % 8);

                sout.println(sel);
            }
        }
        //while (turn == me) {
        //    System.out.println("My turn");

        //readMessage();
        //}
    }


    private int move()
    {
        if(round < 4)
        {
            getValidMoves(round, state, me);
            int rand = generator.nextInt(numValidMoves);
            return validMoves[rand];
        } else
        {
            StateNode rutNode = new StateNode(state, true, me, 0);
            double[] scoreMovePair = rutNode.minimax(-1 * Double.MAX_VALUE, Double.MAX_VALUE);
//            System.out.println("Final score-move: " + scoreMovePair[0] + ", " + scoreMovePair[1]);
            return ((int) scoreMovePair[1]);
        }
    }

    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    private void getValidMoves(int round, int state[][], int curColor) {
        int i, j;

        numValidMoves = 0;
        if (round < 4) {
            if (state[3][3] == 0) {
                validMoves[numValidMoves] = 3*8 + 3;
                numValidMoves ++;
            }
            if (state[3][4] == 0) {
                validMoves[numValidMoves] = 3*8 + 4;
                numValidMoves ++;
            }
            if (state[4][3] == 0) {
                validMoves[numValidMoves] = 4*8 + 3;
                numValidMoves ++;
            }
            if (state[4][4] == 0) {
                validMoves[numValidMoves] = 4*8 + 4;
                numValidMoves ++;
            }
//            System.out.println("Valid Moves:");
//            for (i = 0; i < numValidMoves; i++) {
//                System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
//            }
        }
        else {
//            System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j, curColor)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
//                            System.out.println("Could be: " + i + ", " + j);
                        } //else
//                        {
//                            System.out.println("Couldn't be: " + i + ", " + j);
//                        }
                    }
                }
            }
        }


        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }

    private boolean checkDirection(int state[][], int row, int col, int incx, int incy, int curColor) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;

        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;

            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;

            sequence[seqLen] = state[r][c];
            seqLen++;
        }

        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (curColor == 1) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }

        return false;
    }

    private boolean couldBe(int state[][], int row, int col, int curColor) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                if (checkDirection(state, row, col, incx, incy, curColor))
                    return true;
            }
        }

        return false;
    }

    public void readMessage() {
        int i, j;
        String status;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());

            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println("Error:");
                    System.out.println(e);
                }

                System.exit(1);
            }

            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
//            System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
//            System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

//        System.out.println("Turn: " + turn);
//        System.out.println("Round: " + round);
//        for (i = 7; i >= 0; i--) {
//            for (j = 0; j < 8; j++) {
//                System.out.print(state[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.println();
    }

    public void initClient(String host) {
        int portNumber = 3333+me;

        try {
            s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }


    // compile on your machine: javac *.java
    // call: java SmartGuy [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new SmartGuy(Integer.parseInt(args[1]), args[0]);
    }

    private class StateNode
    {

        private int[][] state;
        private boolean maximizer;
        private int depth;
        private int myColor;
        private List<StateNode> children;
        private List<Integer> moveToChild;

        /**
         * Creates a new state node
         * @param state The state the node represents
         * @param myTurn Whether the current turn is the player's (maximizer's) turn
         * @param myColor 1 for black, 2 for white (0 for no piece)
         */
        public StateNode(int[][] state, boolean myTurn, int myColor, int depth)
        {
//            System.out.println("State Node created at depth " + depth);
            this.state = state;

//            for(int i = 0; i < 8; i++)
//            {
//                for(int j = 0; j < 8; j++)
//                {
//                    System.out.print(state[i][j] + "   ");
//                }
//                System.out.println();
//            }
//            System.out.println();
            this.maximizer = myTurn;
            this.myColor = myColor;
            this.depth = depth;
            children = new ArrayList<>();
            moveToChild = new ArrayList<>();
        }

        private double[] minimax(double alpha, double beta)
        {
//            System.out.println("round + depth = " + round + depth);
            getValidMoves(round + depth, state, myColor);
//            System.out.print("Valid moves: ");
//            for(int i = 0; i < numValidMoves; i++)
//            {
//                System.out.print(validMoves[i] + ", ");
//            }
//            System.out.println(numValidMoves + "moves");
//            Scanner keys = new Scanner(System.in);
//            keys.nextLine();
            if(numValidMoves == 0)
            {
                double[] valMove = new double[2];
                double scoreDifference = calculateScoreDifference();
                if(scoreDifference < 0)
                {
                    valMove[0] = -2000;
                } else if (scoreDifference > 0)
                {
                    valMove[0] = 2000;
                } else
                {
                    valMove[0] = 0;
                }
                valMove[1] = -1;
//                System.out.println("No valid moves. Return: " + valMove[0] + ", " + valMove[1]);
                return valMove;
            } else if(depth == maxDepth)
            {
                double[] valMove = new double[2];
                valMove[0] = heuristicVal();
                valMove[1] = -1;
//                System.out.println("Deepest point found. Return: " + valMove[0] + ", " + valMove[1]);
                return valMove;
            } else if(depth == 0 && numValidMoves == 1)
            {
//                System.out.println("Only one valid move: " + validMoves[0]);
                double[] valMove = new double[2];
                valMove[0] = -1;
                valMove[1] = validMoves[0];
                return valMove;
            }
//            System.out.println("Going deeper..." + depth);
            int nextColor = 0;
            if(myColor == 1)
            {
                nextColor = 2;
            } else
            {
                nextColor = 1;
            }
            int[] myValidMoves = new int[64];
            System.arraycopy(validMoves, 0, myValidMoves, 0, 64);
            int myNumValidMoves = numValidMoves;
            for(int i = 0; i < myNumValidMoves; i++)
            {
                int[][] nextState = calculateNextState(myValidMoves[i]);
                StateNode child = new StateNode(nextState, !maximizer, nextColor, depth + 1);
                children.add(child);
                moveToChild.add(myValidMoves[i]);
            }
            if(maximizer)
            {
                double[] bestValMove = new double[2];
                bestValMove[0] = -1 * Double.MAX_VALUE;
                bestValMove[1] = -2;
                if(children.size() == 0)
                {
//                    System.out.println("No children!");
                    Scanner keys = new Scanner(System.in);
                    keys.next();
                }
                for(int i = 0; i < children.size(); i++)
                {
//                    System.out.println("Max looping: " + i);
                    double[] valueMove = children.get(i).minimax(alpha, beta);
                    if(valueMove[0] > bestValMove[0])
                    {
                        bestValMove[0] = valueMove[0];
                        bestValMove[1] = moveToChild.get(i);
//                        System.out.println("Found larger value: " + bestValMove[0] + ", " + bestValMove[1]);
                    }
                    alpha = Math.max(alpha, bestValMove[0]);
                    if(beta <= alpha)
                    {
//                        System.out.println("MURDER!!!!!");
                        break;
                    }
                }
//                System.out.println("Depth " + depth + ". Return: " + bestValMove[0] + ", " + bestValMove[1]);
//                Scanner keys = new Scanner(System.in);
//                keys.next();
                return bestValMove;
            } else
            {
                double[] bestValMove = new double[2];
                bestValMove[0] = Double.MAX_VALUE;
                bestValMove[1] = -3;
                for(int i = 0; i < children.size(); i++)
                {
//                    System.out.println("Min looping: " + i);
                    double[] valueMove = children.get(i).minimax(alpha, beta);
                    if(valueMove[0] < bestValMove[0])
                    {
                        bestValMove[0] = valueMove[0];
                        bestValMove[1] = moveToChild.get(i);
//                        System.out.println("Found smaller value: " + bestValMove[0] + ", " + bestValMove[1]);
                    }
                    beta = Math.min(beta, bestValMove[0]);
                    if(beta <= alpha)
                    {
//                        System.out.println("MURDER!!!!!");
                        break;
                    }
                }
//                System.out.println("Depth " + depth + ". Return: " + bestValMove[0] + ", " + bestValMove[1]);
//                Scanner keys = new Scanner(System.in);
//                keys.next();
                return bestValMove;
            }
        }

        /**
         * Calculates the next state given the move taken
         * @param move The move taken
         * @return The next state
         */
        private int[][] calculateNextState(int move)
        {
            int[][] nextState = new int[state.length][state[0].length];
            for(int i = 0; i < state.length; i++)
            {
                for(int j = 0; j < state[0].length; j++)
                {
                    nextState[i][j] = state[i][j];
                }
            }
            int[] parsedMove = {move / 8, move % 8};
            int[] m = new int[2];
            m[0] = 1;
            m[1] = 0;
            checkDirection(nextState, parsedMove, m);
            m[1] = 1;
            checkDirection(nextState, parsedMove, m);
            m[1] = -1;
            checkDirection(nextState, parsedMove, m);
            m[0] = 0;
            m[1] = 1;
            checkDirection(nextState, parsedMove, m);
            m[1] = -1;
            checkDirection(nextState, parsedMove, m);
            m[0] = -1;
            m[1] = 1;
            checkDirection(nextState, parsedMove, m);
            m[1] = 0;
            checkDirection(nextState, parsedMove, m);
            m[1] = -1;
            checkDirection(nextState, parsedMove, m);
            nextState[parsedMove[0]][parsedMove[1]] = myColor;
            return nextState;
        }

        /**
         * Checks the given direction. If the tiles should be flipped, flip them
         * @param board The board in its current state
         * @param start The starting location
         * @param direction The direction going (two values: -1,0,1)
         */
        private void checkDirection(int[][] board, int[] start, int[] direction)
        {
            int[] iteratorPosition = new int[2];
            iteratorPosition[0] = start[0] + direction[0];
            iteratorPosition[1] = start[1] + direction[1];
            List<int[]> flippablePoints = new ArrayList<>(6);
            while (iteratorPosition[0] >= 0 && iteratorPosition[0] < 8 && iteratorPosition[1] >= 0 && iteratorPosition[1] < 8)
            {
                if(board[iteratorPosition[0]][iteratorPosition[1]] == 0)
                {
                    return;
                }
                if(board[iteratorPosition[0]][iteratorPosition[1]] == myColor)
                {
                    for(int[] point : flippablePoints)
                    {
                        board[point[0]][point[1]] = myColor;
                    }
                    return;
                } else
                {
                    int[] point = new int[2];
                    point[0] = iteratorPosition[0];
                    point[1] = iteratorPosition[1];
                    flippablePoints.add(point);
                }
                iteratorPosition[0] += direction[0];
                iteratorPosition[1] += direction[1];
            }
        }

        private double heuristicVal()
        {
            double[] hWeights = new double[9];
            hWeights[0] = 0.1; // Score Difference
            hWeights[1] = 300; // Corners
            hWeights[2] = 2; // Mobility
            hWeights[3] = 10; // My stabilitty
            hWeights[4] = 8; // Opponent's stability
            hWeights[5] = 250; // C Points (me)
            hWeights[6] = 250; // C Points (them)
            hWeights[7] = 15; // X Points (me)
            hWeights[8] = 15; // X Points (them)
            double h = hWeights[0] * calculateScoreDifference();
//            System.out.println("h1: " + h);
            double h2 = hWeights[1] * calculateCorners();
//            System.out.println("h2: " + h2);
            h += h2;
            double h3 = hWeights[2] * calculateMobility();
//            System.out.println("h3: " + h3);
            h += h3;
            double[] stabilityScores = checkStablePieces();
//            System.out.println("h4: " + stabilityScores[0]);
            h += hWeights[3] * stabilityScores[me];
//            System.out.println("h5: " + stabilityScores[1]);
            h -= hWeights[4] * stabilityScores[them];
            double[] cScores = calculateCSpaces();
            h += hWeights[5] * cScores[me];
            h += hWeights[6] * cScores[them];
            double[] xScores = calculateXSpaces();
            h += hWeights[7] * xScores[me];
            h += hWeights[8] * xScores[me];
            return h;
        }

        /**
         * Calculates the difference in scores. Positive = winning
         * @return The difference in scores, duh.
         */
        private double calculateScoreDifference()
        {
            double myScore = 0;
            double theirScore = 0;
            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    if(state[i][j] == me)
                    {
                        myScore++;
                    } else if(state[i][j] == them)
                    {
                        theirScore++;
                    }
                }
            }
            return myScore - theirScore;
        }

        /**
         * Calculates the number of corners captured, minus the number of corners the opponent has captured
         * @return The value just described, ya fool
         */
        private double calculateCorners()
        {
            double cornerScore = 0;
            if(state[0][0] == me)
            {
                cornerScore++;
            } else if(state[0][0] != 0)
            {
                cornerScore--;
            }
            if(state[0][7] == me)
            {
                cornerScore++;
            } else if(state[0][7] != 0)
            {
                cornerScore--;
            }
            if(state[7][0] == me)
            {
                cornerScore++;
            } else if(state[7][0] != 0)
            {
                cornerScore--;
            }
            if(state[7][7] == me)
            {
                cornerScore++;
            } else if(state[7][7] != 0)
            {
                cornerScore--;
            }
            return cornerScore;
        }

        private double calculateMobility()
        {
            getValidMoves(4, state, myColor);
            double nvm = numValidMoves;
            if(myColor == me)
            {
                return nvm;
            } else
            {
                nvm *= -1;
                return nvm;
            }
        }

        private double calculateStability()
        {
            double myVal = 0;
            boolean check07 = true;
            boolean check70 = true;
            boolean check77 = true;
            if(state[0][0] == me)
            {
                int maxDist = 8;
                for(int i = 0; i < 8; i++)
                {
                    if(state[i][0] != me)
                    {
                        break;
                    }
                    for(int j = 0; j < maxDist; j++)
                    {
                        if(state[i][j] == me)
                        {
                            myVal++;
                            if(i == 7 && j == 0)
                            {
                                check70 = false;
                            } else if(i == 7 && j == 7)
                            {
                                check77 = false;
                            }
                        } else
                        {
                            maxDist = j - 1;
                            break;
                        }
                    }
                    if(i == 0 && maxDist == 8)
                    {
                        check07 = false;
                    }
                }
            }
            if(state[0][7] == me && check07)
            {
                int maxDist = -1;
                for(int i = 0; i < 8; i++)
                {
                    if(state[i][7] != me)
                    {
                        break;
                    }
                    for(int j = 7; j > maxDist; j--)
                    {
                        if(state[i][j] == me)
                        {
                            myVal++;
                            if(i == 7 && j == 0)
                            {
                                check70 = false;
                            } else if(i == 7 && j == 7)
                            {
                                check77 = false;
                            }
                        } else
                        {
                            maxDist = j - 1;
                            break;
                        }
                    }
                }
            }
            if(state[7][0] == me && check70)
            {
                int maxDist = 8;
                for(int i = 7; i > -1; i--)
                {
                    if(state[i][0] != me)
                    {
                        break;
                    }
                    for(int j = 0; j < maxDist; j++)
                    {
                        if(state[i][j] == me)
                        {
                            myVal++;
                            if(i == 7 && j == 7)
                            {
                                check77 = false;
                            }
                        } else
                        {
                            maxDist = j - 1;
                            break;
                        }
                    }
                }
            }
            if(state[7][7] == me && check77)
            {
                int maxDist = -1;
                for(int i = 7; i > -1; i--)
                {
                    if(state[i][7] != me)
                    {
                        break;
                    }
                    for(int j = 7; j > maxDist; j--)
                    {
                        if(state[i][j] == me)
                        {
                            myVal++;
                        } else
                        {
                            maxDist = j - 1;
                            break;
                        }
                    }
                }
            }
            return myVal;
        }

        /**
         * Creates a pair of scores for stability, with player number used to index (0th index is meaningless)
         * @return An array, length 3, of doubles, where the 0th element is dumb and worthless. It probably went to community college
         */
        private double[] checkStablePieces()
        {
            double[] stabilityScores = new double[3];
            for(int i = 0; i < 3; i++)
            {
                stabilityScores[i] = 0;
            }
            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    int[] location = new int[2];
                    location[0] = i;
                    location[1] = j;
                    if(checkPieceStability(location))
                    {
                        stabilityScores[state[i][j]]++;
                    }
                }
            }
            return stabilityScores;
        }

        /**
         * Determines if a given location is stable
         * @param location The given location
         * @return True if the location is stable, otherwise false. Also returns false if the location is blank
         */
        private boolean checkPieceStability(int[] location)
        {
            if(state[location[0]][location[1]] == 0)
            {
                return false;
            } else if((location[0] == 0 && location[1] == 0) || (location[0] == 0 && location[1] == 7) || (location[0] == 7 && location[1] == 0) ||(location[0] == 7 && location[1] == 7))
            {
                return true;
            } else
            {
                int[] direction1 = new int[2];
                int[] direction2 = new int[2];
                int[] direction3 = new int[2];
                int[] direction4 = new int[2];
                direction1[0] = 1;
                direction1[1] = 0;
                direction2[0] = 1;
                direction2[1] = 1;
                direction3[0] = 0;
                direction3[0] = 1;
                direction4[0] = -1;
                direction4[1] = 1;
                if(!checkPieceStability(location, direction1) || !checkPieceStability(location, direction2) || !checkPieceStability(location, direction3) || !checkPieceStability(location, direction4))
                {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks if the location is stable in the given direction (forward and backward)
         * @param location
         * @param direction
         * @return
         */
        private boolean checkPieceStability(int[] location, int[] direction)
        {
            int[] cur_location = new int[2];
            cur_location[0] = location[0];
            cur_location[1] = location[1];
            int otherColor;

            if (state[location[0]][location[1]] == 1)
                {
                    otherColor = 2;
                } else {
                    otherColor = 1;
                }

            boolean blankFound = false;
            boolean otherColorFound = false;
            while (!blankFound && !otherColorFound){
                cur_location[0] += direction[0];
                cur_location[1] += direction[1];
                if (cur_location[0] < 0 || cur_location[0] > 7 || cur_location[1] < 0 || cur_location[1] > 7) break;
                if (state[cur_location[0]][cur_location[1]] == 0) blankFound = true;
                else if (state[cur_location[0]][cur_location[1]] == otherColor) otherColorFound = true;
            }
            if (!(blankFound || otherColorFound)) return true;
            cur_location[0] = location[0];
            cur_location[1] = location[1];
            while(true)
            {
                cur_location[0] -= direction[0];
                cur_location[1] -= direction[1];
                if (cur_location[0] < 0 || cur_location[0] > 7 || cur_location[1] < 0 || cur_location[1] > 7)
                {
                    return true;
                }
                if(blankFound && (state[cur_location[0]][cur_location[1]] == otherColor))
                {
                    return false;
                }
                if(otherColorFound && (state[cur_location[0]][cur_location[1]] == 0))
                {
                    return false;
                }
            }
        }

        private double[] calculateCSpaces()
        {
            double[] weights = new double[3];
//            for(int i = 0; i < 3; i++)
//            {
//                weights[i] = 0;
//            }
            if(state[0][0] == 0)
            {
                if(state[0][1] == me)
                {
                    weights[me]--;
                } else if(state[0][1] == them)
                {
                    weights[them]++;
                }
                if(state[1][0] == me)
                {
                    weights[me]--;
                } else if(state[1][0] == them)
                {
                    weights[them]++;
                }
            }
            if(state[0][7] == 0)
            {
                if(state[0][6] == me)
                {
                    weights[me]--;
                } else if(state[0][6] == them)
                {
                    weights[them]++;
                }
                if(state[1][7] == me)
                {
                    weights[me]--;
                } else if(state[1][7] == them)
                {
                    weights[them]++;
                }
            }
            if(state[7][0] == 0)
            {
                if(state[6][0] == me)
                {
                    weights[me]--;
                } else if(state[6][0] == them)
                {
                    weights[them]++;
                }
                if(state[7][1] == me)
                {
                    weights[me]--;
                } else if(state[7][1] == them)
                {
                    weights[them]++;
                }
            }
            if(state[7][7] == 0)
            {
                if(state[6][7] == me)
                {
                    weights[me]--;
                } else if(state[6][7] == them)
                {
                    weights[them]++;
                }
                if(state[7][6] == me)
                {
                    weights[me]--;
                } else if(state[7][6] == them)
                {
                    weights[them]++;
                }
            }
            return weights;
        }

        private double[] calculateXSpaces()
        {
            double[] weights = new double[3];
            if(state[0][0] == 0)
            {
                if(state[1][1] == me)
                {
                    weights[me]--;
                } else if(state[1][1] == them)
                {
                    weights[them]++;
                }
            }
            if(state[0][7] == 0)
            {
                if(state[1][6] == me)
                {
                    weights[me]--;
                } else if(state[1][6] == them)
                {
                    weights[them]++;
                }
            }
            if(state[7][0] == 0)
            {
                if(state[6][1] == me)
                {
                    weights[me]--;
                } else if(state[6][1] == them)
                {
                    weights[them]++;
                }
            }
            if(state[7][7] == 0)
            {
                if(state[6][6] == me)
                {
                    weights[me]--;
                } else if(state[6][6] == them)
                {
                    weights[them]++;
                }
            }
            return weights;
        }

    }

}