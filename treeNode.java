/**
 * ***************************************
 ** File:    treeNode.java
 ** Project: CSCE314 Project, Fall 2020
 ** Authors: Zach Griffin, Tony Yang
 ** Date:    12/06/2020
 ** Section: 501/502
 ** E-mail:  zach.griffin@tamu.edu
 **
 **   This file contains the tree node class for the Project.
 **   This class is used for each node of the tree for our linked list
 **   implementation of the tree. As such, each node has a pointer to it's parent
 **   as well as pointers to it's left and right child nodes, as well as an integer
 **   containing the data chunk corresponding to node.
 **
 **********************************************
 */
package merkel;

//GENERICS
public class treeNode<Type> {

    //the data the node will store
    Type data;
    //it's parent node
    treeNode parent;
    //integer for number of the data block corresponding to this node (-1 for all nodes non-leaf nodes)
    int blockNumber;
    //it's left and right child node
    treeNode leftChild;
    treeNode rightChild;

    //checks if a two node's data members are the same
    public boolean equals(treeNode input) {
        if (this.data.equals(input.data)) {
            return true;
        }
        return false;
    }

    //constructor for the node
    treeNode(Type data, int blockNumber) {
        this.data = data;
        this.blockNumber = blockNumber;
        parent = null;
        leftChild = null;
        rightChild = null;
    }
}
