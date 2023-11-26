import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.concurrent.ThreadLocalRandom;
import org.w3c.dom.Node;

/*  Progress bar - Credits: https://stackoverflow.com/questions/852665/command-line-progress-bar-in-java*/

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        Scanner sc = new Scanner(System.in);
        boolean run = true;
        while (run) {
            System.out.println("👉Zadajte operáciu: \n1 - 🚀automaticky generovaná formula \t 2 - 🫳zadajte formulu \t0 - 📴ukončiť program");
            System.out.println("Testovanie: \t77 - 📚testovanie absolutny počet nodov \t 88 - 📚testovanie s bestOrder \t 99 - 📚testovanie bdd_creat");
            int opCode = sc.nextInt();
            String formula;
            String[] variableOrder;
            switch (opCode) {
                case 0 -> {
                    System.out.println("🙋‍♂️Program ukončený");
                    run = false;
                }
                case 1 -> {
                    System.out.println("Zadajte počet premenných [4-13]");
                    int num = sc.nextInt();
                    formula = BDD.generateRandomFormula(num);
                    variableOrder = BDD.getVariables(formula);
                    int numOfVariables = variableOrder.length;
                    System.out.println();
                    long start = System.nanoTime();
                    BDD tree = BDD.BDD_create(formula, variableOrder);
                    long end = System.nanoTime();
                    long timeElapsed = end - start;


                    System.out.println("🌲BDD strom použitie BDD_create:");
                    double k = BDD.percentReduction(numOfVariables, tree);
                    BDD.testPrint(tree, formula, variableOrder, k);
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu: " + timeElapsed / 1000000 + "ms");
                    System.out.println();

                    start = System.nanoTime();
                    BDD tree2 = BDD.BDD_create_with_best_order(formula);
                    end = System.nanoTime();
                    timeElapsed = end - start;
                    double k2 = BDD.percentReduction(numOfVariables, tree2);


                    System.out.println("🌲BDD strom použitie BDD_create_with_best_order:");
                    BDD.testPrint(tree2, formula, tree2.variablesOrder, k2);
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu s bestOrder + n * opakovanie: " + timeElapsed / 1000000 + "ms");
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu s bestOrder: " + (tree2.getTimeElapsed() / 1000000) + "ms");
                    System.out.println();

                    boolean a = true;
                    while (a) {
                        char opCode2 = ' ';
                        System.out.println("use - \uD83D\uDCCE BDD_use \t test - \uD83D\uDDF3 BDD_test \tprint - 🖨️BDD_print \t exit - \uD83D\uDEAE návrat");
                        opCode2 = sc.next().charAt(0);
                        switch (opCode2) {
                            case 'u' -> {
                                System.out.println("BDD_use [počet premenných = " + tree2.variablesOrder.length + "]");
                                String input = "";
                                input = sc.next();
                                BDD.BDD_use(tree2, input);
                            }
                            case 't' -> {
                                System.out.println("BDD_test");
                                BDD.BDD_test(tree2);
                            }
                            case 'e' -> {
                                a = false;
                            }
                            case 'p' -> {
                                BinaryTreePrinter print = new BinaryTreePrinter(tree2.getRoot());
                                print.print(System.out);
                                System.out.println();
                            }
                            default -> {
                                System.out.println("‼️ Zadajte platnú operáciu");
                                break;
                            }
                        }
                    }
                }
                case 2 -> {
                    System.out.println("Zadajte formulu v tvare napr: 'ABC+!A!C+B'");
                    formula = sc.next();
                    variableOrder = BDD.getVariables(formula);
                    int numOfVariables = variableOrder.length;
                    System.out.println();
                    long start = System.nanoTime();
                    BDD tree = BDD.BDD_create(formula, variableOrder);
                    long end = System.nanoTime();
                    long timeElapsed = end - start;
                    System.out.println("BDD strom použitie BDD_create:");
                    double k = BDD.percentReduction(numOfVariables, tree);
                    BDDNode printNode = tree.getRoot();
                    BDD.testPrint(tree, formula, variableOrder, k);
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu: " + timeElapsed / 1000000 + "ms");
                    System.out.println();

                    start = System.nanoTime();
                    BDD tree2 = BDD.BDD_create_with_best_order(formula);
                    end = System.nanoTime();
                    timeElapsed = end - start;
                    double k2 = BDD.percentReduction(numOfVariables, tree2);
                    System.out.println("BDD strom použitie BDD_create_with_best_order:");
                    BDD.testPrint(tree2, formula, tree2.variablesOrder, k2);
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu s bestOrder + n * opakovanie: " + timeElapsed / 1000000 + "ms");
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu s bestOrder: " + (tree2.getTimeElapsed() / 1000000) + "ms");
                    System.out.println();

                    boolean a = true;
                    while (a) {
                        char opCode2 = ' ';
                        System.out.println("use - \uD83D\uDCCE BDD_use \t test - \uD83D\uDDF3 BDD_test \tprint - 🖨️BDD_print \t exit - \uD83D\uDEAE návrat");
                        opCode2 = sc.next().charAt(0);
                        switch (opCode2) {
                            case 'u' -> {
                                System.out.println("BDD_use [počet premenných = " + tree2.variablesOrder.length + "]");
                                String input = "";
                                input = sc.next();
                                BDD.BDD_use(tree2, input);
                            }
                            case 't' -> {
                                System.out.println("BDD_test");
                                BDD.BDD_test(tree2);
                            }
                            case 'e' -> {
                                a = false;
                            }
                            case 'p' -> {
                                System.out.println(Arrays.toString(tree2.variablesOrder));
                                BinaryTreePrinter print = new BinaryTreePrinter(tree2.getRoot());
                                print.print(System.out);
                                System.out.println();
                            }
                            case 'q' -> {
                                System.out.println(Arrays.toString(tree.variablesOrder));
                                BinaryTreePrinter print2 = new BinaryTreePrinter(printNode);
                                print2.print(System.out);
                                System.out.println();
                            }
                            default -> {
                                System.out.println("‼️ Zadajte platnú operáciu");
                            }
                        }
                    }

                }
                case 3 -> {
                    System.out.println("Zadajte formulu v tvare napr: 'ABC+!A!C+B'");
                    formula = sc.next();
                    System.out.println("Zadajte poradie premenných v tvare napr: 'ABC'");
                    variableOrder = sc.next().split("");
                    int numOfVariables = variableOrder.length;
                    System.out.println();
                    long start = System.nanoTime();
                    BDD tree = BDD.BDD_create(formula, variableOrder);
                    long end = System.nanoTime();
                    long timeElapsed = end - start;
                    System.out.println("BDD strom použitie BDD_create:");
                    double k = BDD.percentReduction(numOfVariables, tree);
                    BDD.testPrint(tree, formula, variableOrder, k);
                    System.out.println("\uD83D\uDD50 Čas vytvorenia BDD stromu: " + timeElapsed / 1000000 + "ms");
                    System.out.println();

                    boolean a = true;
                    while (a) {
                        char opCode2 = ' ';
                        System.out.println("use - \uD83D\uDCCE BDD_use \t test - \uD83D\uDDF3 BDD_test \tprint - 🖨️BDD_print \t exit - \uD83D\uDEAE návrat");
                        opCode2 = sc.next().charAt(0);
                        switch (opCode2) {
                            case 'u' -> {
                                System.out.println("BDD_use [počet premenných = " + tree.variablesOrder.length + "]");
                                String input = "";
                                input = sc.next();
                                BDD.BDD_use(tree, input);
                            }
                            case 't' -> {
                                System.out.println("BDD_test");
                                BDD.BDD_test(tree);
                            }
                            case 'e' -> {
                                a = false;
                            }
                            case 'p' -> {
                                System.out.println(Arrays.toString(tree.variablesOrder));
                                BinaryTreePrinter print = new BinaryTreePrinter(tree.getRoot());
                                print.print(System.out);
                                System.out.println();
                            }
                            case '0' -> {
                                System.out.println("🙋‍♂️Program ukončený");
                                return;
                            }
                            default -> {
                                System.out.println("‼️ Zadajte platnú operáciu");
                            }
                        }
                    }

                }
                case 99 -> {
                    System.out.println("Testing - Time complexity BDD_Create");
                    long average = 0;
                    for (int j = 4; j <= 13; j++) {
                        for (int i = 0; i < 200; i++) {
                            String formulaTest = BDD.generateRandomFormula(j);
                            String[] variableOrderTest = BDD.getVariables(formulaTest);
                            long start = System.nanoTime();
                            BDD.BDD_create(formulaTest, variableOrderTest);
                            long end = System.nanoTime();
                            BDD.randomOrder(variableOrderTest);
                            long timeElapsed = end - start;
                            average += timeElapsed;
                        }
                        System.out.println(average / 200);
                    }
                }
                case 88 -> {
                    System.out.println("Testing - Time complexity BDD_Create_with_best_order");
                    long average = 0;
                    long average2 = 0;
                    long[] times = new long[14];
                    for (int j = 4; j <= 13; j++) {
                        for (int i = 0; i < 200; i++) {
                            String formulaTest = BDD.generateRandomFormula(j);
                            String[] variableOrderTest = BDD.getVariables(formulaTest);
                            long start = System.nanoTime();
                            BDD treeTest = BDD.BDD_create_with_best_order(formulaTest);
                            long end = System.nanoTime();
                            BDD.randomOrder(variableOrderTest);
                            long timeElapsed = end - start;
                            average += timeElapsed;
                            average2 += treeTest.getTimeElapsed();
                        }
                        System.out.println(average / 200);
                        times[j] = average2 / 200;
                    }
                    System.out.println("----------------");
                    for (int i = 4; i <= 13; i++) {
                        System.out.println(times[i]);
                    }
                }
                case 77 -> {
                    System.out.println("Testing - Absolute number of nodes ");
                    int average = 0;
                    int average2 = 0;
                    int[] nodes = new int[14];
                    int[] nodes2 = new int[14];
                    int count = 4;

                    for (int j = 4; j <= 13; j++){
                        for (int i = 0; i < 100; i++){
                            String formulaTest = BDD.generateRandomFormula(j);
                            String[] variableOrderTest = BDD.getVariables(formulaTest);
                            BDD treeTest = BDD.BDD_create(formulaTest, variableOrderTest);
                            int A = BDD.countUniqueNodes(treeTest.getRoot());
                            average += A;
                        }
                        nodes[j] = average / 100;
                        for (int i = 0; i < 100; i++){
                            String formulaTest = BDD.generateRandomFormula(j);
                            BDD treeTest2 = BDD.BDD_create_with_best_order(formulaTest);
                            int B = BDD.countUniqueNodes(treeTest2.getRoot());
                            average2 += B;
                        }
                        nodes2[j] = average2 / 100;
                        System.out.println(count);
                        System.out.println(average/100 + " " + average2/100);
                        count++;
                    }
                    for (int i = 4; i <= 13; i++){
                        System.out.println(nodes[i]);
                    }
                    System.out.println("----------------");
                    for (int i = 4; i <= 13; i++){
                        System.out.println(nodes2[i]);
                    }
                }
                default -> {
                    System.out.println("‼️ Zadajte platnú operáciu");
                }
            }
        }
    }
}
