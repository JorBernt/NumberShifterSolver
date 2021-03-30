package codingame;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Command {
    int val, x, y;
    boolean add;
    char dir;
    String output;
    public Command(int val, int x, int y, boolean add, char dir, String output) {
        this.val = val;
        this.x = x;
        this.y = y;
        this.add = add;
        this.dir = dir;
        this.output = output;
    }
}


class Main {
    static List<String> commands = new ArrayList<>();
    static Set<Integer> states = new TreeSet<>();
    static Set<Integer> tempStates = new TreeSet<>();
    static boolean found = false;
    static int winState;
    static int gameBoardHash;
    static boolean justLoaded = false;
    static int fileNumber;
    static long startTime;
    static boolean write;
    static int nodeMin = Integer.MAX_VALUE;
    static int scoreMin = Integer.MAX_VALUE;
    static int skipped = 0;
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        boolean load = false;
        System.out.println("Do you want to load previous board states? y/n");
        load = in.nextLine().equals("y");
        System.out.println("Do you want to write previous board states? y/n");
        write = in.nextLine().equals("y");

        fileNumber = 0;
        System.out.println("Input height, width and board:");
        int width = in.nextInt();
        int height = in.nextInt();
        int[][] map = new int[height][width];
        winState = Arrays.deepHashCode(map);
        long start = System.nanoTime();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int cell = in.nextInt();
                map[i][j] = cell;
            }
        }
        gameBoardHash = Arrays.deepHashCode(map);

        try {
            System.out.println("Writing level to disk..");
            File file = new File("/tmp/"+gameBoardHash+"/");
            file.mkdir();
            FileWriter writer = new FileWriter("/tmp/"+gameBoardHash+"/level.txt");
            writer.write(width+" "+height+"\n");
            for(int[] a : map) {
                writer.write(Arrays.stream(a).mapToObj(Integer::toString).collect(Collectors.joining(" "))+"\n");

            }
            writer.close();
            System.out.println("Done.");

        } catch(IOException e) {
            e.printStackTrace();

        }

        if(load) {
            long startRead = System.nanoTime();
            while(true) {
                try {
                    System.out.println("Reading board state file " + fileNumber+".data");
                    FileReader fileReader = new FileReader("/tmp/"+gameBoardHash+"/boardStates/boardStates"+fileNumber+".data");

                    int i = 0;
                    StringBuilder sb = new StringBuilder();
                    while((i=fileReader.read())!=-1) {
                        sb.append((char)i);
                    }
                    int[] arr = Arrays.stream(sb.toString().split(" ")).mapToInt(Integer::parseInt).toArray();
                    for(int n : arr) states.add(n);
                    System.out.println("Loaded board states: "  + states.size());
                    justLoaded = true;





                } catch(Exception e) {
                    System.out.println("All states loaded.");
                    //e.printStackTrace();
                    break;
                }
                fileNumber++;
            }
            System.out.println("Time used reading files: " + ((System.nanoTime()-startRead)/1000000));
        }
        startTime = System.nanoTime();
        calc(map, new ArrayList<>());
        for(String s : commands) {
            System.out.println("System.out.println(\""+s+"\");");
        }
        long end = System.nanoTime();

        try {
            System.out.println("Writing solution to disk..");
            File file = new File("/tmp/"+gameBoardHash+"/");
            file.mkdir();
            FileWriter writer = new FileWriter("/tmp/"+gameBoardHash+"/solution.txt");
            Iterator it = commands.iterator();
            while(it.hasNext()) {
                writer.write("System.out.println(\""+it.next()+"\");"+"\n");
            }

            writer.close();
            tempStates.clear();
            System.out.println("Done.");

        } catch(IOException e) {
            e.printStackTrace();

        }

        System.out.println("Total time: " + (end-start)/1000000+"ms");
        System.out.println("Number of calculated board states: " + states.size());
    }

    static void calc(int[][] map, List<String> com) {
        int hash = Arrays.deepHashCode(map);
        if(hash == winState) {
            commands.addAll(com);
            found = true;
            System.out.println("Solution found!");
            return;
        }
        if(found) {
            // skipped++;
            return;
        }
        if(!getScore(map)) return;
        if(states.contains(hash)) {
            skipped++;
            return;
        }
        justLoaded = false;
        int h = map.length;
        int w = map[0].length;
        int[] columns = getRandom(h);
        int[] rows = getRandom(w);
        boolean allCalc = true;
        for(int i = 0; i < h; i++) {
            for(int j = 0; j < w; j++) {
                if(found) return;
                if(map[columns[i]][rows[j]] > 0) {
                    List<Command> pos = getMoves(map, columns[i], rows[j], map[columns[i]][rows[j]] );
                    for(Command c : pos) {
                        int[][] newMap = copyMap(map);
                        newMap[c.y][c.x] = 0;
                        switch(c.dir) {
                            case 'R': newMap[c.y][c.x+c.val] = c.add?newMap[c.y][c.x+c.val]+c.val:Math.abs(newMap[c.y][c.x+c.val]-c.val);break;
                            case 'L': newMap[c.y][c.x-c.val] = c.add?newMap[c.y][c.x-c.val]+c.val:Math.abs(newMap[c.y][c.x-c.val]-c.val);break;
                            case 'U': newMap[c.y-c.val][c.x] = c.add?newMap[c.y-c.val][c.x]+c.val:Math.abs(newMap[c.y-c.val][c.x]-c.val);break;
                            case 'D': newMap[c.y+c.val][c.x] = c.add?newMap[c.y+c.val][c.x]+c.val:Math.abs(newMap[c.y+c.val][c.x]-c.val);break;
                        }
                        scoreMin = Integer.MAX_VALUE;
                        nodeMin = Integer.MAX_VALUE;
                        com.add(c.output);
                        calc(newMap, com);
                        com.remove(c.output);
                    }
                }
            }
        }

        states.add(hash);
        if(write)
            tempStates.add(hash);

        if(write && states.size()%1000000==0 && !justLoaded && states.size() > 0) {
            try {
                System.out.println("Writing cache to disk..");
                File file = new File("/tmp/"+gameBoardHash+"/boardStates");
                file.mkdir();
                FileWriter writer = new FileWriter("/tmp/"+gameBoardHash+"/boardStates/boardStates"+fileNumber+".data");
                Iterator it = tempStates.iterator();
                while(it.hasNext()) {
                    writer.write(""+it.next()+" ");
                }
                writer.close();
                tempStates.clear();
                System.out.println("Done.");

            } catch(IOException e) {
                e.printStackTrace();

            }
            fileNumber++;
        }

        if(states.size()%100000==0) {
            System.out.println("Board states checked: " + states.size() + "  Time used: " + ((System.nanoTime()-startTime)/1000000)+"ms  Skipped boards: " + skipped );
            startTime = System.nanoTime();
            skipped = 0;
        }


    }

    static int[] getRandom(int n) {
        int[] arr = new int[n];
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for(int i = 0; i < n; i++) arr[i] = list.get(i);
        return arr;
    }

    static boolean getScore(int[][] map) {
        int sum = 0;
        int cmax = 0;
        int score = 0;
        int nodes = 0;
        for(int i = 0; i < map.length; i++) {
            for(int j = 0; j < map[0].length; j++) {
                if(map[i][j] > 0) {
                    sum+=map[i][j];
                    nodes++;
                    cmax = Math.max(map[i][j], cmax);
                }

            }
        }
        if(cmax > sum/2) {
            return false;
        }
        /*if(sum < scoreMin && nodes < nodeMin) {
            scoreMin = sum;
            nodeMin = nodes;

            return true;
        }*/
        return true;

    }

    static int[][] copyMap(int[][] map) {
        int h = map.length;
        int w = map[0].length;
        int[][] newMap = new int[h][w];
        for(int i = 0; i < h; i++) {
            for(int j = 0; j < w; j++) {
                newMap[i][j] = map[i][j];
            }
        }
        return newMap;
    }

    static List<Command> getMoves(int[][] map, int y, int x, int val) {
        int h = map.length;
        int w = map[0].length;
        List<Command> moves = new ArrayList<>();
        if(x-val >= 0 && map[y][x-val] > 0) {
            moves.add(new Command(val, x, y, false, 'L', x +" "+y+" L -"));
            moves.add(new Command(val, x, y, true, 'L', x +" "+y+" L +"));
        }
        if(x+val < w && map[y][x+val] > 0) {
            moves.add(new Command(val, x, y, false, 'R', x +" "+y+" R -"));
            moves.add(new Command(val, x, y, true, 'R', x +" "+y+" R +"));
        }
        if(y-val >= 0 && map[y-val][x] > 0) {
            moves.add(new Command(val, x, y, false, 'U', x +" "+y+" U -"));
            moves.add(new Command(val, x, y, true, 'U', x +" "+y+" U +"));
        }
        if(y+val < h && map[y+val][x] > 0) {
            moves.add(new Command(val, x, y, false, 'D', x +" "+y+" D -"));
            moves.add(new Command(val, x, y, true, 'D', x +" "+y+" D +"));
        }
        return moves;
    }
}