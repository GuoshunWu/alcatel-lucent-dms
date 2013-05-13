package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Store;

import javax.persistence.*;
import java.util.Collection;

//@Entity
@Embeddable
@Table(name = "DICTIONARY_BASE")
public class DictionaryBase extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4926531636839152201L;

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_DICTIONARY_BASE", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

	private String name;
	private String format;
	private String encoding;
	private String path;
	
	private ApplicationBase applicationBase;
	private Collection<Dictionary> dictionaries;

	private boolean locked;

    private User owner;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

//    @ContainedIn
    @OneToMany(mappedBy = "base")
    public Collection<Dictionary> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Collection<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public DictionaryBase() {
		super();
	}

    @Field(store = Store.YES)
    @Column(name = "NAME", nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

    @Column(name = "FORMAT", nullable = false)
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

    @Column(name = "ENCODING", nullable = false)
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

    @Column(name = "PATH", nullable = false)
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "APPLICATION_BASE_ID")
	public ApplicationBase getApplicationBase() {
		return applicationBase;
	}
	public void setApplicationBase(ApplicationBase applicationBase) {
		this.applicationBase = applicationBase;
	}


	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

}
