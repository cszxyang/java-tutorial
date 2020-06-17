package com.cszxyang.jt.javac.tree;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class LoopLimitEnhancer extends AbstractProcessor {

    private Trees         trees;
    private TreeMaker     maker;
    private JavacElements elements;

    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = Trees.instance(processingEnv);
        Context ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.maker = TreeMaker.instance(ctx);
        this.elements = JavacElements.instance(ctx);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver()) {
            for (Element e : env.getRootElements()) {
                new EndlessLoopChecker(this.trees, maker, this.elements, 3000).scan(this.trees.getPath(e), null);
            }
        }
        return false;
    }
}