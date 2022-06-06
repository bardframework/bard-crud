
package org.bardframework.crud.api.filter;

import java.util.List;

/**
 * Base class for the various attribute filters. It can be added to a criteria class as a member, to support the
 * following query parameters:
 * <pre>
 *      fieldName.equals='something'
 *      fieldName.specified=true
 *      fieldName.specified=false
 *      fieldName.notEquals='somethingElse'
 *      fieldName.in='something','other'
 *      fieldName.notIn='something','other'
 * </pre>
 */
public class Filter<T, F> {
    private T equals;
    private T notEquals;
    private Boolean specified;
    private List<T> in;
    private List<T> notIn;

    /**
     * <p>Constructor for Filter.</p>
     */
    public Filter() {
    }

    /**
     * <p>Getter for the field <code>equals</code>.</p>
     *
     * @return a T object.
     */
    public T getEquals() {
        return equals;
    }

    /**
     * <p>Setter for the field <code>equals</code>.</p>
     *
     * @param equals a T object.
     * @return a {@link Filter} object.
     */
    public F setEquals(T equals) {
        this.equals = equals;
        return (F) this;
    }

    /**
     * <p>Getter for the field <code>notEquals</code>.</p>
     *
     * @return a T object.
     */
    public T getNotEquals() {
        return notEquals;
    }

    /**
     * <p>Setter for the field <code>notEquals</code>.</p>
     *
     * @param notEquals a T object.
     * @return a {@link Filter} object.
     */
    public F setNotEquals(T notEquals) {
        this.notEquals = notEquals;
        return (F) this;
    }

    /**
     * <p>Getter for the field <code>specified</code>.</p>
     *
     * @return a {@link Boolean} object.
     */
    public Boolean getSpecified() {
        return specified;
    }

    /**
     * <p>Setter for the field <code>specified</code>.</p>
     *
     * @param specified a {@link Boolean} object.
     * @return a {@link Filter} object.
     */
    public F setSpecified(Boolean specified) {
        this.specified = specified;
        return (F) this;
    }

    /**
     * <p>Getter for the field <code>in</code>.</p>
     *
     * @return a {@link List} object.
     */
    public List<T> getIn() {
        return in;
    }

    /**
     * <p>Setter for the field <code>in</code>.</p>
     *
     * @param in a {@link List} object.
     * @return a {@link Filter} object.
     */
    public F setIn(List<T> in) {
        this.in = in;
        return (F) this;
    }

    /**
     * <p>Getter for the field <code>notIn</code>.</p>
     *
     * @return a {@link List} object.
     */
    public List<T> getNotIn() {
        return notIn;
    }

    /**
     * <p>Setter for the field <code>notIn</code>.</p>
     *
     * @param notIn a {@link List} object.
     * @return a {@link Filter} object.
     */
    public F setNotIn(List<T> notIn) {
        this.notIn = notIn;
        return (F) this;
    }

    /**
     * <p>getFilterName.</p>
     *
     * @return a {@link String} object.
     */
    protected String getFilterName() {
        return this.getClass().getSimpleName();
    }
}
