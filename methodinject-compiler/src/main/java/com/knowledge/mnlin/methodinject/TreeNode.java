package com.knowledge.mnlin.methodinject;

import java.util.LinkedList;

/**
 * Created on 2017/12/13
 * function : java实现的树形结构节点,依据继承关系来进行排列
 *
 * @author ACChain
 */

public class TreeNode<T> {
    /**
     * 具体数据部分
     */
    public T data;


    /**
     * 父节点
     * <p>
     * 若为根节点的话该成员域为null,表示没有父节点
     */
    public TreeNode<T> parent;


    /**
     * 子树
     * <p>
     * 若为叶子节点的话该成员域为null,表示没有子树
     */
    public LinkedList<TreeNode<T>> children;
}
