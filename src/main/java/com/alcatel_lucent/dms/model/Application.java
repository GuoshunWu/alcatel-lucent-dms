package com.alcatel_lucent.dms.model;

import java.util.Arrays;
import java.util.Collection;

public class Application extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7168527218137875020L;

    private ApplicationBase base;
    private String version;

	private Collection<Dictionary> dictionaries;

    public ApplicationBase getBase() {
        return base;
    }

    public String getName(){
        return base.getName();
    }
    
    public Integer getDictNum(){
        return dictionaries.size();
    }
    
    public Collection getCell(){
        return Arrays.asList(getId(),getName(),version, getDictNum());
    }

    public void setBase(ApplicationBase base) {
        this.base = base;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


	public Collection<Dictionary> getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(Collection<Dictionary> dictionaries) {
		this.dictionaries = dictionaries;
	}
}
