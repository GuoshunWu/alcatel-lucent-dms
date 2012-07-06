package com.alcatel_lucent.dms.model;

public class Label extends BaseEntity {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4086873912554236932L;
	private Dictionary dictionary;
	private String key;
	private String reference;
	private String description;
	private String maxLength;
	private Context context;
	private Text text;
	
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
				.format("Label [dictionary=%s, key=%s, reference=%s, description=%s, maxLength=%s, context=%s, text=%s]",
						dictionary, key, reference, description, maxLength,
						context, text);
	}

	
}
