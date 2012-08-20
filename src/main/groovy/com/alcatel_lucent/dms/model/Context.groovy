package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-17
 * Time: 下午1:34
 * To change this template use File | Settings | File Templates.
 */
class Context extends BaseEntity{
    String name
    public Context(){}
    public Context(String name){
        this.name=name;
    }
}
