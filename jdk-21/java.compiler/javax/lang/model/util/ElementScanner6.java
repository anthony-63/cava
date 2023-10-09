/*
 * Copyright (c) 2005, 2022, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


/**
 * A scanning visitor of program elements with default behavior
 * appropriate for the {@link SourceVersion#RELEASE_6 RELEASE_6}
 * source version.  The <code>visit<i>Xyz</i></code> methods in this
 * class scan their component elements by calling {@link
 * #scan(Element, P) scan} on their {@linkplain
 * Element#getEnclosedElements enclosed elements}, {@linkplain
 * ExecutableElement#getParameters parameters}, etc., as indicated in
 * the individual method specifications.  A subclass can control the
 * order elements are visited by overriding the
 * <code>visit<i>Xyz</i></code> methods.  Note that clients of a
 * scanner may get the desired behavior by invoking {@code v.scan(e,
 * p)} rather than {@code v.visit(e, p)} on the root objects of
 * interest.
 *
 * <p>When a subclass overrides a <code>visit<i>Xyz</i></code> method, the
 * new method can cause the enclosed elements to be scanned in the
 * default way by calling <code>super.visit<i>Xyz</i></code>.  In this
 * fashion, the concrete visitor can control the ordering of traversal
 * over the component elements with respect to the additional
 * processing; for example, consistently calling
 * <code>super.visit<i>Xyz</i></code> at the start of the overridden
 * methods will yield a preorder traversal, etc.  If the component
 * elements should be traversed in some other order, instead of
 * calling <code>super.visit<i>Xyz</i></code>, an overriding visit method
 * should call {@code scan} with the elements in the desired order.
 *
 * @apiNote
 * Methods in this class may be overridden subject to their general
 * contract.
 *
 * <p id=note_for_subclasses><strong>WARNING:</strong> The {@code ElementVisitor} interface
 * implemented by this class may have methods added to it in the
 * future to accommodate new, currently unknown, language structures
 * added to future versions of the Java programming language.
 * Therefore, methods whose names begin with {@code "visit"} may be
 * added to this class in the future; to avoid incompatibilities,
 * classes which extend this class should not declare any instance
 * methods with names beginning with {@code "visit"}.</p>
 *
 * <p>When such a new visit method is added, the default
 * implementation in this class will be to directly or indirectly call the {@link
 * #visitUnknown visitUnknown} method.  A new element scanner visitor
 * class will also be introduced to correspond to the new language
 * level; this visitor will have different default behavior for the
 * visit method in question.  When a new visitor is introduced,
 * portions of this visitor class may be deprecated, including its constructors.
 *
 * @param <R> the return type of this visitor's methods.  Use {@link
 *            Void} for visitors that do not need to return results.
 * @param <P> the type of the additional parameter to this visitor's
 *            methods.  Use {@code Void} for visitors that do not need an
 *            additional parameter.
 *
 * @see ElementScanner7
 * @see ElementScanner8
 * @see ElementScanner9
 * @see ElementScanner14
 * @since 1.6
 */
@SupportedSourceVersion(RELEASE_6)
public class ElementScanner6<R, P> extends AbstractElementVisitor6<R, P> {
    /**
     * The specified default value.
     */
    protected final R DEFAULT_VALUE;

    /**
     * Constructor for concrete subclasses; uses {@code null} for the
     * default value.
     * @deprecated Release 6 is obsolete; update to a visitor for a newer
     * release level.
     */
    @Deprecated(since="9")
    protected ElementScanner6(){
        DEFAULT_VALUE = null;
    }

    /**
     * Constructor for concrete subclasses; uses the argument for the
     * default value.
     *
     * @param defaultValue the default value
     * @deprecated Release 6 is obsolete; update to a visitor for a newer
     * release level.
     */
    @Deprecated(since="9")
    protected ElementScanner6(R defaultValue){
        DEFAULT_VALUE = defaultValue;
    }

    /**
     * Iterates over the given elements and calls {@link
     * #scan(Element, Object) scan(Element, P)} on each one.  Returns
     * the result of the last call to {@code scan} or {@code
     * DEFAULT_VALUE} for an empty iterable.
     *
     * @param iterable the elements to scan
     * @param  p additional parameter
     * @return the scan of the last element or {@code DEFAULT_VALUE} if no elements
     */
    public final R scan(Iterable<? extends Element> iterable, P p) {
        R result = DEFAULT_VALUE;
        for(Element e : iterable)
            result = scan(e, p);
        return result;
    }

    /**
     * Processes an element by calling {@code e.accept(this, p)};
     * this method may be overridden by subclasses.
     *
     * @param e the element to scan
     * @param p a scanner-specified parameter
     * @return the result of visiting {@code e}.
     */
    public R scan(Element e, P p) {
        return e.accept(this, p);
    }

    /**
     * Convenience method equivalent to {@code v.scan(e, null)}.
     *
     * @param e the element to scan
     * @return the result of scanning {@code e}.
     */
    public final R scan(Element e) {
        return scan(e, null);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation scans the enclosed elements.
     *
     * @param e  {@inheritDoc}
     * @param p  {@inheritDoc}
     * @return the result of scanning
     */
    public R visitPackage(PackageElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation scans the enclosed elements.
     * Note that type parameters are <em>not</em> scanned by this
     * implementation since type parameters are not considered to be
     * {@linkplain TypeElement#getEnclosedElements enclosed elements
     * of a type}.
     *
     * @param e  {@inheritDoc}
     * @param p  {@inheritDoc}
     * @return the result of scanning
     */
    public R visitType(TypeElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation scans the enclosed elements, unless the
     * element is a {@code RESOURCE_VARIABLE} in which case {@code
     * visitUnknown} is called.
     *
     * @param e  {@inheritDoc}
     * @param p  {@inheritDoc}
     * @return the result of scanning
     */
    public R visitVariable(VariableElement e, P p) {
        if (e.getKind() != ElementKind.RESOURCE_VARIABLE)
            return scan(e.getEnclosedElements(), p);
        else
            return visitUnknown(e, p);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation scans the parameters.
     * Note that type parameters are <em>not</em> scanned by this
     * implementation.
     *
     * @param e  {@inheritDoc}
     * @param p  {@inheritDoc}
     * @return the result of scanning
     */
    public R visitExecutable(ExecutableElement e, P p) {
        return scan(e.getParameters(), p);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation scans the enclosed elements.
     *
     * @param e  {@inheritDoc}
     * @param p  {@inheritDoc}
     * @return the result of scanning
     */
    public R visitTypeParameter(TypeParameterElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }
}
