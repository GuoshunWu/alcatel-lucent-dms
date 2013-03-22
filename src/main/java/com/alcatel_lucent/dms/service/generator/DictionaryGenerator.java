package com.alcatel_lucent.dms.service.generator;

import java.io.File;
import java.util.Collection;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.Dictionary;

public abstract class DictionaryGenerator {
	public void generateDict(File target, Collection<Dictionary> dictList) throws BusinessException {
		int totalLabels = 0;
		int curLabels = 0;
		int totalDict = dictList.size();
		int curDict = 1;
		for (Dictionary dict : dictList) {
			totalLabels += dict.getLabelNum();
		}
		for (Dictionary dict : dictList) {
			int percent = (int) Math.round(curLabels * 100.0 / totalLabels + .5);
        	ProgressQueue.setProgress("Generating " + dict.getFormat() + " (" + curDict + "/" + totalDict + ") " + dict.getName(), percent);
			generateDict(target, dict.getId());
			curLabels += dict.getLabelNum();
			curDict++;
		}
	}
	abstract public void generateDict(File target, Long dictId) throws BusinessException;

    abstract public Constants.DictionaryFormat getFormat();
}
