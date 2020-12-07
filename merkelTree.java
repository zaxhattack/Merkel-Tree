/**
 * ***************************************
 ** File:    merkelTree.java
 ** Project: CSCE314 Project, Fall 2020
 ** Authors: Zach Griffin, Tony Yang
 ** Date:    12/06/2020
 ** Section: 501/502
 ** E-mail:  zach.griffin@tamu.edu
 **
 **   This file contains the Merkel Tree class for the Project.
 **   This class generates a binary Merkel Tree for a given input.
 *    It can generate trees from raw data files or from files where a tree is saved.
 **   This class also has a compareTo function, in which is can traverse two trees to
 **   identify the exact data chunk that was corrupted (if corruption occured)
 **
 **********************************************
 */
package merkel;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

//INHERITANCE
public class merkelTree extends binaryTree {
    
    @Override
    public int compareTo(binaryTree input) {

        //returns -1 (meaning they're the same) if this tree == input tree
        if (this.top.equals(input.top)) {
            return -1;
        }

        //if they're not equal, we need to call the recursive traverseTree function to find what block number they're different at
        int wrongNode = traverseTree(this.top, input.top);
        return wrongNode;
    }

    //traverses the tree downwards to find the leaf node where they are different
    public int traverseTree(treeNode inputNode1, treeNode inputNode2) {
        //if we find a node that has both children listed as null, we know we have hit the bottom
        if (inputNode1.rightChild == null && inputNode1.leftChild == null) {
            //return the block number. This is the block number where transmission failed
            if (inputNode1.blockNumber >= 0) {
                return inputNode1.blockNumber;
            } else {
                return inputNode2.blockNumber;
            }
            
        //if we haven't hit a leaf yet, keep going down
        //if the left child nodes of the two trees are different, go down that side
        } else if (!inputNode1.leftChild.equals(inputNode2.leftChild)) {
            return traverseTree(inputNode1.leftChild, inputNode2.leftChild);
        } else {
            //if not, go down the right side
            return traverseTree(inputNode1.rightChild, inputNode2.rightChild);
        }
    }

    //computes and returns an MD5 hash for a given arbitrary input
    private String getHash(Object input1, Object input2) throws NoSuchAlgorithmException {
        //this section converts any input type to a byte arrays using that type's toString method
        byte byteArray1[] = input1.toString().getBytes();
        byte byteArray2[] = input2.toString().getBytes();

        //combine the two arrays into one larger one
        byte finalByteArray[] = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, finalByteArray, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, finalByteArray, byteArray1.length, byteArray2.length);

        //declare an MD5 algorithm isnatnce
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //feed it the input
        messageDigest.update(finalByteArray);

        //get the output
        finalByteArray = messageDigest.digest();

        //convert to hex string and return
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < finalByteArray.length; i++) {
            result.append(String.format("%02X", finalByteArray[i]));
        }

        return result.toString();
    }

    //adds a TreeNode to the vector
    public void insertNode(Object input, int baseNumber) {
        //create a TreeNode of the data
        try {
            treeNode<String> newTreeNode = new treeNode<String>(getHash(input, ""), baseNumber);
            //add the TreeNode to the vector
            treeList.get(0).add(newTreeNode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    //makes the rest of the tree from the bottom layer up to the top
    public void makeTree() throws NoSuchAlgorithmException {

        //if the vector only has one element, it is by definition the top TreeNode
        if (treeList.get(0).size() == 1) {
            //set the top TreeNode to be this TreeNode
            top = treeList.get(0).get(0);
        } else {
            //track the current level of the tree we are on (from the bottom up)
            int currentLayer = 1;

            //loop until we reach a tree layer that is only size 1, meaning it is the top and we are done
            while (true) {
                if (treeList.get(currentLayer - 1).size() == 1) {
                    //once we get to a tree layer of size one, we have the top hash. Set this TreeNode as the top hash
                    top = treeList.get(treeList.size() - 1).get(0);
                    break;
                }

                //make the layer
                makeTreeLayer(currentLayer);

                //iterate the layer counter
                currentLayer++;
            }

        }
    }

    //method for using the layer below to make the hashes and TreeNodes for the current layer
    private void makeTreeLayer(int layer) throws NoSuchAlgorithmException {

        //make a new layer of the vector
        treeList.add(layer, new ArrayList<treeNode>());

        //compute the length of the current layer
        int length = (treeList.get(layer - 1).size());

        //loop through every 2 element2 in the layer below
        for (int i = 0; i < length; i += 2) {
            //if the current loop position equals the length of the layer, then there's only one remaining element to hash
            if (i == length - 1) {
                //make a new TreeNode and give it the hash
                treeNode<String> new_TreeNode = new <String> treeNode(treeList.get(layer - 1).get(i).data, -1);
                //set the parent of the TreeNode
                treeList.get(layer - 1).get(i).parent = new_TreeNode;
                //set the child of the TreeNode. there's only one in this case
                new_TreeNode.leftChild = treeList.get(layer - 1).get(i);
                //add the TreeNode to the vector 
                treeList.get(layer).add(new_TreeNode);
                break;
            }

            //make new TreeNode and compute the hash for it using the two below layer hashes
            treeNode<String> new_TreeNode = new treeNode<String>(getHash(treeList.get(layer - 1).get(i).data, treeList.get(layer - 1).get(i + 1).data), -1);
            //set the TreeNode below's parent to be the new TreeNode
            treeList.get(layer - 1).get(i).parent = new_TreeNode;
            treeList.get(layer - 1).get(i + 1).parent = new_TreeNode;
            //set the new TreeNode's children to be the two lower TreeNodes
            new_TreeNode.leftChild = treeList.get(layer - 1).get(i);
            new_TreeNode.rightChild = treeList.get(layer - 1).get(i + 1);
            //add the TreeNode to the vector
            treeList.get(layer).add(new_TreeNode);
        }
    }

    //makes tree from data file
    merkelTree(File file, int blockSize) throws NoSuchAlgorithmException {
        //initialize the array list and create the first layer
        treeList = new ArrayList<ArrayList<treeNode>>();
        treeList.add(0, new ArrayList<treeNode>());

        //make a new input stream
        FileInputStream fileIn;

        try {
            //initialize the input stream using the provided File object
            fileIn = new FileInputStream(file);

            //create a buffer to store the file chunks of size specified by the driver
            byte[] fileInputArray = new byte[blockSize];

            //keep track of the data block number cooresponding to this node
            int blockNumber = 0;
            //read in from the file until no more data is left
            while (fileIn.read(fileInputArray) >= 0) {
                //create a new node of the merkel tree for each data chunk
                insertNode(new String(fileInputArray), blockNumber);
                //reset the input array
                fileInputArray = new byte[blockSize];
                //iterate the block number up
                blockNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //call the method to construct the rest of the tree
        makeTree();
    }

    //makes tree from input merkel tree file
    merkelTree(String input) throws FileNotFoundException {
        //initialize the array list and create the first layer
        treeList = new ArrayList<ArrayList<treeNode>>();
        treeList.add(0, new ArrayList<treeNode>());

        //call the correct method
        makeTreeFromFile(input);
    }

    //default constructor (to make compiler happy)
    merkelTree() {

    }
}
