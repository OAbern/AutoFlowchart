package org.autoflowchart.objects;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Node
{
	public String text;
	public Node next;
	public Node nextFalse;
	public int waitsFor;
	public int level;

	public Block block;

	public Node () {}

	public Node (String text)
	{
		this.text = text;
	}

	public Node (String text, Node next, Node nextFalse, int level)
	{
		this.text = text;
		this.next = next;
		this.nextFalse = nextFalse;
		this.level = level;
	}

	public Node getNext ()
	{
		return this.next;
	}

	public void setNext (Node next)
	{
		if (this.next == null && this.nextFalse == null) {
			if (this.waitsFor == 0)
				this.next = next;
			else
				this.nextFalse = next;
		} else if (this.next == null)
			this.next = next;
		else
			this.nextFalse = next;
	}

	/*
	Connects statement to this node.
	 */
	public Node connectStmt (Statement stmt, Nodes waitList, int level)
	{
		Node lastNode = this;
		if (stmt.isAssertStmt()) {

		} else if (stmt.isBlockStmt()) {
			lastNode = this.connectBlockStmt(stmt.asBlockStmt(), waitList, level);
		} else if (stmt.isBreakStmt()) {
			lastNode = this.connectBreakStmt(stmt.asBreakStmt(), waitList, level);
		} else if (stmt.isContinueStmt()) {
			lastNode = this.connectContinueStmt(stmt.asContinueStmt(), waitList, level);
		} else if (stmt.isDoStmt()) {
			lastNode = this.connectDoStmt(stmt.asDoStmt(), waitList, level);
		} else if (stmt.isEmptyStmt()) {
			lastNode = this.connectEmptyStmt(stmt.asEmptyStmt(), waitList, level);
		} else if (stmt.isExplicitConstructorInvocationStmt()) {

		} else if (stmt.isExpressionStmt()) {
			lastNode = this.connectExpressionStmt(stmt.asExpressionStmt(), waitList, level);
		} else if (stmt.isForeachStmt()) {
			lastNode = this.connectForeachStmt(stmt.asForeachStmt(), waitList, level);
		} else if (stmt.isForStmt()) {
			lastNode = this.connectForStmt(stmt.asForStmt(), waitList, level);
		} else if (stmt.isIfStmt()) {
			lastNode = this.connectIfStmt(stmt.asIfStmt(), waitList, level);
		} else if (stmt.isLabeledStmt()) {

		} else if (stmt.isLocalClassDeclarationStmt()) {

		} else if (stmt.isReturnStmt()) {

		} else if (stmt.isSwitchStmt()) {

		} else if (stmt.isSynchronizedStmt()) {

		} else if (stmt.isThrowStmt()) {

		} else if (stmt.isTryStmt()) {

		} else if (stmt.isUnparsableStmt()) {

		} else if (stmt.isWhileStmt()) {

		} else {

		}
		return lastNode;
	}

	Node connectStmts (NodeList<Statement> stmts, Nodes waitList, int level)
	{
		Node currentNode = this;
		for (Statement stmt : stmts)
		{
			Node newNode = currentNode.connectStmt(stmt, waitList, level);
			if (newNode == null)
				break;
			currentNode = newNode;
		}
		return currentNode;
	}

	Node connectBlockStmt (BlockStmt blockStmt, Nodes waitList, int level)
	{
		return this.connectStmts(blockStmt.getStatements(), waitList, level);
	}

	Node connectBreakStmt (BreakStmt breakStmt, Nodes waitList, int level)
	{
		this.waitsFor = 1;
		waitList.add(this);
		return null;
	}

	Node connectContinueStmt (ContinueStmt continueStmt, Nodes waitList, int level)
	{
		this.waitsFor = 2;
		waitList.add(this);
		return null;
	}

	Node connectDoStmt (DoStmt doStmt, Nodes waitList, int level)
	{
		Node currentNode = this.connectStmt(doStmt.getBody(), waitList, level + 1);
		Node firstNode = this.getNext();
		Node conditionNode = new Node();
		conditionNode.setNext(firstNode);
		currentNode.setNext(conditionNode);
		return conditionNode;
	}

	Node connectEmptyStmt (EmptyStmt emptyStmt, Nodes waitList, int level)
	{
		return this;
	}

	Node connectExplicitConstructorInvocationStmt (ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt, Nodes waitList)
	{
		System.out.println("ExplicitConstructorInvocationStmt:");
		System.out.println(explicitConstructorInvocationStmt);
		return this;
	}

	Node connectExpressionStmt (ExpressionStmt expressionStmt, Nodes waitList, int level)
	{
		Node newNode = new Node(expressionStmt.toString());
		this.setNext(newNode);
		return newNode;
	}

	Node connectForeachStmt (ForeachStmt foreachStmt, Nodes waitList, int level)
	{
		waitList = new Nodes();
		String variable = foreachStmt.getVariable().toString();
		String iterable = foreachStmt.getIterable().toString();

		Node checkNode = new Node( iterable + ".hasNext()");
		this.setNext(checkNode);

		Node updateNode = new Node( variable + " = " + iterable + ".next()");
		checkNode.setNext(updateNode);

		Node lastNode = updateNode.connectStmt(foreachStmt.getBody(), waitList, level + 1);
		lastNode.setNext(checkNode);

		Nodes nodesToConnect = new Nodes();
		nodesToConnect.add(checkNode);

		for (Node node : waitList.nodes)
		{
			// break with hanging, continue connect to update
			if (node.waitsFor == 1) {
				nodesToConnect.add(node);
			} else if (node.waitsFor == 2) {
				node.setNext(checkNode);
			}
		}

		return nodesToConnect;
	}

	Node connectForStmt (ForStmt forStmt, Nodes waitList, int level)
	{
		waitList = new Nodes();
		Node prevNode = this;

		NodeList<Expression> initExprs = forStmt.getInitialization();
		if (initExprs.size() > 0) {
			String init = initExprs.toString();
			init = init.substring(1, init.length() - 1);
			prevNode = new Node(init);
			this.setNext(prevNode);
		}

		Node currentNode;

		Optional<Expression> compareExpr = forStmt.getCompare();
		if (compareExpr.isPresent()) {
			Node compareNode = new Node(compareExpr.get().toString());
			prevNode.setNext(compareNode);
			currentNode = compareNode;
		} else
			currentNode = prevNode;
		currentNode  = currentNode.connectStmt(forStmt.getBody(), waitList, level + 1);
		Node firstNode = prevNode.getNext();

		Node newCycleNode = firstNode;

		NodeList<Expression> updateExprs = forStmt.getUpdate();
		if (updateExprs.size() > 0) {
			Node updateNode = new Node(forStmt.getUpdate().toString());
			currentNode.setNext(updateNode);
			currentNode = updateNode;
			newCycleNode = updateNode;
		}
		currentNode.setNext(firstNode);

		Nodes nodes = new Nodes();
		if (compareExpr.isPresent())
			nodes.add(firstNode);

		for (Node node : waitList.nodes)
		{
			if (node.waitsFor == 1) {
				nodes.add(node);
			} else if (node.waitsFor == 2) {
				node.setNext(newCycleNode);
			}
		}

		return nodes;
	}

	Node connectIfStmt (IfStmt ifStmt, Nodes waitList, int level)
	{
		Node conditionNode = new Node(ifStmt.getCondition().toString());
		this.setNext(conditionNode);
		Node lastNode1 = conditionNode.connectStmt(ifStmt.getThenStmt(), waitList, level + 1);

		Optional<Statement> elseStmt = ifStmt.getElseStmt();
		Node lastNode2;
		if (elseStmt.isPresent()) {
			lastNode2 = conditionNode.connectStmt(elseStmt.get(), waitList, level);
		} else {
			lastNode2 = conditionNode;
		}
		Nodes nodes = new Nodes();
		if (lastNode1 != null) nodes.add(lastNode1);
		if (lastNode2 != null) nodes.add(lastNode2);
		return nodes;

	}

	public List<Block> connectionQueue;

	public void addToConnectionQueue (Block block)
	{
		if (this.connectionQueue == null)
			this.connectionQueue = new ArrayList<Block>();
		this.connectionQueue.add(block);
	}
}