package org.bardframework.crud.api.filter;

/**
 * Class for filtering attributes with {@link Boolean} type. It can be added to a criteria class as a member, to support
 * the following query parameters:
 * <pre>
 *      fieldName.equals=true
 *      fieldName.specified=true
 *      fieldName.specified=false
 *      fieldName.in=true,false
 * </pre>
 */
public class BooleanFilter extends Filter<Boolean, BooleanFilter> {

}
