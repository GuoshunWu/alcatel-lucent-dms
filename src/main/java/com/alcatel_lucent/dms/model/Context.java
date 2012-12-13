package com.alcatel_lucent.dms.model;

public class Context extends BaseEntity {
	
	public static final String DEFAULT = "[DEFAULT]";
	public static final String EXCLUSION = "[EXCLUSION]";
	public static final String DICT = "[DICT]";
	public static final String APP = "[APP]";
	public static final String PROD = "[PROD]";
	
    /**
     *
     */
    private static final long serialVersionUID = 1417207278044645451L;
    private String key;
    private String name;

    public Context() {
    }

    public Context(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Context [name=%s]", name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Context other = (Context) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
