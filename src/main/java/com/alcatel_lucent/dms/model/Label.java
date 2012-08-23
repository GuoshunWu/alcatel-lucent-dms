package com.alcatel_lucent.dms.model;

import java.io.UnsupportedEncodingException;

import com.alcatel_lucent.dms.SystemError;

public class Label extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4086873912554236932L;
	public static final String CHECK_FIELD_NAME = "CHK";
	public static final String REFERENCE_FIELD_NAME = "GAE";

	private Dictionary dictionary;
	private String key;
	private int sortNo;
	private String reference;
	private String description;
	private String maxLength;
	private Context context;
	private Text text;
    private String annotation1;
    private String annotation2;

    public String getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(String annotation1) {
        this.annotation1 = annotation1;
    }

    public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return String
				.format("Label [dictionary=%s, key=%s, reference=%s, description=%s, maxLength=%s]",
						dictionary, key, reference, description, maxLength);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		Label other = (Label) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	/**
	 * Check if text meets max length constraint of the label
	 * @param text
	 * @return
	 */
	public boolean checkLength(String text) {
		if (maxLength == null || maxLength.isEmpty()) {
			return true;	// no constraint
		}
		String[] lens = maxLength.split(",");
		String[] texts = text.split("\n");
		for (int i = 0; i < texts.length; i++) {
			try {
				if (i >= lens.length || texts[i].getBytes("ISO-8859-1").length > Integer.parseInt(lens[i])) {
					return false;
				}
			} catch (NumberFormatException e) {
				throw new SystemError(e);
			} catch (UnsupportedEncodingException e) {
				throw new SystemError(e);
			}
		}
		return true;
	}

	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}

	public int getSortNo() {
		return sortNo;
	}

}
