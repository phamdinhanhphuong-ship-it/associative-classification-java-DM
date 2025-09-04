
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class representing a node of a FP-Tree
 * 
 * @see AlgoCMAR
 */
public class FPNode {
    /**
     * Item saved in current node
     */
    Short item = -1;

    /**
     * Frequency of occurrence for current item
     */
    long support = 1;

    /**
     * Support for this item for each class
     */
    HashMap<Short, Long> supportByklass;

    /**
     * Parent of the current node, null if it is root element
     */
    FPNode parent = null;

    /**
     * Array of immediate childs from current node
     */
    List<FPNode> childs = new ArrayList<FPNode>();

    /**
     * Next node with the same item, used to create the header table
     */
    FPNode nextNode = null;

    /**
     * Default constructor
     */
    FPNode() {
    }

    /**
     * Search in children the specified item, return the child with this item
     * 
     * @param item to look for
     * @return the node with the item, or null otherwise
     */
    FPNode getChildByItem(short item) {
        // Search item in childs
        for (FPNode child : childs) {
            if (child.item == item) {
                return child;
            }
        }

        return null;
    }
}
