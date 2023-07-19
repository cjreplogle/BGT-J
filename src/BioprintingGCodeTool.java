import java.io.*;
import java.util.Scanner;
import java.math.*;

public class BioprintingGCodeTool {

    public static void main(String[] args) {
        float lh = 0.6f;
        float zm = 6;
        float cl = 10000;
        float sp = 60;
        int lr = 200;
        int state = 0;
        int sel = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            File configFile = new File("config.bpc");
            if (configFile.exists()) {
                FileReader fr = new FileReader(configFile);
                BufferedReader configReader = new BufferedReader(fr);
                String configLine = configReader.readLine();
                if (configLine != null) {
                    String[] configValues = configLine.split(" ");
                    lh = Float.parseFloat(configValues[0]);
                    zm = Float.parseFloat(configValues[1]);
                    cl = Float.parseFloat(configValues[2]);
                    sp = Float.parseFloat(configValues[3]);
                    lr = Integer.parseInt(configValues[4]);
                }
                configReader.close();
                System.out.println("config.bpc successfully opened.");
            } else {
                System.out.println("Could not read config.bpc, creating file...");
                BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile));
                configWriter.write("0.6 6 10000 60 200");
                configWriter.close();
            }

            while (state == 0) {
                clearConsole();
                System.out.println("/-------------------------------------------------------\\");
                System.out.println("|---------------- Bioprinting GCode Tool ---------------|");
                System.out.println("|---------- v0.6, made by CJ Replogle c. 2023 ----------|");
                System.out.println("|-------------------------------------------------------|");
                System.out.println("|                                                       |");
                System.out.println("| 1: Generate New Scaffold                              |");
                System.out.println("| 2: Generate From Preset Infill Pattern                |");
                System.out.println("| 3: Generate Infill                                    |");
                System.out.println("| 4: Settings                                           |");
                System.out.println("| 5: About this Program / Help                          |");
                System.out.println("| 6: Exit                                               |");
                System.out.println("|                                                       |");
                System.out.println("\\------------------------------------------------------/\n");
                System.out.print("-> ");
                state = Integer.parseInt(br.readLine());

                switch (state) {
                    case 1:
                        clearConsole();
                        infill(lr, lh);
                        tesselator(lr, lh, zm, cl, sp);
                        System.out.print("Input any value to return to the menu -> ");
                        br.readLine();
                        state = 0;
                        break;
                    case 2:
                        clearConsole();
                        tesselator(lr, lh, zm, cl, sp);
                        System.out.print("Input any value to return to the menu -> ");
                        br.readLine();
                        state = 0;
                        break;
                    case 3:
                        clearConsole();
                        infill(lr, lh);
                        System.out.print("Input any value to return to the menu -> ");
                        br.readLine();
                        state = 0;
                        break;
                    case 4:
                        sel = 0;
                        while (sel != 6) {
                            clearConsole();
                            System.out.println("\n1: Layer Height - " + lh + " mm");
                            System.out.println("2: Scaffold Height - " + zm + " mm");
                            System.out.println("3: Layer Cooldown - " + cl + " ms");
                            System.out.println("4: Printhead Speed - " + sp + " mm/min");
                            System.out.println("5: Layer Resolution - " + lr + " points/layer");
                            System.out.println("\n6: Back\n");
                            System.out.print("-> ");
                            sel = Integer.parseInt(br.readLine());
                            switch (sel) {
                                case 1:
                                    System.out.print("\n New Layer Height (mm) -> ");
                                    lh = Float.parseFloat(br.readLine());
                                    break;
                                case 2:
                                    System.out.print("\n New Scaffold Height (mm) -> ");
                                    zm = Float.parseFloat(br.readLine());
                                    break;
                                case 3:
                                    System.out.print("\n Layer Cooldown (ms) -> ");
                                    cl = Float.parseFloat(br.readLine());
                                    break;
                                case 4:
                                    System.out.print("\n Printhead Speed (mm/min) -> ");
                                    sp = Float.parseFloat(br.readLine());
                                    break;
                                case 5:
                                    System.out.print("\n Layer Resolution (points/layer) -> ");
                                    lr = Integer.parseInt(br.readLine());
                                    break;
                            }
                            clearConsole();
                        }
                        BufferedWriter fset = new BufferedWriter(new FileWriter("config.bpc"));
                        fset.write(lh + " " + zm + " " + cl + " " + sp + " " + lr);
                        fset.close();
                        state = 0;
                        break;
                    case 5:
                        clearConsole();
                        System.out.println("/-------------------------------------------------------\\");
                        System.out.println("|                                                       |");
                        System.out.println("| For detailed description of different variables, see: |");
                        System.out.println("| https://github.com/cjreplogle/BGT/blob/main/help.md   |");
                        System.out.println("|                                                       |");
                        System.out.println("\\-------------------------------------------------------/\n");
                        System.out.print(" Input any value to return to the menu -> ");
                        br.readLine();
                        state = 0;
                        break;
                    case 6:
                        state = 1;
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void infill(int RES, float lh) {

        final float PI = 3.14159f;
        // Open output file
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("layer.bpl"));
        } catch (IOException e) {
            System.err.println("Could not open layer.bpl.");
            System.exit(1);
        }
        System.out.println("\nINFILL PATTERNING\n-----------------");
        // Prompt user for different parameter
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nINFILL TYPE (0=CYLINDER, 1=SQUARE, 2=CONCENTRIC): ");
        int type = scanner.nextInt();

        float ps, pd, pl, fill, radius;
        switch (type) {
            case 0:
                System.out.print("\nRADIUS (MM): ");
                radius = scanner.nextFloat();
                System.out.print("\nPORE SIZE (MM): ");
                ps = scanner.nextFloat();
                System.out.print("\nPATH LINKAGE (1=YES, 0=NO): ");
                pl = scanner.nextFloat();
                pd = 0;
                fill = 0;
                break;
            case 1:
                System.out.print("\nSIDE LENGTH (MM): ");
                radius = scanner.nextFloat();
                radius=radius/2;
                System.out.print("\nPORE SIZE (MM): ");
                ps = scanner.nextFloat();
                pl = 0;
                pd = 0;
                fill = 0;
                break;
            case 2:
                System.out.print("\nRADIUS (MM): ");
                radius = scanner.nextFloat();
                System.out.print("\nPORE DEPTH (MM): ");
                pd = scanner.nextFloat();
                System.out.print("\nPORES PER LAYER(MM): ");
                ps = scanner.nextFloat();
                System.out.print("\nFILL?:\n");
                fill = scanner.nextFloat();
                pl = 0;
                break;
            default:
                System.err.println("Invalid infill type.");
                return;
        }

        int sqindex = 0;
        // Generate points along circular curve
        float[][] pset = new float[((int) ps + 1) * RES][2];
        float opd = pd;
        int lc = 0; // (layer count)

        switch (type) {
            case 0: //Generate circular 180 degree arc of "RES" points (RES=Resolution)
                for(int i=0;i<RES;i++){
                    pset[i][0]= (float) (radius+radius*Math.cos(i*PI/RES-PI));
                    pset[i][1]= (float) (radius*Math.sin(i*PI/RES-PI));
                }
                int side=0;
                float j=-1*radius;
                int k=0;
                int escflag=0;
                for(float i=ps;i<=2*radius+ps-0.001;i+=ps){ //for each multiple of pore size x coord below diameter
                    j=-1*radius;
                    if(side==0){ //if odd
                        while(j<i && escflag==0){
                            j=pset[k][0]; //generate positive y point along circle
                            if(k>RES){
                                escflag=1;
                            }
                            k++;
                        }
                        k--;
                        pset[k][1]=-1*pset[k][1]; //switch sides
                        k++;
                        side=1;
                    } else { //otherwise (if even)
                        while(j<i && escflag==0){
                            j=pset[k][0]; //generate negative y points along circle
                            pset[k][1]=-1*pset[k][1];
                            if(k>RES){
                                escflag=1;
                            }
                            k++;
                        }
                        k--;
                        pset[k][1]=-1*pset[k][1]; //switch sides
                        k++;
                        side=0;
                    }
                }
                //Center coordinates along x y plane
                for(int i=0;i<(RES);i++){
                    pset[i][0]=pset[i][0]-radius;
                }
                break;
            case 1: //Square
                float y=radius;
                float x;
                for(x=-1*radius; x<=radius+0.02;x+=ps){
                    pset[sqindex][0]=x;
                    pset[sqindex][1]=y;
                    sqindex++;
                    y*=-1;
                    pset[sqindex][0]=x;
                    pset[sqindex][1]=y;
                    sqindex++;
                }
                break;
            case 2: //Radial mesh (higher interior porosity has inferior biologics)
                do{ //do while moment!!! (it just does thing once first before checking loop condition)
                    pset[lc*RES][0]=(radius-pd)*-1;
                    pset[lc*RES][1]=0;
                    for(int i=1;i<RES;i++){ //move in radial pattern for certain angular distance, then move to inner radius and do same. When circle closed, move inwards/repeat.
                        if(((int)(2*ps*i/((float)RES)) % 2 ) == 1){
                            pset[i+lc*RES][0]= (float) ((radius-pd+opd)*Math.cos(2*i*PI/RES-PI));
                            pset[i+lc*RES][1]= (float) ((radius-pd+opd)*Math.sin(2*i*PI/RES-PI));
                        } else {
                            pset[i+lc*RES][0]= (float) ((radius-pd)*Math.cos(2*i*PI/RES-PI));
                            pset[i+lc*RES][1]= (float) ((radius-pd)*Math.sin(2*i*PI/RES-PI));
                        }
                    }
                    lc++;
                    pd+=(opd+lh);
                    ps=Math.round(ps*(radius-opd)/(radius))-1;
                } while (fill==1 && (radius-pd>0));
                break;
        }

        // Output to console/file
        try {
            float indextest = -1 * radius; // This thing right here just figures out how to fully close the structure
            int poreindex = 0;
            while (indextest < (-1 * radius + ps)) {
                indextest = pset[poreindex][0];
                poreindex++;
            }
            poreindex--;

            if (pl == 1) { // closes beginning of curve to side
                for (int i = (int) (poreindex / 1.5); i >= 0; i--) {
                    writer.write(String.format("X %f Y %f ;%n", pset[i][0], -1 * pset[i][1]));
                    System.out.println(pset[i][0] + " " + pset[i][1] + "\n");
                }
            }

            if (type == 0) { // prints main curve segment
                for (int i = 0; i < RES; i++) {
                    writer.write(String.format("X %f Y %f ;%n", pset[i][0], pset[i][1]));
                }
            }

            if (type == 1) { // prints main curve segment
                for (int i = 0; i < sqindex; i++) {
                    writer.write(String.format("X %f Y %f ;%n", pset[i][0], pset[i][1]));
                }
            }

            if (type == 2) { // prints main curve segment for radially generated scaffolds
                for (int i = 0; i < lc; i++) {
                    for (int j = i * RES; j < i * RES + RES; j++) {
                        writer.write(String.format("X %f Y %f ;%n", pset[j][0], pset[j][1]));
                    }
                    writer.write(String.format("X %f Y %f ;%n", pset[i * RES][0], pset[i * RES][1]));
                }
            }

            if (pl == 1) { // prints end of curve to connect
                for (int i = 0; i < poreindex / 2.1; i++) {
                    writer.write(String.format("X %f Y %f ;%n", -1 * pset[i][0], pset[i][1]));
                }
            }

            System.out.println("\nINFILL PATTERN SUCCESSFULLY GENERATED");
            writer.close();
        } catch (IOException e) {
            System.err.println("Error while writing to file.");
        }
    }

    private static void tesselator(int RES, float layer, float zmax, float cooldown, float speed) {
        String inputFileName = "layer.bpl";
        String outputFileName = "output.gcode";

        File inFile = new File(inputFileName);
        File outFile = new File(outputFileName);

        try {
            Scanner scanner = new Scanner(inFile);
            FileWriter writer = new FileWriter(outFile);

            float[][] coord = new float[RES * 2][3];
            float[][] tcoord = new float[RES * 2][3];
            int cout = 0;

            char tmpchar;
            float tmpcoord;
            while (scanner.hasNext()) {
                tmpchar = scanner.next().charAt(0);
                if (tmpchar == 'X') {
                    tmpcoord = scanner.nextFloat();
                    coord[cout][0] = tmpcoord;
                }
                if (tmpchar == 'Y') {
                    tmpcoord = scanner.nextFloat();
                    coord[cout][1] = tmpcoord;
                }
                if (tmpchar == ';') {
                    scanner.nextLine();
                    cout++;
                }
            }

            System.out.println("\nLAYERING\n--------");

            Scanner inputScanner = new Scanner(System.in);
            System.out.print("\nHOW MANY PRINTHEADS: ");
            float phead = inputScanner.nextFloat();
            System.out.print("\nHOW MANY SECTIONS: ");
            int sect = inputScanner.nextInt();

            System.out.println("\nWHAT ANGLE SHOULD THESE COORDINATES BE ROTATED: (DEGREES)\n\n");
            float[] angle = new float[sect];
            for (int i = 0; i < sect; i++) {
                System.out.print("  SECTION " + (i + 1) + ": ");
                angle[i] = (float) (Math.PI * inputScanner.nextFloat() / 180);
            }

            System.out.println("\nFLIP X: (1=YES, 0=NO)\n");
            int[] fx = new int[sect];
            for (int i = 0; i < sect; i++) {
                System.out.print("  SECTION " + (i + 1) + ": ");
                fx[i] = inputScanner.nextInt();
            }

            System.out.println("\nFLIP Y: (1=YES, 0=NO)\n");
            int[] fy = new int[sect];
            for (int i = 0; i < sect; i++) {
                System.out.print("  SECTION " + (i + 1) + ": ");
                fy[i] = inputScanner.nextInt();
            }

            System.out.println("\nRECURSIVE TRANSFORM: (1=YES, 0=NO)\n");
            int[] rt = new int[sect];
            for (int i = 0; i < sect; i++) {
                System.out.print("  SECTION " + (i + 1) + ": ");
                rt[i] = inputScanner.nextInt();
            }

            System.out.println("\nSWITCHBACK LAYERING: (1=YES, 0=NO)\n");
            int[] switchback = new int[sect];
            for (int i = 0; i < sect; i++) {
                System.out.print("  SECTION " + (i + 1) + ": ");
                switchback[i] = inputScanner.nextInt();
            }

            float sw = zmax / phead;
            int head = 0;

            if (fx[0] == 1) {
                for (int i = 0; i < cout; i++) {
                    tcoord[i][0] = -1 * coord[i][0];
                }
            } else {
                for (int i = 0; i < cout; i++) {
                    tcoord[i][0] = coord[i][0];
                }
            }
            if (fy[0] == 1) {
                for (int i = 0; i < cout; i++) {
                    tcoord[i][1] = -1 * coord[i][1];
                }
            } else {
                for (int i = 0; i < cout; i++) {
                    tcoord[i][1] = coord[i][1];
                }
            }
            for (int i = 0; i < cout; i++) {
                float transx = tcoord[i][0] * (float) Math.cos(angle[0]) + tcoord[i][1] * (float) Math.sin(angle[0]);
                float transy = -1 * tcoord[i][0] * (float) Math.sin(angle[0]) + tcoord[i][1] * (float) Math.cos(angle[0]);
                tcoord[i][0] = transx;
                tcoord[i][1] = transy;
            }

            // Output to .gcode/console
            writer.write("M107 ;\nG21 ;\nG90 ;\nM83 ;\n"); // initiation gcode statement for biox cellink printer
            if (phead > 1) {
                System.out.println("T0 ;\n"); // select printhead 1 if applicable
            }
            writer.write("G1 Z" + String.format("%.3f", layer) + ";\n"); // move to starting z coordinate
            writer.write("G1 X0 Y0 F60;\n"); // move to starting xy coordinate
            writer.write("G4 P1000;\n"); // give printer a sec for extrusion to catch up (thanks cellink)
            int layercout = 1;
            float sectiontrack = 0;
            float headtrack = 0;
            float previoushead = 0;

            for (float z = layer; z <= zmax; z += layer) { // layer generator/transformer
                sectiontrack = sect * z / zmax;
                previoushead = headtrack;
                headtrack = head * z / zmax;
                if ((int) headtrack > (int) previoushead) { // printhead selection
                    writer.write("T" + (int) headtrack + ";\n");
                }
                if (z != layer) { // z axis movement
                    writer.write("G4 P" + String.format("%.3f", cooldown) + ";\n");
                    writer.write("G1 Z" + String.format("%.3f", z) + ";\n");
                }
                layercout++;
                if (rt[(int) sectiontrack] != 1) { // odd layer non-recursive case
                    if (layercout % 2 == 1) {
                        writer.write("G1 X" + String.format("%.3f", coord[0][0]) + " Y" + String.format("%.3f", coord[0][1]) + " ;\n");
                        for (int i = 1; i < cout; i++) {
                            writer.write("G1 X" + String.format("%.3f", coord[i][0]) + " Y" + String.format("%.3f", coord[i][1]) + " E1;\n");
                        }
                        if (switchback[(int) sectiontrack] == 1 && (z + layer <= zmax) && (layercout > 2)) {
                            System.out.println("G4 P" + String.format("%.3f", cooldown) + ";\n");
                            z += layer;
                            writer.write("G1 Z" + String.format("%.3f", z) + " F" + String.format("%.3f", speed) + ";\n");
                            for (int i = cout - 1; i >= 0; i--) {
                                writer.write("G1 X" + String.format("%.3f", coord[i][0]) + " Y" + String.format("%.3f", coord[i][1]) + " E1;\n");
                            }
                        }
                    } else { // even layer non-recursive case
                        writer.write("G1 X" + String.format("%.3f", tcoord[0][0]) + " Y" + String.format("%.3f", tcoord[0][1]) + " ;\n");
                        for (int i = 1; i < cout; i++) {
                            writer.write("G1 X" + String.format("%.3f", tcoord[i][0]) + " Y" + String.format("%.3f", tcoord[i][1]) + " E1;\n");
                        }
                        if (switchback[(int) sectiontrack] == 1 && (z + layer <= zmax) && (layercout > 2)) { // switchback case
                            z += layer;
                            writer.write("G1 Z" + String.format("%.3f", z) + " F" + String.format("%.3f", speed) + ";\n");
                            for (int i = cout - 1; i >= 0; i--) {
                                writer.write("G1 X" + String.format("%.3f", tcoord[i][0]) + " Y" + String.format("%.3f", tcoord[i][1]) + " E1;\n"); // you reached the deepest part in the code congrats
                            }
                        }
                    }
                } else { // recursive transformation case
                    if (fx[(int) sectiontrack] == 1) { // check x layer swap
                        for (int i = 0; i < cout; i++) {
                            tcoord[i][0] = (float) Math.pow(-1, layercout) * coord[i][0];
                        }
                    } else {
                        for (int i = 0; i < cout; i++) {
                            tcoord[i][0] = coord[i][0];
                        }
                    }
                    if (fy[(int) sectiontrack] == 1) { // check y layer swap
                        for (int i = 0; i < cout; i++) {
                            tcoord[i][1] = (float) Math.pow(-1, layercout) * coord[i][1];
                        }
                    } else {
                        for (int i = 0; i < cout; i++) {
                            tcoord[i][1] = coord[i][1];
                        }
                    }
                    for (int i = 0; i < cout; i++) { // euler rotation matrix transformation
                        float transx = tcoord[i][0] * (float) Math.cos(layercout * angle[(int) sectiontrack]) + tcoord[i][1] * (float) Math.sin(layercout * angle[(int) sectiontrack]);
                        float transy = -1 * tcoord[i][0] * (float) Math.sin(layercout * angle[(int) sectiontrack]) + tcoord[i][1] * (float) Math.cos(layercout * angle[(int) sectiontrack]);
                        tcoord[i][0] = transx;
                        tcoord[i][1] = transy;
                    }
                    writer.write("G1 X" + String.format("%.3f", tcoord[0][0]) + " Y" + String.format("%.3f", tcoord[0][1]) + ";\n");
                    for (int i = 1; i < cout - 1; i++) { // print result :3
                        writer.write("G1 X" + String.format("%.3f", tcoord[i][0]) + " Y" + String.format("%.3f", tcoord[i][1]) + " E1;\n");
                    }
                    writer.write("G1 X" + String.format("%.3f", tcoord[cout - 1][0]) + " Y" + String.format("%.3f", tcoord[cout - 1][1]) + " E1;\n");
                    writer.write("G1 X" + String.format("%.3f", tcoord[cout - 1][0]) + " Y" + String.format("%.3f", tcoord[cout - 1][1]) + ";\n");
                    if (switchback[(int) sectiontrack] == 1 && (z + layer <= zmax) && (layercout > 2)) { // switchback layering case
                        writer.write("G4 P" + String.format("%.3f", cooldown) + ";\n");
                        z += layer;
                        writer.write("G1 Z" + String.format("%.3f", z) + " F" + String.format("%.3f", speed) + ";\n");
                        for (int i = cout - 1; i >= 0; i--) {
                            writer.write("G1 X" + String.format("%.3f", tcoord[i][0]) + " Y" + String.format("%.3f", tcoord[i][1]) + " E1;\n");
                        }
                        writer.write("G1 X" + String.format("%.3f", tcoord[0][0]) + " Y" + String.format("%.3f", tcoord[0][1]) + ";\n");
                    }
                }
            }
            writer.write("M107 ;\nG0 \nM84 ;\n"); // printer end statement
            writer.close();

            System.out.println("\n.GCODE SUCCESSFULLY GENERATED\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
