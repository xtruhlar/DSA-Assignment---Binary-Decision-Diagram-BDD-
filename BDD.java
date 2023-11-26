import org.w3c.dom.ls.LSOutput;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Trieda BDD */
public class BDD {
    static Random random = new Random(System.currentTimeMillis());
    public HashMap<String , BDDNode> table;
    public static BDDNode root;
    public String[] variablesOrder;
    public long timeElapsed;

    /* Konštruktor pre BDD */
    public BDD(BDDNode root, String[] variablesOrder) {
        BDD.root = root;
        this.variablesOrder = variablesOrder;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public BDDNode getRoot() {
        return root;
    }

    public HashMap<String, BDDNode> getTable(){
        return this.table;
    }


    /* Koncové uzly pre "0" a "1" */
    static BDDNode trueNode = new BDDNode("1", null, null);
    static BDDNode falseNode = new BDDNode("0", null, null);


    /* Metóda BDD_create */
    public static BDD BDD_create(String formula, String[] variableOrder) {
        BDDNode root = new BDDNode(formula, null, null);
        ArrayList<BDDNode> array = new ArrayList<>();
        array.add(root);
        root.setParent(root);

        for (String variable : variableOrder) {
            ArrayList<BDDNode> newArray = new ArrayList<>();
            for (BDDNode node : array) {
                if (!node.getVariable().equals("0") && !node.getVariable().equals("1")) {
                    node.setControlVariable(variable);

                    node.setLow(BDD.decompose(node, variable, false));
                    node.getLow().setParent(node);
                    newArray.add(node.getLow());

                    node.setHigh(BDD.decompose(node, variable, true));
                    node.getHigh().setParent(node);
                    newArray.add(node.getHigh());

                    /* Redukovanie - ak low == high */
                    if (node.getLow() != null && node.getLow().getVariable().equals(node.getHigh().getVariable())) {
                        if (node.getParent() != null && node.getParent().getLow().equals(node)) {
                            node.getParent().setLow(node.getLow());
                        } else if (node.getParent() != null && node.getParent().getHigh().equals(node)) {
                            node.getParent().setHigh(node.getHigh());
                        }
                        newArray.remove(node);
                    }

                    /* Redukovanie - ak low aj high == 1 resp. 0 */
                    BDDNode temp = node.getParent();
                    if (temp.getLow() != null && temp.getLow().getVariable().equals(temp.getHigh().getVariable())) {
                        if (temp.getLow().getVariable().equals("1")) {
                            if (temp.getParent().getLow().equals(temp)) {
                                temp.getParent().setLow(trueNode);
                                newArray.remove(node);
                            } else if (temp.getParent().getHigh().equals(temp)) {
                                temp.getParent().setHigh(trueNode);
                                newArray.remove(node);
                            }
                        } else if (temp.getLow().getVariable().equals("0")) {
                            if (temp.getParent().getLow().equals(temp)) {
                                temp.getParent().setLow(falseNode);
                                newArray.remove(node);
                            } else if (temp.getParent().getHigh().equals(temp)) {
                                temp.getParent().setHigh(falseNode);
                                newArray.remove(node);
                            }
                        }
                    }
                }
            }
            BDD.BDDreduce(newArray);
            array = newArray;
        }
        BDD.BDDreduce(array);
        return new BDD(root, variableOrder);
    }


    /* Implementovanie redukcií typu I a S */
    static void BDDreduce(ArrayList<BDDNode> array) {
        /* Redukcia typu I */
        for (int i = 0; i < array.size(); i++) {
            for (int j = i + 1; j < array.size(); j++) {
                BDDNode node1 = array.get(i);
                BDDNode node2 = array.get(j);
                /* Ak nájdeme dva nody s rovnakou formulou */
                if (node1.getVariable().equals(node2.getVariable())) {
                    /* Ak majú rovnakého low aj high node, môžeme jeden z nich vymazať */
                    if (node1.getLow() != null && node1.getHigh() != null && node1.getLow().getVariable().equals(node2.getLow().getVariable()) && node1.getHigh().getVariable().equals(node2.getHigh().getVariable())) {
                        if (node2.getParent().getLow().getVariable().equals(node2.getVariable())) {
                            if (node2.getParent().getParent().equals(node2.getParent())) {
                                node2.getParent().getParent().setLow(node1);
                            } else {
                                node2.getParent().getParent().setHigh(node1);
                            }
                        } else if (node2.getParent().getHigh().getVariable().equals(node2.getVariable())) {
                            if (node2.getParent().getParent().equals(node2.getParent())) {
                                node2.getParent().getParent().setLow(node1);
                            } else {
                                node2.getParent().getParent().setHigh(node1);
                            }
                        }
                        array.remove(node2);
                    }
                }
            }
        }

        /* Redukcia typu S */
        for (int i = 0; i < array.size(); i++) {
            BDDNode node1 = array.get(i);
            /* Ak sa formuly v low a high rovnajú */
            if (node1.getHigh() != null && node1.getLow() != null && node1.getLow().getVariable().equals(node1.getHigh().getVariable())) {
                /* nastavíme pointer parenta na child aby sme mohli node1 vynechať a vymazať */
                if (node1.getParent() != null && node1.getParent().getLow().equals(node1)) {
                    node1.getParent().setLow(node1.getLow());
                }
                if (node1.getParent() != null && node1.getParent().getHigh().equals(node1)) {
                    node1.getParent().setHigh(node1.getLow());
                }
                array.remove(node1.getHigh());
                array.remove(node1);
            }
        }
        /* Ošetrenie prípadu variable + !variable vo formule */
        for (int i = 0; i < array.size(); i++) {
            BDDNode node1 = array.get(i);
            String[] clauses = node1.getVariable().split("\\+");
            String variable = node1.getControlVariable();
            String negatedVariable = "!" + variable;
            /* Ak sa formuly v low a high rovnajú */
            if (node1.getHigh() != null && node1.getLow() != null && node1.getLow().getVariable().equals(node1.getHigh().getVariable())) {
                for (int j = 0; j < clauses.length; j++) {
                    for (int k = j + 1; k < clauses.length; k++) {
                        if (clauses[j].equals(clauses[k])) {
                            clauses[k] = "";
                        }
                        if (clauses[j].equals(negatedVariable) && clauses[k].equals(negatedVariable) || clauses[j].equals(variable) && clauses[k].equals(variable)) {
                            clauses[k] = "";
                        }
                        if (node1.getParent() != null && clauses[j].equals(variable) && clauses[k].equals(negatedVariable) || clauses[j].equals(negatedVariable) && clauses[k].equals(variable)) {
                            if (node1.getParent().getLow().equals(node1)) {
                                node1.getParent().setLow(trueNode);
                            } else {
                                node1.getParent().setHigh(trueNode);
                            }
                            array.remove(node1);
                        }
                    }
                }
            }
        }
    }


    /* Metóda bestOrder */
    public static String[] bestOrder(String formula, String[] chooseFrom) {
        String[] bestOrder = {};
        HashMap<Integer, String[]> table = new HashMap<Integer, String[]>();
        int min = (int) Math.pow(2, chooseFrom.length) + 1;

        // First try with the given order
        BDD temp = BDD_create(formula, chooseFrom);
        int tempInt = countUniqueNodes(temp.getRoot());
        if (tempInt < min) {
            min = tempInt;
        }
        table.put(tempInt, chooseFrom.clone());

        // Iterate over all other orders
        for (int i = 1; i < chooseFrom.length; i++) {
            // Randomly shuffle the array
            Collections.shuffle(Arrays.asList(chooseFrom));
            temp = BDD_create(formula, chooseFrom);
            tempInt = countUniqueNodes(temp.getRoot());
            if (tempInt < min) {
                min = tempInt;
                table.clear();
            }
            if (!table.containsKey(tempInt)) {
                table.put(tempInt, chooseFrom.clone());
            }
        }

        bestOrder = table.get(min);
        return bestOrder;
    }


    /* Metóda BDD_Create_with_best_order */
    public static BDD BDD_create_with_best_order(String formula) {
        /* Extrahovanie premenných z formuly */
        String formulaCleaning = formula.replaceAll("\\+", "");
        formulaCleaning = formulaCleaning.replaceAll("!", "");
        String[] uniqueVariables = formulaCleaning.split("");
        Set<String> variableSet = new HashSet<>(Arrays.asList(uniqueVariables));
        /* Zostrojenie variableOrder */
        String[] variableOrder = variableSet.toArray(new String[0]);
        String[] bestOrder = bestOrder(formula, variableOrder);
        BDD Tree;
        long startTime = System.nanoTime();
        Tree = BDD_create(formula, bestOrder);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        Tree.setTimeElapsed(timeElapsed);
        return Tree;
    }


    /* Metóda test */
    public static void BDD_test(BDD tree) throws InterruptedException {
        /* Vygenerujeme všetky vstupy */
        List<boolean[]> inputs = BDD.generateInputs(tree.variablesOrder.length);
        System.out.println();
        /* vytvorenie všetkých resultov, pomocou prepisu formuly na '0' a '1' */
        List<Boolean> results = BDD.results(tree, inputs);
        System.out.println();
        List<Boolean> results2 = new ArrayList<>();
        String[] variablesOrder = tree.variablesOrder;
        String formulaToRewrite = tree.getRoot().getVariable();
        int total0 = inputs.size();
        int count0 = 0;
        /* Vytvorenei všetkých výsledkov pomocou stromu */
        for (int k = 0; k < inputs.size(); k++) {
            for (String variable : variablesOrder) {
                String negatedVariable = "!" + variable;
                for (boolean[] booleans : inputs) {
                    int index = Arrays.asList(variablesOrder).indexOf(variable);
                    if (index >= booleans.length) {
                        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
                    }
                    boolean input = inputs.get(k)[index];
                    if (input) {
                        formulaToRewrite = formulaToRewrite.replaceAll(negatedVariable, "0");
                        formulaToRewrite = formulaToRewrite.replaceAll(variable, "1");
                    } else {
                        formulaToRewrite = formulaToRewrite.replaceAll(negatedVariable, "1");
                        formulaToRewrite = formulaToRewrite.replaceAll(variable, "0");
                    }
                }
            }
            String[] clauses = formulaToRewrite.split("\\+");
            boolean result = false;
            for (String clause : clauses) {
                if (!clause.contains("0")) {
                    result = true;
                    break;
                }
            }
            results2.add(result);
            formulaToRewrite = tree.getRoot().getVariable();

            count0++;
            int progress0 = (int) Math.round((count0 / (double) total0) * 100);
            System.out.print('\r' + "⌛ Generovanie výsledkov pomocou stromu: [");
            int j = 0;
            for (; j < (progress0 / 5); j++) { System.out.print("##"); }
            for (; j < 20; j++) { System.out.print("  "); }
            System.out.print("] " + progress0 + "%");
            System.out.flush();
            Thread.sleep(1);
        }
        System.out.println();
        int cislo = 0;
        boolean spravne = true;
        int total = results.size();

        for (int i = 0; i < total; i++) {
            if (results.get(i) != results2.get(i)) {
                spravne = false;
                System.out.println("❌❌❌ Výsledok " + i + " sa nezhoduje" + " " + results.get(i) + " " + results2.get(i));
                System.out.println();
            }

            cislo++;
            int progress = (int) Math.round((cislo / (double) total) * 100);
            System.out.print('\r' + "⌛ Porovnávanie výsledkov: [");
            int j = 0;
            for (; j < (progress / 5); j++) { System.out.print("##"); }
            for (; j < 20; j++) { System.out.print("  "); }
            System.out.print("] " + progress + "%");
            System.out.flush();
            Thread.sleep(1);
        }
        System.out.println("\n");
        /* Vyhodnotenie výsledkov */
        if (spravne) {
            System.out.println("✅ Výsledky sa zhodujú");
            System.out.println();
        } else {
            System.out.println("❌ Výsledky sa nezhodujú");
            System.out.println();
        }

    }


    /* Metóda BDD_use */
    public static void BDD_use(BDD tree, String input) {
        String[] variablesOrder = tree.variablesOrder;
        String formulaToRewrite = tree.getRoot().getVariable();
        /* Pre každú premennú ... */
        for (String variable : variablesOrder) {
            String negatedVariable = "!" + variable;
            int index = Arrays.asList(variablesOrder).indexOf(variable);
            if (index >= input.length()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);
            }
            boolean inputBool = input.charAt(index) == '1';
            /* ... prepisujeme na základe 'true' / 'false' premenné na '1' alebo '0' */
            if (inputBool) {
                formulaToRewrite = formulaToRewrite.replaceAll(negatedVariable, "0");
                formulaToRewrite = formulaToRewrite.replaceAll(variable, "1");
            } else {
                formulaToRewrite = formulaToRewrite.replaceAll(negatedVariable, "1");
                formulaToRewrite = formulaToRewrite.replaceAll(variable, "0");
            }
        }
        boolean result = false;
        /* Rozdelíme na klauzuly */
        String[] clauses2 = formulaToRewrite.split("\\+");
        /* Ak je aspoň jedna klauzula, ktorá neobsahuje '0' teda obsahuje samé '1'-tky formula je pravdivá pri danom vstupe */
        for (String clause : clauses2) {
            if (!clause.contains("0")) {
                result = true;
                break;
            }
        }
        String[] inputs = input.split("");
        int k = inputs.length;
        boolean[] arr = new boolean[k];
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i].equals("0")) {
                arr[i] = false;
            }
            if (inputs[i].equals("1")) {
                arr[i] = true;
            }
        }
        /* Result 2 je traverzovanie vo vytvorenom strome, k získaniu 'true' / 'false' */
        boolean result2 = evaluate(tree.getRoot(), arr, tree.variablesOrder);
        System.out.println("Očakávaný výsledok: " + result + " <-> Výsledok: " + result2);
        /* vyhodnotenie result vs result2 */
        if (result == result2) {
            System.out.println("✅ Výsledky sa zhodujú");
            System.out.println();
        } else {
            System.out.println("❌ Výsledky sa nezhodujú");
            System.out.println();
        }
    }


    /* Metóda, ktorá vyhodnotí BDD pre všetky kombinácie inputov */
    public static boolean evaluate(BDDNode node, boolean[] inputs, String[] variablesOrder) {
        /* Ak sme dosiahli koncový vrchol, vrátime hodnotu na tomto vrchole */
        if (node.getVariable() != null && node.getVariable().equals("1")) {
            return true;
        } else if (node.getVariable() != null && node.getVariable().equals("0")) {
            return false;
        }
        /* Ak nie, pokračujeme rekurzívne */
        if (node.getControlVariable() == null && node.getVariable().length() <= 1) {
            return evaluate(node, inputs, variablesOrder);
        }
        int index = Arrays.asList(variablesOrder).indexOf(node.getControlVariable());
        if (index >= inputs.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + node.getControlVariable() + " " + node.getVariable());
        }
        /* Získame ďalší input */
        boolean input = inputs[index];
        BDDNode nextNode = (input) ? node.getHigh() : node.getLow();
        return evaluate(nextNode, inputs, variablesOrder);
    }


    /* Metóda, ktorá vytvorí pole resultov z vytvoreného BDD */
    public static List<Boolean> results(BDD tree, List<boolean[]> inputs) throws InterruptedException {
        List<Boolean> results = new ArrayList<>();
        int total = inputs.size();
        int count = 0;
        for (boolean[] input : inputs) {
            results.add(evaluate(tree.getRoot(), input, tree.variablesOrder));

            count++;
            int progress = (int) Math.round((count / (double) total) * 100);
            System.out.print('\r' + "⌛ Generovanie očakávaných výsledkov: [");
            int j = 0;
            for (; j < (progress / 5); j++) { System.out.print("##"); }
            for (; j < 20; j++) { System.out.print("  "); }
            System.out.print("] " + progress + "%");
            System.out.flush();
            Thread.sleep(1);
        }
        return results;
    }


    /* Metóda, ktorá vytvorí pole všetkých možných inputov */
    public static List<boolean[]> generateInputs(int n) throws InterruptedException {
        int total = (int) Math.pow(2, n);
        int count = 0;
        List<boolean[]> inputs = new ArrayList<>();
        if (n == 0) {
            inputs.add(new boolean[0]);
        } else {
            /* Rekurzívne získame všetky možné inputy pre n-1 premenných a pridáme k nim false a true */
            List<boolean[]> smallerInputs = generateInputs(n - 1);
            for (boolean[] input : smallerInputs) {
                boolean[] falseInput = new boolean[n];
                boolean[] trueInput = new boolean[n];
                System.arraycopy(input, 0, falseInput, 0, n - 1);
                System.arraycopy(input, 0, trueInput, 0, n - 1);
                falseInput[n - 1] = false;
                trueInput[n - 1] = true;
                inputs.add(falseInput);
                inputs.add(trueInput);

                count++;
                int progress = (int) Math.round((count / (double) total) * 100);
                System.out.print('\r' + "⌛ Generovanie 'inputov': [");
                int j = 0;
                for (; j < (progress / 5); j++) { System.out.print("####"); }
                System.out.print("] " + progress * 2 + "%");
                System.out.flush();
                Thread.sleep(1);
            }
        }
        return inputs;
    }


    /* Shannonova dekompozícia */
    public static BDDNode decompose(BDDNode currentNode, String variable, boolean logHodnota) {
        currentNode.setControlVariable(variable);
        /* Rozdelenie vstupnej DNF formuly na jednotlivé klauzuly */
        String[] clauses = currentNode.getVariable().split("\\+");
        /* Vytvorenie negovanej varianty premennej, podľa ktorej rozkladám */
        Set<String> uniqueClauses = new HashSet<>(Arrays.asList(clauses));
        clauses = uniqueClauses.toArray(new String[0]);
        Set<String> nextLevelClauses = new HashSet<>();

        /* Vytvorenie negovanej varianty premennej, podľa ktorej rozkladám */
        String negatedVariable = "!" + variable;

        /* AK !logHodnota vytvárame lavy branch stromu, ak logHodnota pravý branch stromu */
        if (!logHodnota) {
            for (String clause : clauses) {
                /* Ak je klauzula len kontrolovaná negatedVariable vraciame trueNode [true node pretože sme v !logHodnota vetve] */
                if (clause.equals(negatedVariable) && clauses.length > 1) {
                    return trueNode;
                }
                /* Ak klauzula obsahuje kontrolovanú negatedVariable [odstránime danú negatedVariable a priložíme zvyšok ku formule na nižšej úrovni] */
                if (clause.contains(negatedVariable)) {
                    clause = clause.replace(negatedVariable, "");
                    /* Podmienka, ktorá ošetrí aby sa nevkladalo + medzi klauzuly v prípade, formula na nižšej úrovni prázdna */
                    if (!clause.isEmpty()) {
                        nextLevelClauses.add(clause);
                    }
                    /* Ošetrenie prípadu, ak sa v klauzule variable nenachádza [celú klauzulu využijeme vo formule na nižšej úrovni] */
                } else if (!clause.contains(variable)) {
                    nextLevelClauses.add(clause);
                }
            }
            /* Rovnaká logika je použitá pre pravý branch */
        } else {
            for (String clause : clauses) {
                if (clause.equals(variable) && clauses.length > 1) {
                    return trueNode;
                }
                if (clause.contains(variable) && !clause.contains(negatedVariable)) {
                    clause = clause.replace(variable, "");
                    if (!clause.isEmpty()) {
                        nextLevelClauses.add(clause);
                    }
                } else if (!clause.contains(variable) && !clause.contains(negatedVariable)) {
                    nextLevelClauses.add(clause);
                }
            }
        }
        /* Ošetrenie koncových listov - teda ak po dekompozícií ostane na nižšej úrovni prázdna formula */
        if (nextLevelClauses.isEmpty()) {
            /* Ak je akruálna variable negovaná skontrolujem, na ktorú branch pridávame a podľa toho vyberieme trueNode alebo falseNode */
            if (currentNode.getVariable().contains(negatedVariable)) {
                if (logHodnota) {
                    return falseNode;
                } else {
                    return trueNode;
                }
            } else {
                if (logHodnota) {
                    return trueNode;
                } else {
                    return falseNode;
                }
            }
        }
        /* Vytvorenie novej formuly na nižšej úrovni */
        String nextLevelVariable = String.join("+", nextLevelClauses);
        return new BDDNode(nextLevelVariable, null, null);
    }


    /* Náhodne zoradenie poľa */
    static void randomOrder(String[] array) {
        /* Credits: https://www.digitalocean.com/community/tutorials/shuffle-array-java */
        Random random = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            String a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }


    /* Generovanie náhodnej formule */
    public static String generateRandomFormula(int numOfVariables) {
        String[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        randomOrder(alphabet);
        alphabet = Arrays.copyOfRange(alphabet, 0, numOfVariables);
        //Random random = new Random(System.currentTimeMillis());
        int clauses = random.nextInt(5, 10);
        StringBuilder generatedFormula = new StringBuilder();
        ArrayList<String> klauzuly = new ArrayList<>();
        for (int i = 0; i < clauses; i++) {
            /* Náhodne zoradím pole, z ktorého tvorím klauzuly */
            randomOrder(alphabet);
            StringBuilder add = new StringBuilder();
            /* Vyberiem náhodný počet prvkov z poľa [indexy od 0 po random] a pridám to premennej add */
            for (int j = 0; j < random.nextInt(3, alphabet.length); j++) {
                add.append(alphabet[j]);
            }
            /* add pridám do Arraylistu klauzul */
            klauzuly.add(add.toString());
        }

        for (int i = 0; i < clauses; i++) {
            ArrayList<String> s = new ArrayList<>(Arrays.stream(klauzuly.get(i).split("")).toList());
            /* Sortovanie variables v klauzuách podľa abecedy */
            Collections.sort(s);
            ArrayList<String> l = new ArrayList<>();
            /* Náhodné pridanie negácií */
            for (String a : s) {
                if (random.nextBoolean()) {
                    a = "!" + a;
                    l.add(a);
                } else {
                    l.add(a);
                }
            }
            String k = String.join("", l);
            if (generatedFormula.length() > 1) {
                generatedFormula.append("+").append(k);
            } else {
                generatedFormula.append(k);
            }

        }
        /* Return vygenerovanej formuly */
        return generatedFormula.toString();
    }


    /* Spočítanie unikátnych nodov */
    public static int countUniqueNodes(BDDNode root) {
        Set<BDDNode> set = new HashSet<>();
        countUniqueNodesHelper(root, set);
        return set.size() - 2;
    }
    public static void countUniqueNodesHelper(BDDNode root, Set<BDDNode> set) {
        if (root == null) {
            return;
        }
        set.add(root);
        /* Rekurzívne rátanie */
        countUniqueNodesHelper(root.getLow(), set);
        countUniqueNodesHelper(root.getHigh(), set);
    }


    /* Výpočet percentuálnej miery zredukovania */
    public static double percentReduction(int numOfVariables, BDD tree) {
        int fullTree = (int) Math.pow(2, numOfVariables) - 1;
        int ROBDD = BDD.countUniqueNodes(tree.getRoot());
        /* (Vymazané nody / všetky nody)*100 */
        return ((double) (fullTree - ROBDD) / (double) fullTree) * 100;
    }


    /* TestPrint */
    public static void testPrint(BDD root, String formula, String[] variableOrder, double k) {
        System.out.println("\uD83D\uDD24 Testovana formula: " + formula);
        System.out.print("\uD83D\uDD21 Poradie premennych: ");
        for (String s : variableOrder) {
            System.out.print(s);
        }
        System.out.println();
        System.out.printf("♻️ Percentualna miera zredukovania: %.2f%%\n", k);
        System.out.println("\uD83D\uDD22 Počet unikátnych nodov: " + BDD.countUniqueNodes(root.getRoot()));
    }


    /* Metóda, ktorá vráti pole unikátnych premenných */
    public static String[] getVariables(String formula) {
        /* Očistenie formuly od '+' a '!' */
        String formulaCleaning = formula.replaceAll("\\+", "");
        formulaCleaning = formulaCleaning.replaceAll("!", "");

        /* Rozdelenie na znaky */
        String[] uniqueVariables = formulaCleaning.split("");
        Set<String> variableSet = new HashSet<>(Arrays.asList(uniqueVariables));
        /* Zostrojenie variableOrder */
        return variableSet.toArray(new String[0]);
    }
}