import org.w3c.dom.Node;
import java.util.LinkedList;

public class BDDprinter {
     public static void printBinaryTree(BDDNode root)
        {
            LinkedList<BDDNode> treeLevel = new LinkedList<BDDNode>();
            treeLevel.add(root);
            LinkedList<BDDNode> temp = new LinkedList<BDDNode>();
            int counter = 0;
            int height = heightOfTree(root) - 1;
            // System.out.println(height);
            double numberOfElements
                    = (Math.pow(2, (height + 1)) - 1);
            // System.out.println(numberOfElements);
            while (counter <= height) {
                BDDNode removed = treeLevel.removeFirst();
                if (temp.isEmpty()) {
                    printSpace(numberOfElements
                                    / Math.pow(2, counter + 1),
                            removed);
                }
                else {
                    printSpace(numberOfElements
                                    / Math.pow(2, counter),
                            removed);
                }
                if (removed == null) {
                    temp.add(null);
                    temp.add(null);
                }
                else {
                    temp.add(removed.getLow());
                    temp.add(removed.getHigh());
                }

                if (treeLevel.isEmpty()) {
                    System.out.println("");
                    System.out.println("");
                    treeLevel = temp;
                    temp = new LinkedList<>();
                    counter++;
                }
            }
        }

        public static void printSpace(double n, BDDNode removed)
        {
            for (; n > 0; n--) {
                System.out.print("\t");
            }
            if (removed == null) {
                System.out.print(" ");
            }
            else {
                System.out.print(removed.getVariable() + removed.getControlVariable());
                //System.out.print(removed.getVariable() + " -> " +  removed);

            }
        }

        public static int heightOfTree(BDDNode root)
        {
            if (root == null) {
                return 0;
            }
            return 1
                    + Math.max(heightOfTree(root.getLow()), heightOfTree(root.getHigh()));
        }
    }

