package com.cszxyang.jt.javac.tree;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import java.util.Objects;

public class EndlessLoopChecker extends TreePathScanner<Void, Void> {

    private final Trees         trees;
    private final TreeMaker     maker;
    private final JavacElements elements;
    /** 超时时间阈值，超过则抛异常 **/
    private final int           timeLimit;
    /** 线程安全的简单序列产生器 **/
    private final SeqGen        pointSeq;
    /** 当前编译的源文件类名 **/
    private String              clsName;

    public EndlessLoopChecker(Trees trees, TreeMaker maker, JavacElements elements, int timeLimit){
        this.trees = trees;
        this.maker = maker;
        this.elements = elements;
        this.timeLimit = timeLimit;
        this.pointSeq = new SeqGen(0);
    }

    /**
     * 访问 Class 节点，获取源类名
     * @param node  ClassTree
     * @param p     param
     * @return      Void
     */
    public Void visitClass(ClassTree node, Void p) {
        if (Objects.isNull(this.clsName)) {
            this.clsName = node.getSimpleName().toString();
        }
        return super.visitClass(node, p);
    }

    public Void visitForLoop(ForLoopTree node, Void param) {
        super.visitForLoop(node, param);
        manipulateNode(node);
        return null;
    }

    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void param) {
        super.visitEnhancedForLoop(node, param);
        manipulateNode(node);
        return null;
    }

    public Void visitDoWhileLoop(DoWhileLoopTree node, Void param) {
        super.visitDoWhileLoop(node, param);
        manipulateNode(node);
        return null;
    }

    public Void visitWhileLoop(WhileLoopTree node, Void param) {
        super.visitWhileLoop(node, param);
        manipulateNode(node);
        return null;
    }

    private void manipulateNode(Tree node) {
        // 在本loop之前，挂上起始时间声明
        String timeDeclName = genTimeDeclName();
        hookBefore(this.getCurrentPath().getParentPath().getLeaf(), node, genTimeDecl(timeDeclName));
        // 在本loop之内，最后一条语句后，挂上超时判断
        appendAsLastChild(node, genTimeCheck(timeDeclName, node));
    }

    private String resolveRowAndCol(Tree node) {
        CompilationUnitTree unit = this.getCurrentPath().getCompilationUnit();
        long pos = this.trees.getSourcePositions().getStartPosition(unit, node);
        LineMap m = unit.getLineMap();
        return "row: " + m.getLineNumber(pos) + ", col: " + m.getColumnNumber(pos);
    }

    /*
     * 可能的parent有: block, labled, if, while, do-while, for, enhancedFor
     */
    // 将toBeInserted插入到node的最后一个child的位置
    private void appendAsLastChild(Tree node, JCStatement toBeInserted) {
        if (node instanceof JCBlock) {
            JCBlock blk = (JCBlock) node;
            blk.stats = blk.stats.append(toBeInserted);
        } else if (node instanceof JCLabeledStatement) {
            JCLabeledStatement st = (JCLabeledStatement) node;
            st.body = appendLastAsBlock(st.body, toBeInserted);
        } else if (node instanceof JCIf) {
            JCIf p = (JCIf) node;
            p.thenpart = appendLastAsBlock(p.thenpart, toBeInserted);
            p.elsepart = appendLastAsBlock(p.elsepart, toBeInserted);
        } else if (node instanceof JCWhileLoop) {
            JCWhileLoop p = (JCWhileLoop) node;
            p.body = appendLastAsBlock(p.body, toBeInserted);
        } else if (node instanceof JCDoWhileLoop) {
            JCDoWhileLoop p = (JCDoWhileLoop) node;
            p.body = appendLastAsBlock(p.body, toBeInserted);
        } else if (node instanceof JCForLoop) {
            JCForLoop p = (JCForLoop) node;
            p.body = appendLastAsBlock(p.body, toBeInserted);
        } else if (node instanceof JCEnhancedForLoop) {
            JCEnhancedForLoop p = (JCEnhancedForLoop) node;
            p.body = appendLastAsBlock(p.body, toBeInserted);
        } else {
            throw new RuntimeException("Unexpected parent node type: " + node);
        }
    }

    private JCStatement appendLastAsBlock(JCStatement body, JCStatement toBeInserted) {
        if (body instanceof JCBlock) {
            JCBlock b = (JCBlock) body;
            b.stats = b.stats.append(toBeInserted);
            return b;
        } else {
            return maker.Block(0, List.of(body, toBeInserted));
        }
    }

    // 将 toBeInserted 插入到 current 节点之前
    private void hookBefore(Tree parent, Tree current, JCStatement toBeInserted) {
        if (parent instanceof JCBlock) {
            JCBlock blk = (JCBlock) parent;
            blk.stats = insertBefore(blk.stats.head, blk.stats.tail, toBeInserted, parent, current);
        } else if (parent instanceof JCLabeledStatement) {
            JCLabeledStatement st = (JCLabeledStatement) parent;
            st.body = maker.Block(0, List.of(toBeInserted, st.body));
        } else if (parent instanceof JCIf) {
            JCIf p = (JCIf) parent;
            if (p.getThenStatement() == current) {
                p.thenpart = maker.Block(0, List.of(toBeInserted, p.thenpart));
            } else {
                p.elsepart = maker.Block(0, List.of(toBeInserted, p.elsepart));
            }
        } else if (parent instanceof JCWhileLoop) {
            JCWhileLoop p = (JCWhileLoop) parent;
            p.body = maker.Block(0, List.of(toBeInserted, p.body));
        } else if (parent instanceof JCDoWhileLoop) {
            JCDoWhileLoop p = (JCDoWhileLoop) parent;
            p.body = maker.Block(0, List.of(toBeInserted, p.body));
        } else if (parent instanceof JCForLoop) {
            JCForLoop p = (JCForLoop) parent;
            p.body = maker.Block(0, List.of(toBeInserted, p.body));
        } else if (parent instanceof JCEnhancedForLoop) {
            JCEnhancedForLoop p = (JCEnhancedForLoop) parent;
            p.body = maker.Block(0, List.of(toBeInserted, p.body));
        } else {
            throw new RuntimeException("Unexpected parent node type: " + parent);
        }
    }

    // 将toBeInserted插入到current之前, 由于List的设计问题，这里采用蛋疼的尾递归函数式写法...
    private List<JCStatement> insertBefore(JCStatement head, List<JCStatement> tail, JCStatement toBeInserted,
                                           Tree parent, Tree current) {
        List<JCStatement> acc = List.nil();
        while (head != null) {
            if (head == current) {
                return acc.appendList(tail.prepend(head).prepend(toBeInserted));
            } else {
                acc = acc.append(head);
                head = tail.head;
                tail = tail.tail;
            }
        }
        throw new RuntimeException("Cannot find the current node in its parent node, impossible. Parent: " + parent
                                   + ", current node: " + current);
    }

    /*
     * 生成如下检查语句： if (System.currentTimeMillis() - time > 3000) throw new LoopTimedOutException("");
     */
    private JCStatement genTimeCheck(String timeDeclName, Tree parentLoop) {
        JCExpression curMillis = maker.Apply(List.nil(),
                                             maker.Select(maker.Ident(elements.getName("System")),
                                                          elements.getName("currentTimeMillis")),
                                             List.nil());
        JCExpression timeJudge = maker.Binary(JCTree.Tag.GT,
                                              maker.Binary(JCTree.Tag.MINUS, curMillis,
                                                           maker.Ident(elements.getName(timeDeclName))),
                                              maker.Literal(this.timeLimit));
        List<JCExpression> expParams = List.of(maker.Literal(TypeTag.CLASS,
                                                                            "Looping time exceeds "
                                                                                    + this.timeLimit
                                                                                    + ", forced terminate. Expr class: "
                                                                                    + this.clsName + ". Position - "
                                                                                    + resolveRowAndCol(parentLoop)));
        JCExpression newException = maker.NewClass(null, List.nil(),
                                             maker.Ident(elements.getName("RuntimeException")), expParams, null);
        JCStatement throwStmt = maker.Throw(newException);
        return maker.If(timeJudge, throwStmt, null);
    }

    private String genTimeDeclName() {
        return "_time_" + this.pointSeq.next();
    }

    // 生成形如：long timeX = System.currentTimeMillis();的语句，其中"X"是自增序列生成的数字
    private JCStatement genTimeDecl(String timeDeclName) {
        JCExpression funExp = maker.Select(maker.Ident(elements.getName("System")),
                                           elements.getName("currentTimeMillis"));
        JCExpression curMillis = maker.Apply(List.nil(), funExp, List.nil());
        return maker.VarDef(maker.Modifiers(0), elements.getName(timeDeclName), maker.TypeIdent(TypeTag.LONG),
                            curMillis);
    }
}