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

    private final int MAX_DEPTH = 5;

    public Socket s;
    public BufferedReader sin;
    public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;

    int validMoves[] = new int[64];
    int numValidMoves;


    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public SmartGuy(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;

        while (true) {
            System.out.println("Read");
            readMessage();

            if (turn == me) {
                System.out.println("Move");

//                myMove = new StateNode(state, true).pickMove(0, Integer.MIN_VALUE, Integer.MAX_VALUE);

                myMove = move();
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly

                String sel = myMove / 8 + "\n" + myMove % 8;

                System.out.println("Selection: " + validMoves[myMove] / 8 + ", " + validMoves[myMove] % 8);

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
            double[] scoreMovePair = rutNode.minimax(Double.MIN_VALUE, Double.MAX_VALUE);
            System.out.println("Final score-move: " + scoreMovePair[0] + ", " + scoreMovePair[1]);
            return ((int) scoreMovePair[1]);
        }
    }

    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    private void getValidMoves(int round, int state[][], int me) {
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
                        if (couldBe(state, i, j, me)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
//                            System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }


        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }

    private boolean checkDirection(int state[][], int row, int col, int incx, int incy, int me) {
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
            if (me == 1) {
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

    private boolean couldBe(int state[][], int row, int col, int me) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                if (checkDirection(state, row, col, incx, incy, me))
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
            System.out.println("State Node created at depth " + depth);
            this.state = state;
            this.maximizer = myTurn;
            this.myColor = myColor;
            this.depth = depth;
            children = new ArrayList<>();
            moveToChild = new ArrayList<>();
        }

        //        /**
//         * Picks the next move to make from the given state
//         * @return A 1D array whose first value is the expected score and whose second value is the move to get there
//         */
//        public int[] pickMove(int alpha, int beta)
//        {
//            getValidMoves(round + depth, state, myColor);
//            if(round < 4)
//            {
//                return generator.nextInt(numValidMoves);
//            } else
//            {
//        List<StateNode> children = new ArrayList<>();
//        List<Integer> moveToChild = new ArrayList<>();
//        int nextColor = 0;
//                if(myColor == 1)
//        {
//            nextColor = 2;
//        } else
//        {
//            nextColor = 1;
//        }
//                for(int i = 0; i < numValidMoves; i++)
//        {
//            int[][] nextState = calculateNextState(validMoves[i]);
//            StateNode child = new StateNode(nextState, !maximizer, nextColor, depth + 1);
//            children.add(child);
//            moveToChild.add(validMoves[i]);
//        }
//                int bestScore = Integer.MIN_VALUE;
//                int bestChildIndex = -1;
//                for(int i = 0; i < children.size(); i++)
//                {
//                    int score = children.get(i).minimax(Integer.MIN_VALUE, Integer.MAX_VALUE);
//                    if(score > bestScore)
//                    {
//                        bestChildIndex = i;
//                        bestScore = score;
//                    }
//                }
//                return moveToChild.get(bestChildIndex);
//            }
//            return -1;
//        }

        private double[] minimax(double alpha, double beta)
        {
            getValidMoves(round + depth, state, myColor);
            if(numValidMoves == 0)
            {
                double[] valMove = new double[2];
                double scoreRatio = calculateScoreRatio();
                if(scoreRatio < 1)
                {
                    valMove[0] = 0;
                } else
                {
                    valMove[0] = Double.MAX_VALUE;
                }
//                valMove[0] = calculateScore();
                valMove[1] = 0;
                System.out.println("No valid moves. Return: " + valMove[0] + ", " + valMove[1]);
                return valMove;
            } else if(depth == MAX_DEPTH)
            {
                double[] valMove = new double[2];
                valMove[0] = heuristicVal();
                valMove[1] = 0;
                System.out.println("Deepest point found. Return: " + valMove[0] + ", " + valMove[1]);
                return valMove;
            }
            int nextColor = 0;
            if(myColor == 1)
            {
                nextColor = 2;
            } else
            {
                nextColor = 1;
            }
            for(int i = 0; i < numValidMoves; i++)
            {
                int[][] nextState = calculateNextState(validMoves[i]);
                StateNode child = new StateNode(nextState, !maximizer, nextColor, depth + 1);
                children.add(child);
                moveToChild.add(validMoves[i]);
            }
            if(maximizer)
            {
                double[] bestValMove = {Double.MIN_VALUE, 0};
                for(int i = 0; i < children.size(); i++)
                {
                    double[] valueMove = children.get(i).minimax(alpha, beta);
                    if(valueMove[0] > bestValMove[0])
                    {
                        bestValMove[0] = valueMove[0];
                        bestValMove[1] = moveToChild.get(i);
                    }
                    alpha = Math.max(alpha, bestValMove[0]);
                    if(beta <= alpha)
                    {
                        break;
                    }
                }
                System.out.println("Depth " + depth + ". Return: " + bestValMove[0] + ", " + bestValMove[1]);
                return bestValMove;
            } else
            {
                double[] bestValMove = {Double.MAX_VALUE,0};
                for(int i = 0; i < children.size(); i++)
                {
                    double[] valueMove = children.get(i).minimax(alpha, beta);
                    if(valueMove[0] < bestValMove[0])
                    {
                        bestValMove[0] = valueMove[0];
                        bestValMove[1] = moveToChild.get(i);
                    }
                    alpha = Math.max(alpha, bestValMove[0]);
                    if(beta <= alpha)
                    {
                        break;
                    }
                }
                System.out.println("Depth " + depth + ". Return: " + bestValMove[0] + ", " + bestValMove[1]);
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

//        private int calculateScore()
//        {
//            int myVal = 0;
//            for(int i = 0; i < 8; i++)
//                for(int j = 0; j < 8; j++)
//                    if (state[i][j] == myColor)
//                    {
//                        myVal++;
//                    }
//            return myVal;
//        }

        private double heuristicVal()
        {
            double myVal = calculateScoreRatio();
//            boolean check07 = true;
//            boolean check70 = true;
//            boolean check77 = true;
//            if(state[0][0] == me)
//            {
//                int[] stateIter = new int[2];
//                stateIter[0] = 0;
//                stateIter[1] = 0;
//                int maxDist = 7;
//                for(int i = 0; i < 8; i++)
//                {
//                    for(int j = 0; j < maxDist; j++)
//                    {
//
//                    }
//                }
//            }
//            if(state[0][7] == me && check07)
//            {
//                myVal++;
//            }
//            if(state[7][0] == me && check70)
//            {
//                myVal++;
//            }
//            if(state[7][7] == me && check77)
//            {
//                myVal++;
//            }
            return myVal;
        }

        private double calculateScoreRatio()
        {
            int notMe = 0;
            if(me == 1)
            {
                notMe = 2;
            } else
            {
                notMe = 1;
            }
            double myScore = 0;
            double theirScore = 0;
            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    if(state[i][j] == me)
                    {
                        myScore++;
                    } else if(state[i][j] == notMe)
                    {
                        theirScore++;
                    }
                }
            }
            if(theirScore == 0)
            {
                return Double.MAX_VALUE;
            } else
            {
                return myScore / theirScore;
            }
        }

    }

}