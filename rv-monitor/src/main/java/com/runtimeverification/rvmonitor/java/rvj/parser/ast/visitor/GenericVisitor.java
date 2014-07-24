/*
 * Copyright (C) 2007 Julio Vilmar Gesser.
 * 
 * This file is part of Java 1.5 parser and Abstract Syntax Tree.
 *
 * Java 1.5 parser and Abstract Syntax Tree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java 1.5 parser and Abstract Syntax Tree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java 1.5 parser and Abstract Syntax Tree.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 05/10/2006
 */
package com.runtimeverification.rvmonitor.java.rvj.parser.ast.visitor;

import com.runtimeverification.rvmonitor.java.rvj.parser.ast.*;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.expr.*;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.mopspec.*;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.aspectj.*;;

/**
 * @author Julio Vilmar Gesser
 */
public interface GenericVisitor<R, A> {

	public R visit(Node n, A arg);

	// - RV Monitor components

	public R visit(RVMSpecFile f, A arg);

	public R visit(RVMonitorSpec s, A arg);

	public R visit(RVMParameter p, A arg);

	public R visit(EventDefinition e, A arg);

	public R visit(PropertyAndHandlers p, A arg);

	public R visit(Formula f, A arg);

	// - AspectJ components --------------------

	public R visit(BaseTypePattern p, A arg);

	// - Compilation Unit ----------------------------------

	public R visit(PackageDeclaration n, A arg);

	public R visit(ImportDeclaration n, A arg);

	// - Expression ----------------------------------------

	public R visit(NameExpr n, A arg);

	public R visit(QualifiedNameExpr n, A arg);
}
