package com.simonits.adalerts.objects;

public class SiteParam {

    private String name;
    private String value1;
    private String value2;
    private String parent;
    private SiteParam[] children;

    public String getName() {
        return name;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public String getParent() {
        return parent;
    }

    public boolean hasChildren() {
        return children != null;
    }

    public SiteParam[] getChildren() {
        return children;
    }

}
