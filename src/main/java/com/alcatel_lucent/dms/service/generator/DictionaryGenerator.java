package com.alcatel_lucent.dms.service.generator;

import java.io.File;

import com.alcatel_lucent.dms.BusinessException;

public interface DictionaryGenerator {
	void generateDict(File target, Long dictId) throws BusinessException;
}
