package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

//@Entity
@Table(name = "TEXT")
public class Text extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = -4205533860022959713L;

    @Id
    @GeneratedValue(generator = "HILO_GEN")
    @TableGenerator(name = "HILO_GEN", table = "ID_TEXT")
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private Context context;
    private String reference;
    private Collection<Translation> translations;

    private int status;

    public static final int STATUS_NOT_TRANSLATED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_TRANSLATED = 2;

    @ManyToOne
    @JoinColumn(name = "CONTEXT_ID", nullable = false)
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Column(name = "REFERENCE", length = 2048)
    @Field
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Column(name = "STATUS")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @OneToMany
   public Collection<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(Collection<Translation> translations) {
        this.translations = translations;
    }

    @Override
    public String toString() {
        return String.format("Text [context=%s, reference=%s, status=%s]",
                context, reference, status);
    }

    public Translation getTranslation(Long languageId) {
        if (translations != null) {
            for (Translation trans : translations) {
                if (trans.getLanguage().getId().equals(languageId)) {
                    return trans;
                }
            }
        }
        return null;
    }

    public void addTranslation(Translation trans) {
        if (translations == null) {
            translations = new HashSet<Translation>();
        }
        translations.add(trans);
    }
    
    public int getLanguageNum() {
    	if (translations != null) {
    		return translations.size();
    	} else {
    		return 0;
    	}
    }
    

	// transient variable for REST service
    private Integer t;
    private Integer n;
    private Integer i;
    private Integer refs;
    private Integer diff;
    
    public Integer getT() {
		return t;
	}

	public void setT(Integer t) {
		this.t = t;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public Integer getI() {
		return i;
	}

	public void setI(Integer i) {
		this.i = i;
	}

	public Integer getRefs() {
		return refs;
	}

	public void setRefs(Integer refs) {
		this.refs = refs;
	}

	public Integer getDiff() {
		return diff;
	}

	public void setDiff(Integer diff) {
		this.diff = diff;
	}

	public void populateTranslationSummary() {
		t = n = i = 0;
		if (translations != null) {
			for (Translation trans : translations) {
				if (trans.getStatus() == Translation.STATUS_TRANSLATED) t++;
				else if (trans.getStatus() == Translation.STATUS_UNTRANSLATED) n++;
				else i++;
			}
		}
	}
	
	// used by creation of translation history
	private Label refLabel;
	
	@Transient
	public Label getRefLabel() {
		return refLabel;
	}
	
	@Transient
	public void setRefLabel(Label label) {
		this.refLabel = label;
	}

	public void addDiff(int j) {
		if (diff != null) 
			diff += i; 
		else 
			diff = i;
	}

}
