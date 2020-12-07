/**
 * ***************************************
 ** File:    bottumUpBinaryTree.java
 ** Project: CSCE314 Project, Fall 2020
 ** Authors: Zach Griffin, Tony Yang
 ** Date:    12/06/2020
 ** Section: 501/502
 ** E-mail:  zach.griffin@tamu.edu
 **
 **   This file contains the Binary Tree class for the Project, specifically a
 **   binary tree that is build from the bottom up as a Merkel Tree is.
 **   This class is used as the basis for the MerkelTree, which extends this class.
 **   This class also contains some of the basic functionality for a binary tree, such as
 **   creating a tree from a given file output by another tree.
 * 
 **   It uses an ArrayList to build the tree from the bottom up. The ArrayList functions as
 **   scaffolding, and is not needed after the tree is built. The tree is used in practice
 **   as a linked list, with the top node pointing to two children and so on.
 **
 **********************************************
 */
package merkel;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

//ABSTRACTION
public abstract class binaryTree implements Comparable<binaryTree> {

    //TreeNode for the top of the tree
    treeNode top;

    //vector for building the tree from the ground up, only used temporarily
    //COLLECTIONS
    ArrayList<ArrayList<treeNode>> treeList;

    @Override
    public String toString() {
        String print = "";

        //loop through the outermost vector
        for (int i = treeList.size() - 1; i >= 0; i--) {
            //loop through the inner vectors
            for (int j = 0; j < treeList.get(i).size(); j++) {
                //print the hash value at this spot
                print += treeList.get(i).get(j).data + "    ";
            }
            print += '\n';
        }
        return print;
    }

    public void printToFile(String fileName) throws IOException {
        //string we will send to the file
        String print = "";

        //loop through the outermost vector
        for (int i = treeList.size() - 1; i >= 0; i--) {
            //loop through the inner vectors
            for (int j = 0; j < treeList.get(i).size(); j++) {
                //add the hash value at this spot to the string
                print = print + treeList.get(i).get(j).data + " ";
            }
            //newline
            print = print + "\n";
        }

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
        fileWriter.write(print);
        fileWriter.close();
    }

    //constructs a new BinaryTree object from a file printed by another BinaryTree. Stores each node as a string.
    public void makeTreeFromFile(String fileName) throws FileNotFoundException {
        //create the file and file scanner objects
        File treeFile = new File(fileName);
        Scanner fileScanner = new Scanner(treeFile);

        //go through the file to count the number of lines, ie the number of tree layers
        int lineCount = 0;
        while (fileScanner.hasNextLine()) {
            lineCount++;
            fileScanner.nextLine();
        }

        //close the scanner and reopen it to reset the scanner position to the beginning
        fileScanner.close();
        fileScanner = new Scanner(treeFile);

        //the number of lines is overcounted by 1 due to the last line being blank newline char
        lineCount--;

        //add the necessary number of new TreeNode vectors to the 2D vector
        for (int i = 0; i < lineCount; i++) {
            treeList.add(new ArrayList<treeNode>());
        }

        //go back through the file line by line
        while (fileScanner.hasNextLine()) {
            //store the next line as a string and split it by whitespaces
            String line = fileScanner.nextLine();
            String lineArray[] = line.split(" ");
            //go through all the individual segments between whitespaces
            for (int i = 0; i < lineArray.length; i++) {
                //make a new node for each one
                treeNode<String> newTreeNode = new treeNode<String>(lineArray[i], -1);
                //store that node in the vector
                treeList.get(lineCount).add(newTreeNode);
            }
            //decrement the linecount int
            lineCount--;
        }

        //sets the pointers for each node
        //loop through the outside vector
        for (int i = treeList.size() - 1; i >= 1; i--) {

            //set the top node of the tree
            if (treeList.get(i).size() == 1) {
                top = treeList.get(i).get(0);
            }

            //loop through the inside vector
            for (int j = 0; j < treeList.get(i).size(); j++) {
                //checks to make sure we don't go out of bounds as some nodes have only one child
                if (treeList.get(i - 1).size() - 1 >= (j * 2) + 1) {
                    //set the right child of this node
                    treeList.get(i).get(j).rightChild = treeList.get(i - 1).get((j * 2) + 1);
                    //set the right child of this node's parent node
                    treeList.get(i - 1).get((j * 2) + 1).parent = treeList.get(i).get(j);
                }
                //set the left child of this node
                treeList.get(i).get(j).leftChild = treeList.get(i - 1).get(j * 2);
                //set the left child of this node's parent node
                treeList.get(i - 1).get(j * 2).parent = treeList.get(i).get(j);
            }
        }
    }
}
